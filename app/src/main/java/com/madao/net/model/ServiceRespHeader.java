package com.madao.net.model;

public class ServiceRespHeader {
	private int    returnCode;
	private int    sequence;
	private String business;
	private String version;
	private String returnMsg;

	public int getReturnCode() {
		return returnCode;
	}
	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}
	public int getSequence() {
		return sequence;
	}
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	public String getBusiness() {
		return business;
	}
	public void setBusiness(String business) {
		this.business = business;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getReturnMsg() {
		return returnMsg;
	}
	public void setReturnMsg(String returnMsg) {
		this.returnMsg = returnMsg;
	}

	public void copy(ServiceRespHeader obj) {
		if (null == obj)
			return;

        this.setBusiness(obj.getBusiness());
        this.setReturnCode(obj.getReturnCode());
        this.setReturnMsg(obj.getReturnMsg());
        this.setSequence(obj.getSequence());
        this.setVersion(obj.getVersion());
	}

}
