package com.zhengyuan.baselib.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by zy on 2017/11/6.
 */

public class SimpleDialog {

    private AlertDialog.Builder builder;
    private String title, posBtnName, negBtnName;

    public SimpleDialog(Context context,
                        String title,
                        String posBtnName, String negBtnName) {

        builder = new AlertDialog.Builder(context);

        this.title = title;
        this.posBtnName = posBtnName;
        this.negBtnName = negBtnName;
    }

    DialogInterface.OnClickListener posListener, negListener;
    public void setPosBtnListener(DialogInterface.OnClickListener listener) {
        posListener = listener;
    }

    public void setNegBtnListener(DialogInterface.OnClickListener listener) {
        negListener = listener;
    }

    private AlertDialog alertDialog;
    public void show() {

        if (alertDialog == null) {
            builder.setTitle(title);

            builder.setPositiveButton(posBtnName, posListener);
            builder.setNegativeButton(negBtnName, negListener);
            builder.setCancelable(true);
            alertDialog = builder.create();
        }
        alertDialog.show();
    }
}
