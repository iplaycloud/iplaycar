package com.iplay.car.common.ui;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.iplay.car.R;
import com.iplay.car.common.base.BaseActivity;
import com.iplay.car.common.utils.DataUtils;
/**
 * 行车记录页面
 */
public class DrivingRecodeActivity extends BaseActivity {
    private String TAG = "DrivingRecodeActivity";
    private ImageView photoButton; // 拍照按钮
    private ImageView videoButton; // 摄像按钮
    private TextView timeTextView;// 定时器显示器
    private ImageView back;// 返回按钮
    private ImageView voice;// 语音按钮
    private ImageView time;// 设置时间按钮
    private ImageView date;//
    private ImageView photo;// 图片按钮
    private ImageView lock;// 锁定按钮
    private ImageView video;// 视频按钮
    private Intent intent;
    private RecodeReceiver receiver;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private boolean isRecording; // false表示没有录像，点击开始；true表示正在录像，点击暂停
    private boolean isLock;// 视频文件是否锁定
    private boolean threeOrFive;// 录制的视频时长为3分钟还是5分钟
    private ImageView iv;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.drivingrecodeactivity_layout);

        sharedPreferences = getSharedPreferences(DataUtils.NAME, Context.MODE_PRIVATE);

        initViews();
        initData();
        // 发送广播给服务，改变悬浮窗口的位置及大小
        intent = new Intent();
        intent.setAction(DataUtils.ServiceViewAction);
        intent.putExtra("size", 2);
//        intent.putExtra("show", (android.os.Parcelable) showMv);
        sendBroadcast(intent);
//        handler.sendEmptyMessageDelayed(1, 1000);

    }

    private Handler handler = new Handler() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 1) {

                // 发送广播给服务，改变悬浮窗口的位置及大小
                intent = new Intent();
                intent.setAction(DataUtils.ServiceViewAction);
                intent.putExtra("size", 2);
                sendBroadcast(intent);

            } else if (msg.what == 100) {
                byte[] data = (byte[]) msg.obj;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            }

        }
    };

    private ShowMV showMv = new ShowMV() {
        @Override
        public void getData(byte[] data) {

            Message msg = handler.obtainMessage();
            msg.what = 100;
            msg.obj = data;

        }
    };


    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    // 初始化视图组件
    private void initViews() {
        photoButton = (ImageView) findViewById(R.id.drive_photo_iv);
        videoButton = (ImageView) findViewById(R.id.drive_switch_camera);

        back = (ImageView) findViewById(R.id.drive_bottom_back);
        voice = (ImageView) findViewById(R.id.drive_bottom_voice);
        time = (ImageView) findViewById(R.id.drive_bottom_time);
        date = (ImageView) findViewById(R.id.drive_bottom_video);
        photo = (ImageView) findViewById(R.id.drive_bottom_picture);
        lock = (ImageView) findViewById(R.id.drive_bottom_lock);
        video = (ImageView) findViewById(R.id.drive_bottom_record);

        // 注册广播接收器
        receiver = new RecodeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DataUtils.RecodeReceiverAction);
        registerReceiver(receiver, intentFilter);

        ButtonOnClickListener onClickListener = new ButtonOnClickListener();
        photoButton.setOnClickListener(onClickListener);
        videoButton.setOnClickListener(onClickListener);

        back.setOnClickListener(onClickListener);
        voice.setOnClickListener(onClickListener);
        time.setOnClickListener(onClickListener);
        date.setOnClickListener(onClickListener);
        photo.setOnClickListener(onClickListener);
        lock.setOnClickListener(onClickListener);
        video.setOnClickListener(onClickListener);

    }

    private void initData() {

        int videoTime = sharedPreferences.getInt(DataUtils.videoTime, 300000);// 录制视频的时长默认为5分钟
        threeOrFive = videoTime == 300000;
        showVideoTime(threeOrFive);

        isLock = sharedPreferences.getBoolean(DataUtils.LOCK, false);// 默认录制的视频文件为不锁定状态
        showVideoLock(isLock);


    }

    class ButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.drive_switch_camera: // 点击开始录像
                    videoClick();
                    Toast.makeText(DrivingRecodeActivity.this, isRecording ? "点击开始录像" : "点击停止录像", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.drive_photo_iv:// 点击拍照
                    photoClick();
                    Toast.makeText(DrivingRecodeActivity.this, "点击开始拍照", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.drive_bottom_back:// 返回主界面
                    finish();
                    break;
                case R.id.drive_bottom_voice:// 语音
                    break;
                case R.id.drive_bottom_time:// 时间

                    threeOrFive = !threeOrFive;
                    showVideoTime(threeOrFive);
                    editor = sharedPreferences.edit();
                    editor.putInt(DataUtils.videoTime, threeOrFive ? 300000 : 180000);// 存储录制视频的时长
                    editor.commit();

                    break;
                case R.id.drive_bottom_video://
                    break;
                case R.id.drive_bottom_picture:// 照片
                    break;
                case R.id.drive_bottom_lock:// 锁定

                    isLock = !isLock;
                    showVideoLock(isLock);
                    editor = sharedPreferences.edit();
                    editor.putBoolean(DataUtils.videoLock, isLock);// 存储录制视频的锁定标识
                    editor.commit();

                    break;
                case R.id.drive_bottom_record:// 视频
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 拍照的方法,发送广播通知服务拍照
     */
    public void photoClick() {

        intent = new Intent();
        intent.setAction(DataUtils.ServicePhotoAction);
        sendBroadcast(intent);

    }

    /**
     * 录制视频的方法，发送广播通知服务开始、结束录制视频
     */
    public void videoClick() {

        if (!isRecording) {// 未录制视频时，开始录制视频
            intent = new Intent();
            intent.setAction(DataUtils.ServiceVideoAction);
            intent.putExtra("video", "start");
            sendBroadcast(intent);
            isRecording = !isRecording;
            showVideoView(isRecording);

        } else { // 正在录制视频时，停止录制
            intent = new Intent();
            intent.setAction(DataUtils.ServiceVideoAction);
            intent.putExtra("video", "stop");
            sendBroadcast(intent);
            isRecording = !isRecording;
            showVideoView(isRecording);
        }

    }

    // 广播接收者,获取视频录制状态
    private class RecodeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // 是否在录制视频；false表示正在录制、true表示未录制
            isRecording = intent.getBooleanExtra("isRecording", true);

            showVideoView(isRecording);// 显示对应的图片

        }
    }

    // 录制视频按钮显示对应的图标
    private void showVideoView(boolean isSelected) {

        if (isSelected) {// 录制视频时

            videoButton.setSelected(isSelected);

        } else {// 未录制视频时

            videoButton.setSelected(isSelected);

        }
    }

    // 录制视频时长按钮显示对应的图标
    private void showVideoTime(boolean isSelected) {

        if (isSelected) {// 录制时长为5分钟

            time.setImageResource(R.drawable.selector_drive_time_five);

        } else {// 录制视频时长为3分钟

            time.setImageResource(R.drawable.selector_drive_time_three);

        }
    }

    // 视频锁定按钮显示对应的图标
    private void showVideoLock(boolean isSelected) {

        if (isSelected) {// 录制视频为锁定时

            lock.setImageResource(R.drawable.selector_video_four_lock);

        } else {// 录制视频为不锁定时

            lock.setImageResource(R.drawable.selector_video_four_unlock);

        }
    }


    private interface ShowMV {
        public void getData(byte[] data);
    }

}
