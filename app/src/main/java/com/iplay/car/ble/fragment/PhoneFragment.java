package com.iplay.car.ble.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.iplay.car.R;

/**
 * 拨打电话页面
 */
public class PhoneFragment extends Fragment {
    private View view;
    private EditText numEt;// 号码输入框

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.bluetoothfragment_item_layout, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();//初始化控件
    }

    private void initViews() {
        Button btn_one = (Button) view.findViewById(R.id.btn_one);
        Button btn_two = (Button) view.findViewById(R.id.btn_two);
        Button btn_three = (Button) view.findViewById(R.id.btn_three);
        Button btn_four = (Button) view.findViewById(R.id.btn_four);
        Button btn_five = (Button) view.findViewById(R.id.btn_five);
        Button btn_six = (Button) view.findViewById(R.id.btn_six);
        Button btn_seven = (Button) view.findViewById(R.id.btn_seven);
        Button btn_eight = (Button) view.findViewById(R.id.btn_eight);
        Button btn_night = (Button) view.findViewById(R.id.btn_night);
        Button btn_zero = (Button) view.findViewById(R.id.btn_zero);
        Button btn_hash = (Button) view.findViewById(R.id.btn_hash);//#号
        Button btn_start_key = (Button) view.findViewById(R.id.btn_start_key);//*号

        numEt = (EditText) view.findViewById(R.id.et_bluetoothfragment_item);//号码输入编辑框
        //删除
        ImageView deleteIv = (ImageView) view.findViewById(R.id.iv_bluetoothfragment_item_delet);
        //打电话
        ImageView callIv = (ImageView) view.findViewById(R.id.iv_bluetoothfragment_item_dial);

        btn_one.setOnClickListener(onClickListener);
        btn_two.setOnClickListener(onClickListener);
        btn_three.setOnClickListener(onClickListener);
        btn_four.setOnClickListener(onClickListener);
        btn_five.setOnClickListener(onClickListener);
        btn_six.setOnClickListener(onClickListener);
        btn_seven.setOnClickListener(onClickListener);
        btn_eight.setOnClickListener(onClickListener);
        btn_night.setOnClickListener(onClickListener);
        btn_zero.setOnClickListener(onClickListener);
        btn_start_key.setOnClickListener(onClickListener);
        btn_hash.setOnClickListener(onClickListener);
        deleteIv.setOnClickListener(onClickListener);
        callIv.setOnClickListener(onClickListener);

        deleteIv.setOnLongClickListener(onLongClickListener);
    }

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_one:
                    insert(numEt, "1");
                    break;
                case R.id.btn_two:
                    insert(numEt, "2");
                    break;
                case R.id.btn_three:
                    insert(numEt, "3");
                    break;
                case R.id.btn_four:
                    insert(numEt, "4");
                    break;
                case R.id.btn_five:
                    insert(numEt, "5");
                    break;
                case R.id.btn_six:
                    insert(numEt, "6");
                    break;
                case R.id.btn_seven:
                    insert(numEt, "7");
                    break;
                case R.id.btn_eight:
                    insert(numEt, "8");
                    break;
                case R.id.btn_night:
                    insert(numEt, "9");
                    break;
                case R.id.btn_zero:
                    insert(numEt, "0");
                    break;
                case R.id.btn_hash:
                    insert(numEt, "#");
                    break;
                case R.id.btn_start_key:
                    insert(numEt, "*");
                    break;
                case R.id.iv_bluetoothfragment_item_delet:// 删除光标前面的一个字符
                    int index = numEt.getSelectionStart();
                    if (index > 0) {
                        Editable editable = numEt.getText();
                        editable.delete(index - 1, index);
                    }
                    break;
                case R.id.iv_bluetoothfragment_item_dial:// 拨打电话
                    Toast.makeText(getActivity(), "点击了", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /**
     * EditText 在光标前面插入数据
     * @param editText 控件
     * @param str 数据
     */
    private void insert(EditText editText, String str) {
        int index = editText.getSelectionStart();
        Editable editable = editText.getText();
        editable.insert(index, str);
    }

    private View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            numEt.setText("");
            return false;
        }
    };

}
