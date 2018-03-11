package com.example.originaltec.utils;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lhc on 18/3/1.
 */

public class BitmapUtils {
    /**
     * 2.其次压缩图片，通过降低图片的质量来压缩图片
     *
     * @param maxSize
     * @param bitmap
     */
    public static Bitmap compressQuality(int maxSize, Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int quality = 100;
        try {
            //重置为初始状态
            out.reset();
            //将图片进行压缩，并写入到输出流中，此时的quality为100
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
            boolean isCompress = false;
            while (out.toByteArray().length / 1024 > maxSize) {
                //减小压缩质量
                quality -= 10;
                out.reset();
                //减小质量后压缩
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
                isCompress = true;
            }
            if (isCompress) {
                //从流中构造出图片
                bitmap = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.toByteArray().length);

                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * 1.首先压缩图片， 通过压缩图片的尺寸来压缩图片大小，仅仅做了缩小，如果图片本身小于目标大小，不做放大操作
     *
     * @param imageUri
     * @param contentResolver
     * @param targetWidth
     * @param targetHeight
     */

    public static Bitmap compressSize(Uri imageUri, ContentResolver contentResolver,
                                       int targetWidth, int targetHeight) {
        Bitmap thumbnail = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream inputStream;
        try {
            //通过Uri构造图片
            inputStream = contentResolver.openInputStream(imageUri);
            if (inputStream == null) {
                return null;
            } else {
                byte[] data = readStream(inputStream);
                BitmapFactory.decodeByteArray(data, 0, data.length, options);
                //得到图片的宽高
                int outWidth = options.outWidth;
                int outHeight = options.outHeight;
                int scale = Math.min(outWidth / targetWidth, outHeight / targetHeight);

                options.inJustDecodeBounds = false;
                options.inSampleSize = scale;
                thumbnail = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return thumbnail;
    }

    /**
     * 1.首先压缩图片， 通过压缩图片的尺寸来压缩图片大小，仅仅做了缩小，如果图片本身小于目标大小，不做放大操作
     *
     * @param imageFile
     * @param targetWidth
     * @param targetHeight
     */
    public static Bitmap compressSize(File imageFile, int targetWidth, int targetHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //从文件构造bitmap
        BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        int scale = Math.min(outWidth / targetWidth, outHeight / targetHeight);

        options.inJustDecodeBounds = false;
        options.inSampleSize = scale;
        options.inPurgeable = true;
        return BitmapFactory.decodeFile(imageFile.getAbsolutePath());
    }


    /**
     * 解析流数据
     */
    private static byte[] readStream(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return outputStream.toByteArray();
    }

    /**
     * 获取图片旋转角度
     * @param path 图片路径
     * @return
     */
    public static int parseImageDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     *
     ＊ 图片旋转操作
     * @param bm 需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmap(Bitmap bm, int degree) {
        Bitmap returnBm = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }
}
