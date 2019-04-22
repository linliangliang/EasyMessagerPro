package com.zhengyuan.baselib.listener;

import com.zhengyuan.baselib.utils.xml.Element;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;

/**
 * 网络回调接口
 * Created by gpsts on 17-6-16.
 */

public interface NetworkCallbacks {

    interface SimpleCallback {
        void onFinish(boolean isSuccess, String msg);
    }

    interface SimpleDataCallback {
        void onFinish(boolean isSuccess, String msg, Object data);
    }

    interface MessageListenerThinner {
        void processMessage(Element element, Message message, Chat chat);
    }

    interface ChatMessageListener {
        void gotMessage(Element element, Message message, Chat chat, boolean isSuccess);
    }
}
