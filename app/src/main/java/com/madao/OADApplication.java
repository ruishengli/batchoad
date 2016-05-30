package com.madao;

import android.app.Application;
import android.content.Context;

/**
 * desc OADApplication
 *
 * @author: or
 * @since: on 2016/5/30.
 */
public class OADApplication extends Application {
    public static Context applicationContext;
    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
    }
}
