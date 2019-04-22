package com.zhengyuan.baselib.views;


import android.app.ProgressDialog;
import android.content.Context;

/**
 * 用来创建进度条对话框
 */
public class ProgressDialogManager {
    private ProgressDialog mProgressDialog;
    private Context context;

    public ProgressDialogManager(Context context) {
        this.context = context;
    }

    /**
     * 显示等待进度框
     *
     * @param message 需要显示的消息
     */
    public void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage(message);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
        } else {
            mProgressDialog.show();
        }
    }

    /**
     * 隐藏等待进度条
     */
    public void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.cancel();
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
