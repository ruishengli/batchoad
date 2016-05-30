package com.madao.oad.presenter;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.ble.sdk.BleService;
import com.madao.oad.FileHelper;
import com.madao.oad.NetHelper;
import com.madao.oad.ServiceManager;
import com.madao.oad.entry.BleBluetoothDevice;
import com.madao.oad.entry.Firmware;
import com.madao.oad.view.OadView;
import com.madao.util.DeviceParseUtil;
import com.madao.util.Logs;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * desc MainPresenter
 *
 * @author: or
 * @since: on 2016/5/28.
 */
public class MainPresenter implements BluetoothManager.BluetoothCallback {

    private final String TAG = MainPresenter.class.getSimpleName();

    private final int MESSAGE_ADD_DEVICE = 1;
    private final int MESSAGE_QUERY_VERSION_OVER = 2;
    private final int MESSAGE_SHOW_NEW_VERSION_DLG = 3;
    private final int MESSAGE_SHOW_TOP_TIP = 4;
    private final int MESSAGE_SHOW_UPGRADE_VIEW = 5;
    private final int MESSAGE_UPDATE_OAD_RESULT = 6;
    private final int MESSAGE_UPDATE_CUR_LOG = 7;
    private final int MESSAGE_UPDATE_PROGRESSBAR = 8;
    private final int MESSAGE_START_UPGRADE = 9;
    private final int MESSAGE_UPDATE_OVER = 10;

    private static final int DEFAULT_SCAN_TIME = 30000;
    private Firmware mServerFirmware;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mView != null) {
                switch (msg.what) {
                    case MESSAGE_ADD_DEVICE:
                        mView.addDevice((BleBluetoothDevice) msg.obj);
                        break;
                    case MESSAGE_SHOW_TOP_TIP:
                        mView.showTopTip((String) msg.obj);
                        break;
                    case MESSAGE_QUERY_VERSION_OVER:
                        mView.hideProgressBar();
                        mView.enableButton();
                        mView.setButtonText("检测固件版本");
                        break;
                    case MESSAGE_SHOW_NEW_VERSION_DLG:
                        mView.showUpgradeDlg((String) msg.obj, mServerFirmware.getDescriptions());
                        break;
                    case MESSAGE_SHOW_UPGRADE_VIEW:
                        mView.showUpgradeView();
                        break;
                    case MESSAGE_UPDATE_OAD_RESULT:
                        mView.setUpgradeResult((String) msg.obj);
                        break;
                    case MESSAGE_UPDATE_CUR_LOG:
                        mView.setCurUpgradeStatus((String) msg.obj);
                        break;
                    case MESSAGE_UPDATE_PROGRESSBAR:
                        mView.showProgressBar();
                        mView.disEnabledButton();
                        String s = "";
                        if (msg.obj != null) {
                            s = (String) msg.obj;
                        }
                        mView.setButtonText(s);
                        break;
                    case MESSAGE_START_UPGRADE:
                        start();
                        break;
                    case MESSAGE_UPDATE_OVER:
                        mView.setUpgradeTitle("更新完成");
                }
            }

        }
    };

    private OadView mView;
    private Map<String, BleBluetoothDevice> mScanResult;
    private Queue<BleBluetoothDevice> mWaitUpgradeDevices;
    private boolean mOadRunning;
    private int mWaitUpgradeDeviceCount;
    private BluetoothManager mBluetoothManager;

    public MainPresenter(OadView view) {
        this.mView = view;
    }

    public boolean oadRunning() {
        return mOadRunning;
    }
    public void initialize() {
        mWaitUpgradeDevices = new LinkedList();
        mScanResult = new LinkedHashMap<>();
        mBluetoothManager = new BluetoothManager(this);
    }

    public void scan() {
        stopScan();
        mScanResult.clear();
        mView.clearDeviceList();
        if (mBluetoothManager.bleEnabled()) {
            mView.showScanning();
            mBluetoothManager.startScan();
            mHandler.postDelayed(stopScanRunnable, DEFAULT_SCAN_TIME);
        }
    }

    public void stopScan() {
        mView.onScanEnd();
        mHandler.removeCallbacks(stopScanRunnable);
        mBluetoothManager.stopScan();
    }


    public void startQueryVersion() {

        if (mScanResult == null || mScanResult.isEmpty()) {
            showTopTip("没有检测到设备");
            return;
        }

        stopScan();
        mWaitUpgradeDevices.clear();
        mView.showProgressBar();
        mView.disEnabledButton();
        mView.setButtonText("正在查询固件版本");
        NetHelper.getInstance().setQueryVersionCallback(queryVersionCallback);
        NetHelper.getInstance().queryVersion();
    }


    public void startOad() {
        downloadFw();
    }


    private void downloadFw() {
        String downLoadDir = FileHelper.loadStorage();
        if (TextUtils.isEmpty(downLoadDir)) {
            showTopTip("无法加载sd卡");
            return;
        }

        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_PROGRESSBAR,"正在下载固件")) ;
        String saveFile = "firmware_" + mServerFirmware.getVersion() + ".bin";
        NetHelper.getInstance().setDownloadCallback(downloadCallback);
        NetHelper.getInstance().downloadFw(downLoadDir, saveFile, mServerFirmware.getImgUrl());
    }

    public void destroy() {
        stopScan();
        mBluetoothManager.onDestroy();
        NetHelper.getInstance().setQueryVersionCallback(null);
        NetHelper.getInstance().setDownloadCallback(null);
        mView = null;
    }


    private Runnable stopScanRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };


    private void showTopTip(String topTip) {
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SHOW_TOP_TIP, topTip));
    }

    @Override
    public void onFoundDevice(BluetoothDevice device, byte[] scanRecord) {
        addDevice(device, scanRecord);
    }

    @Override
    public void onConnFailed(String address) {
        if (address.equals(mCurDevice.getAddress())) {
            logs.append("连接失败 \n");
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_CUR_LOG, logs.toString()));
        }
    }

    @Override
    public void onConnSuccess(String address) {
        if (address.equals(mCurDevice.getAddress())) {
            logs.append("连接成功 \n");
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_CUR_LOG, logs.toString()));
        }

    }

    private int lastProgress = 0;
    private int oadSuccessCount = 0;
    private int oadFailedCount;

    @Override
    public void onProgressUpdate(int progress) {
        if(lastProgress == progress) {
            return;
        }
        lastProgress = progress;
        if(progress == 1) {
            logs.append("正在升级:" + progress + "% \n");
        } else {
            String s =logs.toString();
            s = s.replaceAll(":\\d{1,}", ":" + progress);
            logs = new StringBuffer();
            logs.append(s);
        }

        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_CUR_LOG, logs.toString()));
    }

    @Override
    public void onOadResult(int code) {
        if(code == 0) {
            oadSuccessCount ++;
        } else {
            oadFailedCount ++;
        }
        String tip = code == 0 ? "升级成功" : "升级失败";
        logs.append(tip + "\n");
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_CUR_LOG, logs.toString()));
        updateCurOadResult();
        mCurDevice =null;
        processNext();
    }


    private void addDevice(BluetoothDevice device, byte[] scanRecord) {
        if (!mScanResult.containsKey(device.getAddress())) {
            BleBluetoothDevice device1 = DeviceParseUtil.convertDevice(device, scanRecord);
            mScanResult.put(device1.getAddress(), device1);
            Message message = mHandler.obtainMessage(MESSAGE_ADD_DEVICE);
            message.obj = device1.clone();
            mHandler.sendMessage(message);
        }
    }

    private NetHelper.IQueryVersionCallback queryVersionCallback = new NetHelper.IQueryVersionCallback() {
        @Override
        public void onQuery(Firmware firmware) {
            if (firmware == null) {
                showTopTip("检测固件版本失败");
            } else {
                //检测可以升级固件的设备数量
                checkUpgradeDevice(firmware);
            }
            mHandler.sendEmptyMessage(MESSAGE_QUERY_VERSION_OVER);
        }
    };


    private void checkUpgradeDevice(Firmware firmware) {
        if (mScanResult != null && !mScanResult.isEmpty()) {
            Set<Map.Entry<String, BleBluetoothDevice>> set = mScanResult.entrySet();
            Iterator<Map.Entry<String, BleBluetoothDevice>> iterator = set.iterator();
            while (iterator.hasNext()) {
                BleBluetoothDevice device = iterator.next().getValue();
               /* if (device != null && device.getFirmwareVersion() > firmware.getVersion()) {
                    mWaitUpgradeDevices.offer(device.clone());
                }*/

                if (device != null) {
                    mWaitUpgradeDevices.offer(device.clone());
                }
            }
        }

        if (mWaitUpgradeDevices == null || mWaitUpgradeDevices.isEmpty()) {
            showTopTip("没有可升级的设备");
        } else {
            mServerFirmware = firmware.clone();
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SHOW_NEW_VERSION_DLG, "共" + mWaitUpgradeDevices.size() + "台设备需要更新"));
        }
    }

    private NetHelper.IDownloadCallback downloadCallback = new NetHelper.IDownloadCallback() {
        @Override
        public void onDownload(String filePath, int code) {
            if (code == 0 && !TextUtils.isEmpty(filePath)) {
                prepareOad(filePath);
            } else {
                showTopTip("下载固件失败");
                mHandler.sendEmptyMessage(MESSAGE_QUERY_VERSION_OVER);
            }
        }
    };

    private void prepareOad(String filePath) {
        if (!mBluetoothManager.loadFile(filePath)) {
            showTopTip("加载固件文件失败");
            return;
        }

        mOadRunning = true;
        mHandler.sendEmptyMessage(MESSAGE_SHOW_UPGRADE_VIEW);

        mWaitUpgradeDeviceCount = mWaitUpgradeDevices.size();
        updateCurOadResult();
        mHandler.sendEmptyMessage(MESSAGE_UPDATE_PROGRESSBAR);


        mHandler.sendEmptyMessage(MESSAGE_START_UPGRADE);
    }

    private BleBluetoothDevice mCurDevice;
    private StringBuffer logs = new StringBuffer();

    private void updateCurOadResult() {
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_OAD_RESULT, "("+oadSuccessCount+"/" + mWaitUpgradeDeviceCount + " )升级成功"));
    }
    private void start() {
        if (mWaitUpgradeDevices != null && !mWaitUpgradeDevices.isEmpty()) {
            processNext();
        }
    }

    private void processNext() {
        if (mCurDevice != null) {
            return;
        }
        if(mWaitUpgradeDevices == null || mWaitUpgradeDevices.isEmpty()) {
            //
            String tip = "共" + mWaitUpgradeDeviceCount + "台设备需要更新,成功"+ oadSuccessCount + "台,失败"+oadFailedCount+"台";
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_OAD_RESULT, tip));

            logs.append("更新完成" + "\n");
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_CUR_LOG, logs.toString()));

            mHandler.sendEmptyMessage(MESSAGE_UPDATE_OVER);
            mOadRunning = false;
            return;
        }
        mCurDevice = mWaitUpgradeDevices.poll();
        connection(mCurDevice);
    }



    private void connection(BleBluetoothDevice device) {
        logs = new StringBuffer();
        if (device != null) {
            lastProgress = 0;
            logs.append("正在连接:" + device.getName() + "\n");
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_CUR_LOG, logs.toString()));
            mBluetoothManager.startOad(device.getAddress());
        } else {
            mCurDevice = null;
            processNext();
        }
    }


}
