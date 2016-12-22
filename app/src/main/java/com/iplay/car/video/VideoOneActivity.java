package com.iplay.car.video;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iplay.car.R;
import com.iplay.car.common.ui.BasicActivity;
import com.iplay.car.common.utils.DataUtils;

/**
 * Created by Administrator on 2016/11/25.
 * 视频————前、后视频界面
 */
public class VideoOneActivity extends BasicActivity {
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_one);
        initViews();
    }

    @Override
    protected void onResume() {
        sendServiceMsg(DataUtils.currentView, "VideoOneActivity"); //设置当前页面
        super.onResume();
    }

    /**
     * 初始化界面控制
     */
    private void initViews() {
        //返回
        ImageView backIv = (ImageView) findViewById(R.id.control_bottom_return);
        TextView titleTv = (TextView) findViewById(R.id.control_bottom_title);
        titleTv.setText(R.string.video);

        // 前录视频
        RelativeLayout videoOneRl = (RelativeLayout) findViewById(R.id.video_one_rl);
        TextView tv1 = (TextView) findViewById(R.id.video_one_tv);
        tv1.setText(R.string.video_before);
        // 后录视频
        RelativeLayout videoTwoRl = (RelativeLayout) findViewById(R.id.video_two_rl);
        TextView tv2 = (TextView) findViewById(R.id.video_two_tv);
        tv2.setText(R.string.video_after);

        backIv.setOnClickListener(onClickListener);
        videoOneRl.setOnClickListener(onClickListener);
        videoTwoRl.setOnClickListener(onClickListener);

    }

    /**
     * 点击事件 VID5456.MP4     LOK21535.MP4
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.control_bottom_return://返回上一层页面---主页面
                    finish();
                    break;
                case R.id.video_one_rl:// 跳转到前录视频页面
                    intent = new Intent(VideoOneActivity.this, VideoTwoActivity.class);
                    intent.putExtra("path", DataUtils.BEFORE);
                    startActivity(intent);
                    break;
                case R.id.video_two_rl:// 跳转到后录视频页面
                    intent = new Intent(VideoOneActivity.this, VideoTwoActivity.class);
                    intent.putExtra("path", DataUtils.AFTER);
                    startActivity(intent);
                    break;
            }
        }
    };

    protected  void selectFunction(int number){
        if(number == 1){
            intent = new Intent(VideoOneActivity.this, VideoTwoActivity.class);
            intent.putExtra("path", DataUtils.BEFORE);
            startActivity(intent);
        }else if(number == 2){
            intent = new Intent(VideoOneActivity.this, VideoTwoActivity.class);
            intent.putExtra("path", DataUtils.AFTER);
            startActivity(intent);
        }
    }
}
