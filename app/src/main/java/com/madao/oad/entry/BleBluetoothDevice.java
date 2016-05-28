package com.madao.oad.entry;

import java.io.Serializable;

/**
 * Created by or on 2015/12/31.
 */
public class BleBluetoothDevice implements Serializable {

    private String mAddress;
    private String mName;
    private String mSerialId;
    private int firmwareVersion;
    private int hardwareVersion;


    public int getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(int hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public int getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(int firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getSerialId() {
        return mSerialId;
    }

    public void setSerialId(String serialId) {
        this.mSerialId = serialId;
    }

    private BleBluetoothDevice() {
    }


    public BleBluetoothDevice clone() {
        BleBluetoothDevice device = new BleBluetoothDevice();
        device.setSerialId(mSerialId);
        device.setName(mName);
        device.setAddress(mAddress);
        device.setHardwareVersion(hardwareVersion);
        device.setFirmwareVersion(firmwareVersion);
        return device;
    }
    public BleBluetoothDevice(String address, String name, String serialId, int firmwareVersion) {
        this.mAddress = address;
        this.mName = name;
        this.mSerialId = serialId;
        this.firmwareVersion = firmwareVersion;
    }


    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String mAddress) {
        this.mAddress = mAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BleBluetoothDevice that = (BleBluetoothDevice) o;

        return mAddress.equals(that.mAddress);

    }

    @Override
    public int hashCode() {
        return mAddress.hashCode();
    }
}
