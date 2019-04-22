package com.zhengyuan.baselib.xmpp;

import org.jivesoftware.smack.packet.IQ;

public class MucMembersIQ extends IQ {

    public static final String ROOT_ELEMENT = "MucMembers";
    public static final String NAMESPACE = "zydq";
    public static String roomName = "";

    public static String getRoomName() {
        return roomName;
    }

    public static void setRoomName(String roomName) {
        MucMembersIQ.roomName = roomName;
    }


    public MucMembersIQ(String roomname) {
        super();
        MucMembersIQ.roomName = roomname;
    }

    public String getChildElementXML() {

        StringBuilder buff = new StringBuilder();
//		buff.append("<"+ELEMENT +"xmlns=\""+NAMESPACE+"\">");
//		if(getType()==IQ.Type.GET)
//		{
//			buff.append("<url>"+getUrlStr()+"</url>");
//			buff.append("<json>"+getJson()+"</json>");
//			buff.append(getExtensionsXML());
//		}
//		buff.append("</"+ELEMENT+">");
        buff.append(this.toXML());
        return buff.toString();
    }

    public String toXML() {
        StringBuilder buff = new StringBuilder();
        buff.append("<iq type=\"get\" id=\"zydq\">");
        buff.append("<" + ROOT_ELEMENT + " xmlns=\"" + NAMESPACE + "\">");
        buff.append("<roomName>" + roomName + "</roomName>");//信息中心@conference.xxzx
        buff.append("</" + ROOT_ELEMENT + ">");
        buff.append("</iq>");
        return buff.toString();
    }

}
