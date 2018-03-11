package com.example.originaltec.utils.permission;

import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 作者：LHC
 * @date 创建时间：2018/3/6
 * @desception 权限申请工具类
 */

public class PermissionUtils {
    private static Map<String, PermissionResultListener> listenerMap = new HashMap<>();

    public static void requestPermission(Context context, String[] permissionArray, PermissionResultListener
            listener,int requestCode) {
        Intent intent = new Intent(context, PermissionActivity.class);
        intent.putExtra("permission", permissionArray);
        String key = String.valueOf(System.currentTimeMillis());
        listenerMap.put(key, listener);
        intent.putExtra("listenerKey", key);
        intent.putExtra("requestCode",requestCode);
        context.startActivity(intent);
    }

    public static PermissionResultListener getListener(String key) {
        return listenerMap.get(key);
    }

    public static void clearListener() {
        listenerMap.clear();
    }
}
