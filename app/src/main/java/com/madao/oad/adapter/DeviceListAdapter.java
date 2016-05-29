package com.madao.oad.adapter;

import android.content.Context;
import android.text.TextUtils;

import com.madao.oad.R;
import com.madao.oad.entry.BleBluetoothDevice;

import java.util.List;

/**
 * com.madao.oad.adapter
 *
 * @auth or
 * @sinced on 2016/5/29.
 */
public class DeviceListAdapter extends CommonAdapter<BleBluetoothDevice> {

    public DeviceListAdapter(Context context, int layoutId, List<BleBluetoothDevice> datas) {
        super(context, layoutId, datas);
    }

    @Override
    public void convert(ViewHolder viewHolder, BleBluetoothDevice item) {
        if (item != null) {
            viewHolder.setText(R.id.label_id, item.getName());
        }
    }

}
