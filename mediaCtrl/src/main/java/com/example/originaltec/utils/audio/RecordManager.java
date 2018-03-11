package com.example.originaltec.utils.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.example.originaltec.utils.PathUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;

/**
 * @author 作者：Somon
 * @date 创建时间：2017/2/20
 * @desception 录音管理类, 实现
 * 1.录音pcm格式或者wav格式
 * 2.录音开始，暂停和继续，结束，保存
 * 3.录音期间可以保存指定时长的片段录音文件
 * 录音文件16k采样率，单通道，16位
 * 保存片段文件，其中片段文件和完整文件格式应该一样
 */

public class RecordManager {

    /**
     * 录音文件配置信息
     */
    private final int frequency = 16000;
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * 外界调用setRecording，isRecording设置录音或者停止
     */
    private boolean isRecording;

    //记录写入的文件大小
    private int mDataSize;

    //完整的wav文件
    private File audioFile;

    /**
     * 暂停录音
     * 外界调用setPause，isPause控制暂停
     */
    private boolean pause = false;

    private static RecordManager recordManager;

    public static RecordManager getRecordManager() {

        if (recordManager == null) {
            recordManager = new RecordManager();
        }
        return recordManager;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    /**
     * 录音
     *
     * @param audioFile        存放最终的完整录音文件
     * @param needSnippet      是否需要片段文件
     * @param snippetAudioList 片段文件保存集合，needSnippet为false时默认为null
     * @param recogCutTime     片段语音分割时间，传入的是字节大小，160000是5秒
     * @param type             pcm或wav,只允许这两个值
     */
    public void record(File audioFile, Boolean needSnippet, LinkedList<byte[]> snippetAudioList, int recogCutTime, String type) {
        this.audioFile = audioFile;

        AudioRecord audioRecord = null;
        DataOutputStream rawBaos = null;
        FileOutputStream rawOS = null;
        File file;

        OutputStream os = null;
        ByteArrayOutputStream baos = null;

        int bufferReadResult;
        boolean fileFlag = true;

        mDataSize = 0;

        snippetAudioList = needSnippet ? snippetAudioList : null;

        try {
            //输出流
            int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);

            byte[] buffer = new byte[bufferSize];
            rawOS = new FileOutputStream(audioFile);
            rawBaos = new DataOutputStream(rawOS);
            if (type.equals("wav")) {
                rawBaos.write(getWavHeader(0));
            }
            audioRecord.startRecording();

            while (isRecording) {
                if (!pause) {
                    bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                    if (bufferReadResult > 0) {
                        //保存原始raw文件
                        rawBaos.write(buffer, 0, bufferReadResult);
                        mDataSize += bufferReadResult;

                        //保存片段文件
                        if (needSnippet) {
                            if (baos == null || baos.size() < recogCutTime) {
                                if (fileFlag) {
                                    baos = new ByteArrayOutputStream();
                                    fileFlag = false;
                                }
                                if (baos != null) {
                                    baos.write(buffer, 0, bufferReadResult);
                                }
                            } else if (baos.size() >= recogCutTime) {
                                try {
                                    //在文件长度超过了recogCutTime时就需要保存一份片段文件，但是这个时候的上一个if没有进，所以会将一个buffer的信息丢失，需要在这个不上，
                                    //所以保存的最终文件时长是recogCutTime+buffer的长度
                                    if (baos != null) {
                                        baos.write(buffer, 0, bufferReadResult);
                                    }
                                    fileFlag = true;
                                    file = type.equals("wav") ? PathUtils.getFile(audioFile.getParent(), "record" + System.currentTimeMillis() + ".wav") : PathUtils.getFile(audioFile.getParent(), "record" + System.currentTimeMillis() + ".pcm");
                                    buffer = baos.toByteArray();
                                    os = new FileOutputStream(file);
                                    //写入文件头就是wav文件
                                    if (type.equals("wav")) {
                                        os.write(getWavHeader(buffer.length));
                                    }
                                    os.write(buffer);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    try {
                                        if (os != null) {
                                            try {
                                                os.close();
                                                os = null;
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        if (baos != null) {
                                            try {
                                                baos.close();
                                                baos = null;
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        synchronized (this) {
                                            snippetAudioList.offer(buffer);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //录音完毕但是不足recogCutTime的部分也要保存下来
            if (baos != null) {
                if (!isRecording && baos.size() < recogCutTime) {
                    try {
                        file = type.equals("wav") ? PathUtils.getFile(audioFile.getParent(), "record" + System.currentTimeMillis() + ".wav")
                                : PathUtils.getFile(audioFile.getParent(), "record" + System.currentTimeMillis() + ".pcm");
                        buffer = baos.toByteArray();
                        os = new FileOutputStream(file);
                        if (type.equals("wav")) {
                            os.write(getWavHeader(buffer.length));
                        }
                        os.write(buffer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (os != null) {
                                try {
                                    os.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (baos != null) {
                                try {
                                    baos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            synchronized (this) {
                                snippetAudioList.offer(buffer);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            audioRecord.stop();
            if (type.equals("wav")) {
                //修改wav头中信息
                writeDataSize();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (rawOS != null) {
                try {
                    rawOS.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (rawBaos != null) {
                try {
                    rawBaos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (audioRecord != null && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
                audioRecord.release();
            }
        }
    }

    /**
     * 添加wav头
     *
     * @param totalAudioLen
     * @return
     */
    private byte[] getWavHeader(long totalAudioLen) {

        int mChannels = 1;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = frequency;
        long byteRate = frequency * 2 * mChannels;

        byte[] header = new byte[44];
        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) mChannels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * mChannels);  // block align
        header[33] = 0;
        header[34] = 16;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        return header;
    }

    /**
     * 更新wav头信息
     */
    private void writeDataSize() {
        try {
            long WAV_CHUNKSIZE_OFFSET = 4;
            int WAV_CHUNKSIZE_EXCLUDE_DATA = 36;
            long WAV_SUB_CHUNKSIZE2_OFFSET = 40;
            RandomAccessFile wavFile = new RandomAccessFile(audioFile, "rw");
            wavFile.seek(WAV_CHUNKSIZE_OFFSET);
            wavFile.write(intToByteArray((int) (mDataSize + WAV_CHUNKSIZE_EXCLUDE_DATA)), 0, 4);
            wavFile.seek(WAV_SUB_CHUNKSIZE2_OFFSET);
            wavFile.write(intToByteArray((int) (mDataSize)), 0, 4);
            wavFile.close();
            if (mListener != null) {
                mListener.completed(audioFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] intToByteArray(int data) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(data).array();
    }

    /**
     * 对完成文件头更新之后的回调，此时可以对文件进行操作
     */
    public interface OnUpdateFileHeadCompleted {
        void completed(File file);
    }

    private OnUpdateFileHeadCompleted mListener;

    public void setOnUpdateFileHeadCompleted(OnUpdateFileHeadCompleted listener) {
        mListener = listener;
    }
}
