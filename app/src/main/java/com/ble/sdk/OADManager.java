package com.ble.sdk;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;

import com.madao.oad.ServiceManager;
import com.madao.util.Logs;
import com.madao.util.UUIDUtils;

import java.util.Arrays;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)

public class OADManager {

    private static final String TAG = OADManager.class.getSimpleName();

    // Programming parameters
    public static final short OAD_CONN_INTERVAL = 12; //15 milliseconds
    public static final short OAD_SUPERVISION_TIMEOUT = 100; //1000 milliseconds
    public static final short OAD_SLAVE_LATENCY = 0;
    public static final int OAD_GATT_WRITE_TIMEOUT = 1500; //milliseconds

    private static OADManager mInstance;

    private boolean mOadIsRunning = false;

    public boolean oadRunning() {
        return mOadIsRunning;
    }

    public void resetOadStatus() {
        mOadIsRunning = false;
    }

    private OADManager() {
    }

    public static synchronized OADManager getInstance() {
        if (mInstance == null) {
            mInstance = new OADManager();
        }
        return mInstance;
    }

    public void onDestory() {
        mOadIsRunning = false;
        mInstance = null;
    }


    public boolean getTargetImageInfo(String address, int timeout) {

        boolean ok = setTargetImageEnableNotification(address, timeout);
       if (ok) {
            ok = writeTargetImage(address,(byte) 0, timeout);
        }

        if (ok) {
            ok = writeTargetImage(address, (byte) 1, timeout);
        }
        return ok;
    }

    private boolean setTargetImageEnableNotification(String address, int timeout) {
        boolean ok = enableTargetImageNotification(address, UUIDUtils.OAD_SERVICE, UUIDUtils.OAD_IMAGE_IDENTIFY);
        if (ok)
            ok = waitIdle(timeout);

        return ok;
    }


    private boolean enableTargetImageNotification(String deviceAddress, String service,
                                                  String characteristic) {
        IBle ble = ServiceManager.getInstance().getIBle();
        if (ble != null) {
            BleGattService gattService = ble.getService(deviceAddress, UUID.fromString(service));
            if (gattService != null) {
                BleGattCharacteristic mCharacteristic = gattService
                        .getCharacteristic(UUID.fromString(characteristic));
                if (mCharacteristic != null) {
                    return ble.setTargetImageEnableNotification(deviceAddress, mCharacteristic);
                }
            }
        }
        return false;
    }


    public boolean writeOad(String deviceAddress, String service, String characteristic,
                            byte[] value, boolean noResponse) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return false;
        }
        if (value == null) {
            Logs.d(TAG, "set value is null...");
            return false;
        }
        IBle ble = ServiceManager.getInstance().getIBle();
        if (ble != null) {
            BleGattService gattService = ble.getService(deviceAddress, UUID.fromString(service));
            if (gattService != null) {
                BleGattCharacteristic mCharacteristic = gattService
                        .getCharacteristic(UUID.fromString(characteristic));
                if (mCharacteristic != null) {
                    if (noResponse) {
                        mCharacteristic.getGattCharacteristic()
                                .setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                    }
                    mCharacteristic.setValue(value);
                    Logs.d(TAG, "write Oad characteristic " + characteristic + " value:" + Arrays.toString(value));
                    return ble.writeImageBlock(deviceAddress, mCharacteristic);
                }
            }
        }
        return false;
    }

    private boolean writeTargetImage(String address, byte value, int timeout) {
        byte[] v = new byte[1];
        v[0] = value;
        boolean ok = writeOad(address, UUIDUtils.OAD_SERVICE, UUIDUtils.OAD_IMAGE_IDENTIFY, v, false);
        ;
        if (ok)
            ok = waitIdle(timeout);

        return ok;
    }

    public boolean writeConnectionParams(String address, byte[] value) {
        return writeOad(address, UUIDUtils.CONN_CONTROL_SERVICE, UUIDUtils.CONN_PARAMS, value, true);
    }

    public boolean writeOadFlag(String address, int timeout) {
        boolean ok = writeOad(address, UUIDUtils.PARAMETER_SERVER, UUIDUtils.OAD_FLAG, new byte[]{Conversion.intTobyte1(1)}, false);
        if (ok)
            ok = waitIdle(timeout);

        if (ok) {
            mOadIsRunning = true;
        }
        return ok;
    }

    public boolean writePrepareImage(String address, byte[] value) {
        return writeOad(address, UUIDUtils.OAD_SERVICE, UUIDUtils.OAD_IMAGE_IDENTIFY, value, false);
    }

    public boolean writeImageBlock(String address, byte[] value) {
        return writeOad(address, UUIDUtils.OAD_SERVICE, UUIDUtils.OAD_BLOCK_REQUEST, value, true);
    }

    public boolean waitIdle(int timeout) {
        timeout /= 10;
        while (--timeout > 0) {
            boolean busy = getOadBusy();
            if (busy) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else
                break;
        }

        Logs.d(TAG, "waitIdle time:" + timeout);
        return timeout > 0;
    }

    public void resetOadBusy() {
        IBle ble = ServiceManager.getInstance().getIBle();
        if (ble != null) {
            ble.setOadNoBusy();
        }
    }

    private boolean getOadBusy() {
        IBle ble = ServiceManager.getInstance().getIBle();
        if (ble != null) {
            return ble.getOadBusy();
        }
        return false;
    }
}
