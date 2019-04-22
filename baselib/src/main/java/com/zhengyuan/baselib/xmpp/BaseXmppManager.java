package com.zhengyuan.baselib.xmpp;

import android.util.Log;

import com.zhengyuan.baselib.xmpp.reconnect.TaskSubmitter;
import com.zhengyuan.baselib.xmpp.reconnect.TaskTracker;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.zhengyuan.baselib.constants.Constants;

import com.zhengyuan.baselib.xmpp.interfaces.IXmppManager;
import com.zhengyuan.baselib.xmpp.util.XmppReliable;

/**
 * Created by zy on 2017/11/4.
 */

public class BaseXmppManager implements IXmppManager {

    public static String ScanFunction = null;
    public static String teamFunction = null;
    public static String ChildTable = null;

    private static final String LOG_TAG = "BaseXmppManager";

    protected ExecutorService executorService;////利用Executors 提供的线程池，执行耗时任务：
    protected TaskSubmitter taskSubmitter;
    protected TaskTracker taskTracker;//计数。记录线程队列中还有多少个待执行的线程
    protected String xmppHost;
    protected int xmppPort;
    protected static XMPPConnection connection;

    protected boolean running = false;
    protected Future<?> futureTask;//Future 可以用来获取Runnable , Callable 任务的执行结果，查询等。
    protected List<Runnable> taskList = new ArrayList<>();//线程队列，挨个执行，

    /**
     * 执行队列和xmpp的初始化
     */
    public void init() {
        configureConnection(ProviderManager.getInstance());

        executorService = Executors.newSingleThreadExecutor();
        taskSubmitter = new TaskSubmitter(executorService);
        taskTracker = new TaskTracker();

        xmppHost = Constants.XMPP_IP;
        xmppPort = Constants.XMPP_HOST;
    }

    /**
     * XmppManager的启动
     */
    @Override
    public void start() {
        Log.i(LOG_TAG, "Xmpp start()...");
        connect(false);//连接时重连标识为false
    }

    /**
     * XmppManager的关闭
     */
    @Override
    public void stop() {
        Log.i(LOG_TAG, "Xmpp stop()...");
        disconnect();
        executorService.shutdown();
    }

    /**
     * @param isReconnection boolean 是否为重连接
     *                       Xmpp登录: 执行connect任务
     */
    @Override
    public void connect(boolean isReconnection) {

        addTask(new ConnectTask());
    }

    /**
     * xmpp断开
     */
    @Override
    public void disconnect() {
        Log.i(LOG_TAG, "disconnect()...");
        Runnable runnable = new Runnable() {

            public void run() {
                if (isConnected()) {
                    Log.i(LOG_TAG, "terminatePersistentConnection()... run()");
                    getConnection().disconnect();
                }
                runNextTask();
            }
        };
        addTask(runnable);
    }

    /**
     * 如果当前没有task在执行, 则直接run加入的task
     * 如果有, 先放到 taskList中
     *
     * @param runnable
     */
    protected void addTask(Runnable runnable) {
        taskTracker.increase();
        synchronized (taskList) {
            if (taskList.isEmpty() && !running) {
                running = true;
                futureTask = taskSubmitter.submit(runnable);
                // 根据TaskSubmitter实现, 这里判断为null的话, 说明task或者executor出了问题, 任务数量直接减一
                if (futureTask == null) {
                    taskTracker.decrease();
                }
            } else {
                taskList.add(runnable);
            }
        }
    }

    /**
     * taskList 起到了任务队列的作用
     * 各个继承Runnable的task执行结束后必须调用的函数
     * 作用:
     * 1. run下一个task
     * 2. 任务数量减一, 为刚刚执行完的task 收尾
     */
    public void runNextTask() {
        Log.i(LOG_TAG, "runNextTask()...");
        synchronized (taskList) {
            running = false;
            futureTask = null;
            if (!taskList.isEmpty()) {
                Log.i(LOG_TAG, "taskList:" + taskList.size());
                Runnable runnable = (Runnable) taskList.get(0);
                taskList.remove(0);
                running = true;
                futureTask = taskSubmitter.submit(runnable);
                if (futureTask == null) {
                    taskTracker.decrease();
                }
            }
        }
        // 因为runTask在Runnable中调用, Runnable中没有做任务数量减少的处理, 所以在这里处理
        taskTracker.decrease();
    }

    /**
     * Xmpp连接服务器
     */
    private class ConnectTask implements Runnable {

        private ConnectTask() {

        }

        public void run() {
            Log.i(LOG_TAG, "ConnectTask.run()...");
            if (!isConnected()) {

                Log.e(LOG_TAG, "XMPP connected start");
                ConnectionConfiguration connConfig = new ConnectionConfiguration(xmppHost, xmppPort);
                connConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
                connConfig.setSASLAuthenticationEnabled(false);
                connConfig.setCompressionEnabled(false);
                connConfig.setReconnectionAllowed(true);
                connection = new XMPPConnection(connConfig, XmppReliable.getInstance());

                try {
                    connection.connect();
                    Log.i(LOG_TAG, "ConnectTask.run() XMPP connected successfully");
                } catch (XMPPException e) {
                    Log.e(LOG_TAG, "ConnectTask.run() XMPP connection failed", e);

//TODO
//                    EventBus.getDefault().post(new EventBusMessageEntity(
//                            LoginActivity.class.getName(), "登录失败，请检查网络！", false,
//                            EventBusMessageEntity.EVENT_TYPE.LOGIN
//                    ));
                } finally {
                    runNextTask();
                }

            } else {
                Log.i(LOG_TAG, "ConnectTask.run() XMPP connected already");
                runNextTask();
            }
        }
    }

    public static XMPPConnection getConnection() {
        return connection;
    }

    public static ChatManager getChatManager() {
        return connection.getChatManager();
    }

    /**
     * 是否已连接(匿名登录)
     *
     * @return
     */
    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    /**
     * 是否已登录
     *
     * @return
     */
    public boolean isAuthenticated() {
        return connection != null && connection.isConnected()
                && connection.isAuthenticated();
    }

    /**
     * XMPP一些配置
     *
     * @param pm
     */
    public void configureConnection(ProviderManager pm) {

        // Private Data Storage
        pm.addIQProvider("query", "jabber:iq:private",
                new PrivateDataManager.PrivateDataIQProvider());

        // Time
        try {
            pm.addIQProvider("query", "jabber:iq:time",
                    Class.forName("org.jivesoftware.smackx.packet.Time"));
        } catch (ClassNotFoundException e) {
        }

        // XHTML
        pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
                new XHTMLExtensionProvider());

        // Roster Exchange
        pm.addExtensionProvider("x", "jabber:x:roster",
                new RosterExchangeProvider());
        // Message Events
        pm.addExtensionProvider("x", "jabber:x:event",
                new MessageEventProvider());
        // Chat State
        pm.addExtensionProvider("active",
                "http://jabber.org/protocol/chatstates",
                new ChatStateExtension.Provider());
        pm.addExtensionProvider("composing",
                "http://jabber.org/protocol/chatstates",
                new ChatStateExtension.Provider());
        pm.addExtensionProvider("paused",
                "http://jabber.org/protocol/chatstates",
                new ChatStateExtension.Provider());
        pm.addExtensionProvider("inactive",
                "http://jabber.org/protocol/chatstates",
                new ChatStateExtension.Provider());
        pm.addExtensionProvider("gone",
                "http://jabber.org/protocol/chatstates",
                new ChatStateExtension.Provider());

        // FileTransfer
        pm.addIQProvider("si", "http://jabber.org/protocol/si",
                new StreamInitiationProvider());

        // Group Chat Invitations
        pm.addExtensionProvider("x", "jabber:x:conference",
                new GroupChatInvitation.Provider());
        // Service Discovery # Items
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
                new DiscoverItemsProvider());
        // Service Discovery # Info
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
                new DiscoverInfoProvider());
        // Data Forms
        pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
        // MUC User
        pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
                new MUCUserProvider());
        // MUC Admin
        pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
                new MUCAdminProvider());
        // MUC Owner
        pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
                new MUCOwnerProvider());
        // Delayed Delivery
        pm.addExtensionProvider("x", "jabber:x:delay",
                new DelayInformationProvider());
        // Version
        try {
            pm.addIQProvider("query", "jabber:iq:version",
                    Class.forName("org.jivesoftware.smackx.packet.Version"));
        } catch (ClassNotFoundException e) {
        }
        // VCard
        pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
        // Offline Message Requests
        pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
                new OfflineMessageRequest.Provider());
        // Offline Message Indicator
        pm.addExtensionProvider("offline",
                "http://jabber.org/protocol/offline",
                new OfflineMessageInfo.Provider());
        // Last Activity
        pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());
        // User Search
        pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());
        // SharedGroupsInfo
        pm.addIQProvider("sharedgroup",
                "http://www.jivesoftware.org/protocol/sharedgroup",
                new SharedGroupsInfo.Provider());
        // JEP-33: Extended Stanza Addressing
        pm.addExtensionProvider("addresses",
                "http://jabber.org/protocol/address",
                new MultipleAddressesProvider());

        // 添加 群聊天IQ包接收对象
        pm.addIQProvider("muc", "zydq", new MUCPacketExtensionProvider());
        // pm.addIQProvider("MucMembers","zydq",new
        // MUCPacketExtensionProvider());
    }
}
