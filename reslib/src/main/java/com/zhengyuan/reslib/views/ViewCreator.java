package com.zhengyuan.reslib.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;

import com.zhengyuan.reslib.R;

/**
 * Created by zy on 2017/12/6.
 */

public class ViewCreator {

    public static Dialog createBottomShowDialog(Activity context) {

        Dialog loadDialog = new Dialog(context, R.style.ActionSheetDialogStyle);

        Window dialogWindow = loadDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);

        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();

        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        lp.height = metrics.heightPixels * 2 / 3;
        lp.width = metrics.widthPixels;
        dialogWindow.setAttributes(lp);

        return loadDialog;
    }
}
