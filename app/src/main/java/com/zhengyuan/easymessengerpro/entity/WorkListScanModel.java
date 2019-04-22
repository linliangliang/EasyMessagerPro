package com.zhengyuan.easymessengerpro.entity;

public class WorkListScanModel {

	public enum ScanStatus {
		SCANNING, SUCCESS, FAILED
	}
	
	public String workListNumber;
	public String materielCode;
	public ScanStatus status;
	private String statusString;
	
	public String getStatusString() {
		
		String statusString = null;
		switch(status) {
		case SCANNING:
			statusString = "处理中...";
			break;
			
		case SUCCESS:
			statusString = "处理成功";
			break;
			
		case FAILED:
			statusString = "处理失败";
			break;
		}
		
		return statusString;
	}
	
	public WorkListScanModel(String workListNumber, 
			String materielCode,
			ScanStatus status) {
		super();
		this.workListNumber = workListNumber;
		this.materielCode = materielCode;
		this.status = status;
	}
}
