package com.iplay.car.common.ui;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.GestureDetector;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SlidingDrawer;

import com.iplay.car.common.base.BaseFragmentActivity;
import com.iplay.car.common.bean.PhotoFileBean;
import com.iplay.car.common.bean.VideoFileBean;
import com.iplay.car.common.utils.DataUtils;

/**
 * Created by Administrator on 2016/7/20.
 */
public class MainActivity extends BaseFragmentActivity{
//        implements android.view.GestureDetector.OnGestureListener {

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private ImageView vv_main_video;
    private ImageView iv_main_voice, iv_main_tack_picture, iv_main_home;
    //定义4个Fragment对象

    private PhotoFileBean photoFileBean;// 图片的数据类
    private VideoFileBean videoFileBean;// 视频文件的数据类
    private int selected;// 记录选中的位置
    private String path1;//记录选中的路径第一段
    private int num;// 记录选中的视频位置

    private boolean isFirst;// 是否第一次显示

    private SlidingDrawer sd;
    GestureDetector detector;

    private SeekBar sb_pen_voice, sb_pen_light;

    public AudioManager audioManager;
    private int maxVolume, currentVolume;

    @Override
    protected void onStart() {
        if (!isFirst) {
            // 不是第一次显示，发送广播给服务，显示位于左上角的悬浮窗口
            Intent intent = new Intent();
            intent.setAction(DataUtils.ServiceViewAction);
            intent.putExtra("size", 1);
            sendBroadcast(intent);
            Log.d("11111", "变小");
        }
        super.onStart();
    }

    @Override
    protected void onPause() {

        isFirst = false;// 不是第一次显示了

        super.onPause();
    }

    @Override
    protected void onDestroy() {
//        endService();// 销毁服务
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        isFirst = true;
        fragmentManager = getSupportFragmentManager();
//        initView();//初始化界面控制
//        setChoiceItem(3);//初始化第一个界面
//
//        // 启动服务
//        startService();

    }

//    private void initView() {
//        //视频
//        vv_main_video = (ImageView) findViewById(R.id.vv_main_video);
//        //主页
//        iv_main_home = (ImageView) findViewById(R.id.iv_main_home);
//        //拍照
//        iv_main_tack_picture = (ImageView) findViewById(R.id.iv_main_tack_picture);
//        //录音
//        iv_main_voice = (ImageView) findViewById(R.id.iv_main_voice);
//        //抽屉
//        sd = (SlidingDrawer) findViewById(R.id.slidingDrawer1);
//        //声音拖动条
//        sb_pen_voice = (SeekBar) findViewById(R.id.sb_pen_voice);
//        //亮度拖动条
//        sb_pen_light = (SeekBar) findViewById(R.id.sb_pen_light);
//
//        //声音调节
//        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//获取系统最大音量
//        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//        sb_pen_voice.setMax(maxVolume);//把拖动条最高值与系统最大声匹配
//        sb_pen_voice.setProgress(currentVolume);
//        voice();
//        //亮度调节
//        sb_pen_light.setProgress((int) (android.provider.Settings.System.getInt(getContentResolver(),
//                android.provider.Settings.System.SCREEN_BRIGHTNESS, 255)));
//        light();
//
//        detector = new GestureDetector(this, this);
//
//        vv_main_video.setOnClickListener(onClickListener);
//        iv_main_voice.setOnClickListener(onClickListener);
//        iv_main_tack_picture.setOnClickListener(onClickListener);
//        iv_main_home.setOnClickListener(onClickListener);
//
//    }
//private View.OnClickListener onClickListener = new View.OnClickListener() {
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            //视频
//            case R.id.vv_main_video:
//                //setChoiceItem(0);
//                break;
//            //主页
//            case R.id.iv_main_home:
//                setChoiceItem(3);
//
//                break;
//            //拍照
//            case R.id.iv_main_tack_picture:
//                setChoiceItem(2);
//
//                break;
//            //录音
//            case R.id.iv_main_voice:
//                // setChoiceItem(1);
//
//                // 发送广播给服务，隐藏悬浮窗口
//                Intent intent = new Intent();
//                intent.setAction(DataUtils.ServiceViewAction);
//                sendBroadcast(intent);
//
//                // 跳转页面
//                intent = new Intent(MainActivity.this, VoiceActivity.class);
//                startActivity(intent);
//                break;
//        }
//    }
//};
//
//    /**
//     * 设置点击选项卡的事件处理
//     * index 选项卡的标号0,1,2,3
//     */
//    private void setChoiceItem(int index) {
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        switch (index) {
//            case 0:
//
//                break;
//            case 1:
////                if(voiceFragment==null){
////                    voiceFragment=new VoiceFragment();
////
////                }
////                fragmentTransaction.replace(R.id.fl_main_content,voiceFragment);
//                break;
//            case 2:
//
//                break;
//            case 3:
////                if (homeFragment == null) {
////                    homeFragment = new HomeFragment();
////                }
////                fragmentTransaction.replace(R.id.fl_main_content, homeFragment);
//                break;
//        }
//        fragmentTransaction.commit();
//    }
//
//    public PhotoFileBean getPhotoFileBean() {
//        return photoFileBean;
//    }
//
//    public void setPhotoFileBean(PhotoFileBean photoFileBean) {
//        this.photoFileBean = photoFileBean;
//    }
//
//    public int getSelected() {
//        return selected;
//    }
//
//    public void setSelected(int selected) {
//        this.selected = selected;
//    }
//
//    public String getPath1() {
//        return path1;
//    }
//
//    public void setPath1(String path1) {
//        this.path1 = path1;
//    }
//
//    public VideoFileBean getVideoFileBean() {
//        return videoFileBean;
//    }
//
//    public void setVideoFileBean(VideoFileBean videoFileBean) {
//        this.videoFileBean = videoFileBean;
//    }
//
//    public int getNum() {
//        return num;
//    }
//
//    public void setNum(int num) {
//        this.num = num;
//    }
//
//    /**
//     * 手势监听
//     *
//     * @param e
//     * @return
//     */
//    @Override
//    public boolean onDown(MotionEvent e) {
//        return false;
//    }
//
//    @Override
//    public void onShowPress(MotionEvent e) {
//
//    }
//
//    @Override
//    public boolean onSingleTapUp(MotionEvent e) {
//        return false;
//    }
//
//    @Override
//    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//        return false;
//    }
//
//    @Override
//    public void onLongPress(MotionEvent e) {
//
//    }
//
//    @Override
//    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//        if (null != e1 && null != e2) {
//            float minMove = 100;//最小滑动距离
//            float minVelocity = 0;//最小滑动速度
////        float beginX = e1.getX();
////        float endX = e2.getX();
//            float beginY = e1.getY();
//            float endY = e2.getY();
//            if (beginY - endY > minMove && Math.abs(velocityY) > minVelocity) {//上滑
//                sd.close();
//            } else if (endY - beginY > minMove && Math.abs(velocityY) > minVelocity) {//下滑
//                sd.open();
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        detector.onTouchEvent(event);
//        return super.onTouchEvent(event);
//    }
//
//    /**
//     * 亮度调节监听
//     */
//    public void light() {
//        sb_pen_light.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            //当拖动条发生变化时调用
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (fromUser) {
//                    Integer tmpInt = seekBar.getProgress();
//                    android.provider.Settings.System.putInt(getContentResolver(),
//                            android.provider.Settings.System.SCREEN_BRIGHTNESS,
//                            tmpInt);
//                    tmpInt = Settings.System.getInt(getContentResolver(),
//                            Settings.System.SCREEN_BRIGHTNESS, -1);
//                    WindowManager.LayoutParams lp = getWindow().getAttributes();
//                    Float tmpFloat = (float) (tmpInt / 255);
//                    if (0 < tmpFloat && tmpFloat <= 1) {
//                        lp.screenBrightness = tmpFloat;
//                    }
//                    getWindow().setAttributes(lp);
//                }
//            }
//
//            //当用户开始滑动滑块时开始调用
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            //当用户停止滑动滑块时开始调用
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
//    }
//
//    /**
//     * 音量调节监听
//     */
//    public void voice() {
//        sb_pen_voice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            //当拖动条发生变化时调用
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
//                        progress, 0);
//                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//                seekBar.setProgress(currentVolume);
//                Log.i("TAG", "--------currentVolume" + currentVolume);
//            }
//
//            //当用户开始滑动滑块时开始调用
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            //当用户停止滑动滑块时开始调用
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
//    }
//
//    private Intent service_intent;
//
//    public void startService() {
//        boolean boo = isServiceWork(getApplicationContext(), MainService.Action);
//        Log.d("MainActivity", "=======boo======" + boo);
//        if (!boo) {
//            Log.d("MainActivity", "======重启service=======");
////			service_intent = new Intent(TTCService.ACTION);
//            service_intent = new Intent(MainActivity.this,MainService.class);
//            startService(service_intent);
//        }
//    }
//
//    public void endService() {
//        boolean boo = isServiceWork(getApplicationContext(), MainService.Action);
//        if (null == service_intent) {
////			service_intent = new Intent(TTCService.ACTION);
//            service_intent = new Intent(MainActivity.this,MainService.class);
//        }
//        if (boo) {
//            this.stopService(service_intent);
//        }
//    }
//
//    /**
//     * 判断某个服务是否正在运行的方法
//     *
//     * @param mContext
//     * @param serviceName 是包名+服务的类名（例如：com.baidu.trace.LBSTraceService）
//     * @return true代表正在运行，false代表服务没有正在运行
//     */
//    public boolean isServiceWork(Context mContext, String serviceName) {
//        boolean isWork = false;
//        ActivityManager myAM = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(80);
//        if (myList.size() <= 0) {
//            return false;
//        }
//        for (int i = 0; i < myList.size(); i++) {
//            String mName = myList.get(i).service.getClassName().toString();
//            if (mName.equals(serviceName)) {
//                isWork = true;
//                break;
//            }
//        }
//        return isWork;
//    }


}
