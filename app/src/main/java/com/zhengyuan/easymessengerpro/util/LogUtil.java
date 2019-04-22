package com.zhengyuan.easymessengerpro.util;

import android.util.Log;

/**
 * Created by 林亮 on 2018/11/23
 * 日志工具包，开发时LOGLEVEL设置6，表示都输出。发布时LOGLEVEL设置为0，表示禁止输出
 */
public class LogUtil {
    private static int LOGLEVEL = 6;//开发时设置6，表示都输出。发布时设置为0，表示禁止输出
    private static int VERBOSE = 1;
    private static int DEBUG = 2;
    private static int INFO = 3;
    private static int WARN = 4;
    private static int ERROR = 5;

    public static void v(String tag, String msg) {
        if (LOGLEVEL > VERBOSE) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (LOGLEVEL > DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (LOGLEVEL > INFO) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (LOGLEVEL > WARN) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (LOGLEVEL > ERROR) {
            Log.e(tag, msg);
        }
    }
}
