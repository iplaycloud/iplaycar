package com.iplay.car.setting.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iplay.car.R;
import com.iplay.car.common.base.BaseActivity;
import com.iplay.car.common.utils.DataUtils;
import com.iplay.car.common.utils.FileUtils;
import com.iplay.car.picture.PictureOneActivity;
import com.iplay.car.video.VideoOneActivity;

/**
 * Created by Administrator on 2016/11/24.
 * SD卡信息界面
 */
public class SDCardsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdcards);
        initView();
    }

    private void initView() {
        // SD卡所有总空间
        TextView tv_sd_space = (TextView) findViewById(R.id.tv_sd_space);
        // SD卡剩余空间
        TextView tv_sd_Residue_size = (TextView) findViewById(R.id.tv_sd_Residue_size);
        // 图片容量
        TextView tv_sd_picture_size = (TextView) findViewById(R.id.tv_sd_picture_size);
        // 视频容量
        TextView tv_sd_video_size = (TextView) findViewById(R.id.tv_sd_video_size);
        // 图片的容器
        RelativeLayout ll_sd_picture = (RelativeLayout) findViewById(R.id.ll_sd_picture);
        // 视频的容器
        RelativeLayout ll_sd_video = (RelativeLayout) findViewById(R.id.ll_sd_video);

        // 返回按钮
        ImageView iv_picture_return = (ImageView) findViewById(R.id.control_bottom_return);
        TextView tv_bottom_photo = (TextView) findViewById(R.id.control_bottom_title);
        tv_bottom_photo.setText(R.string.SD_info);

        //获取SD卡信息，获取数据信息
        if (FileUtils.sdkIsOk()) {
            tv_sd_space.setText(FileUtils.getSDAllSize() + "M");
            tv_sd_Residue_size.setText(FileUtils.getSDFreeSize() + "M");
            // 得到图片的容量
            tv_sd_picture_size.setText(FileUtils.getAutoFileOrFilesSize(DataUtils.photoPath));
            // 得到视频的容量
            tv_sd_video_size.setText(FileUtils.getAutoFileOrFilesSize(DataUtils.videoPath));
        } else {// 未插SD卡时，提醒用户
            Toast.makeText(SDCardsActivity.this, R.string.noSDK, Toast.LENGTH_SHORT).show();
        }

        iv_picture_return.setOnClickListener(onClickListener);
        ll_sd_video.setOnClickListener(onClickListener);
        ll_sd_picture.setOnClickListener(onClickListener);
    }

    private Intent intent;
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
                case R.id.ll_sd_picture:// 图片点击
                    intent = new Intent(SDCardsActivity.this, PictureOneActivity.class);
                    startActivity(intent);
                    break;
                case R.id.ll_sd_video:// 视频点击
                    intent = new Intent(SDCardsActivity.this, VideoOneActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

}
