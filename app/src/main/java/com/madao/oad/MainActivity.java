package com.madao.oad;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ListView;

import com.madao.oad.adapter.CommonAdapter;
import com.madao.oad.adapter.ViewHolder;
import com.madao.oad.entry.BleBluetoothDevice;
import com.madao.oad.presenter.MainPresenter;
import com.madao.oad.view.OadView;

import java.util.List;


public class MainActivity extends AppCompatActivity implements OadView {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ListView mListView;
    private BluetoothAdapter mAdapter;
    private MainPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ServiceManager.getInstance().startBleService(this);

        initView();

        mPresenter = new MainPresenter(this);
        mPresenter.initialize();
    }

    private void initView () {
        mListView = (ListView) findViewById(R.id.listview);
        mAdapter = new BluetoothAdapter(this, R.layout.bluetooth_list_item, null);
        mListView.setAdapter(mAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.base_toolbar_menu);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case  R.id.scan:
                    mPresenter.scan();
                }
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ServiceManager.getInstance().stopBleService(this);
        mPresenter.destroy();
    }


    @Override
    public void addDevice(BleBluetoothDevice device) {
        if (device != null) {
            mAdapter.append(device);
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    public class BluetoothAdapter extends CommonAdapter<BleBluetoothDevice> {

        public BluetoothAdapter(Context context, int layoutId, List<BleBluetoothDevice> datas) {
            super(context, layoutId, datas);
        }

        @Override
        public void convert(ViewHolder viewHolder, BleBluetoothDevice item) {
            if (item != null) {
                viewHolder.setText(R.id.label_id, item.getName()+"_" + converSerial(item.getAddress()) + "_v" +item.getFirmwareVersion());
            }
        }
    }


    private String converSerial(String address){
        if(!TextUtils.isEmpty(address)) {
            address = address.replace(":","");
            if(address.length() > 4) {
                return address.substring(address.length()-4);
            }
        }
        return address;
    }
}
