package com.example.originaltec.utils;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author 作者：Somon
 * @date   创建时间：2018/3/22
 * @desception  文件路径处理类
 */
public class PathUtils {
    /**
     * 获取SD path
     *
     * @return
     */
    public static String getSDPath() {
        File sdDir;
        // 判断sd卡是否存在
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (!sdCardExist) {
            return null;
        }
        // 获取跟目录
        sdDir = Environment.getExternalStorageDirectory();
        return sdDir.getAbsolutePath();
    }

    /**
     * @param dirPath  除去根目录之后的路径
     * @param fileName 文件名，加扩展名
     * @return
     */
    public static File getFile(String dirPath, String fileName) {
        String path = getSDPath();
        if (path != null) {
            File dir = new File(path + "/" + dirPath);
            File file = new File(dir, fileName);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return file;
        }
        return null;
    }

    /**
     * 使用时间对文件起名
     *
     * @return
     */
    public static String getDate() {
        Calendar ca = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        return sdf.format(ca.getTimeInMillis());
    }

    /**
     * 删除文件
     */

    public static void deleteFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 递归删除文件夹内文件
     */
    public static void deleteAll(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }

        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                return;
            }

            for (File childFile : childFiles) {
                deleteAll(childFile);
            }
        }
    }
}
