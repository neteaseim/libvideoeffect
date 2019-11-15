package com.netease.libvideoeffect.demo;

import android.content.Context;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;

import com.netease.nrtc.sdk.common.EglContextWrapper;
import com.netease.nrtc.sdk.video.ExternalVideoCapturer;
import com.netease.nrtc.sdk.video.VideoFrame;
import com.netease.nrtc.video.gl.SurfaceTextureHelper;

import org.webrtc.EglBase;
import org.webrtc.GlTextureFrameBuffer;


/**
 * Created by hzqiujiadi on 2019-11-15.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class OpenGLESCapturer extends ExternalVideoCapturer {

    private final EglContextWrapper wrapper;
    private SurfaceTextureHelper surfaceTextureHelper;
    private GLThread glThread;

    public OpenGLESCapturer(EglContextWrapper eglContextWrapper) {
        this.wrapper = eglContextWrapper;
    }

    @Override
    public void startCapture(int i, int i1, int i2) {
        GLThread thread = glThread;
        if (thread != null) {
            return;
        }

        thread = new GLThreadImpl(wrapper, 66) {
            @Override
            VideoCapturerObserver getVideoCapturerObserver() {
                return OpenGLESCapturer.this.getVideoCapturerObserver();
            }

            @Override
            SurfaceTextureHelper getSurfaceTextureHelper() {
                return surfaceTextureHelper;
            }
        };

        thread.start();
        this.glThread = thread;
    }

    @Override
    public void initialize(Context context, SurfaceTextureHelper surfaceTextureHelper, VideoCapturerObserver videoCapturerObserver) {
        super.initialize(context, surfaceTextureHelper, videoCapturerObserver);
        this.surfaceTextureHelper = surfaceTextureHelper;
    }

    @Override
    public void stopCapture() throws InterruptedException {
        GLThread thread = glThread;
        if (thread != null) {
            thread.stop();
        }

        this.glThread = null;
    }

    @Override
    public void changeCaptureFormat(int i, int i1, int i2) {

    }

    @Override
    public void dispose() {

    }

    private abstract static class GLThreadImpl extends GLThread {

        private final int tickMs;

        private GlTextureFrameBuffer frameBuffer;

        private long lastTs;

        private Simple1 simple1 = new Simple1();

        @Override
        void onInit(EglBase eglBase) {
            frameBuffer = new GlTextureFrameBuffer(GLES20.GL_RGBA);
            frameBuffer.setSize(1024, 1024);
            simple1.init();
            simple1.setSize(1024, 1024);

            next();
        }

        @Override
        void onUnInit(EglBase eglBase) {
            frameBuffer.release();
        }

        void onTick(EglBase eglBase) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer.getFrameBufferId());
            GLES20.glFramebufferTexture2D(
                    GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, frameBuffer.getTextureId(), 0);

            // Check that the framebuffer is in a good state.
            final int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
            if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                throw new IllegalStateException("Framebuffer not complete, status: " + status);
            }

            // draw
            simple1.onDrawFrame();

            eglBase.swapBuffers();
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            android.graphics.Matrix matrix = new android.graphics.Matrix();
            VideoFrame.TextureBuffer buffer = VideoFrame.asTexture(matrix, 1080, 1920, VideoFrame.TextureBuffer.Type.RGB, frameBuffer.getTextureId(), getSurfaceTextureHelper(), new Runnable() {
                @Override
                public void run() {
                    // released, next frame
                    next();
                }
            });
            VideoCapturerObserver observer = getVideoCapturerObserver();
            if (observer != null) {
                observer.onFrameCaptured(new VideoFrame(buffer, 0, SystemClock.elapsedRealtime()), (int) (1000.0 / tickMs), false);
            }

            buffer.release();
        }

        abstract VideoCapturerObserver getVideoCapturerObserver();

        abstract SurfaceTextureHelper getSurfaceTextureHelper();

        private void next() {
            removes();
            long delay;
            if (lastTs == 0) {
                lastTs = System.currentTimeMillis();
                delay = 0;
            } else {
                long now = System.currentTimeMillis();
                long duration = now - lastTs;
                long diff = tickMs - duration;
                delay = diff < 0 ? 0 : diff;
            }

            postDelay(tick, delay);
        }

        GLThreadImpl(EglContextWrapper eglContextWrapper, int tickMs) {
            super(eglContextWrapper);
            this.tickMs = tickMs;
        }

        private Runnable tick = new Runnable() {
            @Override
            public void run() {
                onTick(eglBase);
            }
        };
    }

    private static abstract class GLThread {
        private final EglContextWrapper wrapper;
        private Handler handler;
        EglBase eglBase;
        private final Object glToken = new Object();

        private Runnable init = new Runnable() {
            @Override
            public void run() {
                eglBase = EglBase.create(wrapper.getEglContext(), wrapper.isEGL14Supported());
                eglBase.createDummyPbufferSurface();
                eglBase.makeCurrent();
                onInit(eglBase);
            }
        };

        abstract void onInit(EglBase eglBase);
        abstract void onUnInit(EglBase eglBase);

        private Runnable unInit = new Runnable() {
            @Override
            public void run() {
                eglBase.makeCurrent();
                onUnInit(eglBase);
                eglBase.releaseSurface();
                eglBase.release();
            }
        };

        void postDelay(Runnable runnable, long delay) {
            Handler handler = this.handler;
            if (handler != null) {
                handler.postAtTime(runnable, glToken, SystemClock.uptimeMillis() + delay);
            }
        }

        void removes() {
            Handler handler = this.handler;
            if (handler != null) {
                handler.removeCallbacksAndMessages(glToken);
            }
        }

        GLThread(EglContextWrapper eglContextWrapper) {
            this.wrapper = eglContextWrapper;
        }

        void start() {
            if (handler != null) {
                return;
            }

            HandlerThread thread = new HandlerThread("gl-thread");
            thread.start();
            handler = new Handler(thread.getLooper());
            handler.post(init);
        }

        void stop() {
            if (handler != null) {
                handler.post(unInit);
                handler.getLooper().quitSafely();
            }
            handler = null;
        }
    }
}
