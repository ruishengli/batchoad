package com.madao.oad.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ViewHolder {

    private SparseArray<View> mViews ;
    private View mConvertView;
    
    private ViewHolder(Context context,int layloutId,View convertView,ViewGroup parent){
        mViews = new SparseArray<View>();
        mConvertView =  LayoutInflater.from(context).inflate(layloutId, parent, false);
        mConvertView.setTag(this);
    }
    
    public <T extends View>T get(int viewId){
        View view = mViews.get(viewId);
        if(view == null) {
            view = mConvertView.findViewById(viewId);
            mViews.append(viewId, view);
        }
        
        return (T)view;
    }
    
    public static ViewHolder getView(Context context,int layloutId,View convertView,ViewGroup parent){
       
        if(convertView == null) 
            return  new ViewHolder(context,layloutId,convertView,parent);
         else 
            return   (ViewHolder) convertView.getTag();
    }
    
    public View getConvertView(){
        return mConvertView;
    }
    
    public void setText(int textViewId,String str){
       ((TextView)get(textViewId)).setText(str);
    }
}
