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
import com.iplay.car.common.adapter.VideoFileAdapterFour;
import com.iplay.car.common.bean.VideoFile;
import com.iplay.car.common.ui.BasicActivity;
import com.iplay.car.common.utils.DataUtils;
import com.iplay.car.common.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/28.
 * 视频 -- 视频列表的界面
 */
public class VideoFourActivity extends BasicActivity {

    private TextView hint;// 提示信息
    private ListView listView;
    private Intent intent;
    private ArrayList<VideoFile> list;
    private ImageView compileIv;// 编辑图标
    private ImageView allIv;// 全选、全不选图标
    private ImageView deleteIv;// 删除图标

    private VideoFileAdapterFour adapter;
    private List<Boolean> data;// 选择按钮是否选中的数据集合
    private boolean isShow = true;// 是否进入编辑模式
    private String path;// 父路径
    private String name;// 文件夹名
    private int flag;// 文件类型

    @Override
    public void onDestroy() {
        System.gc();// 建议回收垃圾
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_three);
        initView();
    }

    private void initView() {
        intent = getIntent();
        name = intent.getStringExtra("name");
//        list = intent.getParcelableArrayListExtra("list");
        path = intent.getStringExtra("path");
        flag = intent.getIntExtra("flag", 1);

        //返回
        ImageView backIv = (ImageView) findViewById(R.id.control_bottom_return);
        TextView titleTv = (TextView) findViewById(R.id.control_bottom_title);
        compileIv = (ImageView) findViewById(R.id.control_bottom_compile);
        allIv = (ImageView) findViewById(R.id.control_bottom_select);
        deleteIv = (ImageView) findViewById(R.id.control_bottom_delete);

        compileIv.setVisibility(View.VISIBLE);
        if (null != name) {
            titleTv.setText(name);
        }

        listView = (ListView) findViewById(R.id.video_list_list);
        hint = (TextView) findViewById(R.id.video_list_tv);

        backIv.setOnClickListener(onClickListener);
        compileIv.setOnClickListener(onClickListener);
        allIv.setOnClickListener(onClickListener);
        deleteIv.setOnClickListener(onClickListener);
        listView.setOnItemClickListener(onItemClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initDate();
        sendServiceMsg(DataUtils.currentView, "VideoFourActivity"); //设置当前页面
    }

    private void initDate() {

        // 判断SD卡是否可用
        if (FileUtils.sdkIsOk()) {
            // 开启子线程获取视频文件数据
            new Thread() {
                @Override
                public void run() {

                    Message msg = handler.obtainMessage();
                    msg.what = 2;
                    msg.obj = FileUtils.getVideoFileList(path + "/" + name, flag);// 获取视频文件的数据
                    handler.sendMessage(msg);

                }
            }.start();
        } else {
            hint.setText(getString(R.string.noData));
            Toast.makeText(VideoFourActivity.this, getString(R.string.noSDK), Toast.LENGTH_SHORT).show();
        }

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                finish();
            } else if (msg.what == 2) {

                list = (ArrayList<VideoFile>) msg.obj;
                if (null == list || list.size() < 1) {
                    hint.setText(R.string.noData);
                } else {
                    hint.setVisibility(View.GONE);

                    adapter = new VideoFileAdapterFour(VideoFourActivity.this, list, handler);
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
                case R.id.control_bottom_compile:// 进入编辑模式

                    compileIv.setVisibility(View.GONE);
                    deleteIv.setVisibility(View.VISIBLE);
                    allIv.setVisibility(View.VISIBLE);
                    adapter.setShow(isShow);
                    adapter.notifyDataSetChanged();

                    break;
                case R.id.control_bottom_select:// 全选、全不选

                   allIv.setSelected(isShow);
                    data = adapter.getData();
                    if (null != data) {
                        for (int i = 0; i < data.size(); i++) {
                            data.set(i, isShow);
                        }
                    }

                    isShow = !isShow;
                    adapter.setData(data);
                    adapter.notifyDataSetChanged();

                    break;
                case R.id.control_bottom_delete://删除选中的文件
                    data = adapter.getData();
                    for (int i = data.size() - 1; i > -1; i--) {
                        if (data.get(i)) {
                            if (!list.get(i).isLok()) {// 该文件不是锁定文件
                                File file = new File(list.get(i).getPath());
                                file.delete();// 删除选中的文件
                                list.remove(i);// 数据源移除
                                data.remove(i);// 选中列表移除
                            }
                        }
                    }
                    if (data.size() > 0) {// 还有数据，刷新界面
                        adapter.notifyDataSetChanged();
                    } else {// 没有数据，返回上一页面---视频之全部、锁定页面
                        finish();
                    }

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

            intent = new Intent(VideoFourActivity.this, VideoFiveActivity.class);
            intent.putParcelableArrayListExtra("list", list);
            intent.putExtra("num", position);
            startActivity(intent);

        }
    };

    protected  void selectFunction(int number){
        intent = new Intent(VideoFourActivity.this, VideoFiveActivity.class);
        intent.putParcelableArrayListExtra("list", list);
        intent.putExtra("num", number);
        startActivity(intent);
    }
}
