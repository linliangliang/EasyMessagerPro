package com.zhengyuan.easymessengerpro.util;

import android.content.Context;
import android.os.Environment;

import com.zhengyuan.baselib.constants.Constants;

import java.io.File;


/**
 * Created by 林亮 on 2019/1/23。
 *
 * @author 林亮。
 */

public class FileUtil {
    /**
     * @param context 上下文
     * @param file    文件
     */
    public static void createDir(final Context context, final File file) {
        if (file.isDirectory()) {
            if (!file.exists()) {
                file.mkdir();
            }
        }
    }

    /**
     * @param context 上下文
     * @param path    文件路径
     */
    public static void createDir(final Context context, final String path) {
        File file = new File(path);
        if (file.isDirectory() && !file.exists()) {
            file.mkdir();
        }
    }

    /**
     * @param path 退出删除临时文件
     */
    public static void deleteTemFile(final String path) {
        String appPath = Environment.getExternalStorageDirectory() + File.separator + Constants.APP_DIRECTORY;
        String appDownloadPath = Environment.getExternalStorageDirectory() + File.separator + Constants.DOWNLOAD_PATH;
        if (path.contains(appPath) || path.contains(appDownloadPath)) {//只删除这两个文件夹下面的文件，防止错删
            File file = new File(path);
            if (file.exists()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isDirectory()) { // 判断是否为文件夹
                            deleteTemFile(f.getAbsolutePath());
                            try {
                                f.delete();
                            } catch (Exception e) {
                            }
                        } else {
                            if (f.exists()) { // 判断是否存在
                                try {
                                    f.delete();
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
