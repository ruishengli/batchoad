package com.madao.net.model;

public class RespMsg {
    private String mDstAddress;     // Address of msg
    private String mRequestType;    // type of msg
    private String mStrData;
    private int    mTag;            // tag of msg
    private int    mRespCode;
    private String mReason;

    public RespMsg() {
        this.mTag = -1;
        this.mRespCode = -1;
    }
    /**
     * @return the mDstAddress
     */
    public String getDstAddress() {
        return mDstAddress;
    }
    /**
     * @param dstAddress the mDstAddress to set
     */
    public void setDstAddress(String dstAddress) {
        this.mDstAddress = dstAddress;
    }
    /**
     * @return the mRequestType
     */
    public String getRequestType() {
        return mRequestType;
    }
    /**
     * @param requestType the mRequestType to set
     */
    public void setRequestType(String requestType) {
        this.mRequestType = requestType;
    }
    
    /**
     * @return the mTag
     */
    public int getTag() {
        return mTag;
    }
    /**
     * @param mTag the mTag to set
     */
    public void setTag(int mTag) {
        this.mTag = mTag;
    }
    /**
     * @return the mRespCode
     */
    public int getRespCode() {
        return mRespCode;
    }
    /**
     * @param respCode the mRespCode to set
     */
    public void setRespCode(int respCode) {
        this.mRespCode = respCode;
    }
    /**
     * @return the mReason
     */
    public String getReason() {
        return mReason;
    }
    /**
     * @param reason the mReason to set
     */
    public void setReason(String reason) {
        this.mReason = reason;
    }
    /**
     * @return the mStrData
     */
    public String getStrData() {
        return mStrData;
    }
    /**
     * @param strData the mStrData to set
     */
    public void setStrData(String strData) {
        this.mStrData = strData;
    }
}

