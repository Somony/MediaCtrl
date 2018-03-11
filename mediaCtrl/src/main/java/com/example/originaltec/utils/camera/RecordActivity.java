package com.example.originaltec.utils.camera;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.originaltec.R;
import com.example.originaltec.utils.PathUtils;

import java.io.File;
import java.util.List;

/**
 * 录制视频
 */
public class RecordActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback {

    private SurfaceView mSurfaceview;
    private Button mBtnStartStop;// 开始停止录制按键
    private Button mBtnBack;
    private boolean mStartedFlg = false;
    private MediaRecorder mRecorder;// 录制视频的类
    private SurfaceHolder mSurfaceHolder;// 显示视频
    private Camera camera;
    private ProgressBar timeView;// 在屏幕顶部显示录制时间
    private int time = 0;
    private Handler handler;
    private Button mBtnReview;
    private String path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        getWindow().setFormat(PixelFormat.TRANSLUCENT); // 选择支持半透明模式,在有surfaceview的activity中使用。
        setContentView(R.layout.activity_record);// 加载布局

        timeView = (ProgressBar) findViewById(R.id.time);
        mSurfaceview = (SurfaceView) findViewById(R.id.surfaceview);
        mBtnStartStop = (Button) findViewById(R.id.btn1);
        mBtnReview = (Button) findViewById(R.id.btn2);
        mBtnBack = (Button) findViewById(R.id.btn3);

        SurfaceHolder holder = mSurfaceview.getHolder();// 取得holder

        holder.addCallback(this); // holder加入回调接口

        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// setType必须设置，要不出错.

        mBtnStartStop.setOnClickListener(this);
        mBtnReview.setOnClickListener(this);
        mBtnBack.setOnClickListener(this);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        camera = Camera.open(); // 获取Camera实例

        try {
            camera.setPreviewDisplay(holder);
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);//自动对焦
            if (parameters.getSupportedSceneModes().contains(Camera.Parameters.SCENE_MODE_HDR)) {
                parameters.setSceneMode(Camera.Parameters.SCENE_MODE_HDR);
            }
            if (parameters.isVideoStabilizationSupported()) {
                parameters.setVideoStabilization(true);
            }

            /**
             * 所有的对焦模式
             */
            List<String> focusModeList = parameters.getSupportedFocusModes();
            for (int i = 0; i < focusModeList.size(); i++) {
                String focusMode = focusModeList.get(i);
                Log.i("FOCUS_MODE", String.format("camera focusMode=%s", focusMode));
            }

            /**
             * 手机支持的分辨率
             */
            List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
            for (int i = 0; i < previewSizeList.size(); i++) {
                Camera.Size size = previewSizeList.get(i);
                Log.i("PREVIEW_SIZE", String.format("camera preview width=%d,height=%d", size.width, size.height));
            }

            /**
             * 手机支持的帧率
             */
            List<int[]> fpsList = parameters.getSupportedPreviewFpsRange();
            for (int i = 0; i < fpsList.size(); i++) {
                int[] fps = fpsList.get(i);
                Log.i("FPS", String.format("camera preview fps min=%d,max=%d", fps[0], fps[1]));
            }
            camera.setParameters(parameters);
            mSurfaceview.setLayoutParams(new LinearLayout.LayoutParams(width,
                    height));
        } catch (Exception e) {
            // 如果出现异常，则释放Camera对象
//            camera.release();
        }
        camera.setDisplayOrientation(90);// 设置预览视频时时竖屏
        // 启动预览功能
        camera.startPreview();
        // 将holder，这个holder为开始在onCreate里面取得的holder，将它赋给mSurfaceHolder
        mSurfaceHolder = holder;

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        // 将holder，这个holder为开始在onCreate里面取得的holder，将它赋给mSurfaceHolder
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        // surfaceDestroyed的时候同时对象设置为null
        mSurfaceview = null;
        mSurfaceHolder = null;
        if (mRecorder != null) {
            mRecorder.release(); // Now the object cannot be reused
            mRecorder = null;
        }
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn1:
                //录制和停止
                mBtnReview.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "最大拍摄时间为30s", Toast.LENGTH_SHORT).show();
                if (!mStartedFlg) {
                    handler = new Handler() {
                        public void handleMessage(android.os.Message msg) {
                            switch (msg.what) {
                                case 1:// 开始录制
                                    timeView.setMax(30);
                                    timeView.setProgress(time);
                                    break;
                                case 2://时间到了
                                    stop();
                                    break;
                                default:
                                    break;
                            }
                        }
                    };
                    // 开始
                    if (mRecorder == null) {
                        mRecorder = new MediaRecorder(); // 创建mediarecorder的对象
                        mRecorder.reset();
                    }
                    try {
                        camera.stopPreview();
                        camera.unlock();
                        mRecorder.setCamera(camera);
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);// 这两项需要放在setOutputFormat之前
                        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);// 设置录制视频源为Camera(相机)
                        mRecorder.setOrientationHint(90);
                        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);// 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4

                        // 这两项需要放在setOutputFormat之后
                        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);// 设置录制的视频编码h263
                        mRecorder.setVideoSize(480, 320);// 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
                        mRecorder.setVideoFrameRate(15);// 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
                        mRecorder.setVideoEncodingBitRate(2 * 1024 * 1024);//设置码率，每秒的大小
                        mRecorder.setMaxDuration(80000);// 设置最大的录制时间
                        mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());


                        path = PathUtils.getFile("media/record", PathUtils.getDate() + ".mp4").getAbsolutePath();
                        mRecorder.setOutputFile(path);

                        mRecorder.prepare();// 准备录制
                        mRecorder.start(); // 开始录制
                        Log.d("MainActivity", "开始录制视频");

                        new Thread(() -> {
                            while (time < 31 && mStartedFlg) {//只设置时间会是一个死循环，一直在走
                                try {
                                    time++;
                                    Thread.sleep(1000);
                                    if (handler != null) {
                                        handler.sendEmptyMessage(1);
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (time == 31) {
                                    if (handler != null) {
                                        handler.sendEmptyMessage(2);
                                    }
                                }
                            }
                        }).start();
                        mStartedFlg = true;
                        mBtnStartStop.setText("Stop");
                        Log.d("MainActivity", "文字改为stop");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // stop
                    if (mStartedFlg) {
                        stop();
                    }

                }
                break;
            case R.id.btn2:
                //回放,跳转播放
                Toast.makeText(this, "点击了", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Log.d("MainActivity", "文件地址" + path);
                File file = new File(path);
                intent.setDataAndType(Uri.fromFile(file), "video/mp4");
                this.startActivity(intent);
                break;
            case R.id.btn3:
                Intent data = new Intent();
                data.putExtra("videouri", Uri.parse(path));
                setResult(RESULT_OK, data);
                finish();
                break;
        }
    }

    private void stop() {
        try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            mBtnStartStop.setText("Start");
            time = 0;
            timeView.setProgress(0);
            if (mBtnReview.getVisibility() == View.INVISIBLE)
                mBtnReview.setVisibility(View.VISIBLE);
            if (mBtnBack.getVisibility() == View.INVISIBLE)
                mBtnBack.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mStartedFlg = false;
            handler = null;

        }
    }


}
