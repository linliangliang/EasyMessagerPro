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
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.baselib.utils.ViewUtil;
import com.zhengyuan.easymessengerpro.EMProApplication;
import com.zhengyuan.easymessengerpro.service.NotificationService;

import org.jivesoftware.smack.ConnectionListener;

/**
 * 连接事件监听类
 * <p>
 * 自动促发监听事件
 *
 */
public class PersistentConnectionListener implements ConnectionListener {

    private static final String TAG = "PersisConnectListener";

    private final XmppManager xmppManager;

    public PersistentConnectionListener(XmppManager xmppManager) {
        this.xmppManager = xmppManager;

        Utils.printCurThread(TAG);
    }

    @Override
    public void connectionClosed() {

        if (NotificationService.isLogout) {

            NotificationService.isLogout = false;
            handler.sendEmptyMessage(3);
        }
        Log.e(TAG, "connectionClosed()...");
    }

    /**
     * 网络断开时,xmpp内收机制会回调这个方法. 这也是自定义xmpp重连机制的入口
     */
    @Override
    public void connectionClosedOnError(Exception e) {
        Log.e(TAG, "connectionClosedOnError()...");

        if (e.toString().contains("stream:error (conflict)")) {
            handler.sendEmptyMessage(4);   //异地登录，被挤掉
        } else {
            handler.sendEmptyMessage(1);  //断网
        }
    }

    @Override
    public void reconnectingIn(int seconds) {
        Log.e(TAG, "reconnectingIn()...");
    }

    @Override
    public void reconnectionFailed(Exception e) {
        Log.e(TAG, "reconnectionFailed()..." + e.getCause());
//        Toast.makeText(Constants.context, "登陆失败，正在重试~", Toast.LENGTH_SHORT).show();
        Message message = handler.obtainMessage();
        message.what = 0;

        handler.sendMessage(message);
    }

    @Override
    public void reconnectionSuccessful() {

        Log.d(TAG, "reconnectionSuccessful()...");

        Message message = handler.obtainMessage();
        message.what = 2;

        handler.sendMessage(message);
    }

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    Utils.showToast("重连失败");
                    break;
                case 1:
                    if (Utils.isNetWorkConnected()) {
                        xmppManager.disconnect();
                        xmppManager.startReconnectionThread();
                    } else {
                        Utils.showToast("网络已断开");
                    }
                    break;
                case 2:
                    Utils.showToast("重连成功");
                    Utils.printCurThread(TAG);
                    break;
                case 3:
                    Utils.hideCircleProgressDialog();
                    Utils.showToast("注销成功");
                    break;
                case 4:

                    Log.d(TAG, "isAppOnForeground " + Utils.isAppOnForeground());
                    AlertDialog alertDialog = ViewUtil.createSysDialog(
                            null,
                            "您的账号在另一台设备登录",
                            "退出应用",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    EMProApplication.stopNotificationService();
                                }
                            },
                            "重新登录",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    xmppManager.disconnect();
                                    xmppManager.startReconnectionThread();
                                }
                            });
                    alertDialog.show();
                default:
                    break;
            }
        }

    };
}
