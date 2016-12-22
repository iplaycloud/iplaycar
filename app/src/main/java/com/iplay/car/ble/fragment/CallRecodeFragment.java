package com.iplay.car.ble.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iplay.car.R;

/**
 * 通话记录页面
 */
public class CallRecodeFragment extends Fragment{
    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.callrecodefragment_layout,container,false);
        return view;
    }
}
