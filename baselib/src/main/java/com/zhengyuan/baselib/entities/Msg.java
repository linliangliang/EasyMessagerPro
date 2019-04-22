package com.zhengyuan.baselib.entities;

public class Msg{
	//modified 2017.11.8 增加字段mid和构造函数
	//modified 2017.11.9增加判断发送状态的标志位finSend
	public String userid;
	public String msg;
	public String date;
	public String from;

	public String mid;
	public int finSend=0;
	public Msg(String userid, String msg, String date, String from) {
		this.userid = userid;
		this.msg = msg;
		this.date = date;
		this.from = from;
	}
	public Msg(String userid, String msg, String date, String from,int finSend) {
		this.userid = userid;
		this.msg = msg;
		this.date = date;
		this.from = from;
		this.finSend=finSend;
	}
	public Msg(String mid,String userid, String msg, String date, String from) {
		this.mid=mid;
		this.userid = userid;
		this.msg = msg;
		this.date = date;
		this.from = from;
	}
	public Msg(String mid,String userid, String msg, String date, String from,int finSend) {
		this.mid=mid;
		this.userid = userid;
		this.msg = msg;
		this.date = date;
		this.from = from;
		this.finSend=finSend;
	}
	public synchronized final int getFinSend() {
		return finSend;
	}
	public synchronized final void setFinSend(int finSend) {
		this.finSend = finSend;
	}
	public synchronized final String getMid() {
		return mid;
	}
	public synchronized final void setMid(String mid) {
		this.mid = mid;
	}

	public synchronized final String getMsg() {
		return msg;
	}
	@Override
	public String toString() {
		return "Msg [userid=" + userid + ", msg=" + msg + ", date=" + date
				+ ", from=" + from + ", mid=" + mid + ", finSend=" + finSend
				+ "]";
	}
	
	
}
