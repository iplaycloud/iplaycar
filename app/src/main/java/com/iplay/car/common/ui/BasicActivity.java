package com.iplay.car.common.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.iplay.car.common.base.BaseActivity;
import com.iplay.car.voice.service.MainService;
import com.iplay.car.common.utils.DataUtils;

/**
 * Created by Administrator on 2016/12/5.
 */

public abstract class BasicActivity  extends BaseActivity {
    private UiReceiver uiReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiReceiver = new UiReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DataUtils.broadcastUI);
        this.registerReceiver(uiReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(uiReceiver);
        super.onDestroy();
    }

    protected class UiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String order = intent.getStringExtra("order");
            String data = intent.getStringExtra("data");
            String action = intent.getAction();
            if (action.equals(DataUtils.broadcastUI)) {
                 if(order.equals(DataUtils.returnUI)){
                     finish();
                 }else if (order.equals(DataUtils.selectFunction)){
                     int number = 0;
                     try {
                         number   = Integer.valueOf(data);
                     }catch(Exception e){
                         e.printStackTrace();
                     }
                     selectFunction(number);
                }
            }
        }
    }

    protected void sendServiceMsg(String order, String data) {
        // 发送广播
        Intent intent = new Intent();
        intent.putExtra("order", order);
        intent.putExtra("data", data);
        intent.setAction(MainService.Action);
        sendBroadcast(intent);
    }

    protected  abstract void selectFunction(int number);
}
