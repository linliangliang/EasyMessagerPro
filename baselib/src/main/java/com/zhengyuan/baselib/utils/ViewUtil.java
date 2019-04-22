package com.zhengyuan.baselib.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.Toast;

import com.zhengyuan.baselib.constants.EMProApplicationDelegate;

import java.util.ArrayList;

/**
 * Created by zy on 2017/11/29.
 */

public class ViewUtil {

    public static AlertDialog createNormalDialog(
            Context context,
            String title, String message, String confirm,
            DialogInterface.OnClickListener confirmListener,
            String cancel,
            DialogInterface.OnClickListener cancelListener
    ) {

        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(confirm, confirmListener);
        builder.setNegativeButton(cancel, cancelListener);
        builder.setCancelable(false);

        alertDialog = builder.create();
        return alertDialog;
    }

    public static AlertDialog createSysDialog(
            String title, String message, String confirm,
            DialogInterface.OnClickListener confirmListener,
            String cancel,
            DialogInterface.OnClickListener cancelListener
    ) {

        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(EMProApplicationDelegate.applicationContext);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(confirm, confirmListener);
        builder.setNegativeButton(cancel, cancelListener);
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        return alertDialog;
    }

    public static void showToast(String content) {
        Toast.makeText(EMProApplicationDelegate.applicationContext, content, Toast.LENGTH_SHORT).show();
    }

    /**
     * dp 2 px 单位转换
     *
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {

        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5);
    }

    /**
     * 显示简单dialog
     */
    public static ProgressDialog progressDialog;

    /**
     * new一个加载dialog
     * @param context Activity的context
     * @param message
     * @return
     */
    public static ProgressDialog createNormalCircleProgressDialog(
            Context context,
            String message) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);

        return progressDialog;
    }

    public static ProgressDialog createCircleProgressDialog(String message) {

        ProgressDialog progressDialog = new ProgressDialog(EMProApplicationDelegate.applicationContext);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        return progressDialog;
    }

    /**
     *
     * @param context Activity的context
     * @param message
     * @param cancel
     * @param clickListener
     * @return
     */
    public static ProgressDialog createNormalCircleProgressDialog(
            Context context,
            String message,
            String cancel,
            DialogInterface.OnClickListener clickListener) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, cancel, clickListener);

        return progressDialog;
    }


    public static ProgressDialog createCircleProgressDialog(String message,
                                                            String cancel,
                                                            DialogInterface.OnClickListener clickListener) {

        ProgressDialog progressDialog = new ProgressDialog(EMProApplicationDelegate.applicationContext);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, cancel, clickListener);
        progressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        return progressDialog;
    }

    public static void createCircleProgressDialog(
            Context context,
            String message,
            String cancel,
            DialogInterface.OnClickListener clickListener) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(message);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, cancel, clickListener);
            progressDialog.show();
        } else {
            progressDialog.show();
        }
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
}
