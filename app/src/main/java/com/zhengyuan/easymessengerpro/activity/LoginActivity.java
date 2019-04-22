package com.zhengyuan.easymessengerpro.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.listener.NetworkCallbacks;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.baselib.utils.ValidateUtil;
import com.zhengyuan.baselib.utils.ViewUtil;
import com.zhengyuan.easymessengerpro.EMProApplication;
import com.zhengyuan.easymessengerpro.R;
import com.zhengyuan.easymessengerpro.network.DataObtainer;
import com.zhengyuan.easymessengerpro.service.NotificationService;
import com.zhengyuan.easymessengerpro.util.FileUtil;
import com.zhengyuan.easymessengerpro.util.LogUtil;
import com.zhengyuan.reslib.base.BaseActivity;
import com.zhengyuan.reslib.base.EventBusMessageEntity;

import java.io.File;
import java.io.IOException;

import static com.zhengyuan.baselib.constants.Constants.DOWNLOAD_DIRRECTORY;

//import com.zhengyuan.reslib.base.BaseActivity;

/**
 * 登录界面
 *
 * @author linliang
 */
public class LoginActivity extends BaseActivity implements OnClickListener {

    private EditText userIdET = null;
    private EditText passwordET = null;
    private CheckBox autoLoginCB;
    private Button loginBtn;
    private TextView versionTV, titleTV;
    private LinearLayout fingerPrintLL, originalLL;
    private ImageButton fingerPrintBtn;

    private boolean isServiceRunning = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_login);

        initView();

        checkPermission();//检测相关权限

        FileUtil.createDir(getApplicationContext(), Environment.getExternalStorageDirectory().toString() + File.separator + DOWNLOAD_DIRRECTORY);


        initFingerPrintView();
        Log.d(LOG_TAG, "fingerPrint " + EMProApplicationDelegate.isEnableFingerPrint +
                " " + EMProApplicationDelegate.isUseFingerPrint);
        if (EMProApplicationDelegate.isEnableFingerPrint &&
                EMProApplicationDelegate.isUseFingerPrint) {
            //EMProApplicationDelegate.isUseFingerPrint用于检测手机开启指纹识别
            //指纹验证登录
            verifyFingerPrint();
        }
        /**
         * 2019-2-2取消记住密码并且、自动登录的功能
         */
        /* else if (EMProApplicationDelegate.userInfo.isAutoLogin) {
            login();
        }*/

        Utils.printCurThread(LOG_TAG);

        //创建线程池
    }



    private void verifyFingerPrint() {
        final CancellationSignal cancellationSignal = new CancellationSignal();

        final AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("指纹登录");
        builder.setMessage("请验证已有指纹");

        ImageButton imageButton = new ImageButton(this);
        imageButton.setImageResource(R.drawable.ic_finger_print);
        imageButton.setBackgroundColor(getResources().getColor(R.color.transparent));
        builder.setView(imageButton);

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                cancellationSignal.cancel();
            }
        });
        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();

        //开启指纹识别
        EMProApplicationDelegate.fingerprintManager
                .authenticate(null, 0, cancellationSignal,
                        new FingerprintManagerCompat.AuthenticationCallback() {
                            @Override
                            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                                super.onAuthenticationError(errMsgId, errString);
                                Log.d(LOG_TAG, "onAuthenticationError");
                            }

                            @Override
                            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                                super.onAuthenticationHelp(helpMsgId, helpString);
                                Log.d(LOG_TAG, "onAuthenticationHelp");
                            }

                            @Override
                            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                                super.onAuthenticationSucceeded(result);
                                Log.d(LOG_TAG, "onAuthenticationSucceeded");
                                alertDialog.dismiss();
                                Utils.showToast("指纹识别成功");
                                login();
                            }

                            @Override
                            public void onAuthenticationFailed() {
                                super.onAuthenticationFailed();
                                Log.d(LOG_TAG, "onAuthenticationFailed");
                                Utils.showToast("指纹识别失败");
//                                alertDialog.dismiss();
                            }
                        }, null);
    }

    private String[] permissions = new String[]{

            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_SYNC_SETTINGS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
    };

    private void checkPermission() {

        // 检测Android6.0以上的权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CODE);
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                        == PackageManager.PERMISSION_GRANTED) {

                    EMProApplicationDelegate.getDeviceId();

                    EMProApplicationDelegate.sharedPrefHelper.saveString(Constants.DEVICE_ID,
                            EMProApplicationDelegate.deviceId);
                }
            }
        }
    }

    private final int REQUEST_PERMISSION_CODE = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE) {

            for (int i = 0; i < grantResults.length; i++) {

                Log.d(LOG_TAG, "grantResult " + i + " " +
                        (grantResults[i] == PackageManager.PERMISSION_GRANTED));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                        == PackageManager.PERMISSION_GRANTED) {

                    EMProApplicationDelegate.getDeviceId();

                    EMProApplicationDelegate.sharedPrefHelper.saveString(Constants.DEVICE_ID,
                            EMProApplicationDelegate.deviceId);
                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW)
                        != PackageManager.PERMISSION_GRANTED) {

                    Utils.showToast("请打开应用悬浮窗权限");
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + LoginActivity.this.getPackageName()));
                    startActivityForResult(intent, 11);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(LOG_TAG, "requestCode " + requestCode + " " + resultCode + " data: ");
        if (requestCode == 11) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW)
                    == PackageManager.PERMISSION_GRANTED) {
                Utils.showToast("权限设置成功");
            }
        }
    }

    private void initFingerPrintView() {

        originalLL = findViewById(R.id.original_login_ll);
        fingerPrintLL = findViewById(R.id.fingerPrint);
        fingerPrintBtn = findViewById(R.id.fingerPrintBtn);
        if (EMProApplicationDelegate.isEnableFingerPrint && EMProApplicationDelegate.isUseFingerPrint) {
            fingerPrintLL.setVisibility(View.VISIBLE);
            originalLL.setVisibility(View.GONE);
            titleTV.setText("欢迎回来 " + EMProApplicationDelegate.userInfo.getUserId().toUpperCase());
            fingerPrintBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    verifyFingerPrint();
                }
            });
            Log.d(LOG_TAG, "isShowFinger true");
        } else {
            Log.d(LOG_TAG, "isShowFinger false");
            fingerPrintLL.setVisibility(View.GONE);
            originalLL.setVisibility(View.VISIBLE);
            titleTV.setText("登录");
        }
    }

    private void initView() {

        titleTV = findViewById(R.id.title);
        versionTV = findViewById(R.id.version);
        versionTV.setText("version " + Utils.getVersionName() + "\n" +
                Utils.getStringFromRes(R.string.version_info));
        this.userIdET = findViewById(R.id.login_username_et);
        this.passwordET = findViewById(R.id.login_pwd_et);
        autoLoginCB = findViewById(R.id.auto_login_checkbox);
        autoLoginCB.setChecked(EMProApplicationDelegate.userInfo.isAutoLogin);

        loginBtn = findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(this);

        if (EMProApplicationDelegate.userInfo.isValideUserInfo()) {
            userIdET.setText(EMProApplicationDelegate.userInfo.getUserId());
            /*2019-2-2 取消登录记住密码的功能，为扫码枪专用*/
            //passwordET.setText(EMProApplicationDelegate.userInfo.getPassword());
        }
    }

    @Override
    public void onClick(View v) {
        if (isLoginInfoReady()) {
            switch (v.getId()) {
                case R.id.login_btn:
                    // 取得填入的用户和密码
                    final String userId = userIdET.getText().toString();
                    final String password = passwordET.getText().toString();

                    // 存储帐号密码
                    EMProApplicationDelegate.sharedPrefHelper.saveString(Constants.XMPP_USERNAME, userId);
                    EMProApplicationDelegate.sharedPrefHelper.saveString(Constants.XMPP_PASSWORD, password);
                    EMProApplicationDelegate.sharedPrefHelper.saveBool(Constants.SHARED_PREF_IS_AUTO_LOGIN, autoLoginCB.isChecked());

                    EMProApplicationDelegate.userInfo.setUserId(userId);
                    EMProApplicationDelegate.userInfo.setPassword(password);

                    login();
                    break;
            }
        }
    }

    private ProgressDialog progressDialog;

    /**
     * 调用xmpp登录
     */
    private void login() {
        NotificationService.isLogout = true;//设置为true,防止在前一次登录失败后，第二次登录闪退。但是尚不能解决如果一直连接但是有连接不上的情况，还是需要退出重进来
        if (!Utils.isNetWorkConnected()) {
            Utils.showToast("请检查网络连接");
            return;
        }

        progressDialog = ViewUtil.createNormalCircleProgressDialog(
                this,
                "登录中...", "取消登录",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        progressDialog.show();

        if (isServiceRunning) {

            EMProApplication.stopNotificationService();

            //将没关闭的两个数据库连接关闭 2016-12-30
//            SqliteManager.closeSqitedb();
//            SqliteManager2.closeSqitedb();
//            XmppManager.getInstance().disconnectNetWork();
        }
        isServiceRunning = true;
        EMProApplication.startNotificationService();


//        XmppManager.getInstance().init();
//        XmppManager.getInstance().start();
    }

    @Override
    protected String getFiltTag() {
        return LoginActivity.class.getName();
    }

    @Override
    protected boolean handleFailedMsg(EventBusMessageEntity eventBusMessageEntity) {
        if (!eventBusMessageEntity.isSuccess) {

            Utils.showToast(eventBusMessageEntity.message);
            if (progressDialog != null)
                progressDialog.dismiss();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onHandlerEvent(EventBusMessageEntity event) {

        Log.d(LOG_TAG, "onHandlerEvent " + event.eventType);
        switch (event.eventType) {
            case LOGIN:
                progressDialog.dismiss();
                Utils.showToast("登录成功");
                NotificationService.isLogout = false;//取消登录时候设置的true，防止卸载插件的时候重新刷新不成功

                DataObtainer.INSTANCE.getChangePasswordDate(EMProApplicationDelegate.userInfo.getUserId(), new NetworkCallbacks.SimpleDataCallback() {
                    @Override
                    public void onFinish(boolean b, String s, Object o) {

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                        String res = (String) o;
                        intent.putExtra("changePasswordDate", res);

                        startActivity(intent);
                        finish();
                    }
                });

                break;
        }
    }

    // 进行登陆数据非空验证
    private boolean isLoginInfoReady() {
        boolean checked;
        checked = (!ValidateUtil.isEmpty(userIdET, "账号") && !ValidateUtil
                .isEmpty(passwordET, "密码"));
        return checked;
    }

    @Override
    protected void onDestroy() {

        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "onPause");
        super.onPause();
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    /**
     * 拉活相关代码
     */
    private static final String className = LoginActivity.class.getName();
    private String path;
    String externalPath, pathA1 = "EMfilelockA1", pathA2 = "EMfilelockA2",
            pathB1 = "EMfilelockB1", pathB2 = "EMfilelockB2";
    File fileA1, fileA2, fileB1, fileB2;

    private void initFile() {

        externalPath = Environment.getExternalStorageDirectory() + "/";
        pathA1 = externalPath + pathA1;
        pathA2 = externalPath + pathA2;
        pathB1 = externalPath + pathB1;
        pathB2 = externalPath + pathB2;

        fileA1 = new File(pathA1);
        fileA2 = new File(pathA2);

        fileB1 = new File(pathB1);
        fileB2 = new File(pathB2);

        checkPaths();
    }

    private void checkPaths() {

        checkParentPath(fileA1);
        checkParentPath(fileA2);
        checkParentPath(fileB1);
        checkParentPath(fileB2);
    }

    MyThread thread = new MyThread();

    private class MyThread extends Thread {

        public void run() {

            lockself(pathA1, pathB1, pathA2, pathB2);
            Log.d(LOG_TAG, "********************");
//			}
        }
    }

    LoopThread loopThread = new LoopThread();

    private class LoopThread extends Thread {

        public void run() {

            int waitTime = 0;
            while (true) {
//	
                if (!fileA2.exists()) {
                    Log.d(LOG_TAG, "one file a2 delete");
                    break;
                }

                Log.d(LOG_TAG, "wait a2");
                waitTime++;
                if (waitTime > 10) {
                    startActivity(daemonIntent);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (fileB2.exists()) {
                fileB2.delete();
                Log.d(LOG_TAG, "one fileB2 deleted");
            } else
                Log.d(LOG_TAG, "error fileB2 no exist");
//	            
            watch(pathA1, pathB1, pathA2, pathB2);
            Log.d(LOG_TAG, "********************");
//			}
        }
    }

    private final String LOG_TAG = "LoginActivity";

    private void initSdcardFilePath(String pathCreate) {
        String storageState = Environment.getExternalStorageState();
        // 获取SDCard状态,如果SDCard插入了手机且为非写保护状态
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory() + File.separator + pathCreate;
            Log.i(LOG_TAG, className + " start lock ...path " + path);
        } else {
            // 提示用户SDCard不存在或者为写保护状态
            Log.i(LOG_TAG, className + " SDCard不存在或者为写保护状态");
        }
    }

    private static final int START_MY_APP = 0;
    private final String packageName = "com.zhengyuan.emdeamon";
    private final String targetClassName = "MainActivity";


    Intent daemonIntent;

    private void initStartApp() {

        PackageManager packageManager = getPackageManager();
        daemonIntent = new Intent();
        daemonIntent.setData(Uri.parse("com.zhengyuan.emdeamon://EMDeamon"));
//    	daemonIntent = packageManager.getLaunchIntentForPackage(packageName);
        try {
            startActivityForResult(daemonIntent, RESULT_OK);
        } catch (Exception e) {
            Toast.makeText(this, "没有该子APP，请下载安装", Toast.LENGTH_SHORT).show();
        }
    }

    private void startDaemonApp() {

        Log.i(LOG_TAG, "native one startDaemonApp");

        startActivity(daemonIntent);

        loopThread = new LoopThread();
        loopThread.start();
    }

    private void onDaemonDead() {

        Log.i(LOG_TAG, "native one onDaemonDead");

        checkPaths();
        thread = new MyThread();
        thread.start();
    }

    public void checkParentPath(File file) {

        if (!file.exists()) {

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void deletePath(File file) {

        if (file.exists()) {
            file.delete();
        }
    }

    public native void lockself(String indicatorSelfPath, String indicatorDaemonPath, String observerSelfPath, String observerDaemonPath);

    public native void watch(String indicatorSelfPath, String indicatorDaemonPath, String observerSelfPath, String observerDaemonPath);

}