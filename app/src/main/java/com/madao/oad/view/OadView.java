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

    void showScanning() ;

    void onScanEnd();

    void clearDeviceList();

    void showTopTip(String tip);

    void disEnabledButton();
    void enableButton();

    void setButtonText(String text);
    void showProgressBar();
    void hideProgressBar();

    void showUpgradeDlg(String title,String content);

    void showUpgradeView();

    void hideUpgradeView();

    void setUpgradeResult(String text);

    void setCurUpgradeStatus(String text);

    void setUpgradeTitle(String title);
}
