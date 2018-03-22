package com.example.originaltec.utils.camera;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;


/**
 * @author 作者：Somon
 * @date 创建时间：2018/3/21
 * @desception 自定义camera管理类
 */

public class CustomCameraManager {
    private String TAG = "CustomCameraManager";

    private Camera mCamera;
    private MediaRecorder mRecorder;
    private SurfaceHolder mSurfaceHolder;

    public CustomCameraManager() {

    }

    /**
     * camera参数设置
     *
     * @param activity
     * @param width    预览宽
     * @param height   预览高
     * @return
     */
    public CameraPreview cameraSetting(Activity activity, int width, int height) {
        Log.e("screenSize", String.format("screenSize width=%d,height=%d", width, height));

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        CameraPreview preView = new CameraPreview(activity, mCamera);

        //让预览和拍出来的照片的大小一样
        preView.setLayoutParams(new LinearLayout.LayoutParams(width, height));

        setCameraDisplayOrientation(activity, 0, mCamera);

        //设置拍照参数
        Camera.Parameters parameters = mCamera.getParameters();
        //设置拍照格式
        parameters.setPictureFormat(ImageFormat.JPEG);
        //设置图片质量
        parameters.setJpegQuality(100);

        //设置图片的最佳的分辨率
        List<Camera.Size> pictureSizesList = parameters.getSupportedPictureSizes();
        Camera.Size picSize = getProperSize(pictureSizesList, ((float) height / width));
        parameters.setPictureSize(picSize.width, picSize.height);

        //手机预览的分辨率
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
        Camera.Size previewSize = getProperSize(previewSizeList, ((float) height / width));
        Log.e("previewSize", String.format("camera previewSize width=%d,height=%d", previewSize.width, previewSize
                .height));

        parameters.setPreviewSize(previewSize.width, previewSize.height);

        //设置自动对焦
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);

        if (parameters.getSupportedSceneModes().contains(Camera.Parameters.SCENE_MODE_HDR)) {
            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_HDR);
        }
        if (parameters.isVideoStabilizationSupported()) {
            parameters.setVideoStabilization(true);
        }

        mCamera.setParameters(parameters);

        return preView;
    }

    /**
     * 从列表中选取合适的分辨率
     * 默认w:h = 16:9
     * <p>tip：这里的w对应屏幕的height
     * h对应屏幕的width<p/>
     */
    private Camera.Size getProperSize(List<Camera.Size> pictureSizeList, float screenRatio) {

        Log.e(TAG, "screenRatio=" + screenRatio);
        Camera.Size result = null;
        for (Camera.Size size : pictureSizeList) {
            Log.e("SIZE", String.format("camera size width=%d,height=%d", size.width, size.height));
            float currentRatio = ((float) size.width) / size.height;
            if (currentRatio - screenRatio == 0) {
                result = size;
                break;
            }
        }

        if (null == result) {
            for (Camera.Size size : pictureSizeList) {
                float curRatio = ((float) size.width) / size.height;
                // 默认w:h = 16:9
                if (curRatio == 16f / 9) {
                    result = size;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * 拍照
     */
    public void capture() {
        // 获取最清晰的焦距
        mCamera.autoFocus((success, camera) -> {
            //第三个参数是回调方法
            mCamera.takePicture(null, null, new PicCallBack());
        });
    }

    /**
     * 录制视频
     *
     * @param videoFilePath 录制视频存放文件路径
     */
    public void recordVideo(String videoFilePath) {

        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.reset();
        }
        try {
            mCamera.stopPreview();
            mCamera.unlock();
            mRecorder.setCamera(mCamera);
            // 这两项需要放在setOutputFormat之前
            mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            // 设置录制视频源为Camera(相机)
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mRecorder.setOrientationHint(90);
            // 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            // 这两项需要放在setOutputFormat之后
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            // 设置录制的视频编码h263
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            // 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错,设置的size部分手机不支持会启动失败 暂未找到合适的方法
            //mRecorder.setVideoSize(width, height);
            // 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
            mRecorder.setVideoFrameRate(30);
            //设置码率，每秒的大小
            mRecorder.setVideoEncodingBitRate(3 * 1024 * 1024);
            // 设置最大的录制时间
            mRecorder.setMaxDuration(80000);

            mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

            mRecorder.setOutputFile(videoFilePath);
            // 准备录制
            mRecorder.prepare();
            // 开始录制
            mRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止录制视频
     */
    public void stoprecordVideo() {
        try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */

    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    /**
     * 设置预览方向和照片方向相同
     *
     * @param activity
     * @param cameraId 摄像头id
     * @param camera
     */
    private void setCameraDisplayOrientation(Activity activity,
                                             int cameraId, android.hardware.Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                degrees = 0;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            // compensate the mirror
            result = (360 - result) % 360;
        } else {
            // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }


    /**
     * A basic Camera preview class
     */
    private class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                // 将holder，这个holder为开始在onCreate里面取得的holder，将它赋给mSurfaceHolder
                mSurfaceHolder = holder;
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mHolder = null;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void turnLightCamera(Context context, boolean turnLightOn) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            if (mCamera != null) {
                Camera.Parameters p = mCamera.getParameters();
                if (turnLightOn && Camera.Parameters.FLASH_MODE_OFF.equals(p.getFlashMode())) {
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                } else if (!turnLightOn && Camera.Parameters.FLASH_MODE_TORCH.equals(p.getFlashMode())) {
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                } else {
                    return;
                }
                mCamera.setParameters(p);
            }
        } else {
            Toast.makeText(context, "请检查设备是否支持闪光灯功能", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 拍照的回调，用来存储拍的照片
     */
    private class PicCallBack implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (listener != null) {
                listener.onPicTaken(data, camera);
            }
        }
    }

    public interface OnPictureTakenListener {
        /**
         * 拍照后的回调
         *
         * @param data
         * @param camera
         */
        void onPicTaken(byte[] data, Camera camera);
    }

    private OnPictureTakenListener listener;

    public void setPictureTakenListener(OnPictureTakenListener listener) {
        this.listener = listener;
    }
}
