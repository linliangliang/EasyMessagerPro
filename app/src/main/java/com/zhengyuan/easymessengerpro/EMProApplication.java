package com.zhengyuan.easymessengerpro;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.RePluginApplication;
import com.qihoo360.replugin.RePluginCallbacks;
import com.qihoo360.replugin.RePluginConfig;
import com.qihoo360.replugin.RePluginEventCallbacks;
import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.utils.CrashHandler;
import com.zhengyuan.baselib.utils.SharedPrefHelper;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.easymessengerpro.service.LocationServiceHelper;
import com.zhengyuan.easymessengerpro.service.NotificationService;

/**
 * Created by zy on 2017/10/25.
 */

public class EMProApplication extends RePluginApplication {

    private final static String LOG_TAG = "EMProApplication";

    public LocationServiceHelper locationServiceHelper;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

// FIXME 允许接收rpRunPlugin等Gradle Task，发布时请务必关掉，以免出现问题
        RePlugin.enableDebugger(base, BuildConfig.DEBUG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationIntent = new Intent(this, NotificationService.class);
        EMProApplicationDelegate.applicationContext = this;

        EMProApplicationDelegate.sharedPrefHelper = new SharedPrefHelper(getApplicationContext());
        /***
         * 初始化定位sdk，建议在Application中创建
         */
        locationServiceHelper = new LocationServiceHelper(getApplicationContext());
        SDKInitializer.initialize(getApplicationContext());

        EMProApplicationDelegate.isUseFingerPrint =
                EMProApplicationDelegate.sharedPrefHelper.getBool(Constants.SHARED_PREF_IS_FINGER_PRINT);
        // 取出本地保存的帐号、密码
        String userName = EMProApplicationDelegate.sharedPrefHelper.getString(Constants.XMPP_USERNAME);
        String password = EMProApplicationDelegate.sharedPrefHelper.getString(Constants.XMPP_PASSWORD);
        EMProApplicationDelegate.userInfo.isAutoLogin =
                EMProApplicationDelegate.sharedPrefHelper.getBool(Constants.SHARED_PREF_IS_AUTO_LOGIN);

        if (userName != null && password != null) {
            EMProApplicationDelegate.userInfo.setUserId(userName);
            EMProApplicationDelegate.userInfo.setPassword(password);
        } else {
            if (Utils.isApkInDebug()) {

                userName = "";
                password = "";

                EMProApplicationDelegate.userInfo.setUserId(userName);
                EMProApplicationDelegate.userInfo.setPassword(password);
            }
        }

        // 处理未捕获的异常，异常日志在EasyMessager/Log文件夹
        com.zhengyuan.baselib.utils.Utils.checkAppDirectory();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        crashHandler.setCrashHandle(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {

                Log.d(LOG_TAG, "Crash " + e.getMessage() + "\n" + e.toString());
                EMProApplication.stopNotificationService();
            }
        });

        initDir();

        Utils.printCurThread(LOG_TAG);
    }

    private void initDir() {

        Utils.checkAppDirectory();
    }

    private static Intent notificationIntent;

    private static ServiceConnection notificationServiceConnection;

    /**
     * 用户登录
     */
    public static void startNotificationService() {

//        EMProApplicationDelegate.applicationContext.startService(notificationIntent);
        notificationServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                Log.d(LOG_TAG, "onServiceConnected");

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

                Log.d(LOG_TAG, "onServiceDisconnected");
            }
        };
        EMProApplicationDelegate.applicationContext.bindService(notificationIntent,
                notificationServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 关闭应用时调用此方法关闭xmpp连接
     */
    public static void stopNotificationService() {

        Log.d(LOG_TAG, "stopNotificationService");
        EMProApplicationDelegate.applicationContext.unbindService(notificationServiceConnection);
    }

    /**
     * 注销回到登录页面调用此方法
     */
    public static void logout() {
        NotificationService.isLogout = true;
        EMProApplication.stopNotificationService();
    }
    // ----------
    // 自定义行为
    // ----------

    /**
     * RePlugin允许提供各种“自定义”的行为，让您“无需修改源代码”，即可实现相应的功能
     */
    @Override
    protected RePluginConfig createConfig() {
        RePluginConfig c = new RePluginConfig();

        // 允许“插件使用宿主类”。默认为“关闭”
        c.setUseHostClassIfNotFound(true);

        // FIXME RePlugin默认会对安装的外置插件进行签名校验，这里先关掉，避免调试时出现签名错误
        c.setVerifySign(!BuildConfig.DEBUG);

//        c.setCallbacks(new HostCallbacks(this));
        // 针对“安装失败”等情况来做进一步的事件处理
        c.setEventCallbacks(new HostEventCallbacks(this));

        // 若宿主为Release，则此处应加上您认为"合法"的插件的签名，例如，可以写上"宿主"自己的。
        // MD5, 不要冒号
        // 第一条为宿主签名, 第二条为debug签名的md5, 因为插件目前都是debug版本
        // 第三条为重装系统后debug签名的md5，原来的签名暂时不要删除，以兼容原有插件
        // 如果后期改为release版本的插件包, 添加对应签名即可
        if (!BuildConfig.DEBUG) {
            RePlugin.addCertSignature("7A9060E73A3BBCE1A967827F7D2D679C");
            RePlugin.addCertSignature("3E83C1073BEFC9A1CDE3C9AE5E91D714");
            RePlugin.addCertSignature("72717D273882ACEB5C36872C3F7FAC96");
        }
        return c;
    }

    @Override
    protected RePluginCallbacks createCallbacks() {
        return new HostCallbacks(this);
    }

    /**
     * 宿主针对RePlugin的自定义行为
     */
    private class HostCallbacks extends RePluginCallbacks {

        private static final String TAG = "HostCallbacks";

        private HostCallbacks(Context context) {
            super(context);
        }

        @Override
        public boolean onPluginNotExistsForActivity(Context context, String plugin, Intent intent, int process) {
            // FIXME 当插件"没有安装"时触发此逻辑，可打开您的"下载对话框"并开始下载。
            // FIXME 其中"intent"需传递到"对话框"内，这样可在下载完成后，打开这个插件的Activity
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onPluginNotExistsForActivity: Start download... p=" + plugin + "; i=" + intent);
            }
//            Utils.showToast("插件未安装,请到'插件管理'安装插件");
            return super.onPluginNotExistsForActivity(context, plugin, intent, process);
        }
    }

    private class HostEventCallbacks extends RePluginEventCallbacks {

        private static final String TAG = "HostEventCallbacks";

        public HostEventCallbacks(Context context) {
            super(context);
        }

        @Override
        public void onInstallPluginFailed(String path, InstallResult code) {
            // FIXME 当插件安装失败时触发此逻辑。您可以在此处做“打点统计”，也可以针对安装失败情况做“特殊处理”
            // 大部分可以通过RePlugin.install的返回值来判断是否成功
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onInstallPluginFailed: Failed! path=" + path + "; r=" + code);
            }

            super.onInstallPluginFailed(path, code);
        }

        @Override
        public void onStartActivityCompleted(String plugin, String activity, boolean result) {
            // FIXME 当打开Activity成功时触发此逻辑，可在这里做一些APM、打点统计等相关工作
            super.onStartActivityCompleted(plugin, activity, result);

            Log.d(TAG, plugin + " 启动情况 " + result);
//            Utils.showToast("插件未安装,请到'插件管理'安装插件");
        }
    }
}
