package com.iplay.car.setting.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.iplay.car.R;
import com.iplay.car.common.base.BaseActivity;

/**
 * Created by Administrator on 2016/11/24.
 * 系统设置界面
 */
public class SystemActivity extends BaseActivity {

    private SeekBar lightSb;// 设置亮度的进度条
    private SeekBar voiceSb;// 设置声音的进度条
    private ToggleButton offTb;// 碰撞设置的开关
    private Intent intent;

    protected static final int PROGRESS_CHANGED = 0x101;
    public AudioManager audioManager;
    private Handler myHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case PROGRESS_CHANGED:
                    setVolume();// 与系统音量保持一致
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system);
        initViews();
        initDates();
    }

    private void initViews() {
        //亮度调节
        lightSb = (SeekBar) findViewById(R.id.system_light_sb);
        //声音调节
        voiceSb = (SeekBar) findViewById(R.id.system_voice_sb);
        //打开熄灭开关
        offTb = (ToggleButton) findViewById(R.id.system_off_tb);

        //碰撞管理
        RelativeLayout collisionRl = (RelativeLayout) findViewById(R.id.system_collision_rl);
        //流量管理
        RelativeLayout flowRl = (RelativeLayout) findViewById(R.id.system_flow_rl);
        //热点设置
        RelativeLayout hotRl = (RelativeLayout) findViewById(R.id.system_hot_rl);
        // SD卡信息
        RelativeLayout sdRl = (RelativeLayout) findViewById(R.id.system_sd_rl);
        //二维码
        RelativeLayout codeRl = (RelativeLayout) findViewById(R.id.system_code_rl);
        // 恢复出厂设置
        RelativeLayout RegainRl = (RelativeLayout) findViewById(R.id.system_regain_rl);

        //返回按钮
        ImageView control_return = (ImageView) findViewById(R.id.control_bottom_return);
        TextView control_title = (TextView) findViewById(R.id.control_bottom_title);
        control_title.setText(R.string.system_settings);

        control_return.setOnClickListener(onClickListener);
        collisionRl.setOnClickListener(onClickListener);
        flowRl.setOnClickListener(onClickListener);
        hotRl.setOnClickListener(onClickListener);
        sdRl.setOnClickListener(onClickListener);
        codeRl.setOnClickListener(onClickListener);
        RegainRl.setOnClickListener(onClickListener);

    }

    private void initDates() {

        //声音调节
        setVolume();
        //亮度调节
        lightSb.setProgress((int) (android.provider.Settings.System.getInt(
                getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, 255)) - 30);
        voice();
        light();
    }

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // 返回
                case R.id.control_bottom_return:
                    finish();
                    break;
                case R.id.system_collision_rl:// 跳转到碰撞设置界面
                    intent = new Intent(SystemActivity.this, ImpactActivity.class);
                    startActivity(intent);
                    break;
                case R.id.system_flow_rl:// 跳转到流量管理界面
                    intent = new Intent(SystemActivity.this, FlowActivity.class);
                    startActivity(intent);
                    break;
                case R.id.system_hot_rl:// 跳转到热点设置界面
                    intent = new Intent(SystemActivity.this, WiFiHotActivity.class);
                    startActivity(intent);
                    break;
                case R.id.system_sd_rl:// 跳转到SD卡信息界面
                    intent = new Intent(SystemActivity.this, SDCardsActivity.class);
                    startActivity(intent);
                    break;
                case R.id.system_code_rl:// 跳转到二维码界面
                    intent = new Intent(SystemActivity.this, QRCodeActivity.class);
                    startActivity(intent);
                    break;
                case R.id.system_regain_rl://恢复出厂设置
                    restoreFactory();
                    break;

            }
        }
    };

    /**
     * 亮度调节监听
     */
    public void light() {
        lightSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //当拖动条发生变化时调用
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    Integer tmpInt = seekBar.getProgress();
                    Log.i("TAG", "-----tmpInt=" + tmpInt);
                    android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, tmpInt);
                    tmpInt = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
                    WindowManager.LayoutParams lp = getWindow().getAttributes();
                    Float tmpFloat = (float) (tmpInt / 255);
                    if (0 < tmpFloat && tmpFloat <= 1) {
                        lp.screenBrightness = tmpFloat;
                    }
                    // getActivity().getWindow().setAttributes(lp);
                }
            }
            //当用户开始滑动滑块时开始调用
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            //当用户停止滑动滑块时开始调用
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /**
     * 音量调节监听
     */
    public void voice() {
        voiceSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //当拖动条发生变化时调用
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, progress, 0);
            }
            //当用户开始滑动滑块时开始调用
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            //当用户停止滑动滑块时开始调用
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        new Thread(new MyVolThread()).start();
    }

    /**
     * 与系统音量保持一致
     */
    private void setVolume() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        voiceSb.setMax(maxVolume);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        voiceSb.setProgress(currentVolume);
    }

    class MyVolThread implements Runnable {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Message message = new Message();
                message.what = PROGRESS_CHANGED;
                myHandler.sendMessage(message);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 熄屏的按钮监听
     */
    public void flush() {
        offTb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });
    }

    /**
     * 恢复出厂设置的方法
     */
    private void restoreFactory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SystemActivity.this);
        builder.setMessage(R.string.reset_phone);
        builder.setTitle(R.string.factor_reset);
        builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
            }
        });
        builder.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

}
