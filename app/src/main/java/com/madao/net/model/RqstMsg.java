package com.madao.net.model;

import android.text.TextUtils;


import com.madao.net.dataparser.DataParserHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RqstMsg {

    public static final String UPLOAD_FILE_TYPE_PIC = "pic";
    public static final String UPLOAD_FILE_TYPE_GPX = "gpx";
    public static final String UPLOAD_FILE_TYPE_GPX_PIC = "gpxpic";

    private String mDstAddress;     // Address of msg
    private String mRqstType;
    private int mTag;            // tag of msg
    private int mTimeOut;        // get data from server time out ms
    private int mMsgType;        // type of request message
    private String mData;           // data of message which need to send
    private String mFileName;  //单文件，兼容已经代码
    private Map<String, String> mMutilFilePath;

    private String version = "1.2.1";
    private String clientVersion = "118";
    private int platform = 0;

    public void addFilePath(String fileType, String filePath) {
        if (mMutilFilePath == null) {
            mMutilFilePath = new HashMap<String, String>();
        }
        mMutilFilePath.put(fileType, filePath);
    }

    public Map<String, String> getMutilFilePath() {
        return mMutilFilePath;
    }

    public void setMutilFilePath(Map<String, String> mMutilFileName) {
        this.mMutilFilePath = mMutilFileName;
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

    public RqstMsg() {
        mTag = -1;
        mTimeOut = 30000;
        version = "1.2.0";
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
        mDstAddress = dstAddress;
    }

    /**
     * @return the mTag
     */
    public int getTag() {
        return mTag;
    }

    /**
     * @param tag the mTag to set
     */
    public void setTag(int tag) {
        mTag = tag;
    }

    /**
     * @return the mTimeOut
     */
    public int getTimeOut() {
        return mTimeOut;
    }

    /**
     * @param timeout the mTimeOut to set
     */
    public void setTimeOut(int timeout) {
        mTimeOut = timeout;
    }

    /**
     * @return the mMsgType
     */
    public int getMsgType() {
        return mMsgType;
    }

    /**
     * @param msgType the mMsgType to set
     */
    public void setMsgType(int msgType) {
        mMsgType = msgType;
    }

    public String getRqstType() {
        return mRqstType;
    }

    public void setRqstType(String rqstType) {
        mRqstType = rqstType;
    }

    public String getData() {
        return mData;
    }

    public void setData(String data) {
        mData = data;
    }

    public String getVersion() {
        return version;
    }

    public String toServiceString(Object obj) {
        String data = null;
        if (isValidRqst() && obj != null) {
            ServiceHeader header = this.initMsgHeader();
            ServiceContent<ServiceHeader, Object> buss = new ServiceContent<ServiceHeader, Object>(header, obj);
            data = DataParserHelper.parser(buss);
        }

        return data;
    }

    public boolean isValidRqst() {
        if (getTag() <= 0 ||
                TextUtils.isEmpty(getDstAddress()) ||
                TextUtils.isEmpty(getRqstType())) {
            return false;
        } else {
            return true;
        }
    }

    public ServiceHeader initMsgHeader() {
        ServiceHeader header = new ServiceHeader();
        header.setBusiness(this.getRqstType());
        header.setSquence(this.getTag());
        header.setVersion(this.getVersion());
        header.setPlatform(this.getPlatform());
        header.setClientVersion(this.clientVersion);

//        String key = "A11808E7E70C08BBA8EBFD64E4EB456E";

//         Logs.e("TEST", "key:" + key + ",nonce:" + ne + ",signature:" + signature);
        return header;
    }


    public static String[] stringSort(String[] s) {
        List<String> list = new ArrayList<>(s.length);
        for (int i = 0; i < s.length; i++) {
            list.add(s[i]);
        }
        Collections.sort(list);
        return list.toArray(s);
    }

    public static String arrayConvertStr(String[] s) {
        if (s == null || s.length <= 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        int length = s.length;
        for (int i = 0; i < length; i++) {
            sb.append(s[i]);
        }

        return sb.toString();
    }

    public String ne(long timestamp) {
        return UUID.randomUUID().toString() + timestamp;
    }

    public String getFile() {
        return mFileName;
    }

    public void setFile(String mFile) {
        this.mFileName = mFile;
    }
}
