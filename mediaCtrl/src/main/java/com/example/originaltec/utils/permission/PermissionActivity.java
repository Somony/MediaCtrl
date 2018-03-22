package com.example.originaltec.utils.permission;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 作者：Somon
 * @date 创建时间：2018/3/6
 * @desception 作为申请权限的activity，获取到结果后传递给目标界面
 */
public class PermissionActivity extends AppCompatActivity {
    private List<String> permissionList = new ArrayList<>();
    private PermissionResultListener listener;
    private int requestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        String[] permissions = intent.getStringArrayExtra("permission");
        requestCode = intent.getIntExtra("requestCode", -1);
        String listenerKey = intent.getStringExtra("listenerKey");
        listener = PermissionUtils.getListener(listenerKey);
        requestPermission(permissions);
    }

    private void checkPermission(String[] permissionArray) {
        permissionList.clear();
        for (String permission : permissionArray) {
            int i = ContextCompat.checkSelfPermission(this, permission);
            if (i != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }
    }

    private void requestPermission(String[] permissionArray) {
        checkPermission(permissionArray);
        if (permissionList.isEmpty()) {
            if (listener != null) {
                listener.permissionGranted(requestCode);
            }
            finish();
        } else {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == this.requestCode) {
            int grantedCount = 0;
            boolean hasDenied = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    //判断是否勾选禁止后不再询问
                    boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(this, permissionList.get(i));
                    //未勾选，但是多个权限有一个未通过就会为true
                    if (showRequestPermission && !hasDenied) {
                        if (listener != null) {
                            finish();
                            listener.permissionDenied(requestCode);
                            hasDenied = true;
                        }
                    }
                } else {
                    grantedCount++;
                }
            }

            if (grantedCount == grantResults.length) {
                finish();
                if (listener != null) {
                    listener.permissionGranted(requestCode);
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PermissionUtils.clearListener();
        permissionList.clear();
    }
}
