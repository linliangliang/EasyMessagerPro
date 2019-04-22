package com.zhengyuan.baselib.xmpp.util;

import android.util.Log;

import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.xmpp.BaseXmppManager;
import com.zhengyuan.baselib.xmpp.interfaces.IXmppReliable;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import com.zhengyuan.baselib.constants.Constants;

import com.zhengyuan.baselib.xmpp.db.MessageDAO;
import com.zhengyuan.baselib.xmpp.db.ReliableDAO;
import com.zhengyuan.baselib.xmpp.db.ReliableDAO.UnackMessage;
import com.zhengyuan.baselib.utils.xml.Element;
import com.zhengyuan.baselib.utils.xml.XmlParser;

/*
 * 可靠通讯，包括处理包与应答包
 * 
 * 接收到消息之后，进行应答
 * 并维护消息队列
 * 
 * @author 徐兵
 * 
 * 其他：
 * Apache 下消息队列：activemq
 */

public class XmppReliable implements IXmppReliable {

    String className = "XmppReliable";

    Semaphore numRetryMessage = new Semaphore(0); //理发师模型的信号量
    Semaphore sleep = new Semaphore(0);

    XmppReliableListener sendReliableListener;

    static XmppReliable xmppReliable;

    private Map<String, Message> messageMap = new HashMap<String, Message>();

    /**
     * 从数据库查询
     * 1：时间如果超过4次重发间隔，停止重发
     * 2：时间如果超过5次重发间隔，通知应用层，发送失败，并不再尝试重新发送！
     */
    private int timeout_single = 1000;
    private int time_end = timeout_single * 4;
    private int time_faild = timeout_single * 5;

    private XmppReliable() {

    }

//	public XmppReliable(XmppReliableListener reliableListener)//构造函数
//	{
//		this.sendReliableListener = reliableListener;
//		//启动线程
//		//new Thread(new SendUnackMessage()).start();
//	}

    public void init(XmppReliableListener reliableListener) {
        this.sendReliableListener = reliableListener;
        //启动线程
        SendUnackMessage SendUnackThread = new SendUnackMessage(this, numRetryMessage, sleep);
        SendUnackThread.start();
    }

    public static XmppReliable getInstance() {
        if (xmppReliable == null) {
            xmppReliable = new XmppReliable();
        }
        return xmppReliable;
    }

    public void processAckMessage(Message message) {
        if (message.getBody().contains("ack")) {
            getAnswerMessage(message.getPacketID());
            statusCallback(true, message);

            Log.v("rec_ack", message.getPacketID());
        }
    }

    public void getAnswerMessage(String messageID) {
        removeMessage(messageID);//移除队列消息
        ReliableDAO rd = new ReliableDAO();
        rd.openDb(EMProApplicationDelegate.applicationContext);
        rd.delete(messageID);//更新数据库	ACK字段
        rd.closeDb();

    }



    public void removeMessage(String messageID) {
        messageMap.remove(messageID);
    }

    ///////////////////发送消息/////////////////////////////////
    @Override
    public void addMessage(String messageID, Message message) {
        Message mtmp = new Message();
        //深度复制
        if (message.getBody() != null)
            mtmp.setBody(new String(message.getBody()));
        if (message.getFrom() != null)
            mtmp.setFrom(new String(message.getFrom()));
        else
            mtmp.setFrom(EMProApplicationDelegate.userInfo.getUserId());
        if (message.getExtensions() != null)
            message.addExtensions(message.getExtensions());
        mtmp.setTo(new String(message.getTo()));
        mtmp.setPacketID(new String(message.getPacketID()));
        mtmp.setType(Message.Type.valueOf("" + (message.getType())));
        messageMap.put(messageID, mtmp);
        ReliableDAO rd = new ReliableDAO();
        rd.openDb(EMProApplicationDelegate.applicationContext);
        rd.instert(messageID);//保存未成功发送的数据
        rd.closeDb();

        numRetryMessage.release();//信号量 ++
        if (numRetryMessage.availablePermits() == 1)//从0变为1时，说明有新消息需要重发，叫醒理发师
            sleep.release();
    }

    //保存接收到得消息到rec_message表
    @Override
    public void addRecMessage(String messageID, Message message) {
        Message mtmp = new Message();
        //深度复制
        if (message.getBody() != null)
            mtmp.setBody(new String(message.getBody()));
        if (message.getFrom() != null)
            mtmp.setFrom(new String(message.getFrom()));
        else
            mtmp.setFrom(EMProApplicationDelegate.userInfo.getUserId());
        if (message.getExtensions() != null)
            message.addExtensions(message.getExtensions());
        mtmp.setTo(new String(message.getTo()));
        mtmp.setPacketID(new String(message.getPacketID()));
        mtmp.setType(Message.Type.valueOf("" + (message.getType())));
        messageMap.put(messageID, mtmp);

        //修改，如果是视频信息，不存数据库
        Element element = XmlParser.parse(message.getBody());

        MessageDAO mesdao = new MessageDAO();
//		mesdao.openDb(Constants.contexts.get(Constants.contexts.size()-1));
        if (!element.getElementName().equals("video"))
            mesdao.insertPeerMessage(mtmp);
//		mesdao.closeDb();
    }

    //发送失败，尝试重新发送
    public void retrySend(UnackMessage message) {
        BaseXmppManager.getConnection().sendRetryPacket(message);
        updateMessageCount(message.getPacketID());
        Log.v("retry:" + message.getPacketID(), message.getCount() + "");
    }

    /*
     * 在消息队列中管理所有发送消息
     * 发送失败计数+1
     */
    public void updateMessageCount(String messageid) {
        ReliableDAO rd = new ReliableDAO();
        rd.openDb(EMProApplicationDelegate.applicationContext);
        rd.updateCount(messageid);
        rd.closeDb();
    }

    public synchronized List<UnackMessage> retryMessage() {
        List<UnackMessage> list = new ArrayList<UnackMessage>();

        ReliableDAO rd = new ReliableDAO();
        rd.openDb(EMProApplicationDelegate.applicationContext);
        list = rd.getRetryMessage(6);
        rd.closeDb();

        for (UnackMessage m : list) {
            //按照等待时间远大于发送时间的原则
            //
            if (((new Date()).getTime() - m.getTimestamp()) / 1000 > m.getCount()) {
                if (m.getCount() < 5) {
                    retrySend(m);
                    try {
                        Thread.sleep(m.getCount() * 10 * 100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (m.getCount() > 4 && ((new Date()).getTime() - m.getTimestamp()) >= m.getCount()) {
                    statusCallback(false, m);
                    updateMessageCount(m.getPacketID());
                }
            }
        }
        return list;
    }

    public void statusCallback(Boolean status, Message message) {
        try {
            numRetryMessage.tryAcquire(); //尝试信号量--  当信号量已为0时，执行失败
        } catch (Exception e) {

        }
        sendReliableListener.sendStatusListener(status, message);
    }

    //为了省电，使用理发师模型，解决同步问题，无需重发消息时休眠
    public static class SendUnackMessage extends Thread {
        private XmppReliable xmppReliable;

        Semaphore numRetryMessage;
        Semaphore sleep;

        public SendUnackMessage(XmppReliable xmpp, Semaphore numRetryMessage, Semaphore sleep) {
            this.numRetryMessage = numRetryMessage;
            this.xmppReliable = xmpp;
            this.sleep = sleep;
        }

        @Override
        public void run() {
            while (true) {
//				while(!(numRetryMessage>0))//如果不存在待发送的消息队列，则挂起，并循环检查
//				{
//					synchronized (numRetryMessage) {
//						try {
//							numRetryMessage.wait();
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					}
//				}
                try {
                    sleep.acquire();//如果有数据需要发送,理发师被叫醒
                    Thread.sleep(1000);//第一次发完之后，等待一段时间，防止立刻重发，引起2次同时发送，即使收到ack也会发2次，消耗不必要的带宽
                    while (numRetryMessage.availablePermits() > 0)//存在多个需要发送的数据
                    {
                        xmppReliable.retryMessage();

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public Message string2message(String str) {
        XmlPullParser parser = null;
        Message message = new Message();
        try {
            Reader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(str.getBytes()), "UTF-8"));
            parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(reader);
            message = (Message) PacketParserUtils.parseMessage(parser);
        } catch (Exception ex) {

        }
        return message;
    }
}
