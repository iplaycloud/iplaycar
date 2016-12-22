package com.iplay.car.setting.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.iplay.car.R;
import com.iplay.car.common.base.BaseActivity;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/11/25.
 * 流量界面
 */
public class FlowActivity extends BaseActivity {

    private TextView tv_query_content;// 信息内容
    private MessageReceiver messageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow);
        initView();
        initDate();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(messageReceiver);
        super.onDestroy();
    }

    private void initView() {
        tv_query_content = (TextView) findViewById(R.id.tv_query_content);
        Button btn_query = (Button) findViewById(R.id.btn_query);

        //返回
        ImageView backIv = (ImageView) findViewById(R.id.control_bottom_return);
        TextView titleTv = (TextView) findViewById(R.id.control_bottom_title);
        titleTv.setText(R.string.traffic_query);

        backIv.setOnClickListener(onClickListener);
        btn_query.setOnClickListener(onClickListener);
    }

    private void initDate() {
        // 注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        messageReceiver = new MessageReceiver();
        registerReceiver(messageReceiver, intentFilter);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.control_bottom_return:// 返回上一界面
                    finish();
                    break;
                case R.id.btn_query:// 查询流量
                    sendMms();
                    break;
            }

        }
    };

    /**
     * 流量查询
     */
    private void sendMms() {
        SmsManager manager = SmsManager.getDefault();
        ArrayList<String> list = manager.divideMessage("CXGPRS");
        for (String text : list) {
            manager.sendTextMessage("10086", null, text, null, null);
        }
        Toast.makeText(FlowActivity.this, "发送成功", Toast.LENGTH_SHORT).show();

    }

    /**
     * 接收短信广播
     */
    private class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final Bundle bundle = intent.getExtras();
            Object[] pdus = (Object[]) bundle.get("pdus");
            SmsMessage[] messages = new SmsMessage[pdus.length];
            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }
            String address = messages[0].getOriginatingAddress();//获取发送方号码
            String fullMessage = "";
            for (SmsMessage message : messages) {
                fullMessage += message.getMessageBody();//获取短信内容
            }
            tv_query_content.setText("address:" + address + ",message:" + messages);
            abortBroadcast();
        }
    }

}
