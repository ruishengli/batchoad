package com.madao.oad.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.madao.oad.R;


/**
 * Created by or on 2016/1/21.
 */
public class ProgressButton extends RelativeLayout {

    private Context mContext;
    private ProgressBar mProgressBar;
    private TextView mBtn;

    public ProgressButton(Context context) {
        super(context);
        initView(context);
    }
    public ProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }


    private void initView(Context context){
        mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.progress_button_view,this);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_id);
        mBtn = (TextView) findViewById(R.id.btn_txt_id);
    }


    public void showProgressBar(){
        mProgressBar.setVisibility(VISIBLE);
    }

    public void hideProgressBar(){
        mProgressBar.setVisibility(GONE);
    }
    public void setBtnText(String txt){
        mBtn.setText(txt);
    }

    public void setTextColor(int color) {
        mBtn.setTextColor(color);
    }
}
