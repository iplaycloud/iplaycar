package com.iplay.car.picture;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.iplay.car.R;
import com.iplay.car.common.adapter.PhotoFileAdapter;
import com.iplay.car.common.bean.PhotoFileBean;
import com.iplay.car.voice.service.MainService;
import com.iplay.car.common.ui.BasicActivity;
import com.iplay.car.common.utils.DataUtils;
import com.iplay.car.common.utils.FileUtils;

import java.util.List;

/**
 * Created by Administrator on 2016/11/30.
 * 图片1 --- 文件夹界面
 */
public class PictureOneActivity extends BasicActivity {

    private TextView hint; // 提示数据
    private GridView file;
    private List<PhotoFileBean> list;// 数据集合y:y

    @Override
    public void onDestroy() {
        System.gc();// 建议回收垃圾
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_one);
        initView();
    }

    @Override
    protected void onResume() {
        initData();
        sendServiceMsg(DataUtils.currentView, "PictureOneActivity"); //设置当前页面
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
     * 初始化界面控制
     */
    private void initView() {

        ImageView returnIv = (ImageView) findViewById(R.id.control_bottom_return);
        TextView titleTv = (TextView) findViewById(R.id.control_bottom_title);
        titleTv.setText(getString(R.string.photo));

        file = (GridView) findViewById(R.id.gv_picture);
        hint = (TextView) findViewById(R.id.picture_layout_tv);

        file.setOnItemClickListener(onItemClickListener);
        returnIv.setOnClickListener(onClickListener);
    }

    /**
     * 获取数据
     */
    private void initData() {

        // 判断SD卡是否可用
        if (FileUtils.sdkIsOk()) {

            // 开启子线程查找指定路径下的图片
            new Thread() {
                @Override
                public void run() {

                    List<PhotoFileBean> files = FileUtils.getFileList(DataUtils.photoPath, ".jpg");
//                    List<PhotoFileBean> files = FileUtils.getFileList(DataUtils.photoPath, ".png");

                    if (null != handler) {
                        Message msg = handler.obtainMessage();
                        msg.what = 4;
                        msg.obj = files; // 把数据发给handler处理
                        handler.sendMessage(msg);
                    }
                }
            }.start();

        } else {
            hint.setVisibility(View.VISIBLE);
            file.setVisibility(View.GONE);
            hint.setText(getString(R.string.noData));
            Toast.makeText(PictureOneActivity.this, getString(R.string.noSDK), Toast.LENGTH_SHORT).show();
        }

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 4) {

                // 获取图片文件的数据
                list = (List<PhotoFileBean>) msg.obj;
                if (null != list && list.size() > 0) {
                    // 有数据的时候显示文件夹
                    hint.setVisibility(View.GONE);
                    file.setVisibility(View.VISIBLE);
                    PhotoFileAdapter adapter = new PhotoFileAdapter(PictureOneActivity.this, list);
                    file.setAdapter(adapter);
                } else {
                    // 没有数据
                    hint.setVisibility(View.VISIBLE);
                    file.setVisibility(View.GONE);
                    hint.setText(getString(R.string.noData));
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
                case R.id.control_bottom_return:// 返回
                    finish();
                    break;
            }
        }
    };

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(PictureOneActivity.this,PictureTwoActivity.class);
            intent.putExtra("name", list.get(position).getName());
            startActivity(intent);
        }
    };

    protected  void selectFunction(int number){
        int size = 0;
        if(null != list){
            size = list.size();
        }
        if(number > 0) {
            if (size > 0) {
                Intent intent = new Intent(PictureOneActivity.this,PictureTwoActivity.class);
                intent.putExtra("name", list.get(number).getName());
                startActivity(intent);
            }
        }
    }
}
