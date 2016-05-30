package com.madao.net.model;

public class ServiceHeader {
    private String business;
    private int squence;
    private String version;
    private String clientVersion;
    private int platform;
    private long timestamp;
    private String nonce;
    private String signature;


    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public int getPlatform() {
        return platform;
    }

    public void setPlatform(int platform) {
        this.platform = platform;
    }

    public ServiceHeader() {
        this.setSquence(0);
    }

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String mBusiness) {
        this.business = mBusiness;
    }

    public int getSquence() {
        return squence;
    }

    public void setSquence(int mSquence) {
        this.squence = mSquence;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String mVersion) {
        this.version = mVersion;
    }

}
