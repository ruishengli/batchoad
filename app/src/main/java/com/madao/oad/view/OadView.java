package com.madao.oad.view;

import android.content.Context;

import com.madao.oad.entry.BleBluetoothDevice;

/**
 * desc LoadView
 *
 * @author: or
 * @since: on 2016/5/28.
 */
public interface OadView {

    void addDevice(BleBluetoothDevice device);

    Context getContext();
}
