package com.zhengyuan.easymessengerpro.entity;

public class WorkListScanSendModel {
	public String materiel;
	public String requirement;
	public String overplus;
	public String storeIssue;
	public ScanStatus status;
	private String statusString;
	
	public enum ScanStatus {
		SCANNING, SUCCESS, FAILED
	}

	public String getStatusString() {

		
		if (status==null||status.equals("")) {
			statusString = "状态";
		}else{
		switch (status) {
		case SCANNING:
			statusString = "处理中...";
			break;

		case SUCCESS:
			statusString = "处理成功";
			break;

		case FAILED:
			statusString = "处理失败";
			break;
		}}

		return statusString;
	}

	public WorkListScanSendModel(String requirement, ScanStatus status) {
		super();
		this.requirement = requirement;
		this.status = status;
	}
	
	public WorkListScanSendModel(String materiel,String requirement,String overplus,String storeIssue, String statusString) {
		super();
		this.materiel = materiel;
		this.requirement = requirement;
		this.overplus = overplus;
		this.storeIssue = storeIssue;
		this.statusString = statusString;
	}
}
