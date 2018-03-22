package com.example.originaltec.utils.permission;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 作者：Somon
 * @date 创建时间：2018/3/6
 * @desception 权限申请工具类
 */

public class PermissionUtils {
    private static Map<String, PermissionResultListener> listenerMap = new HashMap<>();

    public static void requestPermission(Context context, String[] permissionArray, PermissionResultListener
            listener, int requestCode) {
        Intent intent = new Intent(context, PermissionActivity.class);
        intent.putExtra("permission", permissionArray);
        String key = String.valueOf(System.currentTimeMillis());
        listenerMap.put(key, listener);
        intent.putExtra("listenerKey", key);
        intent.putExtra("requestCode", requestCode);
        context.startActivity(intent);
    }

    static PermissionResultListener getListener(String key) {
        return listenerMap.get(key);
    }

    static void clearListener() {
        listenerMap.clear();
    }

    /**
     * 显示手动去设置的弹框
     *
     * @param context
     */
    public static void showMissingPermissionDialog(Context context) {
        String defaultTitle = "帮助";
        String defaultContent = "当前应用缺少必要权限。\n \n 请点击 \"设置\"-\"权限\"-打开所需权限。";
        String defaultCancel = "取消";
        String defaultEnsure = "设置";

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(defaultTitle);
        builder.setMessage(defaultContent);

        builder.setNegativeButton(defaultCancel, (dialog, which) -> {
            Toast.makeText(context, "权限被拒绝，您可以手动在设置中进行设置", Toast.LENGTH_SHORT).show();
        });

        builder.setPositiveButton(defaultEnsure, (dialog, which) -> gotoSetting(context));

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
}
