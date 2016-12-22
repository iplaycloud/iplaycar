package com.iplay.car.common.base;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.orhanobut.logger.Logger;
import com.iplay.car.common.base.system.AppManager;

/**
 * Description :
 * Created by iplay on 2016/12/22.
 * E-mail : iplaycloud@gmail.com
 */
public class BaseFragmentActivity extends FragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getAppManager().addActivity(this);
        Logger.d("当前Activity 栈中有：" + AppManager.getAppManager().getActivityCount() + "个Activity");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getAppManager().removeActivity(this);

        Logger.d("当前Activity 栈中有：" + AppManager.getAppManager().getActivityCount() + "个Activity");
    }
}
