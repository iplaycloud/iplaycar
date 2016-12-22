package com.iplay.car.setting.view;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.iplay.car.R;
import com.iplay.car.common.adapter.WifiAdapter;
import com.iplay.car.common.base.BaseActivity;
import com.iplay.car.common.customview.MyListView;
import com.iplay.car.common.utils.OnNetworkChangeListener;
import com.iplay.car.common.utils.WifiAdmin;
import com.iplay.car.common.utils.WifiConnDialog;
import com.iplay.car.common.utils.WifiStatusDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/24.
 * wifi界面
 */
public class WifiActivity extends BaseActivity {
    private static final String TAG = "WifiActivity";
    private static final int REFRESH_CONN = 100;
    private static final int REQ_SET_WIFI = 200;
    //Wifi管理类
    private WifiAdmin mWifiAdmin;
    //扫描结果列表
    private List<ScanResult> list = new ArrayList<>();
    //显示列表
    private MyListView listView;
    private WifiAdapter mAdapter;

    private WifiManager wifiManager = null;

    //private ListView listWifi;
    private ImageView iv_picture_return_wifiListFragment, iv_picture_choice_wifiListFragment;
    private ToggleButton tb_wifilist_button;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        mContext = WifiActivity.this;
        initData();
        initViews();//初始化界面控制
        setListener();

        refreshWifiStatusOnTime();
    }

    private void initData() {
        wifiManager = (WifiManager) mContext.getSystemService(Service.WIFI_SERVICE);
        mWifiAdmin = new WifiAdmin(mContext);
        //获得wifi列表信息
        getWifiListInfo();
    }

    private void initViews() {
        //刷新图片
        iv_picture_choice_wifiListFragment = (ImageView) findViewById(R.id.iv_picture_choice_wifiListFragment);
        iv_picture_choice_wifiListFragment.setVisibility(View.INVISIBLE);
        //返回按钮
        iv_picture_return_wifiListFragment = (ImageView) findViewById(R.id.iv_picture_return_wifiListFragment);
        //开关按钮
        tb_wifilist_button = (ToggleButton) findViewById(R.id.tb_wifilist_button);
        listView = (MyListView) findViewById(R.id.lv_wifiListFragment);
        mAdapter = new WifiAdapter(mContext, list);
        listView.setAdapter(mAdapter);

        int wifiState = mWifiAdmin.checkState();
        if (wifiState == WifiManager.WIFI_STATE_DISABLED || wifiState == WifiManager.WIFI_STATE_DISABLING
                || wifiState == WifiManager.WIFI_STATE_UNKNOWN) {
            tb_wifilist_button.setChecked(false);
        } else {
            tb_wifilist_button.setChecked(true);
        }

        iv_picture_choice_wifiListFragment.setOnClickListener(onClickListener);
        iv_picture_return_wifiListFragment.setOnClickListener(onClickListener);

    }

    private OnNetworkChangeListener mOnNetworkChangeListener = new OnNetworkChangeListener() {
        @Override
        public void onNetWorkDisConnect() {
            getWifiListInfo();
            mAdapter.setDatas(list);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onNetWorkConnect() {
            getWifiListInfo();
            mAdapter.setDatas(list);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mAdapter.notifyDataSetChanged();
        }
    };

    private void setListener() {
        tb_wifilist_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.i(TAG, "===========open wifi=====");
                    //代开wifi
                    mWifiAdmin.openWifi();
                    getWifiListInfo();
                    mAdapter.setDatas(list);
                    mAdapter.notifyDataSetChanged();
                } else {
                    Log.i(TAG, "======close wifi====");
                    mWifiAdmin.closeWifi();
                }
            }
        });
        listView.setonRefreshListener(new MyListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getWifiListInfo();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        mAdapter.setDatas(list);
                        mAdapter.notifyDataSetChanged();
                        listView.onRefreshComplete();
                    }
                }.execute();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                int position = pos - 1;
                ScanResult scanResult = list.get(position);
                String desc = "";
                String descOri = scanResult.capabilities;
                if (descOri.toUpperCase().contains("WPA-PSK")) {
                    desc = "WPA";
                }
                if (descOri.toUpperCase().contains("WPA2-PSK")) {
                    desc = "WPA2";
                }
                if (descOri.toUpperCase().contains("WPA-PSK")
                        && descOri.toUpperCase().contains("WPA2-PSK")) {
                    desc = "WPA/WPA2";
                }
                if (desc.equals("")) {
                    isConnectSelf(scanResult);
                    return;
                }
                isConnect(scanResult);
            }

            public void isConnect(final ScanResult scanResult) {
                if (mWifiAdmin.isConnect(scanResult)) {
                    //已连接，显示连接状态对话框
                    WifiStatusDialog mStatusDialog = new WifiStatusDialog(mContext, scanResult, mOnNetworkChangeListener);
                    mStatusDialog.show();
                } else {
                    if (mWifiAdmin.isExsits(scanResult.SSID) != null) {
                        final int netID = mWifiAdmin.isExsits(scanResult.SSID).networkId;
                        new AlertDialog.Builder(mContext).setTitle(R.string.Prompt).setMessage(R.string.plese_choice).
                                setPositiveButton(R.string.connect, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        WifiConfiguration config = mWifiAdmin.isExsits(scanResult.SSID);
                                        mWifiAdmin.setMaxPriority(config);
                                        mWifiAdmin.ConnectToNetID(config.networkId);
                                    }
                                }).setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setNeutralButton(R.string.forget, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                wifiManager.removeNetwork(netID);
                            }
                        }).show();
                    } else {
                        //未连接显示连接输入对话框
                        WifiConnDialog mDialog = new WifiConnDialog(
                                mContext, scanResult, mOnNetworkChangeListener);
                        mDialog.show();
                    }

                }
            }

            public void isConnectSelf(ScanResult scanResult) {
                if (mWifiAdmin.isConnect(scanResult)) {

                    // 已连接，显示连接状态对话框
                    WifiStatusDialog mStatusDialog = new WifiStatusDialog(mContext,scanResult, mOnNetworkChangeListener);
                    mStatusDialog.show();

                } else {
                    boolean iswifi = mWifiAdmin.connectSpecificAP(scanResult);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (iswifi) {
                        Toast.makeText(mContext, R.string.connect_success, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(mContext, R.string.connect_fail, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_picture_return_wifiListFragment:
                    finish();
                    break;
                case R.id.iv_picture_choice_wifiListFragment:
                    break;
            }
        }
    };

    private void getWifiListInfo() {
        System.out.println("WifiListActivity#getWifiListInfo");
        //开始扫描，得到配置好的网络
        mWifiAdmin.startScan();
        //得到扫描结果
        List<ScanResult> tmpList = mWifiAdmin.getWifiList();
        if (tmpList == null) {
            list.clear();
        } else {
            list = tmpList;
        }
    }

    private Handler mHandler = new MyHandler(WifiActivity.this);

    protected boolean isUpdate = true;

    private static class MyHandler extends Handler {
        private WeakReference<WifiActivity> reference;

        public MyHandler(WifiActivity activity) {
            this.reference = new WeakReference<WifiActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            WifiActivity activity = reference.get();
            switch (msg.what) {
                case REFRESH_CONN:
                    activity.getWifiListInfo();
                    activity.mAdapter.setDatas(activity.list);
                    activity.mAdapter.notifyDataSetChanged();
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private void refreshWifiStatusOnTime() {
        new Thread() {
            @Override
            public void run() {
                while (isUpdate) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mHandler.sendEmptyMessage(REFRESH_CONN);
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isUpdate = false;
    }

}
