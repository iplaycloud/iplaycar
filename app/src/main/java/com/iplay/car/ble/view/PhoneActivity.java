package com.iplay.car.ble.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioGroup;

import com.iplay.car.R;
import com.iplay.car.common.base.BaseFragmentActivity;
import com.iplay.car.ble.fragment.CallRecodeFragment;
import com.iplay.car.ble.fragment.ContastFragment;
import com.iplay.car.ble.fragment.PhoneFragment;
import com.iplay.car.voice.service.MainService;
import com.iplay.car.common.utils.DataUtils;

/**
 * Created by Administrator on 2016/11/30.
 * 拨号界面
 */
public class PhoneActivity extends BaseFragmentActivity {
    private RadioGroup radioGroup;
    private Fragment[] fragments;
    private FragmentManager manager;
    private int currentFragment;
    private UiReceiver uiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        initView();
        initDate();
        uiReceiver = new UiReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DataUtils.broadcastUI);
        this.registerReceiver(uiReceiver, intentFilter);
    }

    private void initView() {
        radioGroup = (RadioGroup) findViewById(R.id.phone_control_rg);
        radioGroup.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(uiReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        sendServiceMsg(DataUtils.currentView, "PhoneActivity"); //设置当前页面
        super.onResume();
    }

    public void sendServiceMsg(String order, String data) {
        // 发送广播
        Intent intent = new Intent();
        intent.putExtra("order", order);
        intent.putExtra("data", data);
        intent.setAction(MainService.Action);
        sendBroadcast(intent);
    }

    private void initDate() {
        manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragments = new Fragment[3];
        fragments[0] = new PhoneFragment();
        fragments[1] = new ContastFragment();
        fragments[2] = new CallRecodeFragment();
        for (int i = 0; i < fragments.length; i++) {
            fragmentTransaction.add(R.id.phone_content_fl, fragments[i], i + "");
            fragmentTransaction.hide(fragments[i]);
        }
        fragmentTransaction.show(fragments[0]);
        fragmentTransaction.commit();

    }

    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            FragmentTransaction ft = manager.beginTransaction();// 隐藏前一页面
            ft.hide(fragments[currentFragment]);
            switch (checkedId) {
                case R.id.phone_call_rb:// 拨号
                    currentFragment = 0;
                    break;
                case R.id.phone_book_rb:// 电话本
                    currentFragment = 1;
                    break;
                case R.id.phone_records_rb:// 电话记录
                    currentFragment = 2;
                    break;
            }
            ft.show(fragments[currentFragment]);// 显示点击的页面
            ft.commit();
        }
    };

    protected class UiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String order = intent.getStringExtra("order");
            String data = intent.getStringExtra("data");
            String action = intent.getAction();
            if (action.equals(DataUtils.broadcastUI)) {
                if(order.equals(DataUtils.returnUI)){
                    finish();
                }
            }
        }
    }
}
