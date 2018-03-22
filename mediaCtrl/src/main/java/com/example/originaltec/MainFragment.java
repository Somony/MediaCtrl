package com.example.originaltec;


import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.originaltec.utils.BitmapUtils;
import com.example.originaltec.utils.DialogUtils;
import com.example.originaltec.utils.PathUtils;
import com.example.originaltec.utils.audio.RecordManager;
import com.example.originaltec.utils.permission.PermissionResultListener;
import com.example.originaltec.utils.permission.PermissionUtils;
import com.example.originaltec.utils.photoselect.PhotoSelectManager;
import com.example.originaltec.utils.photoselect.PhotoSelector;

import java.io.File;


/**
 * @author 作者：Somon
 * @date 创建时间：2018/3/21
 * @desception 测试录音和fragment中选择图片/拍照
 */
public class MainFragment extends Fragment implements View.OnClickListener {

    private RecordManager recordManager;
    private PhotoSelectManager manager;
    private ImageView imageView;
    private int REQUEST_PERMISSION = 300;

    public MainFragment() {
    }

    public static MainFragment newInstance() {
        return new MainFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        recordManager = RecordManager.getRecordManager();
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        imageView = (ImageView) view.findViewById(R.id.iv);
        view.findViewById(R.id.start).setOnClickListener(this);
        view.findViewById(R.id.stop).setOnClickListener(this);
        view.findViewById(R.id.pick).setOnClickListener(this);

        recordManager.setOnUpdateFileHeadCompleted((file) -> {
            //todo 最终的录音文件
        });

        manager = new PhotoSelectManager();
        manager.from(this);

        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                PermissionUtils.requestPermission(getActivity(),
                        new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        new PermissionResultListener() {

                            @Override
                            public void permissionGranted(int requestCode) {
                                new Thread(() -> {
                                    File file = PathUtils.getFile("media/audio", System.currentTimeMillis() + ".wav");
                                    recordManager.prepare();
                                    recordManager.record(file);
                                }).start();
                            }

                            @Override
                            public void permissionDenied(int requestCode) {

                            }
                        }, 200);
                break;
            case R.id.stop:
                recordManager.stop();
                break;
            case R.id.pick:
                photo();
                break;
            default:
                break;
        }
    }

    private void photo() {
        PermissionUtils.requestPermission(getActivity(),
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                new PermissionResultListener() {
                    @Override
                    public void permissionGranted(int requestCode) {
                        View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_photo_selector_layout, null,
                                false);
                        DialogUtils.showDialog(getActivity(), view, Gravity.BOTTOM, true);

                        view.findViewById(R.id.image_capture).setOnClickListener((v) -> {
                            DialogUtils.dialogDismiss();
                            PhotoSelector.newInstance().setIsCrop(true).imageCapture(getActivity(), (file, fileUri) -> {
                                Log.d("MainActivity", "imageCapture:" + file.getAbsolutePath());
                                Log.d("MainActivity", "imageCapture:" + fileUri.getPath());
                                Bitmap bitmap;
                                if (BitmapUtils.parseImageDegree(file.getAbsolutePath()) == 0) {
                                    bitmap = BitmapUtils.compressQuality(50, BitmapUtils.compressSize(file, 300, 300));
                                } else {
                                    bitmap = BitmapUtils.rotateBitmap(
                                            BitmapUtils.compressQuality(50, BitmapUtils.compressSize(file, 300, 300)), 0);
                                }
                                getActivity().runOnUiThread(() -> imageView.setImageBitmap(bitmap));
                            });
                        });

                        view.findViewById(R.id.image_pick).setOnClickListener((v) -> {
                            DialogUtils.dialogDismiss();
                            PhotoSelector.newInstance().setIsCrop(true).imagePick(getActivity(), (file, fileUri) -> {
                                Log.d("MainActivity", "imagePick:" + file.getAbsolutePath());
                                Log.d("MainActivity", "imagePick:" + fileUri.getPath());
                                Bitmap bitmap;
                                if (BitmapUtils.parseImageDegree(file.getAbsolutePath()) == 0) {
                                    bitmap = BitmapUtils.compressQuality(50, BitmapUtils.compressSize(file, 300, 300));
                                } else {
                                    bitmap = BitmapUtils.rotateBitmap(
                                            BitmapUtils.compressQuality(50, BitmapUtils.compressSize(file, 300, 300)), 0);
                                }
                                getActivity().runOnUiThread(() -> imageView.setImageBitmap(bitmap));
                            });
                        });

                        view.findViewById(R.id.custom_camera).setOnClickListener((v) -> {
                            startActivity(new Intent(getActivity(), CustomCameraActivity.class));
                        });

                        view.findViewById(R.id.cancel).setOnClickListener((v) -> {
                            DialogUtils.dialogDismiss();
                        });
                    }

                    @Override
                    public void permissionDenied(int requestCode) {

                    }
                }, REQUEST_PERMISSION);
    }

}
