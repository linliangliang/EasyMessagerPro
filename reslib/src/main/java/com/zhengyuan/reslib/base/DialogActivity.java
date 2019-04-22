package com.zhengyuan.reslib.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by zy on 2017/12/1.
 */

public class DialogActivity extends Activity {

    public void onCreate(Bundle bundle) {

        super.onCreate(bundle);

        ProgressDialog progressDialog = new ProgressDialog(this);

//        Intent intent =
//        progressDialog.setMessage(message);
//        progressDialog.setCancelable(false);
//        progressDialog.setIndeterminate(true);
//        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, cancel, clickListener);
    }
}
