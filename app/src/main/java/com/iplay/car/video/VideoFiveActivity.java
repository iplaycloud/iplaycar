package com.iplay.car.video;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.iplay.car.R;
import com.iplay.car.common.bean.VideoFile;
import com.iplay.car.common.ui.BasicActivity;
import com.iplay.car.common.utils.DataUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/11/28.
 * 视频播放界面
 */
public class VideoFiveActivity extends BasicActivity {

    private SurfaceView sv;// 播放器

    private View control;// 控制框

    private TextView minTime;// 播放的时间
    private TextView maxTime;// 视频时长
    private SeekBar seekBar;// 拖动条

    private ImageView ivBack;// 返回按钮
    private ImageView ivPlay;// 播放、暂停
    private ImageView ivPrevious;// 上一部
    private ImageView ivNext;// 下一部
    private ImageView ivLock;// 是否锁定
    private ImageView ivHint;// 提示

    private View hint;// 提示信息框

    private TextView hintName;// 视频名字
    private TextView hintTime;// 视频时长
    private TextView hintSize;// 视频大小
    private TextView hintType;// 视频类别

    private ArrayList<VideoFile> list; // 视频信息集合
    private int num;// 播放的视频的编号
    private int position = 0;// 视频播放的位置
    private int allTime;// 视频的时长

    private MediaPlayer mediaPlayer;// 播放视频的工具
    private SurfaceHolder holder;

    private boolean isShow = false;// 是否显示控制框
    private Timer timer;// 定时器


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 0) {
                position = mediaPlayer.getCurrentPosition();
                minTime.setText(secToTime(position));// 更新播放的时间
                seekBar.setProgress(seekBar.getMax() * position / allTime);// 更新拖动条的位置
            } else if (msg.what == 1) {// 隐藏提示信息
                hint.setVisibility(View.GONE);
            }

        }
    };

    @Override
    public void onDestroy() {
        System.gc();// 建议回收垃圾
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sendServiceMsg(DataUtils.currentView, "VideoFiveActivity"); //设置当前页面
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_five);
        initView();
        initData();

    }

    private void initView() {

        Intent intent = getIntent();
        list = intent.getParcelableArrayListExtra("list");
        num = intent.getIntExtra("num",0);

        sv = (SurfaceView) findViewById(R.id.video_five_sv);

        control = findViewById(R.id.video_five_ll);

        minTime = (TextView) findViewById(R.id.video_five_min);
        maxTime = (TextView) findViewById(R.id.video_five_max);
        seekBar = (SeekBar) findViewById(R.id.video_five_sb);

        ivBack = (ImageView) findViewById(R.id.video_five_back);
        ivPlay = (ImageView) findViewById(R.id.video_five_play);
        ivPrevious = (ImageView) findViewById(R.id.video_five_previous);
        ivNext = (ImageView) findViewById(R.id.video_five_next);
        ivHint = (ImageView) findViewById(R.id.video_five_hint);
        ivLock = (ImageView) findViewById(R.id.video_five_lock);


        hint = findViewById(R.id.video_five_hint_rl);

        hintName = (TextView) findViewById(R.id.video_five_hint_name);
        hintTime = (TextView) findViewById(R.id.video_five_hint_time);
        hintSize = (TextView) findViewById(R.id.video_five_hint_size);
        hintType = (TextView) findViewById(R.id.video_five_hint_type);

        sv.setOnClickListener(onClickListener);
        ivBack.setOnClickListener(onClickListener);
        ivPlay.setOnClickListener(onClickListener);
        ivPrevious.setOnClickListener(onClickListener);
        ivNext.setOnClickListener(onClickListener);
        ivHint.setOnClickListener(onClickListener);
        ivLock.setOnClickListener(onClickListener);


    }

    private void initData() {

        mediaPlayer = new MediaPlayer();
        holder = sv.getHolder();

        mediaPlayer.setOnCompletionListener(onCompletionListener);
        holder.addCallback(callback);
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        timer = new Timer();
        timer.schedule(timerTask, 0, 1000);

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.video_five_sv:// 是否显示控制器
                    if (isShow) {
                        control.setVisibility(View.VISIBLE);
                    } else {
                        control.setVisibility(View.GONE);
                        hint.setVisibility(View.GONE);
                    }
                    isShow = !isShow;
                    break;
                case R.id.video_five_back:// 返回上一层---视频信息页面
                    finish();
                    break;
                case R.id.video_five_play:// 播放、暂停

                    if (mediaPlayer.isPlaying()) {
                        ivPlay.setImageResource(R.drawable.selector_video_five_pause);
                        mediaPlayer.pause();
                    } else {
                        ivPlay.setImageResource(R.drawable.selector_video_five_play);
                        mediaPlayer.start();
                    }

                    break;
                case R.id.video_five_previous:// 上一部

                    if (num < 1) {
                        num = list.size();
                    }
                    --num;
                    toPlay();

                    break;
                case R.id.video_five_next:// 下一部

                    if (num > list.size() - 2) {
                        num = -1;
                    }
                    ++num;
                    toPlay();

                    break;
                case R.id.video_five_hint:// 显示提示信息，3秒后隐藏

                    String name = list.get(num).getName();

                    hintName.setText(name);
                    hintTime.setText(secToTime(allTime));
                    hintSize.setText(list.get(num).getSize() + "MB");
                    hintType.setText(name.substring(name.lastIndexOf(".") + 1).toUpperCase());

                    hint.setVisibility(View.VISIBLE);

                    handler.sendEmptyMessageDelayed(1, 3000);
                    break;
                case R.id.video_five_lock:// 是否锁定文件
                    if (list.get(num).isLok()) {
                        ivLock.setImageResource(R.drawable.selector_video_four_unlock);
                        toReplaceName(DataUtils.UNLOCK, false);
                    } else {
                        ivLock.setImageResource(R.drawable.selector_video_four_lock);
                        toReplaceName(DataUtils.LOCK, true);
                    }

                    break;

            }

        }
    };

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (position == 0) {
                toPlay();
            }
        }
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    };

    // 播放完视频的监听
    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (num >= list.size() - 1) {
                num = -1;
            }
            ++num;
            Toast.makeText(VideoFiveActivity.this, "播放下一部视频", Toast.LENGTH_SHORT).show();
            toPlay();

        }
    };
    // 拖动条的拖动监听
    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            position = progress * allTime / seekBar.getMax();// 获取拖动条拖动的位置
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mediaPlayer.seekTo(position);// 拖动条拖动停止后播放拖动条所在的位置
        }
    };

    // 定时发送消息给handler
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (mediaPlayer == null) {
                return;
            }
            if (mediaPlayer.isPlaying() && seekBar.isPressed() == false) {
                handler.sendEmptyMessage(0);
            }
        }
    };

    private void toPlay() {
        try {
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(list.get(num).getPath());// 设置路径
            mediaPlayer.setDisplay(holder);
            mediaPlayer.prepare();
            mediaPlayer.start();// 播放视频
            position = 0;// 设置开始时间为 0
            allTime = mediaPlayer.getDuration(); // 获取视频的时长

            minTime.setText("00:00:00");
            maxTime.setText(secToTime(allTime));

            // 视频文件是否为锁定文件
            if (list.get(num).isLok()) {
                ivLock.setImageResource(R.drawable.selector_video_four_lock);
            } else {
                ivLock.setImageResource(R.drawable.selector_video_four_unlock);
            }
            ivPlay.setImageResource(R.drawable.selector_video_five_play);
            seekBar.setProgress(position);// 将拖动条移至开始位置
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 修改文件名，及数据
    private void toReplaceName(String str, boolean isLock) {
        String name = list.get(num).getName();
        name = name.replace(name.substring(0, str.length()), str);
        list.get(num).setName(name);// 修改名字

        String path = list.get(num).getPath();
        File file = new File(path);
        path = file.getParent() + "/" + name;
        list.get(num).setPath(path);// 修改路径

        list.get(num).setLok(isLock);// 修改文件是否锁定

        file.renameTo(new File(path));// 修改文件
    }

    public String secToTime(int time) {
        time = time / 1000;
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0) {
            return "00:00:00";
        } else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99) {
                    return "99:59:59";
                }
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    public String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10) {
            retStr = "0" + Integer.toString(i);
        } else {
            retStr = "" + i;
        }
        return retStr;
    }

    // 回收资源
    @Override
    public void onPause() {
        timer.cancel();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
        super.onPause();
    }

    protected  void selectFunction(int number){
    }
}
