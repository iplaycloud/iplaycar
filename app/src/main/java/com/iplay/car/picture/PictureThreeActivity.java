package com.iplay.car.picture;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.iplay.car.R;
import com.iplay.car.common.adapter.PhotoFileAdapterThree;
import com.iplay.car.common.ui.BasicActivity;
import com.iplay.car.common.utils.DataUtils;
import com.iplay.car.common.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/30.
 * 图片 3 —— 大图界面
 */
public class PictureThreeActivity extends BasicActivity {

    private ViewPager viewPager;//图片
    private ImageView deleteIv;//删除的图标

    private String name;// 文件夹的名称
    private ArrayList<String> list;// 文件下图片的路径集合
    private int selected;// 需要显示的图片位置
    private List<ImageView> data;
    private PhotoFileAdapterThree adapter;

    @Override
    public void onDestroy() {
        data.clear();// 回收资源
        data = null;
        System.gc();// 建议回收垃圾
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_three);
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sendServiceMsg(DataUtils.currentView, "PictureThreeActivity"); //设置当前页面
    }

    private void initView() {

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        list = intent.getStringArrayListExtra("list");
        selected = intent.getIntExtra("num", 0);

        viewPager = (ViewPager) findViewById(R.id.photo_three_vp);

        //返回
        ImageView backIv = (ImageView) findViewById(R.id.control_right_return);
        TextView titleTv = (TextView) findViewById(R.id.control_right_title);
        deleteIv = (ImageView) findViewById(R.id.control_right_delete);
        if (null != name) {
            titleTv.setText(name);
        }
        deleteIv.setVisibility(View.VISIBLE);

        backIv.setOnClickListener(onClickListener);
        deleteIv.setOnClickListener(onClickListener);
        viewPager.addOnPageChangeListener(onPageChangeListener);
    }

    private void initData() {

        data = new ArrayList<ImageView>();
        for (int i = 0; i < list.size(); i++) {
            ImageView iv = new ImageView(PictureThreeActivity.this);

            iv.setImageBitmap(FileUtils.ratio(list.get(i), 640, 270));
            data.add(iv);
        }

        adapter = new PhotoFileAdapterThree(data);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(selected);

    }

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // 返回上一页面
                case R.id.control_right_return:
                    finish();
                    break;
                // 删除选择的图片
                case R.id.control_right_delete:

                    File file = new File(list.get(selected));
                    file.delete();
                    list.remove(selected);
                    data.remove(selected);

                    selected = selected > 0 ? selected - 1 : selected;
                    if (list.size() == 0) {
                        //如果没有图片了，跳回图片文件夹页面
                        finish();
                    } else {
                        // 显示前一张图片，如果只有一张则显示该张
                        adapter.notifyDataSetChanged();
                        viewPager.setCurrentItem(selected);
                    }
                    break;
            }
        }
    };

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            selected = position;// 当前显示的图片的位置
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    protected  void selectFunction(int number){
    }
}
