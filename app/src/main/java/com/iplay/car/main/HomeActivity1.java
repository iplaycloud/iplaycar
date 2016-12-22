package com.iplay.car.main;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.iplay.car.R;
import com.iplay.car.ble.view.PhoneActivity;
import com.iplay.car.common.base.BaseActivity;
import com.iplay.car.picture.PictureOneActivity;
import com.iplay.car.setting.view.SetActivity;
import com.iplay.car.video.VideoOneActivity;
import com.iplay.car.voice.service.MainService1;
import com.iplay.car.common.ui.DogActivity;
import com.iplay.car.common.utils.DataUtils;
import com.iplay.car.common.utils.TtcDemo;

import java.util.List;

/**
 * Created by Administrator on 2016/11/24.
 * 主界面
 */
public class HomeActivity1 extends BaseActivity {
    public static final String TAG = "HomeFragment";
    private Intent intent;
    private ImageView navigationIv;// 导航
    private ImageView edogIv; // 电子狗
    private ImageView bluetoothIv;// 蓝牙
    private ImageView pictureIv;// 图片
    private ImageView videoIv;// 视频
    private ImageView musicIv;// 音乐
    private ImageView recordIv;// 行车记录
    private ImageView setIv;// 设置
    TtcDemo ttc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initView();
        initDate();
        ttc = new TtcDemo(this);
        ttc.speak("测试测试测试");
    }

    @Override
    protected void onDestroy() {
      //  endService();// 停止服务
//        this.mSpeechSynthesizer.release();
        ttc.clear();
        super.onDestroy();
    }

    private void initView() {
        navigationIv = (ImageView) findViewById(R.id.home_menu_navigation);
        edogIv = (ImageView) findViewById(R.id.home_menu_edog);
        bluetoothIv = (ImageView) findViewById(R.id.home_menu_bluetooth);
        pictureIv = (ImageView) findViewById(R.id.home_menu_picture);
        videoIv = (ImageView) findViewById(R.id.home_menu_video);
        musicIv = (ImageView) findViewById(R.id.home_menu_music);
        recordIv = (ImageView) findViewById(R.id.home_menu_record);
        setIv = (ImageView) findViewById(R.id.home_menu_set);

    }

    private void initDate() {
        navigationIv.setOnClickListener(onClickListener);
        edogIv.setOnClickListener(onClickListener);
        bluetoothIv.setOnClickListener(onClickListener);
        pictureIv.setOnClickListener(onClickListener);
        videoIv.setOnClickListener(onClickListener);
        musicIv.setOnClickListener(onClickListener);
        recordIv.setOnClickListener(onClickListener);
        setIv.setOnClickListener(onClickListener);
     //   startService();// 启动服务
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.home_menu_navigation:// 进入导航界面
                    Uri mUri = Uri.parse("androidauto://rootmap?sourceApplication=rearviewMirror");
                    Intent mIntent = new Intent(Intent.ACTION_VIEW, mUri);
                    mIntent.addCategory("android.intent.category.DEFAULT");
                    startActivity(mIntent);
                    break;
                case R.id.home_menu_edog:// 进入电子狗界面
                    intent = new Intent(HomeActivity1.this, DogActivity.class);
                    startActivity(intent);
                    break;
                case R.id.home_menu_bluetooth:// 进入拨号，蓝牙界面
                    intent = new Intent(HomeActivity1.this, PhoneActivity.class);
                    startActivity(intent);
                    break;
                case R.id.home_menu_picture:// 进入照片页面
                    intent = new Intent(HomeActivity1.this, PictureOneActivity.class);
                    startActivity(intent);
                    break;
                case R.id.home_menu_video:// 进入视频界面
                    intent = new Intent(HomeActivity1.this, VideoOneActivity.class);
                    startActivity(intent);
                    break;
                case R.id.home_menu_music:// 进入娱乐界面
                    doStartApplicationWithPackageName("cn.kuwo.kwmusichd");
                    break;
                case R.id.home_menu_record:// 显示行车记录界面
                    // 发送广播给服务，改变悬浮窗口
                    intent = new Intent();
                    intent.setAction(DataUtils.ServiceViewAction);
                    intent.putExtra("size", 2);
                    sendBroadcast(intent);
                    break;
                case R.id.home_menu_set:// 跳转到设置界面
                    intent = new Intent(HomeActivity1.this,SetActivity.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    };

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
        boolean boo = isServiceWork(getApplicationContext(), MainService1.Action);
        Log.d("MainActivity", "=======boo======" + boo);
        if (!boo) {
            Log.d("MainActivity", "======重启service=======");
//			service_intent = new Intent(TTCService.ACTION);
            service_intent = new Intent(HomeActivity1.this,MainService1.class);
            startService(service_intent);
        }
    }

    public void endService() {
        boolean boo = isServiceWork(getApplicationContext(), MainService1.Action);
        if (null == service_intent) {
//			service_intent = new Intent(TTCService.ACTION);
            service_intent = new Intent(HomeActivity1.this,MainService1.class);
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

