package com.zen.android.analysis.sample;


import android.content.Context;
import android.os.SystemClock;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author zen
 * @version 2016/9/13
 */

public class InitManager {

    private static Context sContext;

    private static Context sCache;
    private static boolean hasInit;

    public static void initLazy(Context ctx) {
        sCache = ctx;
    }

    public static boolean isHasInit() {
        return hasInit;
    }

    public static Context getContext() {
        return sContext;
    }

    public static Observable<?> ready() {
        return Observable.defer(
                () -> {
                    init(sCache);
                    return Observable.just(true);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static void clear() {
        hasInit = false;
        sContext = null;
    }

    private static synchronized void init(Context ctx) {
        if (hasInit) {
            return;
        }
        SystemClock.sleep(2000);
        sContext = ctx;
        hasInit = true;
    }

}
