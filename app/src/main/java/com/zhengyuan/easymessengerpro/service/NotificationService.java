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
package com.zhengyuan.easymessengerpro.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.baselib.xmpp.db.SqliteManager;
import com.zhengyuan.baselib.xmpp.db.SqliteManager2;
import com.zhengyuan.easymessengerpro.INotificationBinder;
import com.zhengyuan.easymessengerpro.xmpp.XmppManager;

/**
 * 后台运行的Service
 * <p>
 * 循环监听连接状态，网络状态，并开启相应的连接、注册、登陆线程
 */
public class NotificationService extends Service {

    private static final String LOG_TAG = "NotificationService";

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate()...");

        Utils.printCurThread(LOG_TAG);
        XmppManager.getInstance().init();
        XmppManager.getInstance().start();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(LOG_TAG, "onStart()...");
    }

    public static boolean isLogout = false;

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()..." + isLogout);
        //关闭数据库连接
        SqliteManager.closeSqitedb();
        SqliteManager2.closeSqitedb();
        XmppManager.getInstance().stop();

        // exit可以关掉整个App进程, finish只能关掉某个activity
        // finish可以完整的调用onStop,onDestroy方法, exit不行

        if (!isLogout) {
            // 退出应用

            // 更新插件后, 必须杀死:GuardService进程, 重启才不会出现问题//2018-12-03已经解决，刷新即可使用插件
            // 否则下次启动的已安装插件信息会出错, 再次启动才恢复正常
            // 可能跟:GuardService的内部机制有关系, 没有及时更新插件信息
            ActivityManager mAm = (ActivityManager) EMProApplicationDelegate.applicationContext.
                    getSystemService(Context.ACTIVITY_SERVICE);
            mAm.killBackgroundProcesses("com.zhengyuan.easymessengerpro");

            System.exit(0);

        } //else // 注销应用
        //isLogout = false;
    }

    private IBinder binder = new INotificationBinder.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public String test(String s) throws RemoteException {
            return s + " 收到了";
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "onUnbind");
        return false;
    }

    public void onRebind(Intent intent) {
        Log.d(LOG_TAG, "onRebind");
    }
}
