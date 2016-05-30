package com.madao.oad.presenter;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.ble.sdk.BleRequest;
import com.ble.sdk.BleService;
import com.ble.sdk.Conversion;
import com.ble.sdk.IBle;
import com.ble.sdk.OADManager;
import com.madao.OADApplication;
import com.madao.oad.ServiceManager;
import com.madao.oad.entry.ImgHdr;
import com.madao.oad.entry.ProgInfo;
import com.madao.util.DeviceParseUtil;
import com.madao.util.Logs;
import com.madao.util.UUIDUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * com.madao.oad.presenter
 *
 * @auth or
 * @sinced on 2016/5/29.
 */
public class BluetoothManager {

    public interface BluetoothCallback {
        void onFoundDevice(BluetoothDevice device, byte[] scanRecord);

        void onConnFailed(String address);

        void onConnSuccess(String address);

        void onProgressUpdate(int progress);

        void onOadResult(int code);
    }

    private static final String TAG = BluetoothManager.class.getSimpleName();
    private static final int DEFAULT_RECONN_COUNT = 4;

    private static final int FILE_BUFFER_SIZE = 0x40000;
    private static final int OAD_BLOCK_SIZE = 16;
    private static final int HAL_FLASH_WORD_SIZE = 4;
    private static final int OAD_BUFFER_SIZE = 2 + OAD_BLOCK_SIZE;
    private static final int OAD_IMG_HDR_SIZE = 8;
    private static final long TIMER_INTERVAL = 1000;

    private static final int SEND_INTERVAL = 20; // Milliseconds (make sure this
    private static final int BLOCKS_PER_CONNECTION = 4; // May sent up to four

    // Programming
    private final byte[] mFileBuffer = new byte[FILE_BUFFER_SIZE];
    private final byte[] mOadBuffer = new byte[OAD_BUFFER_SIZE];
    private BluetoothCallback mBluetoothCallback;

    private ImgHdr mFileImgHdr = new ImgHdr();
    private ProgInfo mProgInfo = new ProgInfo();
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    Handler mHandler = new Handler();
    private BluetoothDeviceReceiver mReceiver;

    public BluetoothManager(BluetoothCallback bluetoothCallback) {

        this.mBluetoothCallback = bluetoothCallback;
        mReceiver = new BluetoothDeviceReceiver();
        OADApplication.applicationContext.registerReceiver(mReceiver, BleService.getIntentFilter());
    }

    public void onDestroy() {
        OADApplication.applicationContext.unregisterReceiver(mReceiver);
        if (mTimerTask != null)
            mTimerTask.cancel();
        mTimer = null;
    }

    public boolean loadFile(String filepath) {
        boolean fSuccess = true;
        try {
            InputStream stream;
            File f = new File(filepath);
            stream = new FileInputStream(f);
            stream.read(mFileBuffer, 0, mFileBuffer.length);
            stream.close();
        } catch (IOException e) {
            return false;
        }

        mFileImgHdr.ver = Conversion.buildUint16(mFileBuffer[5], mFileBuffer[4]);
        mFileImgHdr.len = Conversion.buildInt(mFileBuffer[7], mFileBuffer[6]);
        mFileImgHdr.imgType = ((mFileImgHdr.ver & 1) == 1) ? 'B' : 'A';
        System.arraycopy(mFileBuffer, 8, mFileImgHdr.uid, 0, 4);
        return fSuccess;
    }




    public boolean bleEnabled() {
        return ServiceManager.getInstance().getIBle() == null ? false : true;
    }

    public void startScan() {
        if (ServiceManager.getInstance().getIBle() != null) {
            ServiceManager.getInstance().getIBle().startScan();
        }
    }

    public void stopScan() {
        if (ServiceManager.getInstance().getIBle() != null) {
            ServiceManager.getInstance().getIBle().stopScan();
        }
    }


    String mCurDeviceAddress;
    int mCurConnCount = 0;
    boolean mNormalConnSuccess = false;
    boolean mOadConnSuccess = false;
    //1:普通连接
    //2:oad 连接
    int mCurStatus;
    boolean oadSuccess;

    public void startOad(String address) {
        mCurStatus = 1;
        mCurDeviceAddress = address;
        mNormalConnSuccess = false;
        mOadConnSuccess = false;
        oadSuccess = false;
        connection(address);
    }

    private void connection(String address) {
        mCurConnCount++;
        boolean ret = ServiceManager.getInstance().getIBle().requestConnect(address, true);
        if (!ret) {
            if (mBluetoothCallback != null)
                mBluetoothCallback.onConnFailed(address);
        }
    }

    private class BluetoothDeviceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle extras = intent.getExtras();

            if (BleService.BLE_DEVICE_FOUND.equals(action)) { // 发现新设备
                byte[] scanRecor = intent.getByteArrayExtra(BleService.EXTRA_SCAN_RECORD);
                final BluetoothDevice device = extras.getParcelable(BleService.EXTRA_DEVICE);

                Logs.d(TAG, "device " + device.getAddress() + ",name:" + device.getName()
                        + " scanRecor :" + Arrays.toString(scanRecor) + " servie :");

                if (DeviceParseUtil.isOwnDevice(device.getAddress())) {
                    if (mBluetoothCallback != null)
                        mBluetoothCallback.onFoundDevice(device, scanRecor);
                }
            } else if (BleService.BLE_GATT_CONNECTED.equals(action)) {

                Logs.e(TAG, "on device connection...");
                String address = extras.getString(BleService.EXTRA_ADDR);

            } else if (BleService.BLE_GATT_DISCONNECTED.equals(action)) {
                String address = extras.getString(BleService.EXTRA_ADDR);
                Logs.e(TAG, "on device disconnection..." + address);
                //
                mCurConnCount = 0;
                if(mCurStatus == 1 && mNormalConnSuccess) {
                    mCurStatus = 2;
                    mNormalConnSuccess = false;
                    connection(address);
                } else if(mCurStatus == 2 && mOadConnSuccess){
                    mOadConnSuccess = false;
                }

                mImgType = "";
                if(oadSuccess) {
                    if(mBluetoothCallback != null) {
                        mBluetoothCallback.onOadResult(0);
                    }
                }

            } else if (BleService.BLE_SERVICE_DISCOVERED.equals(action)) {

                String address = extras.getString(BleService.EXTRA_ADDR);
                Logs.e(TAG, "on device BLE_SERVICE_DISCOVERED...:" + address);
                onConnSuccess(address);

            } else if (BleService.BLE_CHARACTERISTIC_CHANGED.equals(action)) {

                byte[] val = extras.getByteArray(BleService.EXTRA_VALUE);
                String uuid = extras.getString(BleService.EXTRA_UUID);

                if (UUIDUtils.OAD_IMAGE_IDENTIFY.equalsIgnoreCase(uuid)) {
                    Logs.d(TAG, "on getImage type response...");
                    if(TextUtils.isEmpty(mImgType)) {
                        parseImgVersion(val);
                    }
                }

            } else if (BleService.BLE_REQUEST_FAILED.equals(action)) { // 请求失败

                BleRequest.RequestType requestType = (BleRequest.RequestType) extras
                        .getSerializable(BleService.EXTRA_REQUEST);
                String address = extras.getString(BleService.EXTRA_ADDR);
                switch (requestType) {
                    case CONNECT_GATT:
                        onConnFailed(address);
                        break;
                    case DISCOVER_SERVICE:
                        onConnFailed(address);
                        break;
                }
            }
        }
    }

    private void onConnFailed(final String address) {
        if (mCurConnCount <= (DEFAULT_RECONN_COUNT - 1)) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    connection(address);
                }
            }, 100);
            return;
        }

        mCurConnCount = 0;
        if (mBluetoothCallback != null) {
            if(mCurStatus == 1) {
                mBluetoothCallback.onConnFailed(address);
            }
        } else if(mCurStatus == 2) {
            mBluetoothCallback.onOadResult(-1);
        }
    }

    private void onConnSuccess(String address) {
        if (mCurStatus == 1) {
            mNormalConnSuccess = true;
            if (mBluetoothCallback != null) {
                mBluetoothCallback.onConnSuccess(address);
            }
        } else {
            mOadConnSuccess = true;
        }

        checkABVersion(address);
    }

    private void checkABVersion(String address) {
        boolean ok = OADManager.getInstance().getTargetImageInfo(address, OADManager.OAD_GATT_WRITE_TIMEOUT);
        if (!ok) {
            ServiceManager.getInstance().getIBle().disconnect(address);
            if (mBluetoothCallback != null) {
                mBluetoothCallback.onConnFailed(address);
            }
        }
    }

    private String mImgType;
    private void parseImgVersion(byte[] value) {

        short ver = Conversion.buildUint16(value[1], value[0]);
        char imgType = ((ver & 1) == 1) ? 'B' : 'A';
        mImgType =  String.valueOf(imgType);
        Logs.e(TAG,"image type:" + imgType);
        //setConnectionParams
        if (setConnectionParams()) {
        }

        if (imgType == 'B') {
            //write oad flag
            writeOadFlag();
        } else {
            //直接升级
            startUpdate();
        }
    }

    private boolean setConnectionParams() {
        byte[] value = {Conversion.loUint16(OADManager.OAD_CONN_INTERVAL),
                Conversion.hiUint16(OADManager.OAD_CONN_INTERVAL), Conversion.loUint16(OADManager.OAD_CONN_INTERVAL),
                Conversion.hiUint16(OADManager.OAD_CONN_INTERVAL), 0, 0,
                Conversion.loUint16(OADManager.OAD_SUPERVISION_TIMEOUT),
                Conversion.hiUint16(OADManager.OAD_SUPERVISION_TIMEOUT)};

        boolean success = OADManager.getInstance().writeConnectionParams(mCurDeviceAddress, value);
        Log.e(TAG, "on OAD status setConnectionParameters:" + success);
        if (success) {
            OADManager.getInstance().waitIdle(OADManager.OAD_GATT_WRITE_TIMEOUT);
        }

        return success;
    }

    private void writeOadFlag() {
        boolean writeFlag = OADManager.getInstance().writeOadFlag(mCurDeviceAddress, OADManager.OAD_GATT_WRITE_TIMEOUT);
        Logs.e(TAG, "writeFlag:" + writeFlag);
        if(!writeFlag) {
            IBle ble = ServiceManager.getInstance().getIBle();
            if(ble != null) {
                mCurStatus = 0;
                mNormalConnSuccess = false;
                ble.disconnect(mCurDeviceAddress);
            }

            if(mBluetoothCallback != null) {
                mBluetoothCallback.onOadResult(-1);
            }
        }
    }


    private void startUpdate() {

        Logs.e(TAG, "startUpdate startUpdate startUpdate startUpdate");
        OADManager.getInstance().resetOadBusy();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startProgramming();
            }
        }, 100);
    }

    private boolean mProgramming = false;

    private void startProgramming() {
        // mUpgradeLog.append("Programming started\n mConnectDeviceID:" +mConnectDeviceID);
        mProgramming = true;

        // Prepare image notification
        byte[] buf = new byte[OAD_IMG_HDR_SIZE + 2 + 2];
        buf[0] = Conversion.loUint16(mFileImgHdr.ver);
        buf[1] = Conversion.hiUint16(mFileImgHdr.ver);
        buf[2] = Conversion.loUint16Int(mFileImgHdr.len);
        buf[3] = Conversion.hiUint16Int(mFileImgHdr.len);
        System.arraycopy(mFileImgHdr.uid, 0, buf, 4, 4);

        // Send image notification
        OADManager.getInstance().writePrepareImage(mCurDeviceAddress, buf);
        // Initialize stats
        mProgInfo.reset(mFileImgHdr.len, OAD_BLOCK_SIZE, HAL_FLASH_WORD_SIZE);

        // Start the programming thread
        new Thread(new OadTask()).start();

        mTimer = new Timer();
        mTimerTask = new ProgTimerTask();
        mTimer.scheduleAtFixedRate(mTimerTask, 0, TIMER_INTERVAL);
    }

    private void stopProgramming() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }

        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }

        mProgramming = false;
        mImgType = null;
        mCurConnCount = 0;

        if (mProgInfo.iBlocks == mProgInfo.nBlocks) {
            oadSuccess = true;
        } else {
            oadSuccess = false;
            if (mBluetoothCallback != null) {
                mBluetoothCallback.onOadResult(-1);
            }
        }
    }


    private void programBlock() {
        if (!mProgramming)
            return;

        if (mProgInfo.iBlocks < mProgInfo.nBlocks) {
            mProgramming = true;

            // Prepare block
            mOadBuffer[0] = Conversion.loUint16(mProgInfo.iBlocks);
            mOadBuffer[1] = Conversion.hiUint16(mProgInfo.iBlocks);
            System.arraycopy(mFileBuffer, mProgInfo.iBytes, mOadBuffer, 2, OAD_BLOCK_SIZE);
            // Send block
            boolean success = OADManager.getInstance().writeImageBlock(mCurDeviceAddress, mOadBuffer);

            if (success) {
                // Update stats
                mProgInfo.iBlocks++;
                mProgInfo.iBytes += OAD_BLOCK_SIZE;
                if (mBluetoothCallback != null) {
                    mBluetoothCallback.onProgressUpdate((mProgInfo.iBlocks * 100) / mProgInfo.nBlocks);
                }
                if (!OADManager.getInstance().waitIdle(OADManager.OAD_GATT_WRITE_TIMEOUT)) {
                    mProgramming = false;
                    success = false;
                }
            } else {
                mProgramming = false;
            }
            if (!success) {

            }
        } else {
            mProgramming = false;
        }

        if (!mProgramming) {
            stopProgramming();
        }
    }

    private class OadTask implements Runnable {
        @Override
        public void run() {
            while (mProgramming) {
                try {
                    Thread.sleep(SEND_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < BLOCKS_PER_CONNECTION & mProgramming; i++) {
                    programBlock();
                }
            }
        }
    }

    private class ProgTimerTask extends TimerTask {
        @Override
        public void run() {
            mProgInfo.iTimeElapsed += TIMER_INTERVAL;
        }
    }
}
