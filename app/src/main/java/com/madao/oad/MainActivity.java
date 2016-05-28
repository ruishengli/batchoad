package com.madao.oad;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ble.sdk.BleService;
import com.ble.sdk.IBle;
import com.madao.util.Logs;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private BleService mService;
    private IBle mBle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startBleService();
    }

    /**
     * start ble service
     */
    public void startBleService() {
        Intent bindIntent = new Intent(this, BleService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void stopBleService() {
        unbindService(mServiceConnection);
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
