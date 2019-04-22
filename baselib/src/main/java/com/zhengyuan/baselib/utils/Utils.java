package com.zhengyuan.baselib.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.constants.Constants;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * 公共方法
 * Created by gpsts on 17-6-8.
 */

public class Utils {

    private final static String LOG_TAG = "Utils";

    public static void copyFileUsingFileStreams(File source, File dest)
            throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        } finally {
            input.close();
            output.close();
        }
    }

    public static void showTakePhotoOrChoosePic(final Activity activity,
                                                final String takePhotoName,
                                                final int TAKE_PHOTO_REQUEST_CODE,
                                                final int CHOOSE_PIC_REQUEST_CODE) {

        Dialog dialog = ViewUtil.createNormalDialog(activity,
                null, "选择图片",
                "拍照", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startTakePhotoActivity(activity, takePhotoName, TAKE_PHOTO_REQUEST_CODE);
                    }
                }, "相册", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startChoosePicActivity(activity, CHOOSE_PIC_REQUEST_CODE);
                    }
                });
        dialog.show();
    }

    public static void startTakePhotoActivity(Activity activity, String name, int REQUEST_CODE) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Uri uri = Uri.fromFile(new File(
                Environment.getExternalStorageDirectory() +
                        "/" + Constants.APP_DIRECTORY + "/" + Constants.CACHE_DIRECTORY + "/" + name
        ));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    public static void startChoosePicActivity(Activity activity, int REQUEST_CODE) {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    public static int getIndex(String temp, String i) {
        return temp.lastIndexOf(i);
    }

    public static void printCurThread(String position) {

        Log.d(LOG_TAG, position + " cur process pid " + Process.myPid() + " uid " + Process.myUid()
                        + " tid " + Process.myTid() + " thread id " +
                Thread.currentThread().getId());
    }

    public static DisplayMetrics getWindowMetrics(Activity activity) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        return displayMetrics;
    }

    public static String getStringFromRes(int resId) {

        return EMProApplicationDelegate.applicationContext.getString(resId);
    }
    /**
     * final Context activity  ：调用该方法的context
     * long milliseconds ：震动的时长，单位是毫秒
     * long[] pattern  ：自定义震动模式 。数组中数字的含义依次是[静止时长，震动时长，静止时长，震动时长。。。]时长的单位是毫秒
     * boolean isRepeat ： 是否反复震动，如果是true，反复震动，如果是false，只震动一次
     */
    public static Vibrator Vibrate(final Context activity, long[] pattern, boolean isRepeat) {
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(pattern, isRepeat ? 1 : -1);
        return vib;
    }

    public static String getNetWorkFileSize(String urlPath) {

        float fileLength;
        URL url;
        try {
            url = new URL(urlPath);
            HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
            //根据响应获取文件大小
            fileLength = urlcon.getContentLength();
            if (urlcon.getResponseCode() >= 400) {
                fileLength = -1;
            } else if (fileLength <= 0)
                fileLength = -2;

            Log.d(LOG_TAG, "fileSize " + fileLength + "\n" + "url: " + urlPath);
            if (fileLength < 0)
                return "未知大小";
            else
                return fileLength / 1024 / 1024 + "M";
        } catch (MalformedURLException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }
        return  "未知大小";
    }

    public static String getFileContentFromSDCard(String fileName) {

        try {
            File file = new File(fileName);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String readline = "";
            StringBuffer sb = new StringBuffer();
            while ((readline = br.readLine()) != null) {
                sb.append(readline);
            }
            br.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getFromAssets(Context context, String fileName) {

        AssetManager assetManager = context.getAssets();
        try {
            InputStreamReader inputReader = new InputStreamReader(
                    assetManager.open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line;
            StringBuilder Result = new StringBuilder();
            while ((line = bufReader.readLine()) != null)
                Result.append(line);
            return Result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取apk版本
     *
     * @return
     */
    public static String getVersionName() {

        PackageManager packageManager = EMProApplicationDelegate.applicationContext.getPackageManager();
        try {

            PackageInfo packageInfo = packageManager.getPackageInfo(
                    EMProApplicationDelegate.applicationContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            ActivityInfo[] activityInfos = packageInfo.activities;

            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {

            e.printStackTrace();
        }

        return "";
    }

    public static boolean isApkInDebug() {

        try {
            ApplicationInfo applicationInfo = EMProApplicationDelegate.applicationContext
                    .getApplicationInfo();
            return (applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * APP是否处于前台唤醒状态
     *
     */
    public static boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager)
                EMProApplicationDelegate.applicationContext.getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = EMProApplicationDelegate.applicationContext.getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }

    public static boolean isNetWorkConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                EMProApplicationDelegate.applicationContext
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) {
//            Log.d(LOG_TAG, "Network Type  = " + networkInfo.getTypeName());
//            Log.d(LOG_TAG, "Network State = " + networkInfo.getState());
            if (networkInfo.isConnected()) {

//                Log.i(LOG_TAG, "Network connected");
                return true;
            }
        }

//        Log.e(LOG_TAG, "Network unavailable");
        return false;
    }

    public static void showToast(String content) {
        Toast.makeText(EMProApplicationDelegate.applicationContext, content, Toast.LENGTH_SHORT).show();
    }

    /**
     * 检测APP文件夹是否存在，不存在则创建
     */
    public static void checkAppDirectory() {

        String sdcardPath = Environment.getExternalStorageDirectory().getPath();

        File file = new File(sdcardPath + "/" + Constants.APP_DIRECTORY);

        if (!file.exists()) {
            file.mkdir();
        }

        File logFile = new File(sdcardPath + "/"
                + Constants.APP_DIRECTORY + "/" + Constants.LOG_DIRECTORY);
        if (!logFile.exists()) {
            logFile.mkdir();
        }

        File pluginFile = new File(sdcardPath + "/"
                + Constants.APP_DIRECTORY + "/" + Constants.PLUGIN_DIRECTORY);
        if (!pluginFile.exists()) {
            pluginFile.mkdir();
        }

        File cacheFile = new File(sdcardPath + "/"
                + Constants.APP_DIRECTORY + "/" + Constants.CACHE_DIRECTORY);
        if (!cacheFile.exists()) {
            cacheFile.mkdir();
        }
    }

    public static String getPluginDir() {
        return Environment.getExternalStorageDirectory().getPath() + "/"
                + Constants.APP_DIRECTORY + "/" + Constants.PLUGIN_DIRECTORY + "/";
    }

    public static String getApkDir() {
        return Environment.getExternalStorageDirectory().toString() + File.separator
                + Constants.DOWNLOAD_DIRRECTORY + File.separator + Constants.APK_DIRECTORY + File.separator;
    }

    /**
     * 显示简单dialog
     */
    public static ProgressDialog progressDialog;

    /**
     * new一个加载dialog
     * @param message
     * @return
     */
    public static ProgressDialog createCircleProgressDialog(String message) {

        ProgressDialog progressDialog = new ProgressDialog(EMProApplicationDelegate.applicationContext);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        return progressDialog;
    }

    public static void createCircleProgressDialog(Context context, String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(message);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            progressDialog.show();
        } else {
            progressDialog.show();
        }
    }

    public static void hideCircleProgressDialog() {
        if (progressDialog != null) {
            progressDialog.cancel();
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    /**
     * 检测软键盘显示情况
     *
     * @param context
     * @param view
     */
    public static void showSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        //imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    public static void hideSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0); //强制隐藏键盘
    }

    public static boolean isShowSoftInput(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        //获取状态信息
        return imm.isActive();//true 打开
    }
}
