package com.zen.android.analysis.sample;

import android.app.Application;

/**
 * @author zen
 * @version 2016/9/13
 */

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        InitManager.initLazy(this);
    }
}
