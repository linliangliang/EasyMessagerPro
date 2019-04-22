package com.zhengyuan.baselib.xmpp;
import org.jivesoftware.smack.packet.IQ;

public  class MucQueryIQ extends IQ {

	public static final String ELEMENT="iq";
	public static final String NAMESPACE="zydq";
	
	
	private String urlStr=null;
	private String json=null;
	

	public String getUrlStr() {
		return urlStr;
	}

	public void setUrlStr(String urlStr) {
		this.urlStr = urlStr;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public String getChildElementXML() {

		StringBuilder buff=new StringBuilder();
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
	public String toXML()
	{
		StringBuilder buff=new StringBuilder();
		buff.append("<iq type=\"get\" id=\"zydq\">");
		buff.append("<muc xmlns=\"zydq\">");
		buff.append("<room xmlns=\"\" account=\"xbb@xxzx\">shizhikang@conference.xxzx</room>");
		buff.append("</muc>");
		buff.append("</iq>");
		return buff.toString();
	}

}
