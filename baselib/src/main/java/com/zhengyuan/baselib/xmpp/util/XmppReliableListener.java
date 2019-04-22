package com.zhengyuan.baselib.xmpp.util;

import org.jivesoftware.smack.packet.Message;

/*
 * 发送状态回调函数
 * 
 * @author 徐兵
 */

//在应用层实例化，并重写方法
//在Xmpp协议层调用，以返回是否发送成功的状态

public abstract class XmppReliableListener {

	public abstract void sendStatusListener(boolean status,Message message);
	
}
