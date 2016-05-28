/**
 * This XPG software is supplied to you by Xtreme Programming Group, Inc.
 * ("XPG") in consideration of your agreement to the following terms, and your
 * use, installation, modification or redistribution of this XPG software
 * constitutes acceptance of these terms.� If you do not agree with these terms,
 * please do not use, install, modify or redistribute this XPG software.
 * 
 * In consideration of your agreement to abide by the following terms, and
 * subject to these terms, XPG grants you a non-exclusive license, under XPG's
 * copyrights in this original XPG software (the "XPG Software"), to use and
 * redistribute the XPG Software, in source and/or binary forms; provided that
 * if you redistribute the XPG Software, with or without modifications, you must
 * retain this notice and the following text and disclaimers in all such
 * redistributions of the XPG Software. Neither the name, trademarks, service
 * marks or logos of XPG Inc. may be used to endorse or promote products derived
 * from the XPG Software without specific prior written permission from XPG.�
 * Except as expressly stated in this notice, no other rights or licenses,
 * express or implied, are granted by XPG herein, including but not limited to
 * any patent rights that may be infringed by your derivative works or by other
 * works in which the XPG Software may be incorporated.
 * 
 * The XPG Software is provided by XPG on an "AS IS" basis.� XPG MAKES NO
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED
 * WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE, REGARDING THE XPG SOFTWARE OR ITS USE AND OPERATION ALONE OR IN
 * COMBINATION WITH YOUR PRODUCTS.
 * 
 * IN NO EVENT SHALL XPG BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION
 * AND/OR DISTRIBUTION OF THE XPG SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER
 * THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR
 * OTHERWISE, EVEN IF XPG HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * ABOUT XPG: Established since June 2005, Xtreme Programming Group, Inc. (XPG)
 * is a digital solutions company based in the United States and China. XPG
 * integrates cutting-edge hardware designs, mobile applications, and cloud
 * computing technologies to bring innovative products to the marketplace. XPG's
 * partners and customers include global leading corporations in semiconductor,
 * home appliances, health/wellness electronics, toys and games, and automotive
 * industries. Visit www.xtremeprog.com for more information.
 * 
 * Copyright (C) 2013 Xtreme Programming Group, Inc. All Rights Reserved.
 */

package com.ble.sdk;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;

import com.ble.sdk.BleRequest.FailReason;
import com.ble.sdk.BleRequest.RequestType;
import com.madao.util.Logs;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class BleService extends Service {
    private static final String TAG = "BleService";

    /** Intent for broadcast */
    public static final String BLE_NOT_SUPPORTED = "com.madao.sdk.ble.not_supported";
    public static final String BLE_NO_BT_ADAPTER = "com.madao.sdk.ble.no_bt_adapter";
    public static final String BLE_STATUS_ABNORMAL = "com.madao.sdk.ble.status_abnormal";
    /**
     * @see BleService#bleRequestFailed
     */
    public static final String BLE_REQUEST_FAILED = "com.madao.sdk.ble.request_failed";
    /**
     * @see BleService#bleDeviceFound
     */
    public static final String BLE_DEVICE_FOUND = "com.madao.sdk.ble.device_found";
    /**
     * @see BleService#bleGattConnected
     */
    public static final String BLE_GATT_CONNECTED = "com.madao.sdk.ble.gatt_connected";
    /**
     * @see BleService#bleGattDisConnected
     */
    public static final String BLE_GATT_DISCONNECTED = "com.madao.sdk.ble.gatt_disconnected";
    /**
     * @see BleService#bleServiceDiscovered
     */
    public static final String BLE_SERVICE_DISCOVERED = "com.madao.sdk.ble.service_discovered";
    /**
     * @see BleService#bleCharacteristicRead
     */
    public static final String BLE_CHARACTERISTIC_READ = "com.madao.sdk.ble.characteristic_read";
    /**
     * @see BleService#bleCharacteristicNotification
     */
    public static final String BLE_CHARACTERISTIC_NOTIFICATION = "com.madao.sdk.ble.characteristic_notification";
    /**
     * @see BleService#bleCharacteristicIndication
     */
    public static final String BLE_CHARACTERISTIC_INDICATION = "com.madao.sdk.ble.characteristic_indication";
    /**
     * @see BleService#bleCharacteristicWrite
     */
    public static final String BLE_CHARACTERISTIC_WRITE = "com.madao.sdk.ble.characteristic_write";
    /**
     * @see BleService#bleCharacteristicChanged
     */
    public static final String BLE_CHARACTERISTIC_CHANGED = "com.madao.sdk.ble.characteristic_changed";

    /** Intent extras */
    public static final String EXTRA_DEVICE = "DEVICE";
    public static final String EXTRA_RSSI = "RSSI";
    public static final String EXTRA_SCAN_RECORD = "SCAN_RECORD";
    public static final String EXTRA_SCAN_VENDOR = "scan_vendor";
    public static final String EXTRA_SOURCE = "SOURCE";
    public static final String EXTRA_ADDR = "ADDRESS";
    public static final String EXTRA_CONNECTED = "CONNECTED";
    public static final String EXTRA_STATUS = "STATUS";
    public static final String EXTRA_UUID = "UUID";
    public static final String EXTRA_VALUE = "VALUE";
    public static final String EXTRA_REQUEST = "REQUEST";
    public static final String EXTRA_REASON = "REASON";

    /** Source of device entries in the device list */
    public static final int DEVICE_SOURCE_SCAN = 0;
    public static final int DEVICE_SOURCE_BONDED = 1;
    public static final int DEVICE_SOURCE_CONNECTED = 2;

    public static final UUID DESC_CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public enum BLESDK {
        NOT_SUPPORTED, ANDROID, SAMSUNG, BROADCOM
    }

    private final IBinder mBinder = new LocalBinder();
    private BLESDK mBleSDK;
    private IBle mBle;
    private Queue<BleRequest> mRequestQueue = new LinkedList<BleRequest>();
    private BleRequest mCurrentRequest = null;
    private static final int REQUEST_TIMEOUT = 10 * 10  ; // total timeout =
                                                        // REQUEST_TIMEOUT *
                                                        // 300ms
    private boolean mCheckTimeout = false;
    private int mElapsed = 0;
    private Thread mRequestTimeoutThread;
    private String mNotificationAddress;

    private volatile boolean mBusy = false;

    private int mRequestID;

    public void init(){
        resetRequestQueue();
        mCheckTimeout = false;
        mCurrentRequest = null;
        mRequestID = 0;
    }

    private synchronized int getRequestId(){
        mRequestID ++;
        if(mRequestID >= Integer.MAX_VALUE){
            mRequestID = 0;
        }

        return mRequestID;
    }

    private class TimeoutThread extends Thread {
        private int reqId;
        public TimeoutThread(Runnable runnable , int reqId){
            super(runnable);
            this.reqId = reqId;
        }
    }

    private void resetRequestQueue() {
        mRequestQueue = new LinkedList<>();
        mCheckTimeout = false;
    }

    private class TimeoutRunnable implements Runnable {
        private int reqId;
        public TimeoutRunnable(int reqId){
            this.reqId = reqId;
        }
        @Override
        public void run() {
            Logs.d(TAG, "monitoring thread start");
            mElapsed = 0;
            try {
                while (mCheckTimeout) {
                    mElapsed++;
                    if (mElapsed > REQUEST_TIMEOUT && mCurrentRequest != null && mCurrentRequest.id == reqId) {
                        Logs.e(TAG, "-processrequest type " + mCurrentRequest.type + " address "
                                + mCurrentRequest.address + " [timeout]");

                        String uuid = "";
                        String address = mCurrentRequest.address;
                        RequestType type = mCurrentRequest.type;

                        if(mCurrentRequest.characteristic != null && mCurrentRequest.characteristic.getUuid() != null) {
                            uuid = mCurrentRequest.characteristic.getUuid().toString();
                        }

                        if(type == RequestType.CONNECT_GATT || type == RequestType.DISCOVER_SERVICE) {
                            if (mBle != null) {
                                Logs.d(TAG,"current req is conn:address:" + address);
                                mBle.disconnect(address);
                            }
                            resetRequestQueue();
                        }

                        bleRequestFailed(address, type,
                                uuid, FailReason.TIMEOUT);
                        mCurrentRequest = null;
                        processNextRequest();
                        break;
                    }

                    Thread.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logs.d(TAG, "monitoring thread exception");
            }
            Logs.d(TAG, "monitoring thread stop");
        }
    }

    private void processTimeout(RequestType type, String address) {
        switch(type) {
            case CONNECT_GATT:
            case DISCOVER_SERVICE:
                if (mBle != null) {
                    //if(mCurrentRequest != null)
                        mBle.disconnect(address);
                }


                break;
            default:
                mCurrentRequest = null;
                processNextRequest();
        }

    }

    public static IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLE_NOT_SUPPORTED);
        intentFilter.addAction(BLE_NO_BT_ADAPTER);
        intentFilter.addAction(BLE_STATUS_ABNORMAL);
        intentFilter.addAction(BLE_REQUEST_FAILED);
        intentFilter.addAction(BLE_DEVICE_FOUND);
        intentFilter.addAction(BLE_GATT_CONNECTED);
        intentFilter.addAction(BLE_GATT_DISCONNECTED);
        intentFilter.addAction(BLE_SERVICE_DISCOVERED);
        intentFilter.addAction(BLE_CHARACTERISTIC_READ);
        intentFilter.addAction(BLE_CHARACTERISTIC_NOTIFICATION);
        intentFilter.addAction(BLE_CHARACTERISTIC_WRITE);
        intentFilter.addAction(BLE_CHARACTERISTIC_CHANGED);
        return intentFilter;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

    @Override
    public void onCreate() {
        mBleSDK = getBleSDK();
        Logs.d(TAG, "onCreate");
        if (mBleSDK == BLESDK.NOT_SUPPORTED) {
            Logs.d(TAG, "NOT_SUPPORTED");
            return;
        }

        Logs.d(TAG, " " + mBleSDK);
        mBle = new AndroidBle(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mBle != null) {
            mBle.onDestroy();
        }
    }

    protected void bleNotSupported() {
        Intent intent = new Intent(BleService.BLE_NOT_SUPPORTED);
        sendBroadcast(intent);
    }

    protected void bleNoBtAdapter() {
        Intent intent = new Intent(BleService.BLE_NO_BT_ADAPTER);
        sendBroadcast(intent);
    }

    private BLESDK getBleSDK() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // android 4.3
            return BLESDK.ANDROID;
        }

        bleNotSupported();
        return BLESDK.NOT_SUPPORTED;
    }

    public IBle getBle() {
        return mBle;
    }

    /**
     * Send {@link BleService#BLE_DEVICE_FOUND} broadcast. <br>
     * <br>
     * Data in the broadcast intent: <br>
     * {@link BleService#EXTRA_DEVICE} device {@link BluetoothDevice} <br>
     * {@link BleService#EXTRA_RSSI} rssi int<br>
     * {@link BleService#EXTRA_SCAN_RECORD} scan record byte[] <br>
     * {@link BleService#EXTRA_SOURCE} source int, not used now <br>
     */
    protected void bleDeviceFound(BluetoothDevice device, int rssi, byte[] scanRecord, int source) {
        Logs.d(TAG, "[" + new Date().toLocaleString() + "] device found " + device.getAddress() );
        Intent intent = new Intent(BleService.BLE_DEVICE_FOUND);
        intent.putExtra(BleService.EXTRA_DEVICE, device);
        intent.putExtra(BleService.EXTRA_RSSI, rssi);
        intent.putExtra(BleService.EXTRA_SCAN_RECORD, scanRecord);
        intent.putExtra(BleService.EXTRA_SOURCE, source);
        sendBroadcast(intent);
    }

    /**
     * Send {@link BleService#BLE_GATT_CONNECTED} broadcast. <br>
     * <br>
     * Data in the broadcast intent: <br>
     * {@link BleService#EXTRA_DEVICE} device {@link BluetoothDevice} <br>
     */
    protected void bleGattConnected(BluetoothDevice device) {
        Intent intent = new Intent(BLE_GATT_CONNECTED);
        intent.putExtra(EXTRA_DEVICE, device);
        intent.putExtra(EXTRA_ADDR, device.getAddress());
        sendBroadcast(intent);
        requestProcessed(device.getAddress(), RequestType.CONNECT_GATT, "", true);
    }

    /**
     * Send {@link BleService#BLE_GATT_DISCONNECTED} broadcast. <br>
     * <br>
     * Data in the broadcast intent: <br>
     * {@link BleService#EXTRA_ADDR} device address {@link String} <br>
     * 
     * @param address
     */
    protected void bleGattDisConnected(String address) {

        clearTimeoutThread();
        resetRequestQueue();

       /* if(mCurrentRequest != null && (mCurrentRequest.type == RequestType.CONNECT_GATT
                || mCurrentRequest.type == RequestType.DISCOVER_SERVICE ))*/
        try {
            if(mCurrentRequest != null) {
                String uuid = "";
                RequestType type = mCurrentRequest.type ;
                if(mCurrentRequest.characteristic != null && mCurrentRequest.characteristic.getUuid() != null) {
                    uuid = mCurrentRequest.characteristic.getUuid().toString();
                }

                requestProcessed(address, type, uuid, false);
            } else {
                Logs.e(TAG,"mCurrentRequest is null");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(BLE_GATT_DISCONNECTED);
        intent.putExtra(EXTRA_ADDR, address);
        sendBroadcast(intent);
    }

    /**
     * Send {@link BleService#BLE_SERVICE_DISCOVERED} broadcast. <br>
     * <br>
     * Data in the broadcast intent: <br>
     * {@link BleService#EXTRA_ADDR} device address {@link String} <br>
     * 
     * @param address
     */
    protected void bleServiceDiscovered(String address) {

        Intent intent = new Intent(BLE_SERVICE_DISCOVERED);
        intent.putExtra(EXTRA_ADDR, address);
        sendBroadcast(intent);
        requestProcessed(address, RequestType.DISCOVER_SERVICE, "", true);
    }

    protected  void requestProcessed(String address, RequestType requestType, String uuid,
                                     boolean success) {
        if (mCurrentRequest != null && requestType != null &&  mCurrentRequest.type == requestType) {
            clearTimeoutThread();
            Logs.d(TAG, "-processrequest type " + requestType + " address " + address + " uuid "
                    + uuid + " [success: " + success + "]");
            if (!success) {
                bleRequestFailed(address, requestType, uuid,
                        FailReason.RESULT_FAILED);
            }

            mCurrentRequest = null;
            processNextRequest();
        }
    }

    /*private void clearTimeout(){
        mCheckTimeout = false;
    }*/


    private void clearTimeoutThread() {
        if (mRequestTimeoutThread != null && mRequestTimeoutThread.isAlive()) {
            try {
                mCheckTimeout = false;
                mRequestTimeoutThread.join();
                mRequestTimeoutThread = null;
            } catch (InterruptedException e) {
                Logs.e(TAG,"clearTimeoutThread error");
                e.printStackTrace();

            }
        }
    }


    protected void addBleRequest(BleRequest request) {
        synchronized (mRequestQueue) {
            request.id = getRequestId();
            mRequestQueue.add(request);
            processNextRequest();
        }
    }

    private void processNextRequest() {
        if (mCurrentRequest != null) {
            return;
        }

        synchronized (mRequestQueue) {
            if (mRequestQueue.isEmpty()) {
                return;
            }
            mCurrentRequest = mRequestQueue.remove();
        }

        if(mCurrentRequest == null) {
            Logs.d(TAG, "+processrequest mCurrentRequest is null");
            return;
        }

        int reqId = mCurrentRequest.id;

        Logs.d(TAG, "+processrequest type " + mCurrentRequest.type + " address "
                + mCurrentRequest.address + " remark " + mCurrentRequest.remark + " ID:" + reqId);
        boolean ret = false;

        switch (mCurrentRequest.type) {
        case CONNECT_GATT:

            ret = ((IBleRequestHandler) mBle).connect(mCurrentRequest.address);
            break;
        case DISCOVER_SERVICE:
            ret = mBle.discoverServices(mCurrentRequest.address);
            break;
        case CHARACTERISTIC_NOTIFICATION:
        case CHARACTERISTIC_INDICATION:
        case CHARACTERISTIC_STOP_NOTIFICATION:
            ret = ((IBleRequestHandler) mBle).characteristicNotification(mCurrentRequest.address,
                    mCurrentRequest.characteristic);
            break;
        case READ_CHARACTERISTIC:
            ret = ((IBleRequestHandler) mBle).readCharacteristic(mCurrentRequest.address,
                    mCurrentRequest.characteristic);
            break;
        case WRITE_CHARACTERISTIC:
            ret = ((IBleRequestHandler) mBle).writeCharacteristic(mCurrentRequest.address,
                    mCurrentRequest.characteristic);
            break;
        case READ_DESCRIPTOR:
            break;
        default:
            break;
        }

        if (ret) {
            startTimeoutThread(reqId);
        } else {
            Logs.d(TAG, "-processrequest type " + mCurrentRequest.type + " address "
                    + mCurrentRequest.address + " [fail start]");
            String uuid = null;
            if(mCurrentRequest != null && mCurrentRequest.characteristic!= null) {
                uuid =  mCurrentRequest.characteristic.getUuid().toString();
            }


            if(mCurrentRequest != null && mCurrentRequest.type != null && mCurrentRequest.type == RequestType.DISCOVER_SERVICE) {
                if(mBle != null) {
                    mBle.disconnect(mCurrentRequest.address);
                }
            }

            bleRequestFailed(mCurrentRequest.address, mCurrentRequest.type,
                    uuid, FailReason.START_FAILED);

            mCurrentRequest = null;
            processNextRequest();
        }
    }

    private void startTimeoutThread(int reqId) {
        clearTimeoutThread();

        if(mCurrentRequest != null && mCurrentRequest.id == reqId) {
            mCheckTimeout = true;
            mRequestTimeoutThread = new Thread(new TimeoutRunnable(reqId));
            mRequestTimeoutThread.start();
        }
    }

   /* private void startTimeout(){
        Logs.d(TAG, "monitoring thread start");
        mElapsed = 0;
        try {
            while (mCheckTimeout) {
                // Log.d(TAG, "monitoring timeout seconds: " + mElapsed);
                Thread.sleep(100);
                mElapsed++;

                if (mElapsed > REQUEST_TIMEOUT && mCurrentRequest != null) {
                    Logs.e(TAG, "-processrequest type " + mCurrentRequest.type + " address "
                            + mCurrentRequest.address + " [timeout]");

                    String uuid = "";
                    if(mCurrentRequest.characteristic != null && mCurrentRequest.characteristic.getUuid() != null) {
                        uuid = mCurrentRequest.characteristic.getUuid().toString();
                    }

                    bleRequestFailed(mCurrentRequest.address, mCurrentRequest.type,
                            uuid, FailReason.TIMEOUT);

                    if (mBle != null) {
                        if(mCurrentRequest != null)
                            mBle.disconnect(mCurrentRequest.address);
                    }

                    resetRequestQueue();

                        *//*new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mCurrentRequest = null;
                                processNextRequest();
                            }
                        }, "th-ble").start();*//*
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Logs.d(TAG, "monitoring thread exception");
        }
        Logs.d(TAG, "monitoring thread stop");
    }*/



    protected BleRequest getCurrentRequest() {
        return mCurrentRequest;
    }

    protected void setCurrentRequest(BleRequest mCurrentRequest) {
        this.mCurrentRequest = mCurrentRequest;
    }

    /**
     * Send {@link BleService#BLE_CHARACTERISTIC_NOTIFICATION} broadcast. <br>
     * <br>
     * Data in the broadcast intent: <br>
     * {@link BleService#EXTRA_ADDR} device address {@link String} <br>
     * {@link BleService#EXTRA_UUID} characteristic uuid {@link String}<br>
     * {@link BleService#EXTRA_STATUS} read status {@link Integer} Not used now
     * <br>
     * 
     * @param address
     * @param uuid
     * @param status
     */
    protected void bleCharacteristicNotification(String address, String uuid, boolean isEnabled,
                                                 int status) {
        Intent intent = new Intent(BLE_CHARACTERISTIC_NOTIFICATION);
        intent.putExtra(EXTRA_ADDR, address);
        intent.putExtra(EXTRA_UUID, uuid);
        intent.putExtra(EXTRA_VALUE, isEnabled);
        intent.putExtra(EXTRA_STATUS, status);
        sendBroadcast(intent);
        if (isEnabled) {
            requestProcessed(address, RequestType.CHARACTERISTIC_NOTIFICATION, uuid, true);
        } else {
            requestProcessed(address, RequestType.CHARACTERISTIC_STOP_NOTIFICATION, uuid, true);
        }
        setNotificationAddress(address);
    }

    /**
     * Send {@link BleService#BLE_CHARACTERISTIC_INDICATION} broadcast. <br>
     * <br>
     * Data in the broadcast intent: <br>
     * {@link BleService#EXTRA_ADDR} device address {@link String} <br>
     * {@link BleService#EXTRA_UUID} characteristic uuid {@link String}<br>
     * {@link BleService#EXTRA_STATUS} read status {@link Integer} Not used now
     * <br>
     * 
     * @param address
     * @param uuid
     * @param status
     */
    protected void bleCharacteristicIndication(String address, String uuid, int status) {
        Intent intent = new Intent(BLE_CHARACTERISTIC_INDICATION);
        intent.putExtra(EXTRA_ADDR, address);
        intent.putExtra(EXTRA_UUID, uuid);
        intent.putExtra(EXTRA_STATUS, status);
        sendBroadcast(intent);
        requestProcessed(address, RequestType.CHARACTERISTIC_INDICATION, uuid, true);
        setNotificationAddress(address);
    }


    /**
     * Send {@link BleService#BLE_CHARACTERISTIC_READ} broadcast. <br>
     * <br>
     * Data in the broadcast intent: <br>
     * {@link BleService#EXTRA_ADDR} device address {@link String} <br>
     * {@link BleService#EXTRA_UUID} characteristic uuid {@link String}<br>
     * {@link BleService#EXTRA_STATUS} read status {@link Integer} Not used now
     * <br>
     * {@link BleService#EXTRA_VALUE} data byte[] <br>
     *
     * @param address
     * @param uuid
     * @param status
     * @param value
     */
    protected void bleCharacteristicRead(String address, String uuid, int status, byte[] value) {

        Intent intent = new Intent(BLE_CHARACTERISTIC_READ);
        intent.putExtra(EXTRA_ADDR, address);
        intent.putExtra(EXTRA_UUID, uuid);
        intent.putExtra(EXTRA_STATUS, status);
        intent.putExtra(EXTRA_VALUE, value);
        sendBroadcast(intent);

        requestProcessed(address, RequestType.READ_CHARACTERISTIC, uuid, true);
    }

    /**
     * Send {@link BleService#BLE_CHARACTERISTIC_WRITE} broadcast. <br>
     * <br>
     * Data in the broadcast intent: <br>
     * {@link BleService#EXTRA_ADDR} device address {@link String} <br>
     * {@link BleService#EXTRA_UUID} characteristic uuid {@link String}<br>
     * {@link BleService#EXTRA_STATUS} read status {@link Integer} Not used now
     * <br>
     * 
     * @param address
     * @param uuid
     * @param status
     */
    protected void bleCharacteristicWrite(String address, String uuid, int status) {
        Intent intent = new Intent(BLE_CHARACTERISTIC_WRITE);
        intent.putExtra(EXTRA_ADDR, address);
        intent.putExtra(EXTRA_UUID, uuid);
        intent.putExtra(EXTRA_STATUS, status);
        sendBroadcast(intent);
        requestProcessed(address, RequestType.WRITE_CHARACTERISTIC, uuid, true);
    }

    /**
     * Send {@link BleService#BLE_CHARACTERISTIC_CHANGED} broadcast. <br>
     * <br>
     * Data in the broadcast intent: <br>
     * {@link BleService#EXTRA_ADDR} device address {@link String} <br>
     * {@link BleService#EXTRA_UUID} characteristic uuid {@link String}<br>
     * {@link BleService#EXTRA_VALUE} data byte[] <br>
     * 
     * @param address
     * @param uuid
     * @param value
     */
    protected void bleCharacteristicChanged(String address, String uuid, byte[] value) {

        Intent intent = new Intent(BLE_CHARACTERISTIC_CHANGED);
        intent.putExtra(EXTRA_ADDR, address);
        intent.putExtra(EXTRA_UUID, uuid);
        intent.putExtra(EXTRA_VALUE, value);
        sendBroadcast(intent);
    }

    /**
     * @param reason
     */
    protected void bleStatusAbnormal(String reason) {
        Intent intent = new Intent(BLE_STATUS_ABNORMAL);
        intent.putExtra(EXTRA_VALUE, reason);
        sendBroadcast(intent);
    }

    /**
     * Sent when BLE request failed.<br>
     * <br>
     * Data in the broadcast intent: <br>
     * {@link BleService#EXTRA_ADDR} device address {@link String} <br>
     * {@link BleService#EXTRA_REQUEST} request type
     * {@link BleRequest.RequestType} <br>
     * {@link BleService#EXTRA_REASON} fail reason {@link BleRequest.FailReason}
     * <br>
     */
    protected void bleRequestFailed(String address, RequestType type, String uuid,
                                    FailReason reason) {
        Intent intent = new Intent(BLE_REQUEST_FAILED);
        intent.putExtra(EXTRA_ADDR, address);
        intent.putExtra(EXTRA_REQUEST, type);
        intent.putExtra(EXTRA_UUID, uuid);
        intent.putExtra(EXTRA_REASON, reason.ordinal());
        sendBroadcast(intent);
    }

    protected String getNotificationAddress() {
        return mNotificationAddress;
    }

    protected void setNotificationAddress(String mNotificationAddress) {
        this.mNotificationAddress = mNotificationAddress;
    }
}
