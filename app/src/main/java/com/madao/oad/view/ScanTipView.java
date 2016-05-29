package com.madao.oad.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.madao.oad.R;


public class ScanTipView extends LinearLayout {

    private LinearLayout mTitleLayout;
    private TextView mMessage;
    private TextView mTitle;
    private ProgressBar mProgressBar;
    private ImageView mImageView;


    public ScanTipView(Context paramContext) {
        super(paramContext);
        init(paramContext);
    }

    public ScanTipView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init(paramContext);
    }

    private void init(Context paramContext) {
        View view = LayoutInflater.from(paramContext).inflate(R.layout.scan_cyclowatch_tip_view, this);
        mTitleLayout = (LinearLayout) view.findViewById(R.id.exception_title_layout);
        this.mTitle = (TextView) view.findViewById(R.id.exception_title);
        this.mImageView = (ImageView) view.findViewById(R.id.exception_progressbar);
        this.mMessage = (TextView) view.findViewById(R.id.exception_message);
        mProgressBar = (ProgressBar) view.findViewById(R.id.exception_progress);
    }

    public void setOnClickListener(OnClickListener listener) {
        if(listener !=null && mTitleLayout!=null) {
            mTitleLayout.setOnClickListener(listener);
        }
    }
    
    public void setTitle(String title) {
        if(mTitle !=null) {
            mTitle.setText(title);
        }
    }
    
    public void setMessage(String message) {
        if(mTitle !=null) {
            mMessage.setText(message);
        }
    }


    public void hideProgressBar(){
        mProgressBar.setVisibility(GONE);
    }

    public void shwoProgressBar() {
        mProgressBar.setVisibility(VISIBLE);
    }

    public void hideEmptyImg(){
        mImageView.setVisibility(GONE);
    }

    public void showEmptyImg(){
        mImageView.setVisibility(VISIBLE);
    }
    public void setEmptyImg(int rId) {
        mImageView.setImageResource(rId);
    }
    
   public TextView getTvTitle() {
       return this.mTitle;
   }
    public TextView getTvMessage() {
        return this.mMessage;
    }

}
