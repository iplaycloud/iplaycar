package com.iplay.car.setting.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.iplay.car.R;
import com.iplay.car.common.base.BaseActivity;
import com.iplay.car.common.utils.DeviceUuidFactory;
import com.iplay.car.common.utils.EncodingHandler;

/**
 * Created by Administrator on 2016/11/24.
 * 二维码界面
 */
public class QRCodeActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        initView();
        initData();
    }

    private void initView() {

        //返回
        ImageView backIv = (ImageView) findViewById(R.id.control_bottom_return);
        TextView titleTv = (TextView) findViewById(R.id.control_bottom_title);
        titleTv.setText(R.string.er_Code);

        backIv.setOnClickListener(onClickListener);
    }

    private void initData() {
        TextView idTv = (TextView) findViewById(R.id.qrcode_tv);// 设备ID
        ImageView iv = (ImageView) findViewById(R.id.qrcode_iv);// 设备ID的二维码图片

        String ID = new DeviceUuidFactory(QRCodeActivity.this).uuid.toString();
        idTv.setText(ID);
        // 根据字符串生成二维码图片并显示在界面上，第二个参数为图片的大小（350*350）
        try {
            Bitmap qrCodeBitmap = EncodingHandler.createQRCode(ID, 350);
            iv.setImageBitmap(qrCodeBitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //返回
                case R.id.control_bottom_return:
                    finish();
                    break;
            }
        }
    };

}
