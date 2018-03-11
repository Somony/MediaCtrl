package com.example.originaltec.utils.permission;

/**
 * @author 作者：LHC
 * @date 创建时间：2018/3/6
 * @desception
 */

public interface PermissionResultListener {

    /**
     * 通过授权
     *
     */
    void permissionGranted();

    /**
     * 拒绝授权
     *
     */
    void permissionDenied();
}
