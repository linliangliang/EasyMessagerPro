package com.zhengyuan.easymessengerpro.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;
import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.constants.StaticVariable;
import com.zhengyuan.baselib.http.OkHttpUtil;
import com.zhengyuan.baselib.listener.NetworkCallbacks;
import com.zhengyuan.baselib.network.CommonDataObtainer;
import com.zhengyuan.baselib.network.UserInfoObtainer;
import com.zhengyuan.baselib.utils.ToolClass;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.baselib.utils.ViewUtil;
import com.zhengyuan.baselib.utils.xml.Element;
import com.zhengyuan.baselib.xmpp.BaseXmppManager;
import com.zhengyuan.baselib.xmpp.db.MessageDAO;
import com.zhengyuan.easymessengerpro.EMProApplication;
import com.zhengyuan.easymessengerpro.R;
import com.zhengyuan.easymessengerpro.RePluginHelper;
import com.zhengyuan.easymessengerpro.activity.fragment.LeftFragment;
import com.zhengyuan.easymessengerpro.activity.fragment.RightFragment;
import com.zhengyuan.easymessengerpro.activity.user.ChangePasswordActivity;
import com.zhengyuan.easymessengerpro.entity.updataVersionEntity;
import com.zhengyuan.easymessengerpro.network.DataObtainer;
import com.zhengyuan.easymessengerpro.network.MainPageChatter;
import com.zhengyuan.easymessengerpro.service.LocationService;
import com.zhengyuan.easymessengerpro.util.CommonUtils;
import com.zhengyuan.easymessengerpro.util.FileUtil;
import com.zhengyuan.easymessengerpro.util.GlideCircleTransform;
import com.zhengyuan.easymessengerpro.util.JsonUtil;
import com.zhengyuan.easymessengerpro.util.LogUtil;
import com.zhengyuan.easymessengerpro.util.UpdateAppUtil;
import com.zhengyuan.easymessengerpro.widget.LittleDotsView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.zhengyuan.baselib.constants.Constants.DOWNLOAD_PATH;

/**
 * 主页面,登陆后进行使用导航,开启系统消息接收显示进程
 *
 * @author shimiso
 */
public class MainActivity extends AppCompatActivity {
    /**
     * 强制修改密码的请求值
     */
    private static final  int FORECE_CHANGE_PASSWORD = 15;
    /**
     * 获取服务器上最新版本号的请求值
     */
    private static final  int GET_SERVICE_RELEASE_VERSION = 16;
    private static final int INSTALL_APP = 17;
    private static final String TAG = "MainActivity";
    private ImageView userImage;
    private ImageView mLeftUserImage;
    private LittleDotsView littleDotsView;
    private TextView userIdTV;
    private ImageButton pluginManageBtn;

    String loginId = null;

    private final String LOG_TAG = "MainActivity";
    Intent locationService;


    public RightFragment rf;//右侧主界面的fragment
    public LeftFragment lf;//左侧用户信息设置的fragment

    Intent intent = null;

    private boolean changePasswordSuccess = false;//修改密码成功返回

    private static MyHandle myHandle;//处理网络请求返回后的事件
    public static updataVersionEntity info = null;

    public void onCreate(Bundle savedInstanceState) {
        intent = getIntent();
        initStatusBar();//设置状态栏
        super.onCreate(savedInstanceState);

        Constants.contexts.add(this);
        setContentView(R.layout.activity_main);

        StaticVariable.handler = this.handler;
        StaticVariable.inMainActivity = true;
        loginId = EMProApplicationDelegate.userInfo.getUserId();
        init();
        initData();
        //启动位置服务
        locationService = new Intent(MainActivity.this, LocationService.class);
        startService(locationService);

        //注册添加好友的广播接收器
        ToolClass.registerAddFriendBroadcastReceiver(this);
        Utils.printCurThread(LOG_TAG);
    }

    private void init() {
        //取消严格模式，解决7.0以上拍照的问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        FileUtil.createDir(getApplicationContext(), DOWNLOAD_PATH);
        rf = (RightFragment) MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.fragment_right);//获取RightFragment对象
        lf = (LeftFragment) MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.fragment_left);//获取LeftFragment对象

        myHandle = new MyHandle(this);
        //检查新版本
        updataApp();
    }

    /**
     * 沉浸式
     * 设置状态栏的代码建议写在一个baseactivity中，让要使用该工能的类继承即可。目前尚未这么做
     */
    private void initStatusBar() {
        //4.4以上状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        // 5.0以上系统状态栏透明，并为状态栏设置背景色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.status_bar_bg));//设置状态栏为指定颜色
        }
    }

    private ProgressDialog pluginImproveDialog;

    /**
     * 初始化插件信息
     */
    private void initData() {

        RePluginHelper.INSTANCE.initInstalledPluginInfos();//初始化已经安装的插件列表

        //从服务器获取全部插件信息
        MainPageChatter.INSTANCE.getPluginList(new NetworkCallbacks.SimpleDataCallback() {
            @Override
            public void onFinish(boolean isSuccess, String msg, Object data) {

                String datas = ((Element) data).getProperty("result");
                Log.d(LOG_TAG, "getPluginList " + datas);

                RePluginHelper.INSTANCE.initNetworkPluginInfos(datas);
                handlePlugins();
                getUserInfo();
            }
        }, EMProApplicationDelegate.userInfo.getUserId());

        CommonDataObtainer.INSTANCE.getAllUser(new NetworkCallbacks.SimpleDataCallback() {
            @Override
            public void onFinish(boolean isSuccess, String msg, Object data) {

                if (data != null) {
                    String[] users = ((String) data).split(";");

                    String[] temp;
                    for (String user : users) {

                        temp = user.split(",");
                        EMProApplicationDelegate.allUserInfo.put(temp[0], temp[1]);
                    }
                }
            }
        });

        //林亮：还没看明白这个是干嘛的
        MainPageChatter.INSTANCE.getPermission(new NetworkCallbacks.SimpleDataCallback() {
            @Override
            public void onFinish(boolean isSuccess, String msg, Object data) {

                Element element = (Element) data;
                String result = element.getProperty("permissionResult");
                String childTable = element.getProperty("childTableResult");
                BaseXmppManager.ChildTable = childTable;
                BaseXmppManager.teamFunction = result.split("-")[0];
                BaseXmppManager.ScanFunction = result.split("-")[1];
                System.out.println(BaseXmppManager.teamFunction + "来了" + BaseXmppManager.ScanFunction);
                System.out.println("子菜单：" + BaseXmppManager.ChildTable);
            }
        });
    }

    private void getUserInfo() {

        UserInfoObtainer.INSTANCE.getUserInfo(
                EMProApplicationDelegate.userInfo.getUserId(),
                new NetworkCallbacks.SimpleDataCallback() {
                    @Override
                    public void onFinish(boolean isSuccess, String msg, Object data) {

                        String[] infos = data.toString().split(";")[0].split(",");

                        EMProApplicationDelegate.userInfo.nickName = infos[1].split("=")[1];
                        EMProApplicationDelegate.userInfo.department = infos[2].split("=")[1];
                        EMProApplicationDelegate.userInfo.position = infos[3].split("=")[1];
                        EMProApplicationDelegate.userInfo.mobile = infos[4].split("=")[1];

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //因为本地没有保存人名，电话号码等信息，所以从服务器获取后刷新才能显示
                                rf.refreshToolbarUserName();
                                lf.initData();//获取数据后，更新lf中list要显示的数据，并通知更新
                                lf.leftFragmentAdapter.notifyDataSetChanged();
                            }
                        });

                        Log.d(LOG_TAG, "UserInfoObtainer getUserInfo " + data.toString() +
                                " " + ((Element) data).getBody());
                    }
                }
        );
    }

    private void handlePlugins() {

        final int totalPluginNum = RePluginHelper.INSTANCE.installedPurePluginInfos.size();

        Log.d(LOG_TAG, "插件有: " + totalPluginNum);
        // 是否需要加载插件
        boolean isNeedImprove = false;
        for (int i = 0; i < totalPluginNum; i++) {

            String name = RePluginHelper.INSTANCE.installedPurePluginInfos.get(i).getName();
            if (!RePlugin.isPluginDexExtracted(name)) {
                isNeedImprove = true;
                break;
            }
        }

        // 调试时自动跳过优化插件阶段
        if (Utils.isApkInDebug()) {
            isNeedImprove = false;
        }
        // 不需要优化插件
        if (!isNeedImprove) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rf.initView();
                }
            });
            return;
        }

        // 存在需要优化(提前加载)的插件
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                pluginImproveDialog = ViewUtil.createNormalCircleProgressDialog(
                        MainActivity.this,
                        "首次启动, 插件启动优化中...\n已优化 0/" + totalPluginNum);
                pluginImproveDialog.show();
            }
        });
        for (int i = 0; i < totalPluginNum; i++) {

            String name = RePluginHelper.INSTANCE.installedPurePluginInfos.get(i).getName();
//                            Log.d(LOG_TAG,
//                                    "name: " + name
//                                            + "\nisPluginUsed " + RePlugin.isPluginUsed(name)
//                                            + "\nisPluginInstalled " + RePlugin.isPluginInstalled(name)
//                                            + "\nisPluginDexExtracted " + RePlugin.isPluginDexExtracted(name)
//                                            + "\nisPluginRunning " + RePlugin.isPluginRunning(name)
//                                            + "\ngetType " + RePlugin.getPluginInfo(name).getType());
            if (!RePlugin.isPluginDexExtracted(name))
                RePlugin.preload(name);

            final int finalI = i;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pluginImproveDialog.setMessage("首次启动, 插件启动优化中...\n已优化 " +
                            (finalI + 1) + "/" + totalPluginNum);
                }
            });
        }
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pluginImproveDialog.dismiss();
                Utils.showToast("插件优化成功");

                rf.initView();
            }
        });
    }

    public static boolean isLogout = false;

    private void initUserIcon() {
        String userId = EMProApplicationDelegate.userInfo.getUserId().toUpperCase();
        MessageDAO messageDAO = new MessageDAO();
        List<String> theavatars = messageDAO.qureyTheAvatarsByUserName(userId);

        if (userImage == null)
            userImage = findViewById(R.id.user_image);
        if (mLeftUserImage == null) {
            mLeftUserImage = findViewById(R.id.user_avatar);
        }

        Log.d(LOG_TAG, "avatar " + theavatars.size() + " " + userId);
        for (int i = 0; i < theavatars.size(); i++) {
            Log.d(LOG_TAG, "avatar " + theavatars.get(i));
        }
        if (theavatars.size() == 0 || theavatars.get(0).equals("null")) {
            //userImage.setImageResource(R.drawable.user_avater_default);
            Glide.with(this)
                    .load(R.drawable.user_avater_default)
                    .transform(new GlideCircleTransform(this))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.user_avater_default)
                    .into(userImage);
            Glide.with(this)
                    .load(R.drawable.user_avater_default)
                    .transform(new GlideCircleTransform(this))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.user_avater_default)
                    .into(mLeftUserImage);
        } else {
            String taresult = theavatars.get(0);
            Glide.with(this)
                    .load(DOWNLOAD_PATH + taresult)
                    .transform(new GlideCircleTransform(this))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.user_avater_default)
                    .into(userImage);
            Glide.with(this)
                    .load(DOWNLOAD_PATH + taresult)
                    .transform(new GlideCircleTransform(this))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.user_avater_default)
                    .into(mLeftUserImage);
        }
    }

    @Override
    protected void onResume() {

        initUserIcon();
        refreshLayout();
        StaticVariable.inMainActivity = true;


        String changePasswordDate = intent.getStringExtra("changePasswordDate");
        //返回的是sql.Date类型的数据：changePasswordDate
        if (null != changePasswordDate && !"".equals(changePasswordDate) && changePasswordSuccess == false) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date oldDate = null;
            Date newDate = new Date();
            try {
                oldDate = java.sql.Date.valueOf(changePasswordDate);
                //超过30天强制修改密码
                if ((CommonUtils.dataDiffer(oldDate, newDate)) > 30) {
                    LogUtil.i("test==", "forceChangePassword11111111111");
                    forceChangePassword();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        super.onResume();
    }

    @Override
    protected void onRestart() {
        StaticVariable.inMainActivity = true;
        super.onRestart();
    }

    @Override
    protected void onPause() {

        StaticVariable.inMainActivity = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        StaticVariable.inMainActivity = false;
        super.onStop();
        Log.d(LOG_TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        if (!isLogout) {
            //关闭Xmpp对应service
            EMProApplication.stopNotificationService();
        }

        stopService(locationService);

        StaticVariable.inMainActivity = false;
        Constants.contexts.remove(Constants.contexts.size() - 1);
        ToolClass.unregisterAddFriendBroadcastReceiver(this);
        super.onDestroy();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(MainActivity.this, "" + msg.obj, Toast.LENGTH_LONG).show();
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "requestCode " + requestCode + "  resultCode:" + resultCode + " data: ");
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0://监听班组/质检员扫码
                    Bundle bundle = data.getExtras();
                    final String scanResult = bundle.getString("result");
                    //查出来结果
                    Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("查询结果确定");
                    builder.setMessage("确定查询作业单号" + scanResult + "吗?");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            Intent intent = new Intent(MainActivity.this, BatchQualityTestActivity.class);
                            intent.putExtra("scanResult", scanResult);
                            startActivity(intent);
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.create().show();
                    break;
                case 1://条码扫码
                    Bundle bundle1 = data.getExtras();
                    String scanReslult1 = bundle1.getString("result");
                    showSendScanningResultDialog(scanReslult1);
                    break;
                case 2://二维码扫描
                    Bundle bundle2 = data.getExtras();
                    String scanReslult2 = bundle2.getString("result");
                    showSendScanningResultDialog(scanReslult2);
                    break;
                case FORECE_CHANGE_PASSWORD://强制修改密码
                    String changeRes = data.getExtras().getString("result");
                    LogUtil.i("test==", "FORECE_CHANGE_PASSWORD=" + changeRes);
                    changePasswordSuccess = true;
                    if (!"true".equals(changeRes)) {//没有修改密码就返回，
                        LogUtil.i("test==", "forceChangePassword2222222222");
                        forceChangePassword();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void showSendScanningResultDialog(final String scanResult) {
        Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("发送条码");
        builder.setMessage("是否发送条码" + scanResult + "?");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                MainPageChatter.INSTANCE.sendScanningResult(scanResult, new NetworkCallbacks.SimpleCallback() {
                    @Override
                    public void onFinish(boolean isSuccess, String msg) {
                        android.os.Message message = handler.obtainMessage();
                        message.what = 1;
                        if (isSuccess) {
                            message.obj = "发送成功";
                        } else {
                            message.obj = "发送失败";
                        }
                        message.sendToTarget();
                    }
                });
                // ChatUtils.INSTANCE.sendMessage(Constants.CHAT_TO_USER, element.toString());
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    private long firstTime = 0;

    /**
     * 按两次返回键退出程序
     **/
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 2000) { //如果两次按键时间间隔大于2秒，则不退出
                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;//更新firstTime
                    return true;
                } else {
                    finish();
//                    System.exit(0);
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 刷新fregment布局
     */
    public void refreshLayout() {
        if (rf != null) {
            rf.refreshLayout();
        }
    }

    /**
     * 强制修改密码
     */
    private void forceChangePassword() {
        Intent intentWithFlag = new Intent(MainActivity.this, ChangePasswordActivity.class);
        intentWithFlag.putExtra("forceChange", "true");//强制修改
        startActivityForResult(intentWithFlag, FORECE_CHANGE_PASSWORD);
    }

    /**
     * 易信检查更新
     */
    private void updataApp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataObtainer.INSTANCE.getServiceReleaseAppVersion(new NetworkCallbacks.SimpleDataCallback() {
                    @Override
                    public void onFinish(boolean b, String s, Object o) {

                        String data = JsonUtil.Quot2DoubleQuotationMarks((String) o);//转换&quot;符号
                        LogUtil.i(TAG, "data=" + data);
                        Gson gson = new Gson();
                        HashMap<String, String> serviceVersionMap = gson.fromJson(data, HashMap.class);
                        info = new updataVersionEntity();
                        //UEM_UpdateInfo
                        info.setUpgradeinfo(serviceVersionMap.get("UEM_UpdateInfo"));
                        //UEM_UpdateDate
                        //UEM_LastFocus
                        info.setLastForce(serviceVersionMap.get("UEM_LastFocus"));
                        //UEM_ServerFlag
                        info.setServerFlag(serviceVersionMap.get("UEM_ServerFlag"));
                        //UEM_ServerVersion
                        info.setServerVersion(serviceVersionMap.get("UEM_ServerVersion"));
                        //UEM_UpdateUrl
                        info.setUpdateurl(serviceVersionMap.get("UEM_UpdateUrl"));
                        //UEM_AppName
                        info.setAppname(serviceVersionMap.get("UEM_AppName"));
                        //UEM_AppName
                        info.setAppname(serviceVersionMap.get("UEM_AppType"));
                        LogUtil.i(TAG, "info.getServerVersion(o==" + info.getServerVersion());
                        Message message = new Message();
                        message.what = GET_SERVICE_RELEASE_VERSION;
                        myHandle.handleMessage(message);


                    }
                });
            }
        }).start();
    }


    /**
     * 静态内部类+弱引用
     */
    public static class MyHandle extends Handler {
        private WeakReference<MainActivity> weakReference;

        public MyHandle(MainActivity activity) {
            weakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (weakReference != null) {
                final MainActivity mainActivity = weakReference.get();
                switch (msg.what) {
                    case GET_SERVICE_RELEASE_VERSION://弹窗提示用户下载新的易信
                        if (info != null && info.getServerVersion() != null) {

                            LogUtil.i(TAG, "info.getServerVersion())" + info.getServerVersion());
                            LogUtil.i(TAG, "UpdateAppUtil.getAPPLocalVersion(mainActivity)" + UpdateAppUtil.getAPPLocalVersion(mainActivity));

                            if (Integer.valueOf(info.getServerVersion()) > UpdateAppUtil.getAPPLocalVersion(mainActivity)) {
                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SweetAlertDialog pDialog = new SweetAlertDialog(mainActivity, SweetAlertDialog.WARNING_TYPE);
                                        pDialog.setTitleText("易信增加新的功能，请下载更新");
                                        pDialog.setContentText(info.getUpgradeinfo());
                                        pDialog.setConfirmText("更新");
                                        pDialog.setCancelText("退出");
                                        pDialog.showCancelButton(true);
                                        pDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                sweetAlertDialog.cancel();
                                                if ("1".equals(info.getLastForce())) {//自动推送给用户，强制更新
                                                    MainActivity.isLogout = true;
                                                    EMProApplication.logout();
                                                    EMProApplicationDelegate.userInfo.isAutoLogin = false;
                                                    EMProApplicationDelegate.sharedPrefHelper.saveBool(Constants.SHARED_PREF_IS_AUTO_LOGIN, false);

                                                    Intent intent = new Intent(mainActivity, LoginActivity.class);
                                                    mainActivity.startActivity(intent);
                                                    mainActivity.finish();
                                                }
                                            }
                                        });
                                        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                sweetAlertDialog.cancel();
                                                //下载更新
                                                LogUtil.i(TAG, "---->开始下载...");
                                                //判断有无网络，是移动还是wifi

                                                //下载
                                                mainActivity.downloadNewApp(info.getUpdateurl(), info.getAppname() + ".apk", info.getAppname());
                                            }
                                        });
                                        pDialog.show();

                                    }
                                });
                            }
                        }
                        break;
                    case INSTALL_APP:
                        //安装程序
                        String newAppName = (String) msg.obj;
                        if (newAppName != null) {
                            mainActivity.startInstall(mainActivity, newAppName);
                        }
                        break;
                }
            }
        }
    }

    /**
     * 使用android自己提供的DownloadManager下载最新版本
     *
     * @param url
     * @param apkName
     * @param appName
     */
    public void downloadNewApp(String url, final String apkName, String appName) {

        LogUtil.i(TAG, "downloadNewApp:开始下载最新app" + url);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);


        progressDialog.setTitle(appName + " 下载中...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        String apkSavePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + apkName;
        LogUtil.i(TAG, "downloadNewApp:apkSavePath" + apkSavePath);
        OkHttpUtil.INSTANCE.download(url, apkSavePath, new OkHttpUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();//下载完成
                        LogUtil.i(TAG, "完成下载");
                        //安装程序
                        Message message = new Message();
                        message.what = INSTALL_APP;
                        message.obj = apkName;
                        myHandle.sendMessage(message);
                    }
                });
            }

            @Override
            public void onDownloading(final int progress) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setProgress(progress);
                    }
                });
            }

            @Override
            public void onDownloadFailed() {
                progressDialog.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast("下载失败");
                    }
                });
            }
        });
    }

    /**
     * 安装应用
     */
    public void startInstall(Context context, String newAppName) {
        LogUtil.i(TAG, "startInstall begin :" + newAppName);
        install(context, newAppName);
    }


    /**
     * android7.0之后的更新
     * 通过隐式意图调用系统安装程序安装APK
     *
     * @param context
     * @param file
     */
    /**
     * 通过隐式意图调用系统安装程序安装APK
     */
    public static void install(Context context, String appName) {

        LogUtil.i(TAG, "install APK path:Environment.DIRECTORY_DOWNLOADS" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), appName);
        LogUtil.i(TAG, "install APK path:Environment.file" + file.getAbsolutePath());
        if (file.exists()) {
            LogUtil.i(TAG, "install APK path:Environment.file1" + file.exists());
        } else {
            LogUtil.i(TAG, "install APK path:Environment.file2" + file.exists());
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 由于没有在Activity环境下启动Activity,设置下面的标签
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 24) { //判读版本是否在7.0以上
            Uri apkUri = FileProvider.getUriForFile(context, "com.zhengyuan.easymessengerpro.activity.fileprovider", file);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }

}