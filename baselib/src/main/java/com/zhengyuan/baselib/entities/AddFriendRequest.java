package com.zhengyuan.baselib.entities;

/**
 * @author 9811
 *	加好友请求的类
 */
public class AddFriendRequest {
	private String uid;//当前用户的id(@前面部分)
	private String fid;//对方的id(@前面部分)
	private String nickname;
	private String time;
	private String reason;
	private int state;
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getFid() {
		return fid;
	}
	public void setFid(String fid) {
		this.fid = fid;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	@Override
	public String toString() {
		String string="";
		string+="uid="+uid;
		string+="#fid="+fid;
		string+="#nickname="+nickname;
		string+="#time="+time;
		string+="#reason="+reason;
		string+="#state="+state;
		return string;
	}
}
