package com.example.originaltec.utils.photoselect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.example.originaltec.utils.PathUtils;

import java.io.File;
import java.util.List;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

/**
 * Created by lhc on 18/3/1.
 * 跳转到系统相机拍照／相册选取图片，对图片进行剪切
 */

public class PhotoSelectManager {
    private Activity mActivity;
    private Fragment mFragment;

    static final int REQUEST_IMAGE_CAPTURE = 1000;
    static final int REQUEST_IMAGE_PICK = 2000;
    static final int REQUEST_IMAGE_PICK_N = 3000;
    static final int REQUEST_IMAGE_CROP_CAPTURE = 4000;
    static final int REQUEST_IMAGE_CROP_PICK = 5000;

    private static final String AUTHORITY = "com.example.originaltec.fileprovider";

    //拍照的图片的Uri
    private Uri uriForImageCapture;
    //保存拍照的图片
    private File imageCaptureFile;

    //相册获取的图片 对N以上使用
    private File imagePickFile;
    //相册获取的图片的 Content Uri
    private Uri contentUriForImagePick;

    //裁剪的图片
    private File imageCropFile;
    //裁剪后的 File Uri
    private Uri fileUriForCrop;

    public void from(Activity activity) {
        mFragment = null;
        this.mActivity = activity;
    }

    public void from(Fragment fragment) {
        mActivity = null;
        this.mFragment = fragment;
    }

    private boolean checkExist(Intent intent) {
        boolean exist;
        if (mActivity == null && mFragment == null) {
            throw new NullPointerException("mActivity or mFragment must not be null");
        }
        Activity a = (mActivity == null ? mFragment.getActivity() : mActivity);
        exist = intent.resolveActivity(a.getPackageManager()) != null;

        if (!exist) {
            Toast.makeText(a, "no system app can be use", Toast.LENGTH_SHORT).show();
        }
        return exist;
    }

    private void createImageCaptureUri() {
        Activity a = (mActivity == null ? mFragment.getActivity() : mActivity);
        imageCaptureFile = PathUtils.getFile("media/image", PathUtils.getDate() + ".png");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//7.0及以上
            uriForImageCapture = FileProvider.getUriForFile(a, AUTHORITY, imageCaptureFile);
        } else {
            uriForImageCapture = Uri.fromFile(imageCaptureFile);
        }
    }

    /**
     * 跳转到系统相机拍照
     */
    public void imageCapture() {
        createImageCaptureUri();
        Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriForImageCapture);
        imageCaptureIntent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
        imageCaptureIntent.addFlags(FLAG_GRANT_WRITE_URI_PERMISSION);

        if (!checkExist(imageCaptureIntent)) {
            return;
        }

        if (mActivity != null) {
            mActivity.startActivityForResult(imageCaptureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            mFragment.startActivityForResult(imageCaptureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    /**
     * 跳转到系统相册选取
     */
    public void imagePick() {
        Intent imagePickIntent = new Intent(Intent.ACTION_PICK);
        imagePickIntent.setType("image/*");

        if (!checkExist(imagePickIntent)) {
            return;
        }

        //如果大于等于7.0使用FileProvider将Uri传递过去
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Activity a = (mActivity == null ? mFragment.getActivity() : mActivity);
            imagePickFile = PathUtils.getFile(a);
            contentUriForImagePick = FileProvider.getUriForFile(a, AUTHORITY, imagePickFile);
            imagePickIntent.putExtra(MediaStore.EXTRA_OUTPUT, contentUriForImagePick);
            imagePickIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            imagePickIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (mActivity != null) {
                mActivity.startActivityForResult(imagePickIntent, REQUEST_IMAGE_PICK_N);
            } else {
                mFragment.startActivityForResult(imagePickIntent, REQUEST_IMAGE_PICK_N);
            }
        } else {*/
        if (mActivity != null) {
            mActivity.startActivityForResult(imagePickIntent, REQUEST_IMAGE_PICK);
        } else {
            mFragment.startActivityForResult(imagePickIntent, REQUEST_IMAGE_PICK);
        }
//        }
    }

    /**
     * 拍照图片裁剪
     */
    public void cropImageCapture(Uri uri, int outputX, int outputY) {
        Intent intent = cropSettingIntent(uri, outputX, outputY);

        if (!checkExist(intent)) {
            return;
        }

        if (mActivity != null) {
            mActivity.startActivityForResult(intent, REQUEST_IMAGE_CROP_CAPTURE);
        } else {
            mFragment.startActivityForResult(intent, REQUEST_IMAGE_CROP_CAPTURE);
        }
    }

    /**
     * 相册获取图片裁剪
     */
    public void cropImagePick(Uri uri, int outputX, int outputY) {
        Intent intent = cropSettingIntent(uri, outputX, outputY);

        if (!checkExist(intent)) {
            return;
        }

        if (mActivity != null) {
            mActivity.startActivityForResult(intent, REQUEST_IMAGE_CROP_PICK);
        } else {
            mFragment.startActivityForResult(intent, REQUEST_IMAGE_CROP_PICK);
        }
    }

    /**
     * 创建剪切图片配置
     *
     * @param uri
     * @param outputX
     * @param outputY
     * @return
     */
    @NonNull
    private Intent cropSettingIntent(Uri uri, int outputX, int outputY) {
        Activity a = (mActivity == null ? mFragment.getActivity() : mActivity);
        imageCropFile = PathUtils.getFile("media/image", PathUtils.getDate() + ".png");
        fileUriForCrop = Uri.fromFile(imageCropFile);
//        }

        Intent intent = new Intent("com.android.camera.action.CROP");

        intent.setDataAndType(uri, "image/*");

        intent.putExtra("crop", "true");

        //裁剪的比例
        intent.putExtra("aspectX", 2);

        intent.putExtra("aspectY", 2);

        //裁剪区的宽和高
        intent.putExtra("outputX", outputX);

        intent.putExtra("outputY", outputY);

        intent.putExtra("scale", true);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUriForCrop);

        //true就是返回bitmap，很占内存
        intent.putExtra("return-data", false);

        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        intent.putExtra("noFaceDetection", true); // no face detection

        intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(FLAG_GRANT_WRITE_URI_PERMISSION);

        List<ResolveInfo> resInfoList = a.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            a.grantUriPermission(packageName, fileUriForCrop, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        return intent;
    }

    /**
     * 用来7.0以下相册选取图片后返回的Uri中获取文件路径
     *
     * @param context
     * @param uri
     * @return
     */
    public String getRealPathFromURI(Context context, Uri uri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    public Uri getUriForImageCapture() {
        return uriForImageCapture;
    }

    public File getImageCaptureFile() {
        return imageCaptureFile;
    }

    public File getImagePickFile() {
        return imagePickFile;
    }

    public Uri getContentUriForImagePick() {
        return contentUriForImagePick;
    }

    public File getImageCropFile() {
        return imageCropFile;
    }

    public Uri getFileUriForCrop() {
        return fileUriForCrop;
    }
}
