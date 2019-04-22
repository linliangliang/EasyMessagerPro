package com.zhengyuan.baselib.xmpp;

import com.zhengyuan.baselib.listener.NetworkCallbacks;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.baselib.utils.xml.Element;
import com.zhengyuan.baselib.utils.xml.XmlParser;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zy on 2017/11/14.
 * 封装了XMPP的Chat发送消息的功能,各个模块可以直接使用
 */

public enum ChatUtils {
    INSTANCE;

    private ChatManager chatManager;
    private Map<String, Chat> chatMap = new ConcurrentHashMap<>();
    private Map<String, MultiUserChat> multiUserChatMap = new ConcurrentHashMap<>();

    /**
     * 重连之后必须重新调用此方法初始化chatManager
     * @param chatManager
     */
    public void init(ChatManager chatManager) {

        this.chatManager = chatManager;

    }

    /**
     * 重连时清除map, 因为重连后原来的Chat已经丢失连接, 无法使用, 否则会报错 Not connected to server.
     */
    public void clearChat() {

        chatMap.clear();
        multiUserChatMap.clear();
    }

    public String getUserChatMsgID(String userID) {

        if (chatMap.containsKey(userID)) {

            return chatMap.get(userID).getMessageID();
        } else
            return null;
    }

    public String getGroupChatMsgID(String groupID) {

        if (multiUserChatMap.containsKey(groupID)) {

            return multiUserChatMap.get(groupID).getMessageID();
        } else
            return null;
    }

    /**
     * 给指定组 发送简单的消息,不处理回调
     * @param groupID
     * @param userJID
     */
    public void sendMsg2Group(String groupID, String userJID, String content) {
        synchronized (this) {

            if (!BaseXmppManager.getConnection().isAuthenticated()) {

                Utils.showToast("请在网络连接后重试");
                return;
            }

            MultiUserChat chat;
            if (chatMap.containsKey(groupID)) {
                chat = multiUserChatMap.get(groupID);
            } else {
                chat = new MultiUserChat(BaseXmppManager.getConnection(), groupID);
                multiUserChatMap.put(groupID, chat);
            }
            try {
                chat.join(userJID);
            } catch (XMPPException e) {
                e.printStackTrace();
            }
            try {
                chat.sendMessage(content);
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 给指定组 发送简单的消息,不处理回调
     * @param groupID
     * @param userJID
     */
    public void sendMsg2Group(String groupID, String userJID, String content, String mid) {
        synchronized (this) {

            if (!BaseXmppManager.getConnection().isAuthenticated()) {

                Utils.showToast("请在网络连接后重试");
                return;
            }

            MultiUserChat chat;
            if (chatMap.containsKey(groupID)) {
                chat = multiUserChatMap.get(groupID);
            } else {
                chat = new MultiUserChat(BaseXmppManager.getConnection(), groupID);
                multiUserChatMap.put(groupID, chat);
            }
            try {
                chat.join(userJID);
            } catch (XMPPException e) {
                e.printStackTrace();
            }
            try {
                chat.sendMessage(content, mid);
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 给指定用户 发送简单的消息,不处理回调
     * @param userJID
     * @param content
     */
    public void sendMsg2User(String userJID, String content, String mid) {
        synchronized (this) {

            if (!BaseXmppManager.getConnection().isAuthenticated()) {

                Utils.showToast("请在网络连接后重试");
                return;
            }

            Chat chat;
            if (chatMap.containsKey(userJID)) {
                chat = chatMap.get(userJID);
            } else {
                chat = chatManager.createChat(userJID, null);
                chatMap.put(userJID, chat);
            }
            try {
                chat.sendMessage(content, mid);
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 给指定用户 发送简单的消息,不处理回调
     * @param userJID
     * @param content
     */
    public void sendMsg2User(String userJID, Message content) {
        synchronized (this) {

            if (!BaseXmppManager.getConnection().isAuthenticated()) {

                Utils.showToast("请在网络连接后重试");
                return;
            }

            Chat chat;
            if (chatMap.containsKey(userJID)) {
                chat = chatMap.get(userJID);
            } else {
                chat = chatManager.createChat(userJID, null);
                chatMap.put(userJID, chat);
            }
            try {
                chat.sendMessage(content);
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 给指定用户 发送简单的消息,不处理回调
     * @param userJID
     * @param content
     */
    public void sendMsg2User(String userJID, String content) {
        synchronized (this) {

            if (!BaseXmppManager.getConnection().isAuthenticated()) {

                Utils.showToast("请在网络连接后重试");
                return;
            }

            Chat chat;
            if (chatMap.containsKey(userJID)) {
                chat = chatMap.get(userJID);
            } else {
                chat = chatManager.createChat(userJID, null);
                chatMap.put(userJID, chat);
            }
            try {
                chat.sendMessage(content);
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 给服务器 发送简单的消息,不处理回调
     * @param toUser
     * @param content
     */
    public void sendMessage(String toUser, String content) {
        synchronized (this) {

            if (!BaseXmppManager.getConnection().isAuthenticated()) {

                Utils.showToast("请在网络连接后重试");
                return;
            }
            String userJID = toUser + "@" +
                    BaseXmppManager.getConnection().getServiceName();

            Chat chat;
            if (chatMap.containsKey(userJID)) {
                chat = chatMap.get(userJID);
            } else {
                chat = chatManager.createChat(userJID, null);
                chatMap.put(userJID, chat);
            }
            try {
                chat.sendMessage(content);
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 给服务器 发送消息,并设置回调函数,处理相应回调
     * @param toUser
     * @param content
     * @param dataGotTag
     * @param listenerThinner
     */
    public void sendMessage(String toUser, String content,
                            final String dataGotTag,
                            final NetworkCallbacks.MessageListenerThinner listenerThinner) {
        synchronized (this) {

            if (!BaseXmppManager.getConnection().isAuthenticated()) {

                Utils.showToast("请在网络连接后重试");
                return;
            }

            String userJID = toUser + "@" +
                    BaseXmppManager.getConnection().getServiceName();

            Chat chat;
            if (chatMap.containsKey(userJID)) {
                chat = chatMap.get(userJID);
            } else {
                chat = chatManager.createChat(userJID, null);
                chatMap.put(userJID, chat);
            }
            //TODO 没有处理相同chat添加了相同messageListener的情况
            MessageListener messageListener = new MessageListener() {
                @Override
                public void processMessage(Chat chat, Message message) {
                    if (message.getType().toString().equals("chat")) {
                        Element element = XmlParser.parse(message.getBody());
                        if (element != null && dataGotTag.equals(element.getProperty("type"))) {

                            //收到消息就取消监听
                            chat.removeMessageListener(this);
                            listenerThinner.processMessage(element, message, chat);
                        }
                    }
                }
            };
            chat.addMessageListener(messageListener);
            try {
                chat.sendMessage(content);
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 给服务器 发送消息,并设置回调函数,处理相应回调
     * @param toUser
     * @param content
     * @param dataGotTag
     * @param callback
     */
    public void sendMessage(String toUser, String content,
                            final String dataGotTag,
                            final NetworkCallbacks.ChatMessageListener callback) {
        synchronized (this) {
//            ChatManager chatManager = BaseXmppManager.getChatManager();
            if (!BaseXmppManager.getConnection().isAuthenticated()) {

                Utils.showToast("请在网络连接后重试");
                return;
            }
            String userJID = toUser + "@" +
                    BaseXmppManager.getConnection().getServiceName();

            Chat chat;
            if (chatMap.containsKey(userJID)) {
                chat = chatMap.get(userJID);
            } else {
                chat = chatManager.createChat(userJID, null);
                chatMap.put(userJID, chat);
            }
            //TODO 没有处理相同chat添加了相同messageListener的情况
            MessageListener messageListener = new MessageListener() {
                @Override
                public void processMessage(Chat chat, Message message) {
                    if (message.getType().toString().equals("chat")) {
                        Element element = XmlParser.parse(message.getBody());
                        if (element != null && dataGotTag.equals(element.getProperty("type"))) {

                            //收到消息就取消监听
                            chat.removeMessageListener(this);

                            boolean isSuccess = element.getBody() != null &&
                                    !element.getBody().equals("");
                            callback.gotMessage(element, message, chat, isSuccess);
                        }
                    }
                }
            };
            chat.addMessageListener(messageListener);
            try {
                chat.sendMessage(content);
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
    }
}
