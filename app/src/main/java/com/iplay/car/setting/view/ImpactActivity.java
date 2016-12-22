package com.iplay.car.setting.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.iplay.car.R;
import com.iplay.car.common.base.BaseActivity;
import com.iplay.car.common.utils.DataUtils;

/**
 * Created by Administrator on 2016/11/25.
 * 碰撞设置界面
 */
public class ImpactActivity extends BaseActivity {

    private ToggleButton impactTb;// 停车监控的开关
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impact);
        initViews();
    }

    private void initViews() {
        //停车监控开关
        impactTb = (ToggleButton) findViewById(R.id.tb_impactfragment);
        //返回按钮
        ImageView control_return = (ImageView) findViewById(R.id.control_bottom_return);
        TextView control_title = (TextView) findViewById(R.id.control_bottom_title);
        control_title.setText(R.string.Collision_setting);

        sharedPreferences = getSharedPreferences(DataUtils.NAME, Context.MODE_PRIVATE);
        boolean isOpen = sharedPreferences.getBoolean(DataUtils.impactFlag, false);
        impactTb.setChecked(isOpen);

        control_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.control_bottom_return://返回
                        finish();
                        break;
                }
            }
        });
        impact();
    }

    /**
     * 停车监控开关监听事件
     */
    public void impact() {
        impactTb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 保存是否停车监控的标识
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(DataUtils.impactFlag, isChecked);
                editor.apply();
            }
        });
    }
}
