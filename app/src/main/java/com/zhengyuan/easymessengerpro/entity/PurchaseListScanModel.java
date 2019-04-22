package com.zhengyuan.easymessengerpro.entity;

public class PurchaseListScanModel {
	
	public String purchaseListNumber;
	public WorkListScanModel.ScanStatus status;
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
	
	public PurchaseListScanModel(String workListNumber, WorkListScanModel.ScanStatus status) {
		super();
		this.purchaseListNumber = workListNumber;
		this.status = status;
	}
}
