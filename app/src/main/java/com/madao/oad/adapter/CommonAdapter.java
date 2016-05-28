package com.madao.oad.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class CommonAdapter<T> extends BaseAdapter {

    private List<T> mDatas;
    private Context mContext;
    private int mLayoutId;
    
    public CommonAdapter(Context context,int layoutId,List<T> datas){
        mContext = context;
        mDatas = datas;
        mLayoutId = layoutId;
    }

    public void append(T t) {
        if(mDatas == null) {
            mDatas = new ArrayList<>();
        }

        mDatas.add(t);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDatas == null ? 0 :mDatas.size();
    }

    @Override
    public T getItem(int position) {
        if(mDatas == null || mDatas.isEmpty())
            return null;
        else 
            return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        ViewHolder viewHolde =   ViewHolder.getView(mContext, mLayoutId, convertView, parent);
       
        convert(viewHolde, getItem(position));
        return viewHolde.getConvertView();
    }
    
    public abstract void convert(ViewHolder viewHolder,T item);

    
    
}
