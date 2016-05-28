package com.madao.oad;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.ble.sdk.BleService;
import com.ble.sdk.IBle;
import com.madao.util.Logs;

/**
 * desc ServiceManager
 *
 * @author: or
 * @since: on 2016/5/28.
 */
public class ServiceManager {

    private final String TAG = ServiceManager.class.getSimpleName();

    private static ServiceManager mInstance;
    private BleService mService;
    private IBle mBle;

    private  ServiceManager(){}
    public static ServiceManager getInstance() {
        if(mInstance == null) {
            synchronized (ServiceManager.class) {
                if(mInstance == null) {
                    mInstance = new ServiceManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * start ble service
     */
    public void startBleService(Context context) {
        Intent bindIntent = new Intent(context, BleService.class);
        context.bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void stopBleService(Context context) {
        context.unbindService(mServiceConnection);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((BleService.LocalBinder) rawBinder).getService();
            mBle = mService.getBle();
            if (mBle == null) {
                Logs.d(TAG, "adapterEnabled：mBle is null");
            }
            if (mBle != null && !mBle.adapterEnabled()) {
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName classname) {
            Logs.d(TAG, "onServiceDisconnected：");
            mService = null;
        }
    };

    public IBle getIBle() {
        return mBle;
    }
}
