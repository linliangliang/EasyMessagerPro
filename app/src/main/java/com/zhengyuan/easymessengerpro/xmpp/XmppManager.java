package com.zhengyuan.easymessengerpro.xmpp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.qihoo360.replugin.RePlugin;
import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.constants.StaticVariable;
import com.zhengyuan.baselib.entities.biz.GroupPass;
import com.zhengyuan.baselib.utils.TimeRenderUtil;
import com.zhengyuan.baselib.utils.ToolClass;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.baselib.utils.xml.Element;
import com.zhengyuan.baselib.utils.xml.XmlParser;
import com.zhengyuan.baselib.xmpp.BaseXmppManager;
import com.zhengyuan.baselib.xmpp.ChatUtils;
import com.zhengyuan.baselib.xmpp.db.MessageDAO;
import com.zhengyuan.baselib.xmpp.util.XmppReliable;
import com.zhengyuan.easymessengerpro.activity.BatchQualityTestActivity;
import com.zhengyuan.easymessengerpro.activity.BatchWorkPlanSubmitActivity;
import com.zhengyuan.easymessengerpro.activity.LoginActivity;
import com.zhengyuan.easymessengerpro.util.CommonUtils;
import com.zhengyuan.reslib.base.EventBusMessageEntity;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import java.lang.reflect.Field;

/**
 * 连接管理类，多线程管理登陆，连接
 *
 * @author 徐兵
 */
public class XmppManager extends BaseXmppManager {

    private static final String LOG_TAG = "XmppManager";

    private ConnectionListener persistentConnectionListener;
    private ReconnectionThread reconnection;
    // 重连是否启动
    public static boolean reconnectionFlag = false;

    // 单例获取XmppManager
    public static XmppManager getInstance() {
        return XmppManagerHolder.INSTANCE;
    }

    private static class XmppManagerHolder {
        private static final XmppManager INSTANCE = new XmppManager();
    }

    public XmppManager() {

    }

    private boolean isInit = false;

    /**
     * 重连的初始化
     */
    public void init() {

        super.init();

        //连接监听器，对于连接失败，被挤下线，网络断开情况的监听
        persistentConnectionListener = new PersistentConnectionListener(this);

        reconnection = new ReconnectionThread(this);

        Utils.printCurThread(LOG_TAG);

        isInit = true;
    }

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    Utils.showToast(msg.obj + "");
                    break;
                case 1:
                    CommonUtils.showVideoCall(
                            (String) msg.obj);
                    break;
                case 2:

                    String[] data = (String[]) msg.obj;
                    CommonUtils.showMVideoCall(
                            data[0], data[1]);
                    break;
                default:
                    break;
            }
        }
    };

    public void sendVideoCallMessage(String remoteName) {

        android.os.Message message = new android.os.Message();
        message.obj = remoteName;
        message.what = 1;
        handler.sendMessage(message);
    }

    public void sendMVideoCallMessage(String remoteName, String members) {

        android.os.Message message = new android.os.Message();
        message.obj = new String[]{remoteName, members};
        message.what = 2;
        handler.sendMessage(message);
    }

    private long lastCallbackTime = 0;

    public void networkStatusCallback(boolean isConnect) {

        if (isConnect) {
            if (!XmppManager.getInstance().isAuthenticated()) {

                long curTime = System.currentTimeMillis();
                if (curTime - lastCallbackTime > 3000) {
                    XmppManager.getInstance().startReconnectionThread();
                }
                lastCallbackTime = curTime;
            }
        }
    }

    /**
     * @param isReconnection boolean 是否为重连接
     * @author kangkang
     */
    @Override
    public void connect(boolean isReconnection) {

        super.connect(isReconnection);
        addTask(new LoginTask(isReconnection));
    }

    public ConnectionListener getConnectionListener() {
        return persistentConnectionListener;
    }

    /**
     * 开启重连线程
     */
    public void startReconnectionThread() {

        // 排除登录界面打开网络的情况
        if (isInit) {

            Log.d(LOG_TAG, "startReconnectionThread" + System.currentTimeMillis());
            Utils.showToast("开始重连");

            connect(true);
        }
//        if (reconnection != null) {
//            synchronized (reconnection) {
//                taskList.clear();
//                taskTracker.clear();
//
//                Utils.showToast("开始重连");
//
////            Utils.createCircleProgressDialog(
////                    EMProApplicationDelegate.applicationContext,
////                    "网络重连中...");
//                // kk
//                reconnectionFlag = true;
//                reconnection = new ReconnectionThread(XmppManager.this);
//                reconnection.setWaiting(0);
//                reconnection.start();
//            }
//        }
    }

    public Handler getHandler() {
        return handler;
    }

    /**
     * 登陆时的用户名必须是基本的用户名，不能加'@'
     * 如果是重连接则不跳转到主页面
     */
    private class LoginTask implements Runnable {
        boolean reConnection = false;

        private LoginTask(boolean reConnection) {
            this.reConnection = reConnection;
        }

        public void run() {
            Log.i(LOG_TAG, "LoginTask.run()...");
            if (!isAuthenticated()) {
                try {
                    // 第一次连接之后用户名后面添加了@和/，重连时只能取@前的用户名才能识别
                    getConnection().login(
                            EMProApplicationDelegate.userInfo.getUserId().split("@")[0],
                            EMProApplicationDelegate.userInfo.getPassword(),
                            Constants.XMPP_RESOURCE_NAME);

                    XmppReliable xr = XmppReliable.getInstance();
                    xr.init(new APPReliableListener(EMProApplicationDelegate.applicationContext));

                    // ChatUtils初始化
                    ChatUtils.INSTANCE.init(getChatManager());
                    // 初始化登陆前开启一个线程建立连接

                    // TODO 易聊插件化注释的地方
//                    if (StaticVariable.inFormClient) {// 如果当前在formclient中
//                        SingleChatActivity.formChatManager = getConnection().getChatManager();
//                    }
//
//                    if (StaticVariable.inGroupClient) {// groupchat不需要进行chatmanager的初始化
//                        // GroupChatActivity.theAvatarsChatmanager=XmppManager.getConnection().getChatManager();
//                        GroupChatActivity.newchat0 = new MultiUserChat(
//                                getConnection(), GroupChatActivity.userid);
//                        GroupChatActivity.newchat0.join(EMProApplicationDelegate.userInfo
//                                .getUserId().split("@")[0]);
//                    }

                    //添加消息接收监听器
                    addMsgListener();

                    Log.d(LOG_TAG, "Login successfully");
                    // 添加connect监听, 在网络断开时进入重连机制
                    if (getConnectionListener() != null) {
                        getConnection().addConnectionListener(getConnectionListener());
                    }

                    // 启动心跳
                    getConnection().startKeepAliveThread(XmppManager.getInstance());

                    if (!reConnection) { // 第一次连接成功

                        EventBus.getDefault().post(new EventBusMessageEntity(
                                LoginActivity.class.getName(), "", true,
                                EventBusMessageEntity.EVENT_TYPE.LOGIN
                        ));
                        ChatUtils.INSTANCE.clearChat();
                    } else {
                        // 重连成功
                        reconnectionFlag = false;
                        ChatUtils.INSTANCE.clearChat();

//                        Utils.showToast("重连成功");
//                        Utils.hideCircleProgressDialog();
                        getConnectionListener().reconnectionSuccessful();
                    }

                } catch (Exception e) {
                    Log.e(LOG_TAG, "Failed to login to xmpp server. Caused by: "
                            + e.getMessage());
                    if (isConnected()) {
                        EventBus.getDefault().post(new EventBusMessageEntity(
                                LoginActivity.class.getName(), "登陆失败, 用户名或密码错误", false,
                                EventBusMessageEntity.EVENT_TYPE.LOGIN
                        ));
                    } else {
                        EventBus.getDefault().post(new EventBusMessageEntity(
                                LoginActivity.class.getName(), "登陆失败, 无法连接服务器", false,
                                EventBusMessageEntity.EVENT_TYPE.LOGIN
                        ));
                    }
                    getConnectionListener().reconnectionFailed(e);
                } finally {
                    runNextTask();
                }
            } else {
                Log.i(LOG_TAG, "Logged in already");
                runNextTask();
            }
        }
    }

    private void addMsgListener() {

        ToolClass.startMessageReceiveListener();// 开启消息接收监听器，接收添加好友的消息

        ChatManager cm = getConnection()
                .getChatManager();

        cm.addChatListener(new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean able) {

                chat.addMessageListener(new MessageListener() {
                    @Override
                    public void processMessage(Chat chat2, Message message) {
                        // /插入接收到的消息到sqlite。如果是广播则调用服务发送广播
                        Log.v("XmppManager", "processMessage");
                        insertMessageByType(message);
                    }
                });
            }
        });
    }

    private void insertMessageByType(Message mess) {
        MessageDAO messagedao = new MessageDAO();
        Log.v(LOG_TAG, "insertMessageByType type" + mess.getType() + "from" + mess.getFrom() + "to"
                + mess.getTo() + "body" + mess.getBody());

        if (mess.getType().toString().equals("chat")) {

            // DX改，请求消息不用存进数据库
            Element element = XmlParser.parse(mess.getBody());
            String elementTypeString = element.getProperty("type");
            boolean isRequestMessage = "requestMessage".equals(elementTypeString);

            boolean isVideoCall = false;
            if (element.getElementName().equals("video"))
                isVideoCall = true;

            if (!mess.getFrom().toLowerCase().contains("iqreceiver")// 来自单聊的消息
                    && !isRequestMessage) {// 来自IQReceiver的消息不显示上面弹出的通知消息
                if (!isVideoCall)
                    messagedao.insertPeerMessage(mess);
                String fromuser = mess.getFrom().split("@")[0];
                /***
                 * 当前不在formclient中或者接收到的消息不是由当前用户发送的
                 */

                // 反射获取插件的变量值和名称
                String userId = "";
                try {
                    Class singleChatClass = RePlugin.fetchClassLoader("EMChat")
                            .loadClass("com.zhengyuan.emchat.activity.SingleChatActivity");

                    Field field = singleChatClass.getField("userid");

                    userId = (String) field.get(singleChatClass);
                    Log.d(LOG_TAG, "SingleChatActivity " + userId);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                // 只处理不在单聊页面中的消息, 在单聊页面中的消息放到SingleChatActivity中单独处理
                if (!StaticVariable.inFormClient
                        || (!fromuser.equals(userId.split("@")[0]))) {
                    Intent intent = new Intent("ContactBroadcast");
                    intent.setAction("ContactBroadcast");
                    intent.putExtra("NAME",
                            mess.getFrom().split("/")[0]);
                    Constants.contexts.get(Constants.contexts.size() - 1)
                            .sendBroadcast(intent);
                }
            }
        }

        if (mess.getType().toString().equals("chat")) {
            Element element = XmlParser.parse(mess.getBody());
            if (element != null) {

                if ("returnWorkPlanDetailDatas".equals(element
                        .getProperty("type"))) {// 返回接收子工作单详细工作数据
                    if (StaticVariable.inSubWorkPlanShowActivity) {// 当前在subWorkPlanShowActivity中,没有发生断线重连
                        Utils.hideCircleProgressDialog();
                        String detailDatas = element.getBody();
                        if (detailDatas != null && !detailDatas.equals("")) {// 查询出来结果
                            System.out.println("xmppmanager--detailDatas="
                                    + detailDatas);
                            Bundle bundle = new Bundle();
                            bundle.putCharSequence("allDetailDatas",
                                    detailDatas);

                            Intent intent = RePlugin.createIntent("EMWorkPlanShow",
                                    "com.zhengyuan.emworkplanshow.WorkPlanDetailActivity");
                            intent.putExtras(bundle);
                            RePlugin.startActivity(Constants.contexts.get(Constants.contexts
                                    .size() - 1), intent);

                        } else {
                            Toast.makeText(
                                    Constants.contexts.get(Constants.contexts
                                            .size() - 1), "没有查到结果",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                } else if ("returnQueryWorkPlanByScanning".equals(element
                        .getProperty("type"))) {// 返回条码扫描结果
                    if (StaticVariable.inMainActivity) {// 当前在MainActivity中,没有发生断线重连
                        Utils.hideCircleProgressDialog();
                        if (element.getBody() == null
                                || element.getBody().equals("")) {// 没有查询到结果
                            Log.i(LOG_TAG, "nodata");
                            android.os.Message msg = StaticVariable.handler
                                    .obtainMessage();
                            msg.what = 1;
                            msg.obj = "没有查到结果";
                            msg.sendToTarget();
                        } else {
                            String queryData = element.getBody();
                            System.out.println("xmppmanager--queryData="
                                    + queryData);
                            String scanning = element.getProperty("scanning");
                            String userPermission = element
                                    .getProperty("userpermisssion");
                            Log.i(LOG_TAG,
                                    "userPermission=" + userPermission);
                            if (userPermission.equals("yes")) { // 质检员
                                Log.i(LOG_TAG,
                                        "intent");
                                Bundle bundle = new Bundle();
                                bundle.putCharSequence("scanning", scanning);
                                bundle.putCharSequence("allDetailDatas",
                                        queryData);
                                Intent intent = new Intent(
                                        Constants.contexts.get(Constants.contexts
                                                .size() - 1),
                                        BatchQualityTestActivity.class);
                                intent.putExtras(bundle);
                                Constants.contexts.get(
                                        Constants.contexts.size() - 1)
                                        .startActivity(intent);

                            } else if (userPermission.equals("no")) {// 通过条码扫描查询，直接跳转到工作完成填写界面
                                Log.i(LOG_TAG,
                                        "intent");
                                Bundle bundle = new Bundle();
                                bundle.putCharSequence("scanning", scanning);
                                bundle.putCharSequence("allDetailDatas",
                                        queryData);
                                Intent intent = new Intent(
                                        Constants.contexts.get(Constants.contexts
                                                .size() - 1),
                                        BatchWorkPlanSubmitActivity.class);
                                intent.putExtras(bundle);
                                Constants.contexts.get(
                                        Constants.contexts.size() - 1)
                                        .startActivity(intent);
                            }

                        }

                    }
                }
//                } else if ("returnQueryWorkPlanByDate".equals(element
//                        .getProperty("type"))) {// 返回条件查询物料代码数据
//                    if (StaticVariable.inWorkPlanShowActivity) {// 当前在WorkPlanShowActivity中,没有发生断线重连
//                        Utils.hideCircleProgressDialog();
//                        if (element.getBody() == null
//                                || element.getBody().equals("")) {// 没有查询到结果
//                            Log.i(LOG_TAG, "nodata");
//                            android.os.Message msg = StaticVariable.handler
//                                    .obtainMessage();
//                            msg.what = 1;
//                            msg.obj = "没有查到结果";
//                            msg.sendToTarget();
//                            // Toast.makeText(Constants.contexts
//                            // .get(Constants.contexts.size() - 1), "没有查到结果",
//                            // Toast.LENGTH_LONG).show();
//                        } else {
//                            String queryData = element.getBody();
//                            System.out.println("xmppmanager--queryData="
//                                    + queryData);
//                            String scanning = element.getProperty("scanning");
//                            Log.i(LOG_TAG,
//                                    "scanning=" + scanning);
//                            if (scanning == null || scanning.equals("null")) { // 不是条码扫描，普通查询，只需要刷新workplanShowActivity界面
//                                Log.i(LOG_TAG, "handler");
//                                android.os.Message msg = StaticVariable.handler
//                                        .obtainMessage();
//                                msg.what = 0;
//                                msg.obj = queryData;
//                                msg.sendToTarget();
//
//                            }
//                            // else {//通过条码扫描查询，直接跳转到工作完成填写界面
//                            // Log.i("----scanning is not null------>",
//                            // "intent");
//                            // Bundle bundle = new Bundle();
//                            // bundle.putCharSequence("scanning", scanning);
//                            // bundle.putCharSequence("allDetailDatas",
//                            // queryData);
//                            // Intent intent = new Intent(
//                            // Constants.contexts
//                            // .get(Constants.contexts.size() - 1),
//                            // WorkPlanDetailActivity.class);
//                            // intent.putExtras(bundle);
//                            // Constants.contexts.get(Constants.contexts.size()
//                            // - 1)
//                            // .startActivity(intent);
//                            // }
//                        }
//                    }
//                }

                // else if
                // ("returnUsersPositionByDepartment".equals(element.getProperty("type")))
                // {//收到部门员工位置进行处理
                // if (StaticVariable.inRadarViewActivity) {
                // String queryData=element.getProperty("UserPositions");
                // android.os.Message msg =
                // StaticVariable.handler.obtainMessage();
                // msg.what = 0;
                // msg.obj = queryData;
                // msg.sendToTarget();
                // }
                // }
                else if ("videoCall".equals(element.getProperty("type"))) {

                    // 视频弹框
                    String remoteName = mess.getFrom().split("@")[0];
                    sendVideoCallMessage(remoteName);
                    // TODO
//                    if (StaticVariable.inFormClient) {
//                        String result = element.getProperty("roomUserResult");
//                        System.out.println("接受房间：" + result);
//                        android.os.Message msg = StaticVariable.handler
//                                .obtainMessage();
//                        msg.what = 11;
//                        msg.obj = "remotename";
//                        msg.sendToTarget();
//                    }
                } else if ("MVideoCall".equals(element.getProperty("type"))) {
                    String remoteName = mess.getFrom().split("@")[0];
                    String members = element.getProperty("members");
                    sendMVideoCallMessage(remoteName, members);

                } else if ("returnVideoCall".equals(element.getProperty("type"))) {

                    if (StaticVariable.inVideoCallActivity) {
                        android.os.Message msg = StaticVariable.handler
                                .obtainMessage();
                        msg.what = 3;
                        msg.obj = mess;
                        msg.sendToTarget();

                        Log.d(LOG_TAG, "SdpOffer" + " msg: " + msg);
                    }
                } else if ("MAcceptCall".equals(element.getProperty("type"))) {
                    if (StaticVariable.inMVideoCallActivity) {
                        android.os.Message msg = StaticVariable.handler
                                .obtainMessage();
                        msg.what = 4;
                        msg.obj = mess;
                        msg.sendToTarget();
                    }
                } else if ("SdpOffer".equals(element.getProperty("type"))) {

                    if (StaticVariable.inVideoCallActivity) {
                        android.os.Message msg = StaticVariable.handler
                                .obtainMessage();
                        msg.what = 0;
                        msg.obj = mess;
                        msg.sendToTarget();

                        Log.d(LOG_TAG, "SdpOffer" + " msg: " + msg);
                    }
                } else if ("MSdpOffer".equals(element.getProperty("type"))) {
                    if (StaticVariable.inMVideoCallActivity) {
                        android.os.Message msg = StaticVariable.handler
                                .obtainMessage();
                        msg.what = 1;
                        msg.obj = mess;
                        msg.sendToTarget();
                    }
                } else if ("IceCandidate".equals(element.getProperty("type"))) {

                    if (StaticVariable.inVideoCallActivity) {
                        android.os.Message msg = StaticVariable.handler
                                .obtainMessage();
                        msg.what = 1;
                        msg.obj = mess;
                        msg.sendToTarget();

                        Log.d(LOG_TAG, "IceCandidate" + " msg: " + msg);
                    }
                } else if ("MIceCandidate".equals(element.getProperty("type"))) {
                    if (StaticVariable.inMVideoCallActivity) {
                        android.os.Message msg = StaticVariable.handler
                                .obtainMessage();
                        msg.what = 2;
                        msg.obj = mess;
                        msg.sendToTarget();
                    }
                } else if ("videoEnd".equals(element.getProperty("type"))) {

                    if (StaticVariable.inVideoCallActivity) {
                        android.os.Message msg = StaticVariable.handler
                                .obtainMessage();
                        msg.what = 2;
                        msg.obj = mess;
                        msg.sendToTarget();

                        Log.d(LOG_TAG, "videoEnd" + " msg: " + msg);
                    }
                } else if ("MVideoEnd".equals(element.getProperty("type"))) {
                    if (StaticVariable.inMVideoCallActivity) {
                        android.os.Message msg = StaticVariable.handler
                                .obtainMessage();
                        msg.what = 3;
                        msg.obj = mess;
                        msg.sendToTarget();
                    }
                } else if ("MemberWaiting".equals(element.getProperty("type"))) {
                    if (StaticVariable.inMVideoCallActivity) {
                        android.os.Message msg = StaticVariable.handler
                                .obtainMessage();
                        msg.what = 5;
                        msg.obj = mess;
                        msg.sendToTarget();
                    }
                }
            }
        }
        // 我的消息接收到后
        if (mess.getType().toString().equals("normal")
                && !mess.getFrom().toString().contains("@"))// 如果是广播消息，需要判断
        // &&XmlParser.parse(mess.getBody())==null)
        {
            // Log.v("登录页面的inNoticePage", "StaticVariable.inNoticePage");
            // 广播，不仅插入sqlite，而且要发送广播
            Element element = XmlParser.parse(mess.getBody());
            if (element == null) {
                messagedao.insert(mess);
                if (!StaticVariable.inNoticePage) {// 不在NoticeActivity页面
                    System.out.println("!StaticVariable.inNoticePage");
                    Intent NoticeBroadcast = new Intent("NoticeBroadcast");
                    NoticeBroadcast.setAction("NoticeBroadcast");
                    // NoticeBroadcast.putExtra("USER", mess.getFrom());
                    // NoticeBroadcast.putExtra("BODY", mess.getBody());
                    // NoticeBroadcast.putExtra("TIME", TimeRenderUtil.getDate());
                    NoticeBroadcast.putExtra("TYPE", "NoticePage");
                    NoticeBroadcast.putExtra("NAME", mess.getFrom());
                    NoticeBroadcast.putExtra("MESSAGE", mess.getBody());
                    NoticeBroadcast.putExtra("TIME", TimeRenderUtil.getDate());
                    Constants.contexts.get(Constants.contexts.size() - 1)
                            .sendBroadcast(NoticeBroadcast);
                } else {// 在NoticeActivity页面
                    //TODO
//                    EventBus.getDefault().post(new EventBusMessageEntity(
//                            MyMessageActivity.class.getName(), "", true, EventBusMessageEntity.EVENT_TYPE.ALL
//                    ));
                }
            } else if (element.getProperty("type").equals("WorkListChange")) {
                System.out.println("------WorkListChange------->");
                String messageString = element.getProperty("message");
                Log.i(LOG_TAG, "" + messageString);
                if (!StaticVariable.inWorkPlanShowActivity) {
                    Intent NoticeBroadcast = new Intent("NoticeBroadcast");
                    NoticeBroadcast.setAction("NoticeBroadcast");
                    // NoticeBroadcast.putExtra("USER", mess.getFrom());
                    // NoticeBroadcast.putExtra("BODY", mess.getBody());
                    // NoticeBroadcast.putExtra("TIME", TimeRenderUtil.getDate());
                    NoticeBroadcast.putExtra("TYPE", "WorkListChange");
                    NoticeBroadcast.putExtra("NAME", mess.getFrom());
                    NoticeBroadcast.putExtra("MESSAGE", messageString);
                    NoticeBroadcast.putExtra("TIME", TimeRenderUtil.getDate());
                    Constants.contexts.get(Constants.contexts.size() - 1)
                            .sendBroadcast(NoticeBroadcast);
                } else {// 如果在WorkPlanShowActivity中，刷新WorkPlanShowActivity页面
                    android.os.Message msg = StaticVariable.handler
                            .obtainMessage();
                    msg.what = 2;
                    msg.sendToTarget();
                }
            }
        }
        // 群组消息
        if (mess.getType().toString().equals("normal")
                && mess.getFrom().toString().contains("@")
                && mess.getFrom().toString().contains("/")) {
            Log.v(LOG_TAG, GroupPass.getInstance().getGroupid()
                    + "===" + mess.getFrom().split("/")[0]);

            if (mess.getFrom().split("/")[1].split("@")[0]// 如果群聊消息来自自己不存入数据库
                    .contentEquals(EMProApplicationDelegate.userInfo.getUserId()
                            .split("@")[0]))
                ;
            else {
                messagedao.insertPeerMessage(mess);
            }

            // 只处理不在群组聊天界面的消息, 在群组页面中的消息已经放到GroupChatActivity中单独处理
            if (!GroupPass.getInstance().getGroupid().split("/")[0]
                    .contentEquals(mess.getFrom().split("/")[0])) {
                Intent GroupBroadcast = new Intent("GroupBroadcast");
                GroupBroadcast.setAction("GroupBroadcast");
                GroupBroadcast.putExtra("NAME", mess.getFrom().toString());
                Constants.contexts.get(Constants.contexts.size() - 1)
                        .sendBroadcast(GroupBroadcast);
            }
        }
    }
}