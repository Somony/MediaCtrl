package com.example.originaltec.utils.photoselect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.File;

public class PhotoSelectActivity extends AppCompatActivity {
    private PhotoSelectManager manager;
    private PhotoSelectResultListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = new PhotoSelectManager();
        manager.from(this);

        Intent data = getIntent();
        if (data == null) {
            return;
        }
        String listenerKey = data.getStringExtra("listenerKey");
        listener = PhotoSelector.newInstance().getListener(listenerKey);
        String type = data.getStringExtra("type");
        switch (type) {
            case PhotoSelector.TYPE_CAPTURE:
                manager.imageCapture();
                break;
            case PhotoSelector.TYPE_PICK:
                manager.imagePick();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        File imageFile = null;
        Uri imageUri = null;
        boolean needFinish = true;

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PhotoSelectManager.REQUEST_IMAGE_CAPTURE:
                    imageFile = manager.getImageCaptureFile();
                    imageUri = manager.getUriForImageCapture();
                    if (PhotoSelector.newInstance().isCrop()) {
                        manager.cropImageCapture(imageUri, 200, 200);
                        needFinish = false;
                    }
                    break;
                case PhotoSelectManager.REQUEST_IMAGE_PICK:
                    imageUri = data.getData();
                    imageFile = new File(manager.getRealPathFromURI(this, imageUri));
                    if (PhotoSelector.newInstance().isCrop()) {
                        manager.cropImagePick(imageUri, 200, 200);
                        needFinish = false;
                    }
                    break;
                //todo 7.0相册选取不需要FileProvider兼。待需要测试其他手机
                case PhotoSelectManager.REQUEST_IMAGE_PICK_N:
                    imageFile = manager.getImagePickFile();
                    imageUri = manager.getContentUriForImagePick();
                  /*  if (PhotoSelector.newInstance().isCrop()) {
                        manager.cropImagePick(imageUri, 200, 200);
                        needFinish = false;
                    }*/

                    break;
                case PhotoSelectManager.REQUEST_IMAGE_CROP_CAPTURE:
                case PhotoSelectManager.REQUEST_IMAGE_CROP_PICK:
                    imageFile = manager.getImageCropFile();
                    imageUri = manager.getFileUriForCrop();
                    break;
                default:
                    break;
            }

            if (listener != null && needFinish) {
                //对结果的处理太费时间，需要在自线程处理
                File finalImageFile = imageFile;
                Uri finalImageUri = imageUri;
                new Thread(() -> listener.photoSelectResult(finalImageFile, finalImageUri)).start();
                finish();
            }
        }
    }
}
