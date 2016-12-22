package com.iplay.car.picture;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.iplay.car.R;
import com.iplay.car.common.adapter.PhotoFileAdapterTwo;
import com.iplay.car.common.ui.BasicActivity;
import com.iplay.car.common.utils.DataUtils;
import com.iplay.car.common.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/30.
 * 图片 2 -- 小图界面
 */
public class PictureTwoActivity extends BasicActivity {

    private ImageView compileIv;// 编辑图标
    private ImageView allIv;// 全选、全不选图标
    private ImageView deleteIv;// 删除图标
    private TextView hint; // 提示数据
    private GridView file;
    private String name;// 文件夹名
    private ArrayList<String> list;// 文件夹下图片的路径

    private PhotoFileAdapterTwo adapterTwo;//适配器
    private List<Boolean> data;//图片是否为选中的数据集合
    private boolean isCheck = true;
    private Intent intent;

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

    /**
     * 初始化界面控制
     */
    private void initView() {

        intent = getIntent();
        name = intent.getStringExtra("name");

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

        file = (GridView) findViewById(R.id.gv_picture);
        hint = (TextView) findViewById(R.id.picture_layout_tv);

        file.setOnItemClickListener(onItemClickListener);

        backIv.setOnClickListener(onClickListener);
        compileIv.setOnClickListener(onClickListener);
        allIv.setOnClickListener(onClickListener);
        deleteIv.setOnClickListener(onClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
        sendServiceMsg(DataUtils.currentView, "PictureTwoActivity"); //设置当前页面
    }

    /**
     * 获取数据
     */
    private void initData() {

        if (FileUtils.sdkIsOk()) {
            File folder = new File(DataUtils.photoPath + "/" + name);
            if (folder.exists()) {
                File[] pictures = folder.listFiles();
                list = new ArrayList<>();
                for (int i = 0; i < pictures.length; i++) {
                    if (pictures[i].isFile()) { // 该文件为文件时
                        String photoName = pictures[i].getName();// 获取文件名
                        if (photoName.endsWith(".jpg")) {// 该文件为需要获取的文件
                            list.add(pictures[i].getAbsolutePath());// 将文件路径添加到集合中
                        }
                    }
                }
            }
            if (null == list || list.size() < 1) {
                hint.setVisibility(View.VISIBLE);
                hint.setText(R.string.noData);
                file.setVisibility(View.GONE);
            } else {
                hint.setVisibility(View.GONE);
                file.setVisibility(View.VISIBLE);
                adapterTwo = new PhotoFileAdapterTwo(PictureTwoActivity.this, list);
                file.setAdapter(adapterTwo);
            }
        } else {
            hint.setText(getString(R.string.noData));
            Toast.makeText(PictureTwoActivity.this, getString(R.string.noSDK), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // 返回上一页面
                case R.id.control_bottom_return:
                    finish();

                    break;
                // 进入编辑模式
                case R.id.control_bottom_compile:
                    compileIv.setVisibility(View.GONE);
                    deleteIv.setVisibility(View.VISIBLE);
                    allIv.setVisibility(View.VISIBLE);

                    adapterTwo.setShow(true);
                    adapterTwo.notifyDataSetChanged();
                    break;
                // 全选
                case R.id.control_bottom_select:

                    data = new ArrayList<Boolean>();
                    allIv.setSelected(isCheck);
                    if (null != list) {
                        for (int i = 0; i < list.size(); i++) {
                            data.add(isCheck);
                        }
                    }
                    adapterTwo.setData(data);
                    adapterTwo.notifyDataSetChanged();
                    isCheck = !isCheck;

                    break;
                // 删除选择的图片
                case R.id.control_bottom_delete:

                    data = adapterTwo.getData();
                    for (int i = data.size() - 1; i > -1; i--) {
                        if (data.get(i)) {
                            File photoFile = new File(list.get(i));
                            photoFile.delete();
                            list.remove(i);
                            data.remove(i);
                        }
                    }

                    if (data.size() == 0) {
                        // 如果图片全删除了，回到上一页面
                        finish();
                    } else {
                        // 还有图片，重新加载数据，刷新页面
                        adapterTwo.setData(data);
                        adapterTwo.notifyDataSetChanged();
                    }

                    break;
            }
        }
    };

    // GridView的点击事件，跳转到图片浏览页面
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            intent = new Intent(PictureTwoActivity.this, PictureThreeActivity.class);
            intent.putExtra("name", name);
            intent.putStringArrayListExtra("list", list);
            intent.putExtra("num", position);
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
                intent = new Intent(PictureTwoActivity.this, PictureThreeActivity.class);
                intent.putExtra("name", name);
                intent.putStringArrayListExtra("list", list);
                intent.putExtra("num", number);
                startActivity(intent);
            }
        }
    }
}
