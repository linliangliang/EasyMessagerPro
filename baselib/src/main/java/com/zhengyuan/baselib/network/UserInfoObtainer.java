package com.zhengyuan.baselib.network;

import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.listener.NetworkCallbacks;
import com.zhengyuan.baselib.utils.xml.Element;
import com.zhengyuan.baselib.xmpp.BaseXmppManager;
import com.zhengyuan.baselib.xmpp.ChatUtils;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

/**
 * Created by zy on 2017/12/4.
 */

public enum UserInfoObtainer {

    INSTANCE;

    public void getUserInfo(String userId, final NetworkCallbacks.SimpleDataCallback callback) {

        Element element = new Element();
        element.setType( "requestMessage");
        element.addProperty("describe", "getUserInfo");
        element.setBody(userId);

        ChatUtils.INSTANCE.sendMessage(Constants.CHAT_TO_USER, element.toString(),
                element.getType(), new NetworkCallbacks.ChatMessageListener() {
                    @Override
                    public void gotMessage(Element element, Message message, Chat chat, boolean isSuccess) {

                        if (element.getProperty("describe").equals("returnUserInfo")) {
                            callback.onFinish(isSuccess, "", element);
                        }
                    }
                });
    }

    /**
     * 向服务器发送请求获取所有用户的员工编号和姓名
     */
    public void getAllUser(final NetworkCallbacks.SimpleDataCallback callback) {

        Element element = new Element("mybody");
        element.addProperty("type", "requestAllUser");

        ChatUtils.INSTANCE.sendMessage("iqreceiver", element.toString(), "returnAllUser",
                new NetworkCallbacks.MessageListenerThinner() {
                    @Override
                    public void processMessage(Element element, Message message, Chat chat) {

                        boolean isSuccess = element.getBody() != null &&
                                !element.getBody().equals("");

                        callback.onFinish(isSuccess, "", element.getBody());
                    }
                });
    }
}
