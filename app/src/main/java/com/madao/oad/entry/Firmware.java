package com.madao.oad.entry;

import java.io.Serializable;

/**
 * Created by or on 2016/1/8.
 */
public class Firmware implements Serializable{
    private String descriptions;

    private String imgUrl;
    private String versionName;
    private int version;

    public Firmware clone() {
        Firmware fw = new Firmware();
        fw.setDescriptions(descriptions);
        fw.setVersion(version);
        fw.setVersionName(versionName);
        fw.setImgUrl(imgUrl);
        return fw;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }

    public String getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(String descriptions) {
        this.descriptions = descriptions;
    }


    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

}
