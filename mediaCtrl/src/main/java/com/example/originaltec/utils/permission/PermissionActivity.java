package com.example.originaltec.utils.permission;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 作者：LHC
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
                listener.permissionGranted();
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
            int count = 0;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    //判断是否勾选禁止后不再询问
                    boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(this, permissionList.get(i));
                    //未勾选，但是多个权限有一个未通过就会为true
                    if (showRequestPermission) {
                        //提示手动开启
                        showMissingPermissionDialog();
                    }
                } else {
                    count++;
                }
            }

            if (count == grantResults.length) {
                finish();
                if (listener != null) {
                    listener.permissionGranted();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showMissingPermissionDialog() {
        String defaultTitle = "帮助";
        String defaultContent = "当前应用缺少必要权限。\n \n 请点击 \"设置\"-\"权限\"-打开所需权限。";
        String defaultCancel = "取消";
        String defaultEnsure = "设置";

        AlertDialog.Builder builder = new AlertDialog.Builder(PermissionActivity.this);
        builder.setTitle(defaultTitle);
        builder.setMessage(defaultContent);

        builder.setNegativeButton(defaultCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(PermissionActivity.this, "权限被拒绝，您可以手动在设置中进行设置", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.permissionDenied();
                }
                finish();
            }
        });

        builder.setPositiveButton(defaultEnsure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                gotoSetting(PermissionActivity.this);
                finish();
            }
        });

        builder.setCancelable(false);
        builder.show();
    }

    /**
     * 跳转到当前应用对应的设置页面
     *
     * @param context
     */
    public static void gotoSetting(@NonNull Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PermissionUtils.clearListener();
        permissionList.clear();
    }
}
