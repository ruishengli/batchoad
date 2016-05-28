package com.madao.oad.presenter;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.ble.sdk.BleService;
import com.madao.oad.ServiceManager;
import com.madao.oad.entry.BleBluetoothDevice;
import com.madao.oad.view.OadView;
import com.madao.util.DeviceParseUtil;
import com.madao.util.Logs;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

/**
 * desc MainPresenter
 *
 * @author: or
 * @since: on 2016/5/28.
 */
public class MainPresenter {

    private final String TAG = MainPresenter.class.getSimpleName();

    private static final int DEFAULT_SCAN_TIME = 30000;
    private BluetoothDeviceReceiver mReciver;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (mView != null) {
                        mView.addDevice((BleBluetoothDevice) msg.obj);
                    }
            }
        }
    };


    private OadView mView;

    private Map<String, BleBluetoothDevice> mScanResult;

    public MainPresenter(OadView view) {
        this.mView = view;
    }

    public void initialize() {
        mScanResult = new Hashtable<>();
        mReciver = new BluetoothDeviceReceiver();
        mView.getContext().registerReceiver(mReciver,BleService.getIntentFilter());
    }

    public void scan() {
        if (ServiceManager.getInstance().getIBle() != null) {
            ServiceManager.getInstance().getIBle().startScan();
            mHandler.postDelayed(stopScanRunnable, DEFAULT_SCAN_TIME);
        }
    }

    public void stopScan() {
        mHandler.removeCallbacks(stopScanRunnable);
        if (ServiceManager.getInstance().getIBle() != null) {
            ServiceManager.getInstance().getIBle().stopScan();
        }
    }

    public void destroy() {
        stopScan();
        mView.getContext().unregisterReceiver(mReciver);
        mView = null;
    }

    private Runnable stopScanRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };


    private class BluetoothDeviceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle extras = intent.getExtras();

            if (BleService.BLE_NOT_SUPPORTED.equals(action)) { // ble不支持

            } else if (BleService.BLE_DEVICE_FOUND.equals(action)) { // 发现新设备
                Logs.d(TAG, "on found device... ");
                byte[] scanRecor = intent.getByteArrayExtra(BleService.EXTRA_SCAN_RECORD);
                final BluetoothDevice device = extras.getParcelable(BleService.EXTRA_DEVICE);

                Logs.d(TAG, "device " + device.getAddress() + ",name:" + device.getName()
                        + " scanRecor :" + Arrays.toString(scanRecor) + " servie :");

                if (DeviceParseUtil.isOwnDevice(scanRecor)) {
                    addDevice(device, scanRecor);
                }
            }
        }
    }

    private void addDevice(BluetoothDevice device, byte[] scanRecord) {
        if (!mScanResult.containsKey(device.getAddress())) {
            BleBluetoothDevice device1 = DeviceParseUtil.convertDevice(device, scanRecord);
            mScanResult.put(device1.getAddress(), device1);

            Message message = mHandler.obtainMessage(1);
            message.obj = device1.clone();
            mHandler.sendMessage(message);
        }
    }
}
