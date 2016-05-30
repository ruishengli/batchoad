package com.madao.util;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import com.ble.sdk.Conversion;
import com.madao.oad.entry.BleBluetoothDevice;

import java.util.Arrays;

/**
 * desc DeviceConvert
 *
 * @author: or
 * @since: on 2016/5/28.
 */
public class DeviceParseUtil {

    private static final String TAG = DeviceParseUtil.class.getSimpleName();
    private static final String DEFAULT_NAME = "LEQI";
    private static final String DEFAULT_BOOT = "BOOT";
    private static final String DEFAULT_MADAO = "MADAO";

    static final byte[] FILTER_BROADCAST_NAME_1 = new byte[]{77, 65, 68, 65, 79, 32, 76, 69, 81, 73};
    static final byte[] FILTER_BROADCAST_NAME_2 = new byte[]{77, 65, 68, 65, 79, 32, 66, 79, 79, 84};

    //E0:E5:CF:C2:90:3D
    //5C:F8:21:D2:0E:67

    public static boolean isOwnDevice(String address) {
        if(address.equals("E0:E5:CF:C2:90:3D") || address.equals("5C:F8:21:D2:0E:67")) {
            return true;
        }
        return false;
    }
    public static boolean isOwnDevice(byte[] scanRecord) {
        boolean isOwn = false;

        int nameStartIndex = 15;
        int nameLength = 10;

        int bootNameStartIndex = 9;


        if (scanRecord != null && scanRecord.length > 0) {
            try {
                byte[] deviceName = Conversion.getBytes(scanRecord, nameStartIndex, nameLength);
                if (Arrays.equals(FILTER_BROADCAST_NAME_1, deviceName) || Arrays.equals(FILTER_BROADCAST_NAME_2, deviceName)) {
                    return true;
                }

                deviceName = Conversion.getBytes(scanRecord, bootNameStartIndex, nameLength);
                if (Arrays.equals(FILTER_BROADCAST_NAME_2, deviceName)) {
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return isOwn;
    }

    public static BleBluetoothDevice convertDevice(BluetoothDevice connectDevice, byte[] scanRecord) {
        String name = connectDevice.getName();
        Logs.e(TAG, "convertDevice:" + name);
        if (TextUtils.isEmpty(name) || name.indexOf(DEFAULT_MADAO) >= 0 || name.indexOf(DEFAULT_BOOT) >= 0) {
            name = DEFAULT_NAME;
        }

        int version = getFirmwareVersion(scanRecord);
        if (version == 0) {
            return null;
        }

        if (connectDevice.getName().indexOf(DEFAULT_BOOT) >= 0) {//
            version = 0;
        }

        name = name + "_" + converSerial(connectDevice.getAddress()) ;
        if(version > 0) {
            name += "_v" + version;
        } else {
            name += "_疑似砖机" ;
        }
        return new BleBluetoothDevice(connectDevice.getAddress(),name , "", version);
    }


    private static String converSerial(String address) {
        if (!TextUtils.isEmpty(address)) {
            address = address.replace(":", "");
            if (address.length() > 4) {
                return address.substring(address.length() - 4);
            }
        }
        return address;
    }


    private static int getFirmwareVersion(byte[] scanRecord) {
        int firmwareVersionStartIndex = 32;
        int firmwareVersionLength = 3;
        if (scanRecord != null && scanRecord.length > 0) {
            try {
                byte[] bVersion = Conversion.getBytes(scanRecord, firmwareVersionStartIndex, firmwareVersionLength);
                int version = bVersion[0] * 100 + bVersion[1] * 10 + bVersion[2];

                return version;
            } catch (Exception e) {
            }
        }
        return 0;
    }
}
