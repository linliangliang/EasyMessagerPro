package com.zhengyuan.easymessengerpro.network;

import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.listener.NetworkCallbacks;
import com.zhengyuan.baselib.utils.xml.Element;
import com.zhengyuan.baselib.xmpp.ChatUtils;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;

/**
 * @author 林亮
 * @description:
 * @date :2019/2/3 8:55
 */

public enum DataObtainer {
    INSTANCE;

    private final static String TAG = "DataObtainer";

    /**
     * 从服务器获取用户修改密码的事件
     *
     * @param userId   请求信息的用户的id
     * @param callback 回调函数
     */
    public void getChangePasswordDate(String userId, final NetworkCallbacks.SimpleDataCallback callback) {

        Element element = new Element();
        element.addProperty("type", "requestGetChangePasswordDate");
        element.addProperty("userId", userId);

        ChatUtils.INSTANCE.sendMessage(Constants.CHAT_TO_USER, element.toString(),
                "returnGetChangePasswordDate", new NetworkCallbacks.MessageListenerThinner() {
                    @Override
                    public void processMessage(Element element, Message message, Chat chat) {
                        boolean isSuccess = element.getBody() != null &&
                                !element.getBody().equals("");
                        callback.onFinish(isSuccess, "", element.getProperty("result"));
                    }
                });

    }

    public void uploadChangePasswordDate(String userId, final NetworkCallbacks.SimpleDataCallback callback) {
        Element element = new Element();
        element.addProperty("type", "requestUploadChangePasswordDate");
        element.addProperty("userId", userId);

        ChatUtils.INSTANCE.sendMessage(Constants.CHAT_TO_USER, element.toString(), "returnUploadChangePasswordDate", new NetworkCallbacks.MessageListenerThinner() {
            @Override
            public void processMessage(Element element, Message message, Chat chat) {
                boolean isSuccess = element.getBody() != null &&
                        !element.getBody().equals("");
                callback.onFinish(isSuccess, "", element.getProperty("result"));
            }
        });
    }

    public void getServiceReleaseAppVersion(final NetworkCallbacks.SimpleDataCallback callback) {
        Element element = new Element("mybody");
        element.addProperty("type", "requestGetServiceAppVersion");
        element.addProperty("appType", "release");//获取发布版

        ChatUtils.INSTANCE.sendMessage(Constants.CHAT_TO_USER, element.toString(), "returnGetServiceAppVersion", new NetworkCallbacks.MessageListenerThinner() {
            @Override
            public void processMessage(Element element, Message message, Chat chat) {
                boolean isSuccess = element.getBody() != null &&
                        !element.getBody().equals("");
                callback.onFinish(isSuccess, "", element.getProperty("result"));
            }
        });
    }

    public void getServiceDebugAppVersion(final NetworkCallbacks.SimpleDataCallback callback) {
        Element element = new Element("mybody");
        element.addProperty("type", "requestGetServiceAppVersion");
        element.addProperty("appType", "debug");//获取调试

        ChatUtils.INSTANCE.sendMessage(Constants.CHAT_TO_USER, element.toString(), "returnGetServiceAppVersion", new NetworkCallbacks.MessageListenerThinner() {
            @Override
            public void processMessage(Element element, Message message, Chat chat) {
                boolean isSuccess = element.getBody() != null &&
                        !element.getBody().equals("");
                callback.onFinish(isSuccess, "", element.getProperty("result"));
            }
        });
    }

}
