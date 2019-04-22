package com.zhengyuan.easymessengerpro.xmpp;

import android.content.Context;
import android.util.Log;

import com.zhengyuan.baselib.constants.StaticVariable;
import com.zhengyuan.baselib.xmpp.util.XmppReliableListener;

import org.jivesoftware.smack.packet.Message;

public class APPReliableListener extends XmppReliableListener {

    private Context context = null;

    public APPReliableListener() {

    }

    public APPReliableListener(Context context) {
        this.context = context;
    }

    @Override
    public void sendStatusListener(boolean status, Message message) {

        /**
         * 发送是否成功需要做相应的操作，每个message不同导致操作不同，需要对message进行判断，可以根据xmppmanager
         * */
        Log.e("消息：" + message.getPacketID() + " Send", status ? "Success" : "Failed");

        if (StaticVariable.handler != null && StaticVariable.inFormClient) {
            Log.v("inFormClient" + status, "单聊消息反馈");
            Object args[] = new Object[]{status, message};
            Log.v("inFormClient221", "");
            android.os.Message msg = StaticVariable.handler.obtainMessage();
            msg.what = 8;
            msg.obj = args;
            msg.sendToTarget();
        } else if (StaticVariable.handler != null
                && StaticVariable.inGroupClient) {
            Log.v("inGroupClient" + status, message.getBody());
            Object args[] = new Object[]{status, message};
            Log.v("inGroupClient221", "");
            android.os.Message msg = StaticVariable.handler.obtainMessage();
            Log.v("inGroupClient222", "");
            msg.what = 8;
            msg.obj = args;
            Log.v("inGroupClient223", "");
            msg.sendToTarget();
            Log.v("inGroupClient224", "");
        }
    }
}
