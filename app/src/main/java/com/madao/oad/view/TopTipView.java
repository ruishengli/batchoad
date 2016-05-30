package com.madao.oad.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.madao.oad.R;

import java.lang.ref.WeakReference;

/**
 * TopTipView
 * <p>
 * 顶部提示窗<br/>
 */
public class TopTipView extends LinearLayout {
    private final String TAG = getClass().getSimpleName();

    private Context mContext;
    private Handler mHandler;

    private TextView mTipText;

    /**
     * 默认提示持续时间3s
     */
    private final int DEFAULT_DURATION = 3000;

    public TopTipView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.top_tip_view, this, true);

        mTipText = (TextView) findViewById(R.id.tip_text);

        mHandler = new MyHandler(mContext);
        // 默认隐藏
        setVisibility(View.GONE);
    }

    /**
     * 显示提示
     *
     * @param tip
     * @param duration 持续时间
     */
    public void showTip(String tip, int duration) {
        setVisibility(View.VISIBLE);
        mTipText.setText(tip);

        mHandler.removeCallbacks(mTipDisappearRunnable);
        mHandler.postDelayed(mTipDisappearRunnable, duration);
    }

    /**
     * 显示提示
     *
     * @param tip
     */
    public void showTip(String tip) {
        showTip(tip, DEFAULT_DURATION);
    }

    public void showTip(CharSequence tip) {
        setVisibility(View.VISIBLE);
        mTipText.setText(tip);

        mHandler.removeCallbacks(mTipDisappearRunnable);
        mHandler.postDelayed(mTipDisappearRunnable, DEFAULT_DURATION);
    }

    /**
     * 提示消失
     */
    private Runnable mTipDisappearRunnable = new Runnable() {

        @Override
        public void run() {
            mTipText.setText("");
            setVisibility(View.GONE);
        }
    };

    private static class MyHandler extends Handler {
        private final WeakReference<Context> mActivity;

        public MyHandler(Context context) {
            mActivity = new WeakReference<Context>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            System.out.println(msg);
            if (mActivity.get() == null) {
                return;
            }
        }
    }
}
