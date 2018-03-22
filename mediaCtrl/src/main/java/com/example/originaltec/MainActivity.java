package com.example.originaltec;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.example.originaltec.utils.BitmapUtils;
import com.example.originaltec.utils.DialogUtils;
import com.example.originaltec.utils.permission.PermissionResultListener;
import com.example.originaltec.utils.permission.PermissionUtils;
import com.example.originaltec.utils.photoselect.PhotoSelectManager;
import com.example.originaltec.utils.photoselect.PhotoSelector;
/**
 * @author 作者：Somon
 * @date   创建时间：2018/3/22
 * @desception  测试图片选择和拍照
 */
public class MainActivity extends AppCompatActivity {

    private PhotoSelectManager manager;
    private final static int REQUEST_PERMISSION = 1;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.iv_photo);
        manager = new PhotoSelectManager();
        manager.from(this);

        MainFragment mainFragment = MainFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_container, mainFragment, mainFragment.getClass()
                .getSimpleName()).commit();
    }

    public void click(View view) {
        PermissionUtils.requestPermission(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                new PermissionResultListener() {
                    @Override
                    public void permissionGranted(int requestCode) {
                        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_photo_selector_layout, null, false);
                        DialogUtils.showDialog(MainActivity.this, view, Gravity.BOTTOM, true);
                        view.findViewById(R.id.image_capture).setOnClickListener((v) -> {
                            DialogUtils.dialogDismiss();
                            PhotoSelector.newInstance().setIsCrop(true).imageCapture(MainActivity.this, (file, fileUri) -> {
                                Log.d("MainActivity", "imageCapture:" + file.getAbsolutePath());
                                Log.d("MainActivity", "imageCapture:" + fileUri.getPath());
                                Bitmap bitmap;
                                if (BitmapUtils.parseImageDegree(file.getAbsolutePath()) == 0) {
                                    bitmap = BitmapUtils.compressQuality(50, BitmapUtils.compressSize(file, 300, 300));
                                } else {
                                    bitmap = BitmapUtils.rotateBitmap(
                                            BitmapUtils.compressQuality(50, BitmapUtils.compressSize(file, 300, 300)), 0);
                                }
                                runOnUiThread(() -> imageView.setImageBitmap(bitmap));
                            });
                        });

                        view.findViewById(R.id.image_pick).setOnClickListener((v) -> {
                            DialogUtils.dialogDismiss();
                            PhotoSelector.newInstance().setIsCrop(true).imagePick(MainActivity.this, (file, fileUri) -> {
                                Log.d("MainActivity", "imagePick:" + file.getAbsolutePath());
                                Log.d("MainActivity", "imagePick:" + fileUri.getPath());
                                Bitmap bitmap;
                                if (BitmapUtils.parseImageDegree(file.getAbsolutePath()) == 0) {
                                    bitmap = BitmapUtils.compressQuality(50, BitmapUtils.compressSize(file, 300, 300));
                                } else {
                                    bitmap = BitmapUtils.rotateBitmap(
                                            BitmapUtils.compressQuality(50, BitmapUtils.compressSize(file, 300, 300)), 0);
                                }
                                runOnUiThread(() -> imageView.setImageBitmap(bitmap));
                            });
                        });

                        view.findViewById(R.id.custom_camera).setOnClickListener((v) -> startActivity(new Intent(MainActivity.this, CustomCameraActivity.class)));

                        view.findViewById(R.id.cancel).setOnClickListener((v) -> {
                            DialogUtils.dialogDismiss();
                        });
                    }

                    @Override
                    public void permissionDenied(int requestCode) {
                        PermissionUtils.showMissingPermissionDialog(MainActivity.this);

                    }
                }, REQUEST_PERMISSION);
    }
}
