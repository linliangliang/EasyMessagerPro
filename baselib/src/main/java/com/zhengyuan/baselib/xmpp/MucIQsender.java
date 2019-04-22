package com.zhengyuan.baselib.xmpp;

import android.util.Log;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;


/**
 * 发送IQ包，请求已加入群组
 *
 * @author 徐兵
 */

public class MucIQsender {


    public static void run() throws Exception {
        XMPPConnection con = BaseXmppManager.getConnection();//getInstance().getConnection();
        PacketCollector collector = con.createPacketCollector(new PacketFilter() {
            @Override
            public boolean accept(Packet p) {
                if (p instanceof MucQueryIQ) {
                    System.out.println("发送数据:" + p.toXML());
                    return true;
                }
                return false;
            }
        });

        MucQueryIQ iq = new MucQueryIQ();
        iq.setType(IQ.Type.GET);

        iq.setUrlStr("xu");
        iq.setJson("bing");
        con.sendPacket(iq);

        Log.v("iq----", iq.toXML());

        Packet packet = collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
        collector.cancel();
        if (packet == null)
            System.out.println("packet = 1111");

//		con.disconnect();

    }

    public static void getMucList() throws Exception {
        XMPPConnection con = BaseXmppManager.getConnection();//getInstance().getConnection();
        PacketCollector collector = con.createPacketCollector(new PacketFilter() {
            @Override
            public boolean accept(Packet p) {
                if (p instanceof MucQueryIQ) {
                    System.out.println("发送数据:" + p.toXML());
                    return true;
                }
                return false;
            }
        });

        MucQueryIQ iq = new MucQueryIQ();
        iq.setType(IQ.Type.GET);

        iq.setUrlStr("xu");
        iq.setJson("bing");
        con.sendPacket(iq);

        Log.v("iq----", iq.toXML());

        Packet packet = collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
        collector.cancel();
        if (packet == null)
            System.out.println("packet = 1111");

//		con.disconnect();

    }


    public static void getMucMembers(String roomname) throws Exception {
        MUCPacketExtensionProvider.getResultSet().clear();//清除旧缓存

        XMPPConnection con = BaseXmppManager.getConnection();//getInstance().getConnection();
        PacketCollector collector = con.createPacketCollector(new PacketFilter() {
            @Override
            public boolean accept(Packet p) {
                if (p instanceof MucMembersIQ) {
                    System.out.println("发送数据:" + p.toXML());
                    return true;
                }
                return false;
            }
        });

        MucMembersIQ iq = new MucMembersIQ(roomname);
        iq.setType(IQ.Type.GET);
        con.sendPacket(iq);

        Log.v("iq----", iq.toXML());
        Packet packet = collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
        collector.cancel();
        if (packet == null)
            System.out.println("packet = 1111");
    }
}
