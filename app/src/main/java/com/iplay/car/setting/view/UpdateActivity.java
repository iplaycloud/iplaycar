package com.iplay.car.setting.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.iplay.car.R;
import com.iplay.car.navigation.app.AppData;
import com.iplay.car.common.base.BaseActivity;
import com.iplay.car.common.protocol.ProtocolAgreementByte;
import com.iplay.car.common.utils.DataUtils;

/**
 * Created by Administrator on 2016/11/25.
 * 软件升级界面
 */
public class UpdateActivity extends BaseActivity {
    private TextView versionNameTv;// 版本名
    private TextView hintTv;// 更新提示
    private Button updateBtn;// 更新按钮
    private UpdateReceiver receiver;// 广播
    private int versionCode;
    private int newVersion;

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        initViews();
        initData();
    }

    private void initViews() {
        ImageView backIv = (ImageView) findViewById(R.id.control_bottom_return);
        TextView titleTv = (TextView) findViewById(R.id.control_bottom_title);
        titleTv.setText(R.string.soft_update);

        versionNameTv = (TextView) findViewById(R.id.update_detail_tv);
        hintTv = (TextView) findViewById(R.id.update_hint_tv);
        updateBtn = (Button) findViewById(R.id.update_update_but);

        backIv.setOnClickListener(onClickListener);
        updateBtn.setOnClickListener(onClickListener);
    }

    private void initData() {

        // 注册广播
        receiver = new UpdateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(DataUtils.UpdateAction);
        registerReceiver(receiver, filter);

        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;// 获取当前的版本号
            versionNameTv.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);// 获取当前版本名
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.control_bottom_return://返回
                    finish();
                    break;
                case R.id.update_update_but://立即更新版本

                    // 发送指令下载新版本，更新应用
                    ProtocolAgreementByte pab = new ProtocolAgreementByte();
                    pab.setOrderName(DataUtils.sendUpdate);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("versionCode",newVersion);
                    pab.setContentStr(jsonObject.toString());
                    pab.assemblyData();
                    ((AppData) getApplication()).getCommandData().out_client.offer(pab.getMsg());
                    updateBtn.setText(R.string.update_btn_ing);// 开始下载，设置按钮不可点击
                    updateBtn.setClickable(false);
                    break;
            }
        }
    };
    /**
     * 广播接收
     */
    private class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            newVersion = intent.getIntExtra("versionCode", 0);

            Log.d("1111", "旧版本----" + versionCode + "新版本---" + newVersion);

            if (newVersion > versionCode) {// 有新版本可以更新
                hintTv.setText(R.string.update_hint_yes);
                updateBtn.setVisibility(View.VISIBLE);
            } else {// 没有新版本可以更新
                hintTv.setText(R.string.update_hint_no);
                updateBtn.setVisibility(View.GONE);
            }
        }
    }
}
