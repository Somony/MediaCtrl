package com.example.originaltec.utils.photoselect;

import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;


/**
 * @author 作者：Somon
 * @date   创建时间：2018/3/22
 * @desception
 */
public class PhotoSelector {
    private static PhotoSelector selector = new PhotoSelector();
    static final String TYPE_CAPTURE = "imageCapture";
    static final String TYPE_PICK = "imagePick";

    /**
     * 是否进行裁剪
     */
    private boolean isCrop;

    private Map<String, PhotoSelectResultListener> listenerMap = new HashMap<>();

    private PhotoSelector() {
    }

    public static PhotoSelector newInstance() {
        return selector;
    }

    public void imageCapture(Context context, PhotoSelectResultListener listener) {
        Intent intent = new Intent(context, PhotoSelectActivity.class);
        intent.putExtra("type", TYPE_CAPTURE);
        listenerMap.put(TYPE_CAPTURE, listener);
        intent.putExtra("listenerKey", TYPE_CAPTURE);
        context.startActivity(intent);
    }

    public void imagePick(Context context, PhotoSelectResultListener listener) {
        Intent intent = new Intent(context, PhotoSelectActivity.class);
        intent.putExtra("type", TYPE_PICK);
        listenerMap.put(TYPE_PICK, listener);
        intent.putExtra("listenerKey", TYPE_PICK);
        context.startActivity(intent);
    }

    public PhotoSelectResultListener getListener(String key) {
        return listenerMap.get(key);
    }

    public void clearListener() {
        listenerMap.clear();
    }

    public boolean isCrop() {
        return isCrop;
    }

    public PhotoSelector setIsCrop(boolean isCrop) {
        this.isCrop = isCrop;
        return this;
    }
}
