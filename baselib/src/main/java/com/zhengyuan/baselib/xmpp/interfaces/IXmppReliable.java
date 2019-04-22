package com.zhengyuan.baselib.xmpp.interfaces;

import org.jivesoftware.smack.packet.Message;

/**
 * Created by zy on 2017/11/4.
 */

public interface IXmppReliable {

    void addMessage(String messageID, Message message);
    void addRecMessage(String messageID, Message message);

    void processAckMessage(Message message);
}
