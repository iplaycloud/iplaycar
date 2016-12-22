package com.iplay.car.video;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.iplay.car.R;
import com.iplay.car.common.adapter.VideoFileAdapterThree;
import com.iplay.car.common.ui.BasicActivity;
import com.iplay.car.common.utils.DataUtils;
import com.iplay.car.common.utils.FileUtils;

import java.util.List;

/**
 * Created by Administrator on 2016/11/25.
 * 视频————日期文件夹界面
 */
public class VideoThreeActivity extends BasicActivity {

    private TextView hint;// 提示信息
    private ListView listView;
//    private List<VideoFileBean> list;// 视频文件数据集合
    private List<String> date;// 视频文件夹名称的集合
    private VideoFileAdapterThree adapter;
    private Intent intent;
    private String path;// 文件路径
    private int flag;// 文件的类型

    @Override
    public void onDestroy() {
        System.gc();// 建议回收垃圾
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_three);
        initViews();
    }

    private void initViews() {
        intent = getIntent();
        String folder = intent.getStringExtra("path");
        flag = intent.getIntExtra("flag", 1);

        if (null != folder) {// 收到路径，拼接完整的路径
            path = DataUtils.videoPath + folder;
        } else {// 为收到数据，默认为前录视频的路径
            path = DataUtils.videoPath + DataUtils.BEFORE;
        }

        //返回
        ImageView backIv = (ImageView) findViewById(R.id.control_bottom_return);
        TextView titleTv = (TextView) findViewById(R.id.control_bottom_title);

        if (flag == 1) {
            titleTv.setText(R.string.video_all);// 显示所有视频
        } else {
            titleTv.setText(R.string.video_lok);// 显示锁定视频
        }

        listView = (ListView) findViewById(R.id.video_list_list);
        hint = (TextView) findViewById(R.id.video_list_tv);

        backIv.setOnClickListener(onClickListener);
        listView.setOnItemClickListener(onItemClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
        sendServiceMsg(DataUtils.currentView, "VideoThreeActivity"); //设置当前页面
    }

    private void initData() {
        // 判断SD卡是否可用
        if (FileUtils.sdkIsOk()) {
            // 开启子线程获取视频文件数据
            new Thread() {
                @Override
                public void run() {

                    Message msg = handler.obtainMessage();
                    msg.what = 23;
//                    msg.obj = FileUtils.getVideoList(path, flag);// 获取视频文件的数据
                    msg.obj = FileUtils.getVideoFolderList(path, flag);// 获取视频文件的数据
                    handler.sendMessage(msg);

                }
            }.start();
        } else {
            hint.setText(getString(R.string.noData));
            Toast.makeText(VideoThreeActivity.this, getString(R.string.noSDK), Toast.LENGTH_SHORT).show();
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 23) {

//                list = (List<VideoFileBean>) msg.obj;
                date = (List<String>) msg.obj;
                if (null == date || date.size() < 1) {
                    Toast.makeText(VideoThreeActivity.this, "0000000", Toast.LENGTH_SHORT).show();
                    listView.setVisibility(View.GONE);
                    hint.setVisibility(View.VISIBLE);
                    hint.setText(getString(R.string.noData));
                } else {
                    hint.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    Toast.makeText(VideoThreeActivity.this, "1111111111", Toast.LENGTH_SHORT).show();
//                    adapter = new VideoFileAdapterThree(VideoThreeActivity.this, list);
                    adapter = new VideoFileAdapterThree(VideoThreeActivity.this, date);
                    listView.setAdapter(adapter);
                }
            }
        }
    };

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.control_bottom_return:
                    finish();
                    break;
            }
        }
    };

    /**
     * ListView的点击事件
     */
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            intent = new Intent(VideoThreeActivity.this, VideoFourActivity.class);
            intent.putExtra("path", path);
            intent.putExtra("name", date.get(position));
            intent.putExtra("flag", flag);
//            intent.putParcelableArrayListExtra("list",list.get(position).getList());

            startActivity(intent);

        }
    };

    protected  void selectFunction(int number){
        intent = new Intent(VideoThreeActivity.this, VideoFourActivity.class);
        intent.putExtra("path", path);
        intent.putExtra("name", date.get(number));
        intent.putExtra("flag", flag);
//            intent.putParcelableArrayListExtra("list",list.get(position).getList());
        startActivity(intent);
    }
}
