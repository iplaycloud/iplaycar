package com.iplay.car.common.base;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

/**
 * Author : iplay on 2015/7/20 20:39
 * Mail：iplaycloud@gmail.com
 * Description：
 */
public class BaseApplication extends Application {

    public static Context sContext;
    public static Resources sResource;
    //运行系统是否为2.3或以上
    public static boolean isAtLeastGB;
    private static String PREF_NAME = "creativelocker.pref";
    private static String LAST_REFRESH_TIME = "last_refresh_time.pref";
    private static long lastToastTime;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            isAtLeastGB = true;
        }
    }

    public static synchronized BaseApplication context() {
        return (BaseApplication) sContext;
    }

    public static Resources resources() {
        return sResource;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        sResource = sContext.getResources();
    }

}
