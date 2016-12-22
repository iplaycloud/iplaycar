package com.iplay.car.setting.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.iplay.car.R;
import com.iplay.car.common.base.BaseActivity;
import com.iplay.car.common.utils.DataUtils;
import com.iplay.car.common.utils.StringUtil;
import com.iplay.car.common.utils.WifiHotUtil;

/**
 * Created by Administrator on 2016/11/24.
 * WiFi热点设置界面
 */
public class WiFiHotActivity extends BaseActivity {

    private WifiHotUtil wifiHotUtil;
    private ToggleButton hotTb;// 热点开关
    private TextView hotOpenName;// 热点开关处的热点名
    private SharedPreferences sharedPreferences;
    private String hotName;// 热点名
    private String hotPassword;// 热点密码
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    hotOpenName.setText(hotName);// 显示热点名
                    hotOpenName.setVisibility(View.VISIBLE);
                    wifiHotUtil.startWifiAp(hotName, hotPassword);// 打开热点
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifihot);

        initView();//初始化控件
        initDate();

    }

    private void initView() {

        //返回按钮
        ImageView control_return = (ImageView) findViewById(R.id.control_bottom_return);
        TextView control_title = (TextView) findViewById(R.id.control_bottom_title);
        control_title.setText(R.string.set_wifi_hot);

        //热点开关
        hotTb = (ToggleButton) findViewById(R.id.hot_open_tb);
        //热点开关处的热点名
        hotOpenName = (TextView) findViewById(R.id.hot_state_tv);

        //热点设置
        RelativeLayout hotSetRl = (RelativeLayout) findViewById(R.id.hot_set_rl);

        control_return.setOnClickListener(onClickListener);
        hotSetRl.setOnClickListener(onClickListener);
    }

    private void initDate() {
        wifiHotUtil = new WifiHotUtil(WiFiHotActivity.this);
        sharedPreferences = getSharedPreferences(DataUtils.NAME, Context.MODE_PRIVATE);
        hotName = sharedPreferences.getString(DataUtils.hotName, "XinChen_Mirror_" + StringUtil.getRandomString(4, "ABCDEFGHIJKLMNOPQRSTUVWSYZ"));
        hotPassword = sharedPreferences.getString(DataUtils.hotPassword, "12345678");
        if (wifiHotUtil.isWifiApEnabled()) {// 热点是否打开
            hotTb.setChecked(true);
            hotOpenName.setText(hotName);// 热点打开时显示热点名字
            hotOpenName.setVisibility(View.VISIBLE);
        } else {
            hotTb.setChecked(false);
        }
        openHotWifi();

    }

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //返回点击事件
            switch (v.getId()) {
                case R.id.control_bottom_return:// 返回
                    finish();
                    break;
                case R.id.hot_set_rl:// 显示设置热点的对话框
                    showAddDialog();
                    break;
            }
        }
    };

    /**
     * wifi热点开关的点击时间
     */
    private void openHotWifi() {

        hotTb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    wifiHotUtil.startWifiAp(hotName, hotPassword);// 打开热点
                    hotOpenName.setText(hotName);//显示热点开关处的热点名
                    hotOpenName.setVisibility(View.VISIBLE);
                } else {
                    wifiHotUtil.closeWifiAp();// 关闭热点
                    hotOpenName.setVisibility(View.INVISIBLE);// 隐藏热点开关处的热点名
                }
            }
        });
    }

    /**
     * 展示dialog
     */
    private void showAddDialog() {

        View textEntryView = LayoutInflater.from(WiFiHotActivity.this).inflate(R.layout.wifihot_item_dialog_layout, null);
        final EditText nameEt = (EditText) textEntryView.findViewById(R.id.et_wifihot_item_ssid);
        final EditText passwordEt = (EditText) textEntryView.findViewById(R.id.et_wifihot_item_pass);

        nameEt.setText(hotName);
        passwordEt.setText(hotPassword);
        CheckBox showCb = (CheckBox) textEntryView.findViewById(R.id.cb_wifihot_item);
        showCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // 文本正常显示
                    passwordEt.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    Editable e = passwordEt.getText();
                    Selection.setSelection(e, e.length());
                } else {
                    // 文本以密码形式显示
                    passwordEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    // 下面两行代码实现：输入框光标一直在输入文本后面
                    Editable e = passwordEt.getText();
                    Selection.setSelection(e, e.length());
                }
            }
        });
        AlertDialog.Builder ad1 = new AlertDialog.Builder(WiFiHotActivity.this);
        ad1.setTitle(R.string.set_wifi_hot);
        ad1.setIcon(android.R.drawable.ic_dialog_info);
        ad1.setView(textEntryView);
        ad1.setCancelable(false);
        ad1.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameEt.getText().toString().trim();
                String password = passwordEt.getText().toString().trim();

                if (name.endsWith(hotName) && password.endsWith(hotPassword)) {// 未做修改动作

                } else {// 修改了
                    hotName = name;
                    hotPassword = password;
                    SharedPreferences.Editor edit = sharedPreferences.edit();// 保存修改后的热点名和密码
                    edit.putString(DataUtils.hotName, hotName);
                    edit.putString(DataUtils.hotPassword, hotPassword);
                    edit.commit();

                    if (wifiHotUtil.isWifiApEnabled()) {// 如果热点是打开状态时,先关闭热点，500毫秒后在开启热点
                        hotTb.setChecked(false);
                        wifiHotUtil.closeWifiAp();// 关闭热点
                        hotOpenName.setVisibility(View.INVISIBLE);
                        mHandler.sendEmptyMessageDelayed(0, 500);
                    }
                }
            }
        });
        ad1.setNegativeButton(R.string.cancle, null);
        ad1.show();
    }
}