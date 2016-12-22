package com.iplay.car.ble.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.iplay.car.R;

/**
 * Created by Administrator on 2016/7/28.
 * 调频广播界面
 */
public class BroadcastFragment extends Fragment{
    private View view;
    private ImageView iv_broadcastfragment_return,iv_broadcastfragment_left,iv_broadcastfragment_right;
    private ToggleButton tb_broadcastfragment_button;
    private TextView tv_broadcastfragment_number;
    private SeekBar sb_broadcastfragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.broadcast,container,false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();//初始化控件
        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
    }

    private void initViews() {
        //左边图片按钮
        iv_broadcastfragment_left= (ImageView) view.findViewById(R.id.iv_broadcastfragment_left);
        //返回按钮
        iv_broadcastfragment_return= (ImageView) view.findViewById(R.id.iv_broadcastfragment_return);
        //右边图片按钮
        iv_broadcastfragment_right= (ImageView) view.findViewById(R.id.iv_broadcastfragment_right);
        //选择开关
        tb_broadcastfragment_button= (ToggleButton) view.findViewById(R.id.tb_broadcastfragment_button);
        //调频数字
        tv_broadcastfragment_number= (TextView) view.findViewById(R.id.tb_broadcastfragment_button);
        //拖动条
        sb_broadcastfragment= (SeekBar) view.findViewById(R.id.sb_broadcastfragment);

        iv_broadcastfragment_return.setOnClickListener(onClickListener);
        iv_broadcastfragment_right.setOnClickListener(onClickListener);
        iv_broadcastfragment_left.setOnClickListener(onClickListener);
        //选择开关方法
        toggleButton();
        //进度条的监听方法
        seekBar();


    }
    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener=new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                //返回
                case R.id.iv_broadcastfragment_return:
//                    fragmentTransaction.replace(R.id.fl_main_content,new SettingFragment());
//                    fragmentTransaction.commit();
                    break;
                //左边按钮
                case R.id.iv_broadcastfragment_left:
                    break;
                //右边按钮
                case R.id.iv_broadcastfragment_right:
                    break;
            }
        }
    };
    //选择开关监听
    public void toggleButton(){
        tb_broadcastfragment_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });
    }
    //seekbar的监听
    public void seekBar(){
        sb_broadcastfragment.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //进度条改变时开始调用
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }
            //进度条开始拖动时调用
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            //停止拖动时调用
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
