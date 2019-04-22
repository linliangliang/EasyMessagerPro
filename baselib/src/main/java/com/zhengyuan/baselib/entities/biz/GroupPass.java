package com.zhengyuan.baselib.entities.biz;

public class GroupPass {

	private static GroupPass gp=null;
	private String groupid="empty";

	public String getGroupid() {
		return groupid;
	}
	public void setGroupid(String groupid) {
		this.groupid = groupid;
	}
	private GroupPass() {
		
	}
	public static GroupPass getInstance(){
		if(gp==null)
			gp=new GroupPass();
		return gp;
	}

	
	
	
	

}
