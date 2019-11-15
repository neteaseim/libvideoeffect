package com.netease.libvideoeffect.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.netease.nrtc.sdk.NRtc;
import com.netease.nrtc.sdk.NRtcCallback;
import com.netease.nrtc.sdk.NRtcParameters;
import com.netease.nrtc.sdk.audio.AudioFrame;
import com.netease.nrtc.sdk.common.ImageFormat;
import com.netease.nrtc.sdk.common.VideoFilterParameter;
import com.netease.nrtc.sdk.common.VideoFrame;
import com.netease.nrtc.sdk.common.statistics.NetStats;
import com.netease.nrtc.sdk.common.statistics.RtcStats;
import com.netease.nrtc.sdk.common.statistics.SessionStats;
import com.netease.nrtc.sdk.video.VideoCapturerFactory;
import com.netease.nrtc.video.render.IVideoRender;
import com.netease.vcloud.video.effect.VideoEffect;
import com.netease.vcloud.video.effect.VideoEffectFactory;

import java.util.Random;
import java.util.Set;

/**
 * Created by hzqiujiadi on 2019-08-07.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class PreviewActivity extends AppCompatActivity {

    private NRtc nrtc;

    public static void launch(Context context) {
        Intent i = new Intent(context, PreviewActivity.class);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        try {
            nrtc = NRtc.create(this, "fake", new NRtcCallback() {
                @Override
                public void onJoinedChannel(long l, String s, String s1, int i) {

                }

                @Override
                public void onLeftChannel(SessionStats sessionStats) {

                }

                @Override
                public void onError(int i, int i1) {

                }

                @Override
                public void onDeviceEvent(int i, String s) {

                }

                @Override
                public void onVideoCapturerStarted(boolean b) {

                }

                @Override
                public void onVideoCapturerStopped() {

                }

                @Override
                public void onCallEstablished() {

                }

                @Override
                public void onUserJoined(long l) {

                }

                @Override
                public void onUserLeft(long l, RtcStats rtcStats, int i) {

                }

                @Override
                public void onNetworkQuality(long l, int i, NetStats netStats) {

                }

                @Override
                public void onUserMuteAudio(long l, boolean b) {

                }

                @Override
                public void onUserMuteVideo(long l, boolean b) {

                }

                @Override
                public void onUserEnableVideo(long l, boolean b) {

                }

                @Override
                public void onConnectionTypeChanged(int i) {

                }

                @Override
                public void onFirstVideoFrameAvailable(long l) {

                }

                @Override
                public void onVideoFpsReported(long l, int i) {

                }

                @Override
                public void onFirstVideoFrameRendered(long l) {

                }

                @Override
                public void onVideoFrameResolutionChanged(long l, int i, int i1, int i2) {

                }

                @Override
                public boolean onVideoFrameFilter(com.netease.nrtc.sdk.video.VideoFrame videoFrame, com.netease.nrtc.sdk.video.VideoFrame[] videoFrames, VideoFilterParameter videoFilterParameter) {
                    return false;
                }

                @Override
                public boolean onVideoFrameFilter(VideoFrame videoFrame, boolean b) {
                    applyFilter(videoFrame, b);
                    return true;
                }

                @Override
                public boolean onAudioFrameFilter(AudioFrame audioFrame) {
                    return false;
                }

                @Override
                public void onAudioDeviceChanged(int i, Set<Integer> set, boolean b) {

                }

                @Override
                public void onReportSpeaker(int i, long[] longs, int[] ints, int i1) {

                }

                @Override
                public void onSessionStats(SessionStats sessionStats) {

                }

                @Override
                public void onLiveEvent(int i) {

                }

                @Override
                public void onAudioMixingProgressUpdated(long l, long l1) {

                }

                @Override
                public void onAudioEffectPreload(int i, int i1) {

                }

                @Override
                public void onAudioEffectPlayEvent(int i, int i1) {

                }

                @Override
                public void onPublishVideoResult(int i) {

                }

                @Override
                public void onUnpublishVideoResult(int i) {

                }

                @Override
                public void onSubscribeVideoResult(long l, int i, int i1) {

                }

                @Override
                public void onUnsubscribeVideoResult(long l, int i, int i1) {

                }

                @Override
                public void onRemoteUnpublishVideo(long l) {

                }

                @Override
                public void onRemotePublishVideo(long l, int[] ints) {

                }

                @Override
                public void onSubscribeAudioResult(int i) {

                }

                @Override
                public void onUnsubscribeAudioResult(int i) {

                }
            });
            nrtc.setParameter(NRtcParameters.KEY_VIDEO_FRAME_FILTER, true);
            IVideoRender render = findViewById(R.id.renderer);
            nrtc.enableVideo();
            nrtc.setupLocalVideoRenderer(render, 0, false);
            nrtc.setupVideoCapturer(VideoCapturerFactory.createCameraCapturer(true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nrtc != null) {
            nrtc.startVideoPreview();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nrtc != null) {
            nrtc.stopVideoPreview();
        }
    }

    private VideoEffect mVideoEffect;
    private Handler mVideoEffectHandler;
    private boolean mHasSetFilterType;
    private Random mRandom;
    private int mCurWidth, mCurHeight;
    int mDropFramesWhenConfigChanged = 0;
    private static final VideoEffect.FilterType[] FILTERS = new VideoEffect.FilterType[]{
            VideoEffect.FilterType.brooklyn, VideoEffect.FilterType.calm, VideoEffect.FilterType.clean,
            VideoEffect.FilterType.fairytale, VideoEffect.FilterType.nature, VideoEffect.FilterType.healthy,
            VideoEffect.FilterType.pixar, VideoEffect.FilterType.tender, VideoEffect.FilterType.whiten

    };

    private void randomSelectFilterType() {
        if (mVideoEffect == null) return;
        if (mRandom == null) {
            mRandom = new Random();
        }
        int ordinal = mRandom.nextInt(200);
        VideoEffect.FilterType filter = FILTERS[ordinal % FILTERS.length];
        mVideoEffect.setFilterLevel(0.4f);
        mVideoEffect.setFilterType(filter);
    }

    protected synchronized void notifyCapturerConfigChange() {
        mDropFramesWhenConfigChanged = 2;
    }

    protected boolean applyFilter(VideoFrame frame, boolean mirrorExternal) {
        if (isFinishing()) return false;

        if (mVideoEffect == null) {
            mVideoEffectHandler = new Handler();
            mVideoEffect = VideoEffectFactory.getVCloudEffect();
            mVideoEffect.init(getApplicationContext(), true, false);
            mVideoEffect.setBeautyLevel(3);
            mVideoEffect.setFilterLevel(0.3f);
        }

        if (mCurWidth != frame.width || mCurHeight != frame.height) {
            mCurWidth = frame.width;
            mCurHeight = frame.height;
            notifyCapturerConfigChange();
        }



        VideoEffect.DataFormat format = frame.format == ImageFormat.I420 ? VideoEffect.DataFormat.YUV420 : VideoEffect.DataFormat.NV21;
        byte[] intermediate = mVideoEffect.filterBufferToRGBA(format, frame.data, frame.width, frame.height);
        if (!mHasSetFilterType) {
            mHasSetFilterType = true;
            mVideoEffect.setFilterType(VideoEffect.FilterType.fairytale);
            return true;
        }

        boolean needMirrorData = false;
        VideoEffect.YUVData[] result = mVideoEffect.TOYUV420(intermediate, VideoEffect.DataFormat.RGBA, frame.width, frame.height,
                frame.rotation, 90, frame.width, frame.height, needMirrorData, true);

        synchronized (this) {
            if (mDropFramesWhenConfigChanged-- > 0) return false;
        }

        System.arraycopy(result[0].data, 0, frame.data, 0, result[0].data.length);
        frame.width = result[0].width;
        frame.height = result[0].height;
        frame.dataLen = result[0].data.length;
        frame.rotation = 0;
        frame.dualInput = needMirrorData;

        //默认都是转换成I420
        frame.format = ImageFormat.I420;
        return true;
    }
}
