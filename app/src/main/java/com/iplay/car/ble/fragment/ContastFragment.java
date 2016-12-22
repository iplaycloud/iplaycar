package com.iplay.car.ble.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.iplay.car.R;

/**
 * 通讯录页面
 */
public class ContastFragment extends Fragment {
    private View view;
    private TextView agTv;
    private TextView hnTv;
    private TextView otTv;
    private TextView uzTv;
    private TextView symbolTv;
    private FrameLayout fl_contastfragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.contasrfragment_layout, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();//初始化界面

        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
    }

    private void initViews() {
        agTv = (TextView) view.findViewById(R.id.book_ag_tv);
        hnTv = (TextView) view.findViewById(R.id.book_hn_tv);
        otTv = (TextView) view.findViewById(R.id.book_ot_tv);
        uzTv = (TextView) view.findViewById(R.id.book_uz_tv);
        symbolTv = (TextView) view.findViewById(R.id.book_symbol_tv);
        fl_contastfragment = (FrameLayout) view.findViewById(R.id.fl_contastfragment);

        agTv.setSelected(true);

        agTv.setOnClickListener(onClickListener);
        hnTv.setOnClickListener(onClickListener);
        otTv.setOnClickListener(onClickListener);
        uzTv.setOnClickListener(onClickListener);
        symbolTv.setOnClickListener(onClickListener);
    }

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //a-g
                case R.id.book_ag_tv:
                    setTvBackColor(0);
                    break;
                //h-n
                case R.id.book_hn_tv:
                    setTvBackColor(1);
                    break;
                //o-t
                case R.id.book_ot_tv:
                    setTvBackColor(2);
                    break;
                //u-z
                case R.id.book_uz_tv:
                    setTvBackColor(3);
                    break;
                //#号
                case R.id.book_symbol_tv:
                    setTvBackColor(4);
                    break;
            }
        }
    };

    /**
     * 设置选中的状态
     * @param tag
     */
    private void setTvBackColor(int tag) {
        agTv.setSelected(0 == tag);
        hnTv.setSelected(1 == tag);
        otTv.setSelected(2 == tag);
        uzTv.setSelected(3 == tag);
        symbolTv.setSelected(4 == tag);
    }

}
