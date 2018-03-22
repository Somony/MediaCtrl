package com.example.originaltec.utils.permission;

/**
 * @author 作者：Somon
 * @date 创建时间：2018/3/6
 * @desception 权限申请回调
 */

public interface PermissionResultListener {

    /**
     * 通过授权
     *@param requestCode 请求码
     */
    void permissionGranted(int requestCode);

    /**
     * 拒绝授权
     *@param requestCode 请求码
     */
    void permissionDenied(int requestCode);
}
