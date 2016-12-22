package com.iplay.car.common.base;

import android.app.Activity;
import android.os.Bundle;

import com.orhanobut.logger.Logger;
import com.iplay.car.common.base.system.AppManager;


/**
 * Author : iplay on 2015/7/21 14:41
 * Mail：iplaycloud@gmail.com
 * Description：
 */
public abstract class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getAppManager().addActivity(this);
        Logger.d("当前Activity 栈中有：" + AppManager.getAppManager().getActivityCount() + "个Activity");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getAppManager().removeActivity(this);

        Logger.d("当前Activity 栈中有：" + AppManager.getAppManager().getActivityCount() + "个Activity");
    }
}
