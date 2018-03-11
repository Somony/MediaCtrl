package com.example.originaltec.utils.photoselect;

import android.net.Uri;

import java.io.File;

/**
 * @author 作者：LHC
 * @date 创建时间：2018/3/6
 * @desception
 */

public interface PhotoSelectResultListener {

    /**
     * 图片选择的结果
     */
    void photoSelectResult(File file, Uri fileUri);
}
