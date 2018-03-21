package com.example.originaltec;

import android.Manifest;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.example.originaltec.utils.PathUtils;
import com.example.originaltec.utils.camera.CustomCameraManager;
import com.example.originaltec.utils.permission.PermissionResultListener;
import com.example.originaltec.utils.permission.PermissionUtils;

import java.io.File;
import java.io.FileOutputStream;

public class CustomCameraActivity extends AppCompatActivity {
    private int recordStatus = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 选择支持半透明模式,在有surfaceview的activity中使用。
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_custom_camera);

        CustomCameraManager manager = new CustomCameraManager();
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        Button capture = (Button) findViewById(R.id.button_capture);
        Button record = (Button) findViewById(R.id.button_record);
        preview.addView(manager.cameraSetting(this, 1080, 1500));

        capture.setOnClickListener(v -> manager.capture());

        record.setOnClickListener(v -> {
            if (recordStatus == 0) {
                manager.recordVideo(
                        PathUtils.getFile("media/record", PathUtils.getDate() + ".mp4").getAbsolutePath());
                recordStatus = 1;
                record.setText("stop");
            } else {
                manager.stoprecordVideo();
                recordStatus = 0;
                record.setText("record");

            }
        });

        manager.setPictureTakenListener((data, camera) -> PermissionUtils.requestPermission(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionResultListener() {
            @Override
            public void permissionGranted() {
/*
         * 照片信息就存储在data数组中，用一个fileoutputStream接收即可
         */
                File file = PathUtils.getFile("media/capture", System.currentTimeMillis() + ".jpg");
                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(file);
                    fos.write(data);
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void permissionDenied() {

            }
        }, 400));
    }
}
