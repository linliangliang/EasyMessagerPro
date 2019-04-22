/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhengyuan.easymessengerpro.xmpp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.easymessengerpro.EMProApplication;

/**
 * 重连接线程
 * <p>
 * 在于服务器断开连接后，自动重连接，
 *
 * @author 徐兵
 */
public class ReconnectionThread extends Thread {
    private static final String LOG_TAG = "ReconnectionThread";
    private final XmppManager xmppManager;
    private AlertDialog dialog;
    private int waiting;

    public void setWaiting(int waiting) {
        this.waiting = waiting;
    }

    public ReconnectionThread(XmppManager xmppManager) {
        this.xmppManager = xmppManager;
        this.waiting = 0;
        dialog = new AlertDialog.Builder(EMProApplicationDelegate.applicationContext).create();
        dialog.setTitle("重连失败，请检查网络");
        dialog.setMessage("是否继续重连");
        dialog.setCancelable(false);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setButton(Dialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                waiting = 0;
            }
        });
        dialog.setButton(Dialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                EMProApplication.stopNotificationService();
            }
        });
        Utils.printCurThread(LOG_TAG);
    }

    /**
     * run方法中代码的才运行在ReconnectionThread线程中
     * 其他代码,包括构造函数, 成员变量(尤其是handler) 都仍然是在 new ReconnectionThread 所在的线程中执行的
     * 所以此handler拿到的仍然是主线程的looper, 也自然能够把代码切到主线程中运行
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
//                    dialog.show();
                    Utils.printCurThread(LOG_TAG);
                    Utils.showToast("重连失败");
                    break;

                default:
                    break;
            }
        }
    };

    public void run() {

        Utils.printCurThread(LOG_TAG);
//        try {

            if (XmppManager.reconnectionFlag) {
                XmppManager.reconnectionFlag = false;
                xmppManager.connect(true);
            } else {

                Message message = handler.obtainMessage();
                message.what = 0;
                message.sendToTarget();
            }
//            while (XmppManager.reconnectionFlag) {
//                if (waiting < 10) {
//                    Log.d(LOG_TAG, "Trying to reconnect in " + waiting + " seconds");
//                    Thread.sleep(1000);
//                    xmppManager.connect(true);
//                    waiting++;
//                } else if (waiting == 10) {
//                    waiting = 11;//不让继续弹框
//
//                    Message message = handler.obtainMessage();
//                    message.what = 0;
//                    message.sendToTarget();
//                }
//            }
//        } catch (final InterruptedException e) {
//            xmppManager.getHandler().post(new Runnable() {
//                public void run() {
//                    xmppManager.getConnectionListener().reconnectionFailed(e);
//                }
//            });
//        }
    }
}
