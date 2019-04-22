package com.zhengyuan.baselib.network;

import com.zhengyuan.baselib.listener.NetworkCallbacks;
import com.zhengyuan.baselib.utils.xml.Element;
import com.zhengyuan.baselib.xmpp.ChatUtils;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;

/**
 * Created by zy on 2017/11/13.
 */

public enum  CommonDataObtainer {

    INSTANCE;

    /**
     * 向服务器发送请求获取所有用户的员工编号和姓名
     * @param callback 回调函数
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

    /**
     * 向服务器发送请求获取所有用户的员工编号和姓名
     */
    public void getMaterialNameByNumber(final NetworkCallbacks.SimpleDataCallback callback,
                                        String materialNumber) {

        Element element = new Element("mybody");
        element.addProperty("type", "requestMaterialNameByNumber");
        element.addProperty("materialNumber", materialNumber);

        ChatUtils.INSTANCE.sendMessage("iqreceiver", element.toString(), "returnMaterialNameByNumber",
                new NetworkCallbacks.MessageListenerThinner() {
                    @Override
                    public void processMessage(Element element, Message message, Chat chat) {

                        boolean isSuccess = element.getBody() != null &&
                                !element.getBody().equals("");

                        callback.onFinish(isSuccess, element.getProperty("msg"),
                                element.getProperty("result"));
                    }
                });
    }
}
