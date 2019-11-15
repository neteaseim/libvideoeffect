package com.netease.libvideoeffect.demo;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static android.content.pm.PackageManager.PERMISSION_DENIED;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
            }, 1000);
        } else {
            onPermissionOk();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 1000) {
            return;
        }

        for (int k : grantResults) {
            if (k == PERMISSION_DENIED) {
                onPermissionDenied();
                return;
            }
        }

        onPermissionOk();
    }

    private void onPermissionDenied() {
        finish();
    }

    private void onPermissionOk() {
        findViewById(R.id.preview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreviewActivity.launch(v.getContext());
            }
        });
    }
}
