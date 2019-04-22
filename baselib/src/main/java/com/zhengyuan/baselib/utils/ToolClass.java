package com.zhengyuan.baselib.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.xmpp.BaseXmppManager;
import com.zhengyuan.baselib.xmpp.ChatUtils;
import com.zhengyuan.baselib.xmpp.MucIQsender;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zhengyuan.baselib.broadcasts.BroadcastReceiverAddFriend;
import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.constants.StaticVariable;

import com.zhengyuan.baselib.utils.xml.Element;
import com.zhengyuan.baselib.utils.xml.XmlParser;


public class ToolClass {
    //存储某个用户所有的信息
    private static String userInfoString = "";
    private static boolean userInfoUpdate = false;
    private static String serverNameString = BaseXmppManager.getConnection().getServiceName();
    private static BroadcastReceiverAddFriend broadcastReceiverAddFriend = null;
    private static Context context = null;
    //用于缓存当前用户的nickname
    private static String currentUserNickname = "";

    /**
     * 判断考勤时用户输入的数据是否合法
     *
     * @return
     */
    public static boolean isAttendanceValid(String str) {
        if (str.contains("<") || str.contains(">") || str.contains("|") || str.contains("の")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 判断修改昵称时用户输入的数据是否合法
     *
     * @return
     */
    public static boolean isNickNameValid(String str) {
        if (str.contains("<") || str.contains(">") || str.contains(",") || str.contains(";")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 判断该用户是否已经是好友关系了(已经自动过滤了@后面的内容)
     *
     * @param userName
     * @return
     */
    public static boolean isFriend(String userName) {
        Roster roster = BaseXmppManager.getConnection().getRoster();
        List<String> friendNameList = new ArrayList<String>();
        //遍历该用户所有的组以及组下的用户，获取该用户所有的好友名
        for (RosterGroup group : roster.getGroups()) {
            for (RosterEntry entry : group.getEntries()) {
                friendNameList.add(entry.getUser().split("@")[0]);
            }
        }
        //输出该用户所有的好友
        Log.d("ToolClass.isFriend", "该用户所有的好友：" + friendNameList.toString());
        for (int i = 0; i < friendNameList.size(); i++) {
            if (userName.split("@")[0].equals(friendNameList.get(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取当前界面的Context
     *
     * @return
     */
    public static Context getContext() {
        return Constants.contexts.get(Constants.contexts.size() - 1);
    }

    /**
     * 使用登录界面的Activity进行广播注册
     *
     * @return
     */
    public static Context initContext() {
        context = Constants.contexts.get(0);
        return context;
    }

    /**
     * 注册添加好友的广播接收器，并设置为手动处理加好友的请求
     */
    public static void registerAddFriendBroadcastReceiver(Context context) {
        if (broadcastReceiverAddFriend == null) {
            initContext();

            BroadcastReceiverAddFriend bradf = new BroadcastReceiverAddFriend();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("BroadcastReceiverAddFriend");//监听谁发出的广播
            context.registerReceiver(bradf, intentFilter);
            broadcastReceiverAddFriend = bradf;
        }

        serverNameString = BaseXmppManager.getConnection().getServiceName();
    }

    /**
     * 注销添加好友的广播接收器
     */
    public static void unregisterAddFriendBroadcastReceiver(Context context) {
        if (broadcastReceiverAddFriend != null) {
            currentUserNickname = "";
            context.unregisterReceiver(broadcastReceiverAddFriend);
            broadcastReceiverAddFriend = null;
        }
    }

    /**
     * 发送邀请多个好友进群的请求消息
     *
     * @param friendNames
     * @param roomName
     */
    public static void sendInviteMessages(List<String> friendNames, String roomName) {
        for (int i = 0; i < friendNames.size(); i++) {
            sendInviteMessage(friendNames.get(i), roomName);
        }
    }

    /**
     * 发送邀请某个好友进群的请求消息
     *
     * @param friendName
     * @param roomName
     */
    public static void sendInviteMessage(String friendName, String roomName) {
        String friendNickName = AccountManager.getInstance().getFriendNickname(friendName);
        if (friendNickName.equals("")) {
            friendNickName = friendName;
        }
        Element element = new Element("mybody");
        element.addProperty("type", "requestMessage");
        element.addProperty("describe", "requestJoinRoom");
        element.addProperty("roomName", roomName);
        element.addProperty("nickName", friendNickName);//把对方的nickName也发过去，不用对方再获取了
        sendMessage(friendName.split("@")[0], element.toString());
    }

    /**
     * 添加好友
     *
     * @param friendName 用户名
     * @param nickname   好友的昵称
     * @return
     */
    public static boolean addFriend(String friendName, String nickname) {
        Roster roster = BaseXmppManager.getConnection().getRoster();
        String friendNameString = friendName.trim().split("@")[0];
        try {
            roster.createEntry(friendNameString, nickname, new String[]{"Friends"});//好友默认在Friend组里面
            System.out.println("添加好友成功！！");
            return true;
        } catch (XMPPException e) {
            e.printStackTrace();
            System.out.println("失败！！" + e);
            return false;
        }
    }

    /**
     * 发送加好友的消息请求
     *
     * @param friendName
     * @param reason     说明(为什么要加好友)
     * @param nickName   本用户的nickName
     */
    public static void sendAddFriendMessage(String friendName, String reason, String nickName) {
        Element element = new Element("mybody");
        element.addProperty("type", "requestMessage");
        element.addProperty("describe", "requestAddFriend");
        if (nickName == null) {
            element.addProperty("nickName", EMProApplicationDelegate.userInfo.getUserId().split("@")[0]);
        } else {
            element.addProperty("nickName", nickName);
        }
        if (reason == null) {
            element.addProperty("reason", "老司机，带带我！");
        } else {
            element.addProperty("reason", reason);
        }
        sendMessage(friendName, element.toString());
    }

    /**
     * 发送加好友的消息请求确认
     *
     * @param friendName
     */
    public static void sendAddFriendMessageVerify(String friendName) {
        Element element = new Element("mybody");
        element.addProperty("type", "requestMessage");
        element.addProperty("describe", "requestAddFriendVerify");
        element.setBody("");
        sendMessage(friendName, element.toString());
    }

    /**
     * 发送消息
     *
     * @param toUser
     * @param msg
     * @return
     */
    public static boolean sendMessage(String toUser, String msg) {
        toUser = toUser.split("@")[0];
        toUser += "@" + BaseXmppManager.getConnection().getServiceName();
        String currentUserName = EMProApplicationDelegate.userInfo.getUserId().split("@")[0];
        Message message = new Message();
        message.setFrom(currentUserName + "@" + BaseXmppManager.getConnection().getServiceName());
        message.setTo(toUser);
        message.setBody(msg);
        message.setType(Message.Type.chat);

        ChatManager chatManager = BaseXmppManager.getConnection().getChatManager();
        Chat chat = chatManager.createChat(toUser, null);
        try {
            chat.sendMessage(message);
            Log.i("sendmessage()", "--------------发送成功");
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static String getCurrentUserNickName() {
        if (currentUserNickname.equals("")) {
            currentUserNickname = haveThisUser(EMProApplicationDelegate.userInfo.getUserId().split("@")[0]);
        }
        if (currentUserNickname.equals("notExist")) {
            currentUserNickname = EMProApplicationDelegate.userInfo.getUserId().split("@")[0];
        }
        Log.d("ToolClass", "getCurrentUserNickName-end");
        return currentUserNickname;
    }

    /**
     * 判断是否存在指定的用户,如果用户存在，则返回该用户的用户名或者工号，否则返回"notExist"
     *
     * @param userName
     * @return
     */
    public static String haveThisUser(String userName) {
        List<Map<String, String>> userInfoList = getUsersInfo(userName);
        if (userInfoList.size() == 1) {
            Log.v("haveThisUser---->DX", userInfoList.get(0).get("name"));
            if (userInfoList.get(0).get("name").equals("null"))
                return userName;
            else {
                return userInfoList.get(0).get("name");
            }
        } else if (userInfoList.size() == 0) {//不存在
            Log.v("haveThisUser---->DX", "notExist");
            return "notExist";
        } else {//存在太多
            for (int i = 0; i < userInfoList.size(); i++)//先查找是否存在能匹配的
            {
                if (userName.equals(userInfoList.get(i).get("userName"))) {
                    return userInfoList.get(i).get("name").equals("null") ? userName : userInfoList.get(i).get("name");
                }
            }
            Log.v("haveThisUser---->DX", "Exist " + userInfoList.size() + " items,it is not unique");
            return "notExist";
        }
    }

    /**
     * 获取某个用户的所有信息，如果不存在此用户，则返回size为0的Map<String, String>对象
     *
     * @param userName
     * @return username:用户工号；name：用户昵称；dept_id:部门；empl_duty:职位；empl_mobile:电话号码；
     */
    public static Map<String, String> getUserInfo(String userName) {
        List<Map<String, String>> userInfoList = getUsersInfo(userName);
        Map<String, String> mapInfo = new HashMap<>();
        if (userInfoList.size() == 1) {
            Log.v("getUserInfo---->DX", userInfoList.get(0).toString());
            return userInfoList.get(0);
        } else if (userInfoList.size() == 0) {//不存在
            Log.v("getUserInfo---->DX", "notExist");
            return mapInfo;
        } else {//存在太多
            Log.v("getUserInfo---->DX", "Exist " + userInfoList.size() + " items,it is not unique");
            return mapInfo;
        }
    }

    /**
     * 获取多个用户的所有信息，如果不存，则返回size为0的List<Map<String, String>>对象
     *
     * @param userName
     * @return username:用户工号；name：用户昵称；dept_id:部门；empl_duty:职位；empl_mobile:电话号码；
     */
    public static List<Map<String, String>> getUsersInfo(String userName) {

        sendGetUserInfoMessage(userName);
        List<Map<String, String>> userInfoList = new ArrayList<Map<String, String>>();
        while (!userInfoUpdate)//循环等待服务端发回该用户的信息
        {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        userInfoUpdate = false;

        if ("notExist".equals(userInfoString)) {
            //	Log.v("getUsersInfo---->DX", "notExist");
            return userInfoList;
        }
        String[] users = userInfoString.split(";");
        String[] userString;
        String[] keyValue;
        for (int i = 0; i < users.length; i++) {
            userString = users[i].split("\\,");
            Map<String, String> userInfoMap = new HashMap<String, String>();
            for (int j = 0; j < userString.length; j++) {
                keyValue = userString[j].split("=");
                if (keyValue.length == 2) {
                    userInfoMap.put(keyValue[0], keyValue[1]);
                } else {
                    userInfoMap.put(keyValue[0], "无");//如果字段不存在，则置为无
                }
            }
            userInfoList.add(userInfoMap);
        }

        Log.v("getUsersInfo", "getUsersInfo userInfoList.size=" + userInfoList.size());
        return userInfoList;
    }

    /**
     * 发送获取某个用户信息的消息的请求
     */
    public static void sendGetUserInfoMessage(String userName) {
        ChatManager chatManager = BaseXmppManager.getConnection().getChatManager();
        Element element = new Element("mybody");
        element.addProperty("type", "requestMessage");
        element.addProperty("describe", "getUserInfo");
        element.setBody(userName);
        Chat newchat0 = chatManager.createChat("iqreceiver@" + serverNameString, null);// xxzx-dx9811
        try {
            newchat0.sendMessage(element.toString());
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开启消息接收监听器
     */
    public static void startMessageReceiveListener() {
        ChatManager chatManager = BaseXmppManager.getConnection().getChatManager();

        chatManager.addChatListener(new ChatManagerListener() {

            @Override
            public void chatCreated(Chat chat, boolean able) {
                chat.addMessageListener(new MessageListener() {
                    @Override
                    public void processMessage(Chat chat2, Message message) {
                        receiveMessage(message);
                    }
                });
            }
        });
    }

    /**
     * 处理所有从服务端接收到的消息
     *
     * @param mess
     */
    private static void receiveMessage(Message mess) {
        if (mess.getType().toString().equals("chat") && !mess.getBody().equals("")) {//如果消息体不为空
            Element element = XmlParser.parse(mess.getBody());
            if (element != null && "requestMessage".equals(element.getProperty("type"))) {//如果为请求类型的消息
//				Log.d("ToolClass", "receiveMessage");
//				Log.d("ToolClass->mess.body", mess.getBody());
//				Log.d("ToolClass->mess.type", element.getProperty("type"));
                if ("returnUserInfo".equals(element.getProperty("describe"))) {//如果为获取某个用户的信息
                    String theUserInfo = element.getBody();
                    userInfoString = theUserInfo;
                    userInfoUpdate = true;
                    Log.d("ToolClass-->getUserInfo", theUserInfo);
                } else if ("requestAddFriend".equals(element.getProperty("describe"))) {//如果为添加好友的请求
                    System.out.println("收到添加请求！");
                    // 发送广播传递发送方的JIDfrom及字符串
                    String acceptAdd = "收到添加请求！";

                    Bundle bundle = new Bundle();
                    bundle.putString("fromName", mess.getFrom());
                    bundle.putString("acceptAdd", acceptAdd);
                    bundle.putString("nickName", element.getProperty("nickName"));
                    bundle.putString("reason", element.getProperty("reason"));

                    Intent intent = new Intent();
//					intent.putExtra("fromName", mess.getFrom());
//					System.out.println("From---->DX:"+mess.getFrom());
//					intent.putExtra("acceptAdd", acceptAdd);
                    intent.putExtras(bundle);
                    intent.setAction("BroadcastReceiverAddFriend");
                    Constants.contexts.get(Constants.contexts.size() - 1).sendBroadcast(intent);
                } else if ("requestAddFriendVerify".equals(element.getProperty("describe"))) {//如果为添加好友确认请求
                    System.out.println("收到添加确认！");
                    // 发送广播传递发送方的JIDfrom及字符串
                    String acceptAdd = "收到添加确认！";

                    Bundle bundle = new Bundle();
                    bundle.putString("fromName", mess.getFrom());
                    bundle.putString("acceptAdd", acceptAdd);
                    bundle.putString("reason", element.getProperty("reason"));

                    Intent intent = new Intent();
//					intent.putExtra("fromName", mess.getFrom());
//					System.out.println("From---->DX:"+mess.getFrom());
//					intent.putExtra("acceptAdd", acceptAdd);
                    intent.putExtras(bundle);
                    intent.setAction("BroadcastReceiverAddFriend");
                    Constants.contexts.get(Constants.contexts.size() - 1).sendBroadcast(intent);
                } else if ("requestJoinRoom".equals(element.getProperty("describe"))) {
                    Log.d("TooClass---DX", "收到requestJoinRoom请求");
                    final String fromName = mess.getFrom().split("@")[0];
                    final String roomName = element.getProperty("roomName");
                    String nickName = element.getProperty("nickName");
//					if(AccountManager.getInstance().joinRoom(roomName, EMProApplicationDelegate.userInfo.getUserId().split("@")[0])){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                MucIQsender.run();//更新本地群组列表
                                System.out.println("inContactMainActivity=" + StaticVariable.inContactMainActivity);
                                if (StaticVariable.inContactMainActivity) {
                                    android.os.Message msg = StaticVariable.handler.obtainMessage();
                                    msg.what = 2;
                                    System.out.println("msg.what = 2");
                                    msg.sendToTarget();
                                }
                                Log.d("ToolClass--->DX", fromName + "邀请加入" + roomName + "群，刷新成功");
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e("ToolClass--->DX", fromName + "邀请加入" + roomName + "群错误，刷新失败");
                            }
                        }
                    }).start();
//					}
//					Bundle bundle=new Bundle();
//					bundle.putString("fromName", mess.getFrom());
//					bundle.putString("roomName", element.getProperty("roomName"));
//					Intent intent = new Intent();
//					intent.putExtras(bundle);
//					intent.setAction("com.example.openfireregisttest.MainActivity");
//					Constants.contexts.get(Constants.contexts.size()-1).sendBroadcast(intent);
                }
            }
        }
    }
}
