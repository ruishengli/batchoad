package com.madao.oad;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.madao.oad.adapter.DeviceListAdapter;
import com.madao.oad.entry.BleBluetoothDevice;
import com.madao.oad.presenter.MainPresenter;
import com.madao.oad.view.OadView;
import com.madao.oad.view.ProgressButton;
import com.madao.oad.view.ScanTipView;
import com.madao.oad.view.TopTipView;


public class MainActivity extends AppCompatActivity implements OadView, View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ListView mListView;
    private DeviceListAdapter mAdapter;
    private MainPresenter mPresenter;
    private ScanTipView mScanTipView;
    private ProgressButton mButton;
    private TopTipView mTopTipView;
    private View mUpgradeView;

    private TextView mUpgradeTitle;
    private TextView mUpgradeResult;
    private TextView mCurUpgradeStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ServiceManager.getInstance().startBleService(this);

        initView();

        mPresenter = new MainPresenter(this);
        mPresenter.initialize();
    }

    private void initView() {
        mScanTipView = (ScanTipView) findViewById(R.id.scan_tip);
        mTopTipView = (TopTipView) findViewById(R.id.top_tip_view);
        mUpgradeView = findViewById(R.id.upgrade_view);
        mUpgradeTitle = (TextView) findViewById(R.id.upgrade_title);
        mUpgradeResult = (TextView) findViewById(R.id.upgrade_tip1);
        mCurUpgradeStatus = (TextView) findViewById(R.id.upgrade_tip2);

        mListView = (ListView) findViewById(R.id.listview);
        mButton = (ProgressButton) findViewById(R.id.button);
        mButton.hideProgressBar();
        mAdapter = new DeviceListAdapter(this, R.layout.bluetooth_list_item, null);
        mListView.setAdapter(mAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.base_toolbar_menu);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.scan:
                        mPresenter.scan();
                }
                return false;
            }
        });

        mButton.setOnClickListener(this);
    }

    @Override
    public void showScanning() {
        mScanTipView.setTitle("正在检测");
        mScanTipView.hideEmptyImg();
        mScanTipView.shwoProgressBar();
    }

    @Override
    public void onScanEnd() {
        if (mAdapter.getCount() <= 0) {
            mScanTipView.setTitle("没有检测到设备");
            mScanTipView.showEmptyImg();
            mScanTipView.hideProgressBar();
        } else {
            mScanTipView.setTitle("");
            mScanTipView.hideProgressBar();
        }
    }


    @Override
    public void clearDeviceList() {
        mAdapter.clear();
    }

    @Override
    public void showTopTip(String tip) {
        mTopTipView.showTip(tip);
    }

    @Override
    public void addDevice(BleBluetoothDevice device) {
        if (device != null) {
            mAdapter.append(device);
        }
    }

    @Override
    public void disEnabledButton() {
        mButton.setEnabled(false);
    }

    @Override
    public void enableButton() {
        mButton.setEnabled(true);
    }

    @Override
    public void setButtonText(String text) {
        mButton.setBtnText(text);
    }

    @Override
    public void showProgressBar() {
        mButton.showProgressBar();
    }

    @Override
    public void hideProgressBar() {
        mButton.hideProgressBar();
    }

    @Override
    public void showUpgradeView() {
        mUpgradeView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideUpgradeView() {
        mUpgradeView.setVisibility(View.GONE);
    }

    @Override
    public void setUpgradeTitle(String title) {
        mUpgradeTitle.setText(title);
    }

    @Override
    public void setUpgradeResult(String text) {
        mUpgradeResult.setText(text);
    }

    @Override
    public void setCurUpgradeStatus(String text) {
        mCurUpgradeStatus.setText(text);
    }


    @Override
    public void showUpgradeDlg(String title, String content) {

        final Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.new_version_dialog);

        if (!TextUtils.isEmpty(title)) {
            ((TextView) dialog.findViewById(R.id.version_info_title)).setText(Html.fromHtml(title));
        }
        if (!TextUtils.isEmpty(content)) {
            ((TextView) dialog.findViewById(R.id.version_info_content)).setText(Html.fromHtml(content));
        }

        dialog.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.dialog_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mPresenter.startOad();
            }
        });
        dialog.show();
    }


    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseScreenOn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        acquireScreenOn();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ServiceManager.getInstance().stopBleService(this);
        mPresenter.destroy();
    }


    @Override
    public void onBackPressed() {
        if (mPresenter.oadRunning()) {
            Toast.makeText(this, "正在升级请勿退出", Toast.LENGTH_LONG).show();
        } else {
            if(mUpgradeView.getVisibility() == View.VISIBLE) {
                hideUpgradeView();
                resetViewStatus();
                return;
            }
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                mPresenter.startQueryVersion();
        }
    }

    private void resetViewStatus() {
        setUpgradeTitle("正在升级请勿退出");
        mUpgradeResult.setText("");
        mCurUpgradeStatus.setText("");
    }

    /**
     * 保持屏幕长亮
     */
    private void acquireScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 屏幕亮屏恢复系统控制
     */
    private void releaseScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
