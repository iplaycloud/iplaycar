package com.iplay.car.setting.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iplay.car.R;
import com.iplay.car.common.base.BaseActivity;
import com.iplay.car.voice.service.MainService;
import com.iplay.car.common.utils.DataUtils;

/**
 * Created by Administrator on 2016/11/24.
 * 设置界面
 */
public class SetActivity extends BaseActivity {
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        initViews();
    }

    private void initViews() {
        ImageView control_return = (ImageView) findViewById(R.id.control_bottom_return);
        TextView control_title = (TextView) findViewById(R.id.control_bottom_title);
        control_title.setText(R.string.seeting);

        RelativeLayout wifiRl = (RelativeLayout) findViewById(R.id.set_wifi_rl);
        RelativeLayout broadcastRl = (RelativeLayout) findViewById(R.id.set_broadcast_rl);
        RelativeLayout systemRl = (RelativeLayout) findViewById(R.id.set_system_rl);
        RelativeLayout updateRl = (RelativeLayout) findViewById(R.id.set_update_rl);

        control_return.setOnClickListener(onClickListener);
        wifiRl.setOnClickListener(onClickListener);
        broadcastRl.setOnClickListener(onClickListener);
        systemRl.setOnClickListener(onClickListener);
        updateRl.setOnClickListener(onClickListener);
    }

    @Override
    protected void onResume() {
        sendServiceMsg(DataUtils.currentView, "SetActivity"); //设置当前页面
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

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //返回
                case R.id.control_bottom_return:
                    finish();
                    break;
                //wifi设置
                case R.id.set_wifi_rl:
                    intent = new Intent(SetActivity.this,WifiActivity.class);
                    startActivity(intent);
                    break;
                //调频广播
                case R.id.set_broadcast_rl:
                    Toast.makeText(SetActivity.this,"调频广播",Toast.LENGTH_SHORT).show();
                    break;
                //系统设置
                case R.id.set_system_rl:
                    intent = new Intent(SetActivity.this,SystemActivity.class);
                    startActivity(intent);
                    break;
                //软件更新
                case R.id.set_update_rl:
                    intent = new Intent(SetActivity.this,UpdateActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

}
