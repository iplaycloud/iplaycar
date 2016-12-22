package com.iplay.car.main;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.iplay.car.R;
import com.iplay.car.backcar.view.BackcarActivity;
import com.iplay.car.ble.view.PhoneActivity;
import com.iplay.car.common.base.BaseActivity;
import com.iplay.car.picture.PictureOneActivity;
import com.iplay.car.setting.view.SetActivity;
import com.iplay.car.setting.view.WeixinActivity;
import com.iplay.car.video.VideoOneActivity;
import com.iplay.car.voice.service.MainService;
import com.iplay.car.common.ui.DogActivity;
import com.iplay.car.common.utils.DataUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/11/24.
 * 主界面
 */
public class HomeActivity extends BaseActivity {
    public static final String TAG = "HomeFragment";
    private Intent intent;
    private ImageView navigationIv;// 导航
    private ImageView edogIv; // 电子狗
    private ImageView bluetoothIv;// 蓝牙
    private ImageView pictureIv;// 图片
    private ImageView videoIv;// 视频
    private ImageView musicIv;// 音乐
    private ImageView recordIv;// 行车记录
    private ImageView backcarIv;// 行车记录
    private ImageView setIv;// 设置
    private ImageView weChatIv;// 设置
    private HomeReceiver receiver;// 跳转界面的广播
    private TextView timeTv;
    private TextView weekTv;
    private TextView dateTv;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initView();
        initDate();
    }

    // 发送广播隐藏预览窗口
    private void hideView(){
        intent = new Intent();
        intent.setAction(DataUtils.ServiceViewAction);
        intent.putExtra("size",0);
        sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        sendServiceMsg(DataUtils.currentView, "HomeActivity"); //设置当前页面
        intent = new Intent();
        intent.setAction(DataUtils.ServiceViewAction);
        intent.putExtra("size",1);
        sendBroadcast(intent);
        super.onResume();
    }

    public void sendServiceMsg(String order, String data) {
        // 发送广播
        Intent intent = new Intent();
        intent.putExtra("order", order);
        intent.putExtra("data", data);
        intent.setAction(MainService.Action);
        sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
      //  endService();// 停止服务
        unregisterReceiver(receiver);
     //   endService();// 停止服务
        super.onDestroy();
    }

    private void initView() {

        timeTv = (TextView) findViewById(R.id.home_time_tv);
        weekTv = (TextView) findViewById(R.id.home_week_tv);
        dateTv = (TextView) findViewById(R.id.home_date_tv);

        navigationIv = (ImageView) findViewById(R.id.home_menu_navigation);
        edogIv = (ImageView) findViewById(R.id.home_menu_edog);
        bluetoothIv = (ImageView) findViewById(R.id.home_menu_bluetooth);
        pictureIv = (ImageView) findViewById(R.id.home_menu_picture);
        videoIv = (ImageView) findViewById(R.id.home_menu_video);
        musicIv = (ImageView) findViewById(R.id.home_menu_music);
        recordIv = (ImageView) findViewById(R.id.home_menu_record);
        setIv = (ImageView) findViewById(R.id.home_menu_set);
        weChatIv = (ImageView) findViewById(R.id.home_menu_wechat);
        backcarIv = (ImageView)findViewById(R.id.home_menu_backcar);
    }

    private void initDate() {

        timeTv.setText(new SimpleDateFormat("HH:mm").format(new Date()));
        weekTv.setText(new SimpleDateFormat("EEEE").format(new Date()));
        dateTv.setText(new SimpleDateFormat("yyyy/MM/dd").format(new Date()));

        navigationIv.setOnClickListener(onClickListener);
        edogIv.setOnClickListener(onClickListener);
        bluetoothIv.setOnClickListener(onClickListener);
        pictureIv.setOnClickListener(onClickListener);
        videoIv.setOnClickListener(onClickListener);
        musicIv.setOnClickListener(onClickListener);
        recordIv.setOnClickListener(onClickListener);
        setIv.setOnClickListener(onClickListener);
        weChatIv.setOnClickListener(onClickListener);
        backcarIv.setOnClickListener(onClickListener);

        //startService();// 启动服务
        receiver = new HomeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DataUtils.gotoClass);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(receiver,intentFilter);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          gotoActivity(v.getId());
        }
    };

    private void gotoActivity(int id){
        switch (id) {
            case R.id.home_menu_navigation:// 进入导航界面
                hideView();
                sendServiceMsg(DataUtils.currentView, "gaodeDH"); //设置当前页面
                Uri mUri = Uri.parse("androidauto://rootmap?sourceApplication=rearviewMirror");
                Intent mIntent = new Intent(Intent.ACTION_VIEW, mUri);
                mIntent.addCategory("android.intent.category.DEFAULT");
                startActivity(mIntent);
                break;
            case R.id.home_menu_edog:// 进入电子狗界面
                hideView();
                intent = new Intent(HomeActivity.this, DogActivity.class);
                startActivity(intent);
                break;
            case R.id.home_menu_bluetooth:// 进入拨号，蓝牙界面
                hideView();
                intent = new Intent(HomeActivity.this, PhoneActivity.class);
                startActivity(intent);
                break;
            case R.id.home_menu_picture:// 进入照片页面
                hideView();
                intent = new Intent(HomeActivity.this, PictureOneActivity.class);
                startActivity(intent);
                break;
            case R.id.home_menu_video:// 进入视频界面
                hideView();
                intent = new Intent(HomeActivity.this, VideoOneActivity.class);
                startActivity(intent);
                break;
            case R.id.home_menu_music:// 进入娱乐界面
                hideView();
                sendServiceMsg(DataUtils.currentView, "cn.kuwo.kwmusichd"); //设置当前页面
                doStartApplicationWithPackageName("cn.kuwo.kwmusichd");
                break;
            case R.id.home_menu_record:// 显示行车记录界面
                // 发送广播给服务，改变悬浮窗口
                sendServiceMsg(DataUtils.currentView, "ServiceViewAction"); //设置当前页面
                intent = new Intent();
                intent.setAction(DataUtils.ServiceViewAction);
                intent.putExtra("size", 2);
                sendBroadcast(intent);
                break;

            case R.id.home_menu_backcar: //显示倒车画面
                hideView();
                intent = new Intent(HomeActivity.this, BackcarActivity.class);
                startActivity(intent);
                break;

            case R.id.home_menu_set:// 跳转到设置界面
                hideView();
                intent = new Intent(HomeActivity.this, SetActivity.class);
                startActivity(intent);
                break;

            case R.id.home_menu_wechat:// 跳转到微信助手界面
            //    Toast.makeText(HomeActivity.this,"跳转到微信助手界面",Toast.LENGTH_SHORT).show();
                hideView();
                intent = new Intent(HomeActivity.this, WeixinActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private class HomeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().endsWith(DataUtils.gotoClass)) {
                int flag = intent.getIntExtra("class", 0);
                if (1 == flag) {// 跳转到视频界面
                    intent = new Intent(HomeActivity.this, VideoOneActivity.class);
                    startActivity(intent);
                } else if (2 == flag) {// 跳转到图片界面
                    intent = new Intent(HomeActivity.this, PictureOneActivity.class);
                    startActivity(intent);
                }
            }else if (Intent.ACTION_TIME_TICK.equals(intent.getAction())){// 每一分钟的广播，改变显示的时间
                timeTv.setText(new SimpleDateFormat("HH:mm").format(new Date()));
                weekTv.setText(new SimpleDateFormat("EEEE").format(new Date()));
                dateTv.setText(new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
            }
        }
    }

    /**
     * 通过包名跳转到酷我音乐app
     */
    private void doStartApplicationWithPackageName(String packagename) {
        //通过包名获取此APP详细信息，包括Activity，services,versioncode,name等等
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null) {
            return;
        }
        //创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageInfo.packageName);
        //通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = getPackageManager().
                queryIntentActivities(resolveIntent, 0);
        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            //packagename=参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            //这个就是我们要走的该App的LAUNCHER的Activity[组织形式:packageName,mainActovotyname]
            String className = resolveinfo.activityInfo.name;
            //LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            //设置ComponentName参数1:packagename参数2:mainActivity的路径
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            startActivity(intent);
        }
    }

    private Intent service_intent;
    public void startService() {
        boolean boo = isServiceWork(getApplicationContext(), MainService.Action);
        Log.d("MainActivity", "=======boo======" + boo);
        if (!boo) {
            Log.d("MainActivity", "======重启service=======");
//			service_intent = new Intent(TTCService.ACTION);
            service_intent = new Intent(this,MainService.class);
            startService(service_intent);
        }
    }

    public void endService() {
        boolean boo = isServiceWork(getApplicationContext(), MainService.Action);
        if (null == service_intent) {
//			service_intent = new Intent(TTCService.ACTION);
            service_intent = new Intent(this,MainService.class);
        }
        if (boo) {
            this.stopService(service_intent);
        }
    }

    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName 是包名+服务的类名（例如：com.baidu.trace.LBSTraceService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(80);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
}