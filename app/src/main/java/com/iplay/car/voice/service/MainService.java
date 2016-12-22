package com.iplay.car.voice.service;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.speech.SpeechRecognizer;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.trace.OnStartTraceListener;
import com.google.gson.Gson;
import com.iplay.car.R;
import com.iplay.car.navigation.app.AppData;
import com.iplay.car.common.bean.CatalogBean;
import com.iplay.car.common.bean.JsonData;
import com.iplay.car.common.bean.PhotoFileBean;
import com.iplay.car.common.protocol.ProtocolAgreementByte;
import com.iplay.car.common.socket.RearviewMirrorClientSocket;
import com.iplay.car.common.socket.RearviewMirrorServerSocket;
import com.iplay.car.main.HomeActivity;
import com.iplay.car.ble.view.PhoneActivity;
import com.iplay.car.picture.PictureOneActivity;
import com.iplay.car.setting.view.SetActivity;
import com.iplay.car.video.VideoOneActivity;
import com.iplay.car.common.utils.CommandData;
import com.iplay.car.common.utils.DataUtils;
import com.iplay.car.common.utils.DeviceUuidFactory;
import com.iplay.car.common.utils.FileUtils;
import com.iplay.car.common.utils.FormatUtil;
import com.iplay.car.common.utils.StringUtil;
import com.iplay.car.common.utils.TtcDemo;
import com.iplay.car.common.utils.VoiceControlDemo;
import com.iplay.car.common.utils.WifiHotUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Administrator on 2016/8/12.
 */
public class MainService extends Service {
    private static final String TAG = "MainService";
    public static final String Action = "com.xctx.rearviewMirror.voice.service.MainService";

    private WindowManager windowManager;
    private WindowManager.LayoutParams wmParams;
    private int maxWidth;// 屏幕的跨度
    private int maxHeight;// 屏幕的高度

    private SharedPreferences sharedPreferences;// 轻量存储
    private SharedPreferences.Editor editor;
    private ServiceReceiver receiver;// 广播接收者
    private UsbBroadCastReceiver usbBroadCastReceiver;// SD卡拔出、插好的广播接收器

    public Camera afterCamera;// 后摄像头
    private SurfaceHolder afterSurfaceHolder = null;
    private MediaRecorder afterMediaRecorder;

    private boolean isRecording;// 是否在拍摄;true为正在长录制、false为未录制
    protected boolean isPreview = false; // 摄像区域是否准备良好
    private boolean isPreviewCallback = false; // 是否开启了预览

    private View view;// 预览界面
    private int cameraNum = 0;// 当前打开的摄像头编号
    private boolean isSwitch = false;// 切换前后摄像头
    private boolean isLock;// 视频文件是否锁定；true为锁定、false为不锁定
    private boolean threeOrFive;// 录制的视频时长；true为3分钟、false为5分钟
    private int videoSetTime = 0;// 设置录制视频的时间
    private int recordTime = 0;// 已录制视频的时间

    private RearviewMirrorClientSocket clientSocket;// 连接服务器的工具类
    private RearviewMirrorServerSocket rearviewMirrorServerSocket;// 直连的工具类
    private boolean isConnect;// 是否连接服务器
    private CommandData commandData;// 存储数据类
    private ProtocolAgreementByte pab;// 封装的数据类
    private String ID;// 设备ID
    private Gson gson;
    private WifiHotUtil wifiHotUtil = null;// 热点工具类
    private JsonData jsonData = null;// 接收的数据bean类
    private AppData appData;
    private OnStartTraceListener startTraceListener;
    private DataOutputStream fileOut;// 接收升级文件的输出流
    private String apkPath; // APK文件的存储路径
    private EventManager mWpEventManager;  // 语音唤醒
    private TtcDemo ttcDemo;

    private Animation animation;// 录制状态的小圆球的动画
    private SensorManager mSensorManager = null;// 监听传感器管理者
    private Sensor mSensor = null;// 传感器
    private boolean isEmergency = false;// 传感器的事件，碰撞后为true，未碰撞时false
    private VoiceControlDemo voiceControlDemo;

    // 传感器的监听时间
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        float old_x = 0;
        float old_y = 0;
        float old_z = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                if (old_x - x > 15 && !isEmergency) {// 发生碰撞时，处理视频文件
                    if (null != handler) {
                        isEmergency = true;
                        handler.sendEmptyMessage(4);
                        Log.d("111111", x + "------" + "碰撞-----" + old_x);
                    }
                }
                old_x = x;
                old_y = y;
                old_z = z;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    public void onDestroy() {
        Log.d(TAG, "销毁");
        unregisterReceiver(receiver);
//        unregisterReceiver(usbBroadCastReceiver);
        clientSocket.destroy();// 停止与服务器连接的工具类，回收资源
        rearviewMirrorServerSocket.destroy();// 停止直连的工具类，回收资源
        handler.removeCallbacks(RunnableCallback2);
        handler = null;

        // 如果在录制视频，停止录制视频
        if (isRecording) {
            stopVideo();
        }
        mSensorManager.unregisterListener(mSensorEventListener);// 注销传感器监听
        windowManager.removeView(view);
        clear(afterMediaRecorder, afterCamera, afterSurfaceView, afterSurfaceHolder);//回收后摄像头的资源
        stopWakeUp();
        ttcDemo.clear();
        voiceControlDemo.cancel();
        super.onDestroy();
    }

    // 回收资源
    private void clear(MediaRecorder mediaRecorder, Camera camera, SurfaceView surfaceView, SurfaceHolder surfaceHolder) {
        if (null != mediaRecorder) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
        if (null != camera) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.lock();
            camera.release();
            camera = null;
        }
        if (null != surfaceView) {
            surfaceView.clearFocus();
            surfaceView = null;
            surfaceHolder = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("1111", "绑定");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        int i = 5/0;
        appData = (AppData) getApplicationContext();
        sharedPreferences = this.getSharedPreferences(DataUtils.NAME, Context.MODE_PRIVATE);
        gson = new Gson();

        initView();// 初始化控件
        initCamera(cameraNum);// 初始化摄像头,默认打开后摄像头
        initMediaRecorder();// 初始化mediaRecorder，并监听录制事件
        // 后摄像头
        afterSurfaceHolder = afterSurfaceView.getHolder();
        afterSurfaceHolder.addCallback(afterCallback);
        afterSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 红色小圆球的动画
        animation = AnimationUtils.loadAnimation(MainService.this, R.anim.alpha_drive_recording);

        // 初始化传感器监听管理者
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // 获取窗口管理器
        windowManager = (WindowManager) MainService.this.getSystemService(WINDOW_SERVICE);
        maxWidth = windowManager.getDefaultDisplay().getWidth();
        maxHeight = windowManager.getDefaultDisplay().getHeight();
        wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        // 设置Window flag
        // 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;// 调整悬浮窗口在左上角
        wmParams.x = 10;
        wmParams.y = 10;
        wmParams.width = 200;// 悬浮窗口的宽度
        wmParams.height = 150;// 悬浮窗口的高度

        windowManager.addView(view, wmParams);// 展示窗口
        FileUtils.create();// 创建图片和视频的文件夹

        wifiHotUtil = new WifiHotUtil(MainService.this);
        // 注册广播接收者
        receiver = new ServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DataUtils.ServiceViewAction);
        intentFilter.addAction(MainService.Action);
        this.registerReceiver(receiver, intentFilter);

        // 注册SD卡插好、拔出的广播接收者
//        usbBroadCastReceiver = new UsbBroadCastReceiver();
//        intentFilter = new IntentFilter();
//        intentFilter.addDataScheme("file");
//        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
//        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
//        this.registerReceiver(usbBroadCastReceiver,intentFilter);

        ID = new DeviceUuidFactory(MainService.this).uuid.toString();
        appData.setEntityName(ID);
        Log.d(TAG, "111111111" + ID);
        commandData = new CommandData();
        appData.setCommandData(commandData);//设置数据类
        clientSocket = new RearviewMirrorClientSocket();
        rearviewMirrorServerSocket = new RearviewMirrorServerSocket();
        rearviewMirrorServerSocket.init(MainService.this, commandData, handler);
        handler.sendEmptyMessage(1002);// 发送消息给handler，连接服务器
        handler.sendEmptyMessageDelayed(1000, 10000);// 10秒后发消息给handler，判断是否连接上服务器


        startTrace();
        startWakeUp();
        ttcDemo = new TtcDemo(this);
        voiceControlDemo = new VoiceControlDemo(this) {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> nbest = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String res = nbest.get(0).toString();
                System.out.println("*********识别结果**************=" + res);
                boolean isDealWith = false;
                speakChinese("您说的是:" + res + "?");
                if (res.contains("导航")) {
                    //跳转到导航
                    if (!currentView.equals("gaodeDH")) {
                        currentView = "gaodeDH";
                        Uri mUri = Uri.parse("androidauto://rootmap?sourceApplication=rearviewMirror");
                        Intent mIntent = new Intent(Intent.ACTION_VIEW, mUri);
                        mIntent.addCategory("android.intent.category.DEFAULT");
                        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplication().startActivity(mIntent);
                        sendServiceMsg(DataUtils.openWakeUp, ""); //打开语音唤醒
                        isVoiceControl = false;
                        isDealWith = true;
                    }
                } else if (res.contains("蓝牙")) {
                    if (!currentView.equals("PhoneActivity")) {
                        currentView = "PhoneActivity";
                        Intent dialogIntent = new Intent(getBaseContext(), PhoneActivity.class);
                        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplication().startActivity(dialogIntent);
                        isDealWith = true;
                    }
                } else if (res.contains("照片") || res.contains("图片") || res.contains("相册")) {
                    if (!currentView.equals("PictureOneActivity")) {
                        currentView = "PictureOneActivity";
                        Intent dialogIntent = new Intent(getBaseContext(), PictureOneActivity.class);
                        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplication().startActivity(dialogIntent);
                        isDealWith = true;
                    }
                } else if (res.contains("视频") || res.contains("录像")) {
                    if (!currentView.equals("VideoOneActivity")) {
                        currentView = "VideoOneActivity";
                        Intent dialogIntent = new Intent(getBaseContext(), VideoOneActivity.class);
                        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplication().startActivity(dialogIntent);
                        isDealWith = true;
                    }
                } else if (res.contains("音乐") || res.contains("歌曲") || res.contains("听歌")) {
                    if (!currentView.equals("cn.kuwo.kwmusichd")) {
                        currentView = "cn.kuwo.kwmusichd";
                        doStartApplicationWithPackageName("cn.kuwo.kwmusichd");
                        sendServiceMsg(DataUtils.openWakeUp, ""); //打开语音唤醒
                        isVoiceControl = false;
                        isDealWith = true;
                    }
                } else if (res.contains("行车记录")) {
                    // 发送广播给服务，改变悬浮窗口
                    if (!currentView.equals("ServiceViewAction")) {
                        currentView = "ServiceViewAction";
                        Intent intent = new Intent();
                        intent.setAction(DataUtils.ServiceViewAction);
                        intent.putExtra("size", 2);
                        sendBroadcast(intent);
                        isDealWith = true;
                    }
                } else if (res.contains("设置")) {
                    if (!currentView.equals("SetActivity")) {
                        currentView = "SetActivity";
                        Intent dialogIntent = new Intent(getBaseContext(), SetActivity.class);
                        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplication().startActivity(dialogIntent);
                        isDealWith = true;
                    }
                } else if (res.contains("主页")) {
                    if (!currentView.equals("HomeActivity")) {
                        currentView = "HomeActivity";
                        Intent dialogIntent = new Intent(getBaseContext(), HomeActivity.class);
                        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplication().startActivity(dialogIntent);
                        isDealWith = true;
                    }
                } else if (res.contains("返回")) {
                    if (!currentView.equals("HomeActivity")) {
                        //发送到UI返回
                        sendUIMsg(DataUtils.returnUI, ""); //发送给UI,返回操作
                        isDealWith = true;
                    }
                } else if (res.contains("关闭语音")) {
                    speakChinese("我们下次再见,谢谢!");
                    sendServiceMsg(DataUtils.openWakeUp, ""); //打开语音唤醒
                    isVoiceControl = false;
                    isDealWith = true;
                }
                if (!isDealWith) {
                    if (currentView.equals("PhoneActivity")) {
                        isDealWith = true;
                    } else if (currentView.equals("PictureOneActivity")) {
                        int number = voiceToNulmber(res);
                        if (number != 0) {
                            //发送消息给UI 打开第number个目录，或打开第number个文件
                            sendUIMsg(DataUtils.selectFunction, String.valueOf(number)); //发送给UI,返回操作
                            isDealWith = true;
                        }
                    } else if (currentView.equals("PictureTwoActivity")) {
                        int number = voiceToNulmber(res);
                        if (number != 0) {
                            //发送消息给UI 打开第number个目录，或打开第number个文件
                            sendUIMsg(DataUtils.selectFunction, String.valueOf(number)); //发送给UI,返回操作
                            isDealWith = true;
                        }
                    } else if (currentView.equals("PictureThreeActivity")) {
                        int number = voiceToNulmber(res);
                        if (number != 0) {
                            //发送消息给UI 打开第number个目录，或打开第number个文件
                            sendUIMsg(DataUtils.selectFunction, String.valueOf(number)); //发送给UI,返回操作
                            isDealWith = true;
                        }
                    } else if (currentView.equals("VideoOneActivity")) {
                        int number = voiceToNulmber(res);
                        if (number != 0) {
                            //发送消息给UI 打开第number个目录，或打开第number个文件
                            sendUIMsg(DataUtils.selectFunction, String.valueOf(number)); //发送给UI,返回操作
                            isDealWith = true;
                        }
                    } else if (currentView.equals("VideoTwoActivity")) {
                        int number = voiceToNulmber(res);
                        if (number != 0) {
                            //发送消息给UI 打开第number个目录，或打开第number个文件
                            sendUIMsg(DataUtils.selectFunction, String.valueOf(number)); //发送给UI,返回操作
                            isDealWith = true;
                        }
                    } else if (currentView.equals("VideoThreeActivity")) {
                        int number = voiceToNulmber(res);
                        if (number != 0) {
                            //发送消息给UI 打开第number个目录，或打开第number个文件
                            sendUIMsg(DataUtils.selectFunction, String.valueOf(number)); //发送给UI,返回操作
                            isDealWith = true;
                        }
                    } else if (currentView.equals("VideoFourActivity")) {
                        int number = voiceToNulmber(res);
                        if (number != 0) {
                            //发送消息给UI 打开第number个目录，或打开第number个文件
                            sendUIMsg(DataUtils.selectFunction, String.valueOf(number)); //发送给UI,返回操作
                            isDealWith = true;
                        }
                    } else if (currentView.equals("VideoFiveActivity")) {
                        int number = voiceToNulmber(res);
                        if (number != 0) {
                            //发送消息给UI 打开第number个目录，或打开第number个文件
                            sendUIMsg(DataUtils.selectFunction, String.valueOf(number)); //发送给UI,返回操作
                            isDealWith = true;
                        }
                    } else if (currentView.equals("SetActivity")) {
                        int number = 0;
                        if (res.contains("wifi")) {
                            number = 1;
                        } else if (res.contains("调频广播")) {
                            number = 2;
                        } else if (res.contains("系统设置")) {
                            number = 3;
                        } else if (res.contains("软件更新")) {
                            number = 4;
                        }
                        if (!isDealWith) {
                            number = voiceToNulmber(res);
                        }
                        if (number != 0) {
                            //发送消息给UI 打开第number个目录，或打开第number个文件
                            sendUIMsg(DataUtils.selectFunction, String.valueOf(number)); //发送给UI,返回操作
                            isDealWith = true;
                        }
                    }
                }
                if (isVoiceControl) {
                    myHandler.sendEmptyMessageDelayed(8888, res.length() * 900 + 2000);
                    //   myHandler.sendEmptyMessageDelayed(8888, 1);
                }
            }
        };
//        handler.sendEmptyMessageDelayed(2,1000);// 1秒后发送消息开始录制视频
        return super.onStartCommand(intent, flags, startId);
    }

    private int voiceToNulmber(String res) {
        boolean isDealWith = false;
        int number = 0;
        if (res.contains("前")) {
            number = 1;
            isDealWith = true;
        }
        if (res.contains("后")) {
            number = 2;
            isDealWith = true;
        }
        if (!isDealWith) {
            if (res.contains("全部")) {
                number = 1;
                isDealWith = true;
            }
            if (res.contains("已锁定")) {
                number = 2;
                isDealWith = true;
            }
        }
        if (!isDealWith) {
            if (res.contains("一") || res.contains("1")) {
                number = 1;
            } else if (res.contains("二") || res.contains("2")) {
                number = 2;
            } else if (res.contains("三") || res.contains("3")) {
                number = 3;
            } else if (res.contains("四") || res.contains("4")) {
                number = 4;
            } else if (res.contains("五") || res.contains("5")) {
                number = 5;
            } else if (res.contains("六") || res.contains("6")) {
                number = 6;
            } else if (res.contains("七") || res.contains("7")) {
                number = 7;
            } else if (res.contains("八") || res.contains("8")) {
                number = 8;
            } else if (res.contains("九") || res.contains("9")) {
                number = 9;
            } else if (res.contains("十") || res.contains("10")) {
                number = 10;
            }
        }
        return number;
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
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //设置ComponentName参数1:packagename参数2:mainActivity的路径
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            getApplication().startActivity(intent);
            //       startActivity(intent);
        }
    }

    private ImageView photoIv; // 拍照按钮
    private ImageView switchCameraIv; // 切换摄像头按钮
    private SurfaceView afterSurfaceView; // 后摄像头的预览界面
    private SurfaceView beforeSurfaceView; // 前摄像头的预览界面
    private View stateView;// 录制状态栏
    private TextView dayTv;// 日期时间
    private TextView timeTv;// 录制视频的时间
    private ImageView redIv;// 红色小圆球

    private View bottomView;// 底部控件
    private ImageView returnIv;// 返回按钮
    private ImageView voiceIv;// 语音按钮
    private ImageView timeIv;// 设置时间按钮
    private ImageView videoIv;// 视频按钮
    private ImageView pictureIv;// 图片按钮
    private ImageView lockIv;// 锁定按钮
    private ImageView recordIv;// 录制视频按钮

    // 获取布局的方法
    private void initView() {

        view = LayoutInflater.from(MainService.this).inflate(R.layout.drivingrecodeactivity_layout, null);

        photoIv = (ImageView) view.findViewById(R.id.drive_photo_iv);
        switchCameraIv = (ImageView) view.findViewById(R.id.drive_switch_camera);
        afterSurfaceView = (SurfaceView) view.findViewById(R.id.camera_afterSurfaceView);
        beforeSurfaceView = (SurfaceView) view.findViewById(R.id.camera_beforeSurfaceView);
        stateView = view.findViewById(R.id.drive_state_rl);
        dayTv = (TextView) view.findViewById(R.id.drive_day_time);
        timeTv = (TextView) view.findViewById(R.id.drive_pvr_time);
        redIv = (ImageView) view.findViewById(R.id.drive_pvr_iv);

        bottomView = view.findViewById(R.id.drive_video_bottom);

        returnIv = (ImageView) view.findViewById(R.id.drive_bottom_back);
        voiceIv = (ImageView) view.findViewById(R.id.drive_bottom_voice);
        timeIv = (ImageView) view.findViewById(R.id.drive_bottom_time);
        videoIv = (ImageView) view.findViewById(R.id.drive_bottom_video);
        pictureIv = (ImageView) view.findViewById(R.id.drive_bottom_picture);
        lockIv = (ImageView) view.findViewById(R.id.drive_bottom_lock);
        recordIv = (ImageView) view.findViewById(R.id.drive_bottom_record);

        photoIv.setOnClickListener(onClickListener);
        switchCameraIv.setOnClickListener(onClickListener);

        returnIv.setOnClickListener(onClickListener);
        voiceIv.setOnClickListener(onClickListener);
        timeIv.setOnClickListener(onClickListener);
        videoIv.setOnClickListener(onClickListener);
        pictureIv.setOnClickListener(onClickListener);
        lockIv.setOnClickListener(onClickListener);
        recordIv.setOnClickListener(onClickListener);
        isShow(false);

        threeOrFive = sharedPreferences.getBoolean(DataUtils.videoTime, false);// 录制视频的时长默认为5分钟
        showVideoTime(threeOrFive);
        isLock = sharedPreferences.getBoolean(DataUtils.videoLock, false);// 默认录制的视频文件为不锁定状态
        showVideoLock(isLock);
//        isRecording = sharedPreferences.getBoolean(DataUtils.videoIsRecording, false);// 默认不录制视频
//        recordIv.setImageResource(isRecording ? R.mipmap.drive_stop : R.mipmap.drive_start);

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.drive_switch_camera: // 切换前后摄像头
                    switchCamera(isSwitch ? 0 : 1);
                    isSwitch = !isSwitch;
                    Log.d(TAG, "切换摄像头-------切换图标");
                    break;
                case R.id.drive_photo_iv:// 点击拍照
                    photoClick();
                    break;
                case R.id.drive_bottom_back:// 返回主界面
                    isShow(false);
                    // 未显示行车记录界面时，隐藏预览界面
                    setViewSize(1 == voiceLevel ? 1 : 0);// 需要显示的界面是主界面时，显示小的预览窗口，不是则隐藏
                    break;
                case R.id.drive_bottom_voice:// 语音
                    Toast.makeText(MainService.this, "4444444444444", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "4444444444444");
                    break;
                case R.id.drive_bottom_time:// 设置录制视频的时长
                    threeOrFive = !threeOrFive;
                    showVideoTime(threeOrFive);
                    editor = sharedPreferences.edit();
                    editor.putBoolean(DataUtils.videoTime, threeOrFive);// 存储录制视频的时长
                    editor.apply();
                    break;
                case R.id.drive_bottom_video://跳转到视频界面
                    // 未显示行车记录界面时，隐藏预览界面
                    isShow(false);
                    setViewSize(0);
                    Intent intent = new Intent();
                    intent.setAction(DataUtils.gotoClass);
                    intent.putExtra("class", 1);
                    sendBroadcast(intent);
                    break;
                case R.id.drive_bottom_picture:// 跳转到图片界面
                    // 未显示行车记录界面时，隐藏预览界面
                    isShow(false);
                    setViewSize(0);
                    Intent intent1 = new Intent();
                    intent1.setAction(DataUtils.gotoClass);
                    intent1.putExtra("class", 2);
                    sendBroadcast(intent1);
                    break;
                case R.id.drive_bottom_lock:// 是否锁定录制的视频文件
                    isLock = !isLock;
                    showVideoLock(isLock);
                    editor = sharedPreferences.edit();
                    editor.putBoolean(DataUtils.videoLock, isLock);// 存储录制视频的锁定标识
                    editor.apply();
                    break;
                case R.id.drive_bottom_record:// 点击开始录像
                    if (isRecording) {
                        stopVideo();// 停止录制视频
                    } else {
                        startVideo();// 开启录像
                    }
//                    editor = sharedPreferences.edit();
//                    editor.putBoolean(DataUtils.videoIsRecording, isRecording);// 存储录制视频的开关标识
//                    editor.apply();
                    break;
                default:
                    break;
            }
        }
    };
    private boolean isGo;// 保存录制状态

    /**
     * 切换前后摄像头
     * <p/>
     * true为后摄像头，false为前摄像头
     */
    private void switchCamera(int num) {
        if (num != cameraNum) {// 需要打开的不是当前打开的摄像头
            isGo = isRecording;
            if (null != afterCamera) {
                if (isRecording) {// 处于录制视频状态
                    if (isPreviewCallback) {// 处于预览状态
                        handler.removeCallbacks(RunnableCallback2);
                    }
                    stopVideo();// 停止录制视频
                } else {// 未录制视频时
                    if (isPreviewCallback) {// 处于预览状态
                        afterCamera.setPreviewCallback(null);
                    }
                }
                afterCamera.stopPreview();
                afterCamera.release();
                afterCamera = null;
            }
            try {
                initCamera(num);
                if (null == afterCamera) {// 切换失败，打开原来的摄像头
                    afterCamera = Camera.open(cameraNum);
                }
                if (null != afterCamera) {// 打开
                    afterCamera.setPreviewDisplay(afterSurfaceHolder);
                    afterCamera.startPreview();
                    if (isGo) {// 处于录制视频状态
                        if (isPreviewCallback) {// 处于预览状态
                            handler.postDelayed(RunnableCallback2, 200);
                        }
                        startVideo();// 开始录制视频
                    } else {// 未录制视频时
                        if (isPreviewCallback) {// 处于预览状态
                            cameraPreviewCallback();
                        }
                    }
                } else {
                    Toast.makeText(MainService.this, "打开摄像头失败，请检查连个摄像头", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 切换前后摄像头的图标
            switchCameraIv.setImageResource(0 == cameraNum ? R.drawable.selector_drive_after : R.drawable.selector_drive_before);
        }
    }

    // 后摄像头的预览界面的回调
    private android.view.SurfaceHolder.Callback afterCallback = new SurfaceHolder.Callback() {

        // 创建控件调用
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "后摄像头----创建控件调用---");
            if (null != afterCamera) {
                try {
                    afterCamera.setPreviewDisplay(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                afterCamera.startPreview();
                afterSurfaceHolder = holder;
            }
        }

        // 控件大小发生改变调用
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            afterSurfaceHolder = holder;
            Log.d(TAG, "后摄像头----控件大小发生改变调用---");
        }

        //销毁控件调用
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "后摄像头---销毁控件调用----");
            // 回收摄像头资源
//            if (null != afterCamera) {
//                if (isPreview) {
//                    holder.removeCallback(this);
//                afterCamera.release();
//                    afterCamera.setPreviewCallback(null);
//                    afterCamera.stopPreview();
//                    isPreview = false;
//                }
//                afterCamera = null;
//            }

        }
    };

    /**
     * 初始化摄像头
     */
    private void initCamera(int num) {

        afterCamera = Camera.open(num);
        if (null != afterCamera) {
            Camera.Parameters parameters = afterCamera.getParameters();
            parameters.setPreviewFrameRate(30); // 每秒30帧
            parameters.setPictureFormat(ImageFormat.JPEG);// 设置照片的输出格式
            parameters.set("jpeg-quality", 85);// 照片质量
            afterCamera.setParameters(parameters);
            isPreview = true;// 摄像准备OK
            cameraNum = num;
        } else {
            Toast.makeText(MainService.this, "无法连接该摄像头！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 初始化mediaRecorder，并设置录制监听
     */
    private void initMediaRecorder() {
        if (null == afterMediaRecorder) {
            afterMediaRecorder = new MediaRecorder();
        }
        // 录制监听，录制达到最大时间时又开始录制视频
        afterMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    Log.d(TAG, "已经达到最长录制时间");
                    afterMediaRecorder.reset();
                    afterOldFilePath = afterFilePath;// 记录上次的路径
                    if (readyToMemory()) {// 检查空间是否足够，不足够则删除最早的视频文件并开始录制，没有可以删除的文件时提示手动删除文件并停止录制
                        if (prepareVideoRecorder()) {
                            afterMediaRecorder.start();//开始录制
                        }
                    }
                }
            }
        });

    }

    private boolean isFirstRecord = true;// 第一次开始录制视频

    // 开始录制视频的方法
    public void startVideo() {
        if (FileUtils.sdkIsOk()) {// SD卡OK时，准备录制视频
            if (isPreviewCallback) {// 开启了预览的状态，
                afterCamera.setPreviewCallback(null);//停止捕捉摄像头的界面
                handler.postDelayed(RunnableCallback2, 100);// 通过拍照的方式传递预览图片
            }

            // 检查空间是否足够，不足够则删除最早的视频文件并开始录制，没有可以删除的文件时提示手动删除文件并停止录制
            if (readyToMemory()) {// 默认为后录视频
                if (prepareVideoRecorder()) {// 准备录制视频
                    if (null != afterMediaRecorder) {
                        afterMediaRecorder.start();// 开始录制视频
                        // 发送广播改变录制状态按钮的图标
                        Intent intent = new Intent();
                        intent.setAction(DataUtils.ServiceViewShowAction);
                        intent.putExtra("show", false);
                        sendBroadcast(intent);
                    }
                    if (isFirstRecord || !isRecording) {// 第一次开始录制视频，或从停止录制视频到开始录制视频
                        stateView.setVisibility(View.VISIBLE);
                        redIv.startAnimation(animation);

                        recordTime = 0;
                        if (isFirstRecord) {
                            handler.sendEmptyMessage(3);
                        }
                        isFirstRecord = false;
                    }
                    isRecording = true;
                    recordIv.setImageResource(R.mipmap.drive_stop);
                    mSensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);// 注册传感器监听
                }
                Log.d(TAG, "开始录制视频失败！");
            }
        } else {// SD卡无法使用时
            Toast.makeText(MainService.this, getString(R.string.noSDK), Toast.LENGTH_SHORT).show();
        }
    }

    // 停止录制视频
    public void stopVideo() {

        Log.d(TAG, "11111111111111111停止录制视频");
        if (null != afterMediaRecorder) {
            afterMediaRecorder.stop();
        }
        mSensorManager.unregisterListener(mSensorEventListener);// 注销传感器监听

        recordIv.setImageResource(R.mipmap.drive_start);
        isRecording = false;

        redIv.clearAnimation();
        stateView.setVisibility(View.GONE);
        recordTime = 0;

        // 发送广播改变录制状态按钮的图标
        Intent intent = new Intent();
        intent.setAction(DataUtils.ServiceViewShowAction);
        intent.putExtra("show", false);
        sendBroadcast(intent);

        afterOldFilePath = afterFilePath;// 记录上次的路径
        if (isPreviewCallback) {// 预览状态
            handler.removeCallbacks(RunnableCallback2);// 停止通过拍照获取预览界面
            cameraPreviewCallback();// 开始捕捉摄像头界面传递预览界面
        }

    }

    private String afterParentPath = "";// 后录视频的日期文件夹路径
    private String afterFilePath = "";// 后录视频的视频文件路径
    private String afterOldFilePath = "";// 上次后录视频文件路径

    /**
     * 准备录制视频
     */
    private boolean prepareVideoRecorder() {
        // Step 1: 开启并设置相机
        if (null != afterCamera && null != afterMediaRecorder) {
            afterCamera.unlock();
            afterMediaRecorder.setCamera(afterCamera);
        }

        if (null != afterMediaRecorder) {
            // Step 2: 设置资源

            // 开始捕捉和编码数据到setOutputFile（指定的文件）
            afterMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            // 设置用于录制的音源
            afterMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            // 设置在录制过程中产生的输出文件的格式
            afterMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            // 设置视频编码器，用于录制
            afterMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            // 设置audio的编码格式
            afterMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            // Step 4: 设置视频大小;设置要捕获的视频的宽度和高度
            afterMediaRecorder.setVideoSize(1280, 720);

            // Step 5:  设置要捕获的视频帧速率
            afterMediaRecorder.setVideoFrameRate(30);
            // Step 6: 设置输出文件
            if (1 == cameraNum) {// 前摄像头录制的视频
                afterParentPath = DataUtils.videoPath + DataUtils.BEFORE + "/" + new SimpleDateFormat("yyyyMMdd").format(new Date());
            } else {// 后摄像头录制的视频
                afterParentPath = DataUtils.videoPath + DataUtils.AFTER + "/" + new SimpleDateFormat("yyyyMMdd").format(new Date());
            }

            FileUtils.createFile(afterParentPath);
            String lock = isLock ? DataUtils.LOCK : DataUtils.UNLOCK;//录制的视频是否为锁定文件
            afterFilePath = afterParentPath + "/" + lock + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".3gp";
            afterMediaRecorder.setOutputFile(afterFilePath);

            // Step 7: 设置输出预览
            afterMediaRecorder.setPreviewDisplay(afterSurfaceHolder.getSurface());

            // Step 8: 设置最大持续时间
            videoSetTime = threeOrFive ? 3 * 60 * 1000 : 5 * 60 * 1000;
//            videoSetTime = threeOrFive ? 30 * 1000 : 10 * 1000;
            afterMediaRecorder.setMaxDuration(videoSetTime);
            // Step 9: 准备配置MediaRecorder
            try {
                afterMediaRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
                releaseMediaRecorder();
                return false;
            }
        }
        return true;
    }

    // 回收资源
    private void releaseMediaRecorder() {
        if (afterMediaRecorder != null) {
            afterMediaRecorder.reset();   // clear recorder configuration
            afterMediaRecorder.release(); // release the recorder object
            afterMediaRecorder = null;
            if (afterCamera != null) {
                afterCamera.lock(); // lock camera for later use
            }
        }
    }


    private final long MAX_RECORDER_FILE_SIZE = 2 * 1024 * 1024 * 1024L;//视频占用的最大空间
    private final long MAX_RECORDER_PREP_FILE_SIZE = 200;// 视频文件夹最少剩余的空间

    /**
     * 录制视频前，先检查SD卡剩余存储空间
     *
     * @param type 摄像头类型，1为前，2为后
     */
    private boolean toRecording(int type) {

        String path = null;
        if (type == 1) {//前录视频文件夹
            path = DataUtils.videoPath + DataUtils.BEFORE;
        } else if (type == 2) {//后录视频文件夹
            path = DataUtils.videoPath + DataUtils.AFTER;
        }

        if (FileUtils.getDirSize(path) > MAX_RECORDER_FILE_SIZE || FileUtils.getSDFreeSize() < MAX_RECORDER_PREP_FILE_SIZE) {// 后录视频文件夹大小大于设定的文件夹最大值,或者SD卡剩余空间小于设置的预留空间时；删除最早的普通视频文件
            do {
                if (!FileUtils.deleteFile(path)) {
                    //没有视频文件可以删除时，提示用户空间不够，需手动删除
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainService.this);
                    builder.setMessage(R.string.no_space);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            stopVideo();// 停止录制视频
                            /////////////////////////////////

                        }
                    });
                    builder.show();
                    return false;
                }
            }
            while (FileUtils.getDirSize(path) > MAX_RECORDER_FILE_SIZE || FileUtils.getSDFreeSize() < MAX_RECORDER_PREP_FILE_SIZE);
        }
        return true;
    }

    /**
     * 准备录制视频的内存，内存足够返回true，不够则false
     *
     * @return 结果
     */
    private boolean readyToMemory() {

        String path = null;
        if (1 == cameraNum) {//前录视频文件夹
            path = DataUtils.videoPath + DataUtils.BEFORE;
        } else {//后录视频文件夹
            path = DataUtils.videoPath + DataUtils.AFTER;
        }
        TreeSet<String> set = new TreeSet<>();
        if (FileUtils.getVideoFilesAndSize(set, path) > MAX_RECORDER_FILE_SIZE || FileUtils.getSDFreeSize() < MAX_RECORDER_PREP_FILE_SIZE) {// 后录视频文件夹大小大于设定的文件夹最大值,或者SD卡剩余空间小于设置的预留空间时；删除最早的普通视频文件
            do {
                if (set.size() < 1) {
                    //没有视频文件可以删除时，提示用户空间不够，需手动删除
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainService.this);
                    builder.setMessage(R.string.no_space);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            stopVideo();// 停止录制视频
                            /////////////////////////////////

                        }
                    });
                    builder.show();
                    return false;
                } else {
                    String deletePath = set.first();
                    new File(deletePath).delete();
                    set.remove(deletePath);
                }

            }
            while (FileUtils.getDirSize(path) > MAX_RECORDER_FILE_SIZE || FileUtils.getSDFreeSize() < MAX_RECORDER_PREP_FILE_SIZE);
        }
        return true;
    }

    // 拍照的方法
    public void photoClick() {
        if (FileUtils.sdkIsOk()) {// SD卡可以用时
            if (null != afterCamera) {
                afterCamera.autoFocus(null);

                afterCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {

                        new SavePictureTask().execute(data);// 开启异步任务，保存照片；
                        camera.startPreview();

                        Log.d(TAG, "----- 拍照成功 ----");

                    }
                });
            }
        } else {// SD卡不可用
            Toast.makeText(MainService.this, getString(R.string.noSDK), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 异步任务 ---- 保存拍摄的照片
     */
    private class SavePictureTask extends AsyncTask<byte[], String, String> {

        @Override
        protected String doInBackground(byte[]... params) {

            //按照当前时间来创建文件夹（2016年08月）
            Calendar now = Calendar.getInstance();
            String path = DataUtils.photoPath + "/" + now.get(Calendar.YEAR) + "年" + FormatUtil.format(now.get(Calendar.MONTH) + 1) + "月/";//父路径

            File picture = new File(path);

            if (!picture.exists()) {
                picture.mkdirs();
            }
            // 创建文件名（当前的毫秒数 + .jpg）
            picture = new File(path, now.getTimeInMillis() + ".jpg");

            try {
                FileOutputStream fos = new FileOutputStream(picture.getPath());
                fos.write(params[0]);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e(TAG, "=====照片保存完成=====" + picture.getPath());

            editor = sharedPreferences.edit();
            editor.putString(DataUtils.lastPhotoPath, picture.getPath());// 存储最后一张照片的路径
            editor.commit();

            return null;
        }
    }

    /**
     * SD卡拔出、插好的广播接收器
     */
    public class UsbBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                Log.d(TAG, "拔出");
            } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                Log.d(TAG, "插好");
            }
        }
    }

    /**
     * 预览界面的大小设置方法
     *
     * @param size 类型
     */
    private void setViewSize(int size) {
        if (size == 0) {
            // 设置Window flag
            // 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。
            wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            wmParams.gravity = Gravity.LEFT | Gravity.TOP;// 调整悬浮窗口在左上角
            wmParams.width = 1;// 悬浮窗口的宽度
            wmParams.height = 1;// 悬浮窗口的高度
            Log.d("11111", "00000000");
        } else if (size == 1) {
//                    显示主界面时
            isShow(false);
            wmParams = new WindowManager.LayoutParams();
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            // 设置Window flag
            // 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。
            wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            wmParams.gravity = Gravity.LEFT | Gravity.TOP;// 调整悬浮窗口在左上角
            wmParams.x = 10;
            wmParams.y = 10;
            wmParams.width = 200;// 悬浮窗口的宽度
            wmParams.height = 150;// 悬浮窗口的高度
            Log.d("11111", "11111111");
        } else if (size == 2) {
            // 显示行车记录页面时
            view.setSystemUiVisibility(View.INVISIBLE);// 全屏显示，隐藏状态栏
//            isShow(true);
            wmParams = new WindowManager.LayoutParams();
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
//                    设置Window flag
//                    悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。
//                    wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            wmParams.gravity = Gravity.CENTER_HORIZONTAL;// 调整悬浮窗口在中间位置
            wmParams.width = maxWidth;// 悬浮窗口的宽度
            wmParams.height = maxHeight;// 悬浮窗口的高度
            handler.sendEmptyMessageDelayed(1, 500);// 显示底部布局
            Log.d("11111", "222222222222");
        }
        windowManager.updateViewLayout(view, wmParams);
    }

    // 是否显示控制按钮的方法
    private void isShow(boolean isShow) {
        photoIv.setVisibility(isShow ? View.VISIBLE : View.GONE);
        switchCameraIv.setVisibility(isShow ? View.VISIBLE : View.GONE);
        bottomView.setVisibility(isShow ? View.VISIBLE : View.GONE);
        if (isShow) {
            stateView.setVisibility(isRecording ? View.VISIBLE : View.GONE);
            if (isRecording) {
                redIv.startAnimation(animation);
            }
        } else {
            stateView.setVisibility(View.GONE);
            redIv.clearAnimation();
        }
    }

    // 录制视频时长按钮显示对应的图标
    private void showVideoTime(boolean isSelected) {

        if (isSelected) {// 录制时长为3分钟
            timeIv.setImageResource(R.drawable.selector_drive_time_three);
        } else {// 录制视频时长为5分钟
            timeIv.setImageResource(R.drawable.selector_drive_time_five);
        }
    }

    // 视频锁定按钮显示对应的图标
    private void showVideoLock(boolean isSelected) {

        if (isSelected) {// 录制视频为锁定时
            lockIv.setImageResource(R.drawable.selector_video_four_lock);
        } else {// 录制视频为不锁定时
            lockIv.setImageResource(R.drawable.selector_video_four_unlock);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 1000) {// 判断是否连接服务器
                if (null != handler) {
                    isConnect = clientSocket.isReceive;
                    Log.d(TAG, "连接状态---" + isConnect);
                    if (!isConnect) {
                        handler.sendEmptyMessage(1002);
                    }
                    handler.sendEmptyMessageDelayed(1000, 10000);
                }
            } else if (msg.what == 1002) {// 连接服务器；发送ID给服务器
                commandData.out_client.clear();// 清空待发送的数据
                commandData.out_preview_client.clear();// 清空待发送的预览数据
                clientSocket.connected(DataUtils.ip, DataUtils.port, handler, commandData);// 连接服务器
                sendMsg(DataUtils.sendID, null, null, returnIdJson(), null, commandData.out_client, commandData.out_preview_client);// 发送本设备的ID给服务器

                Log.d(TAG, "连接---" + clientSocket.isReceive);
                Toast.makeText(MainService.this, "连接---" + clientSocket.isReceive, Toast.LENGTH_SHORT).show();

            } else if (msg.what == 1101) {// 与服务器交互
                // 处理接收到数据
                while (commandData.isClientHasData()) {//可以处理
                    byte[] data = commandData.read_client.poll();//取出数据
                    if (null != data) {//数据不为空
                        disposeData(data, commandData.out_client, commandData.out_preview_client);//处理数据
                    } else {
                        commandData.setClientHasData(false);//等待处理
                    }
                }

            } else if (msg.what == 1102) {// 直连与设备交互
                // 处理接收到数据
                while (commandData.isServerHasData()) {//可以处理
                    byte[] data = commandData.read_server.poll();//取出数据
                    if (null != data) {//数据不为空
                        disposeData(data, commandData.out_server, commandData.out_preview_server);//处理数据
                    } else {
                        commandData.setServerHasData(false);//等待处理
                    }
                }

            } else if (msg.what == 999) {// 发送的消息
                byte[] b = (byte[]) msg.obj;
                ProtocolAgreementByte pab = new ProtocolAgreementByte();
                pab.analyseData(b);
                Log.d("1111", "发送数据的长度---" + b.length + "-----命令：---" + pab.getOrderName());
                Toast.makeText(MainService.this, "发送数据的长度---" + b.length + "-----命令：---" + pab.getOrderName(), Toast.LENGTH_SHORT).show();
            } else if (msg.what == 9999) {// 连接设备的WiFi热点
                wifiHotUtil.startWifiAp(jsonData.getHotName(), jsonData.getHotPwd());
            } else if (msg.what == 8888) {
                //   sendServiceMsg(DataUtils.openVoiceControl,""); //打开语音识别
                if (voiceControlDemo.status == 0) {
                    voiceControlDemo.init();
                    voiceControlDemo.begin();
                    isVoiceControl = true;
                }
            } else if (msg.what == 8887) {
                //   sendServiceMsg(DataUtils.openVoiceControl,""); //打开语音识别
                if (voiceControlDemo.status == 0) {
                    voiceControlDemo.begin();
                    isVoiceControl = true;
                }
            } else if (msg.what == 1) {// 显示行车记录界面时，显示控制按钮
                isShow(true);
            } else if (msg.what == 2) {
                Toast.makeText(MainService.this, "摄像头", Toast.LENGTH_SHORT).show();
                startVideo();// 开始录制视频
                isShow(false);
            } else if (msg.what == 3) {// 改变显示的录制时间数据
                if (null != handler) {
                    dayTv.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
                    timeTv.setText(StringUtil.intToTime(recordTime));
                    recordTime++;
                    handler.sendEmptyMessageDelayed(3, 1000);
                }
            } else if (msg.what == 4) {// 碰撞过后的处理事件

                if (recordTime < 30) {// 已录制的时间不超过30秒
                    handler.sendEmptyMessageDelayed(5, 30000);// 30秒后将上次、本次录制的视频文件复制到碰撞文件夹下，并锁定
                    Log.d("111111", "30秒后将上次、本次录制的视频文件复制到碰撞文件夹下，并锁定");
                } else {// 剩余录制的时间小于30秒
                    if (videoSetTime / 1000 - recordTime > 30) {// 已录制的时间超过30秒且剩余录制的时间大于30秒
                        handler.sendEmptyMessageDelayed(6, 30000);// 30秒后将本次录制的视频文件复制到碰撞文件夹下，并锁定
                        Log.d("111111", "30秒后将本次录制的视频文件复制到碰撞文件夹下，并锁定");
                    } else {// 已录制的时间超过30秒且剩余录制的时间小于30秒
                        handler.sendEmptyMessageDelayed(5, 30000 + videoSetTime - recordTime * 1000);// 下次录制30秒视频后，将本次、下次录制的视频文件复制到碰撞文件夹下，并锁定
                        Log.d("111111", "下次录制30秒视频后，将本次、下次录制的视频文件复制到碰撞文件夹下，并锁定");
                    }
                }

            } else if (msg.what == 5) {// 将上次、本次录制的视频文件复制到碰撞文件夹下，并锁定
                isEmergency = false;
                if (isRecording) {
                    FileUtils.moveFile(afterOldFilePath, DataUtils.after_hit);// 将上一段视频移至碰撞文件夹
                    stopVideo();
                    FileUtils.moveFile(afterFilePath, DataUtils.after_hit);// 将本端视频移至碰撞文件夹
                    startVideo();
                    Log.d("111111", "msg.what == 55555555555555555555");
                }

            } else if (msg.what == 6) {// 将本次录制的视频文件复制到碰撞文件夹下，并锁定
                isEmergency = false;
                if (isRecording) {
                    stopVideo();
                    FileUtils.moveFile(afterFilePath, DataUtils.after_hit);// 将本端视频移至碰撞文件夹
                    startVideo();
                    Log.d("111111", "msg.what == 66666666666666666");
                }
            }
        }
    };

    private String idJson = "";

    /**
     * @return 获取ID的JSON字符串
     */
    private String returnIdJson() {
        if ("".equals(idJson)) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("equipmentID", ID);
                idJson = jsonObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return idJson;
    }

    /**
     * 执行接收到的指令对应的操作的方法
     *
     * @param data    数据
     * @param out     发送数据的列表
     * @param preview 发送预览数据的列表
     */
    private void disposeData(byte[] data, LinkedBlockingQueue<byte[]> out, LinkedBlockingQueue<byte[]> preview) {

        ProtocolAgreementByte pab = new ProtocolAgreementByte();
        pab.analyseData(data);
        String orderName = pab.getOrderName();//操作指令
        Toast.makeText(MainService.this, orderName, Toast.LENGTH_SHORT).show();
        if (!StringUtil.checkNull(pab.getContentStr())) {//数据内容
            jsonData = gson.fromJson(pab.getContentStr(), JsonData.class);
        }
        if (orderName.equals(DataUtils.getToPhoto)) {// 控制拍照
            if (FileUtils.sdkIsOk()) {//SD卡OK时，调用拍照方法，回复拍照成功
                photoClick();
                sendResult(DataUtils.sendToPhoto, 1, jsonData.getToken(), out, preview);
            } else {// 未插SD卡时，直接回复拍照失败，未插SD卡
                sendResult(DataUtils.sendToPhoto, 3, jsonData.getToken(), out, preview);
            }
        } else if (orderName.equals(DataUtils.getToVideo)) {// 控制开始、停止录制视频
            if (FileUtils.sdkIsOk()) {//SD卡OK时，调用拍照方法，回复拍照成功
                if (jsonData.getFlag() == 1) {// 控制设备开始录制视频
                    if (isRecording) {// 正在录制视频，回复开始失败
                        sendResult(DataUtils.sendToVideo, 2, jsonData.getToken(), out, preview);
                    } else {// 未录制视频时，开始录制视频
                        startVideo();// 开启录像
                        sendResult(DataUtils.sendToVideo, 1, jsonData.getToken(), out, preview);

                        editor = sharedPreferences.edit();
                        editor.putBoolean(DataUtils.videoIsRecording, isRecording);// 存储录制视频的开关标识
                        editor.apply();
                    }
                } else if (jsonData.getFlag() == 2) {// 控制设备停止录制视频
                    if (isRecording) {// 正在录制视频，停止录制视频，回复停止成功
                        stopVideo();
                        sendResult(DataUtils.sendToVideo, 1, jsonData.getToken(), out, preview);

                        editor = sharedPreferences.edit();
                        editor.putBoolean(DataUtils.videoIsRecording, isRecording);// 存储录制视频的开关标识
                        editor.apply();
                    } else {// 未录制视频时，回复停止失败
                        sendResult(DataUtils.sendToVideo, 2, jsonData.getToken(), out, preview);
                    }
                }
            } else {// 未插SD卡时，直接回复失败，未插SD卡
                sendResult(DataUtils.sendToVideo, 3, jsonData.getToken(), out, preview);
            }

        } else if (orderName.equals(DataUtils.getLastPhoto)) {// 获取最近拍的一张照片

            if (FileUtils.sdkIsOk()) {// SD卡OK
                // 获取最后一张照片的路径
                String lastPhotoPath = sharedPreferences.getString(DataUtils.lastPhotoPath, "");
                if (null != lastPhotoPath && !"".equals(lastPhotoPath)) {
                    // 路径存在
                    File lastPhoto = new File(lastPhotoPath);
                    if (lastPhoto.exists()) {
                        //  文件存在
                        sendFile(lastPhotoPath, DataUtils.sendLastPhoto, out, preview);// 直接发送最后一张照片
                    } else {
                        // 文件不存在
                        List<PhotoFileBean> list = FileUtils.getFileList(DataUtils.photoPath, ".jpg");// 获取所有的图片路径
                        if (null != list) {
                            List<String> paths = list.get(list.size() - 1).getList();
                            sendFile(paths.get(paths.size() - 1), DataUtils.sendLastPhoto, out, preview);
                        } else { // 没有拍照，或所有照片已删除
                            sendResult(DataUtils.returnResult, 4, jsonData.getToken(), out, preview);
                        }
                    }
                } else {
                    // 未存储最后一张图片的路径
                    List<PhotoFileBean> list = FileUtils.getFileList(DataUtils.photoPath, ".jpg");// 获取所有的图片路径
                    if (null != list) {
                        List<String> paths = list.get(list.size() - 1).getList();
                        sendFile(paths.get(paths.size() - 1), DataUtils.sendLastPhoto, out, preview);
                    } else { // 没有拍照，或所有照片已删除
                        sendResult(DataUtils.returnResult, 4, jsonData.getToken(), out, preview);
                    }
                }
            } else { // SD卡不行时
                sendResult(DataUtils.returnResult, 3, jsonData.getToken(), out, preview);
            }
        } else if (orderName.equals(DataUtils.getPreview)) {// 开启、关闭预览
            if (jsonData.getFlag() == 1) {// 开始传送预览数据
                isPreviewCallback = true;
                if (isRecording) {
                    // 循环录制视频的状态
                    handler.postDelayed(RunnableCallback2, 100);
                } else {
                    // 不是循环录制视频的状态
                    cameraPreviewCallback();
                }
            } else if (jsonData.getFlag() == 2) { // 停止传送预览数据
                isPreviewCallback = false;
                if (isRecording) {
                    // 循环录制视频的状态
                    handler.removeCallbacks(RunnableCallback2);
                } else {
                    // 不是循环录制视频的状态
                    afterCamera.setPreviewCallback(null);
                }
                commandData.out_preview_client.clear();
            }
        } else if (orderName.equals(DataUtils.shortDistanceConnection)) {
            //收到端距离连接命令
            //随机生成热点名称和密码
            jsonData.setHotName("C66_" + StringUtil.getRandomString(4, "ABCDEFGHIJKLMNOPQRSTUVWSYZ"));
            jsonData.setHotPwd("C66_" + StringUtil.getRandomString(4, "ABCDEFGHIJKLMNOPQRSTUVWSYZ"));
            String json = gson.toJson(jsonData);
            sendMsg(DataUtils.replayShortDistanceConnection, null, null, json, null, out, preview);
            handler.sendEmptyMessageDelayed(9999, 2000);
        } else if (orderName.equals(DataUtils.setHotPwd)) {
            //收到端距离连接命令
            //随机生成热点名称和密码
//                    jsonData.setHotName("C66_" + StringUtil.getRandomString(4, "ABCDEFGHIJKLMNOPQRSTUVWSYZ"));
//                    jsonData.setHotPwd("C66_" + StringUtil.getRandomString(4, "ABCDEFGHIJKLMNOPQRSTUVWSYZ"));
            String json = gson.toJson(jsonData);
            sendMsg(DataUtils.replayShortDistanceConnection, null, null, json, null, out, preview);
            handler.sendEmptyMessageDelayed(9999, 2000);
        } else if (orderName.equals(DataUtils.getPhotoCatalogList)) {
            // 获取照片目录列表
            String picPath = DataUtils.photoPath;
            HashMap<String, Object> map = new HashMap<String, Object>();
            List<CatalogBean> list = FileUtils.getCatalogListFromCatalog(picPath);
            int size = 0;
            if (null != list) {
                size = list.size();
            }
            if (size > 0) {
                map.put("size", size);
                map.put("list", list);
            } else {
                map.put("size", size);
            }
            String json = gson.toJson(map);
            sendMsg(DataUtils.replayPhotoCatalogList, null, null, json, null, out, preview);
        } else if (orderName.equals(DataUtils.getVideoCatalogList)) {
            // 获取视频1目录列表
            // 获取视频2目录列表
            List<CatalogBean> list = new ArrayList<CatalogBean>();
//            String picPath1 = DataUtils.videoPath + DataUtils.BEFORE;// 前摄像头录制视频
            String picPath2 = DataUtils.videoPath + DataUtils.AFTER;// 后摄像头录制视频
//            List<CatalogBean> list1 = FileUtils.getCatalogListFromCatalog(picPath1);
            List<CatalogBean> list2 = FileUtils.getCatalogListFromCatalog(picPath2);//获取后摄像头录制的视频的文件夹集合
//            for (CatalogBean cb : list1) {
//                list.add(cb);
//            }
            if (null != list2) {//数据不为空
                for (CatalogBean cb : list2) {
                    list.add(cb);
                }
            }
            HashMap<String, Object> map = new HashMap<String, Object>();
            int size = 0;
            if (null != list) {
                size = list.size();
            }
            if (size > 0) {
                map.put("size", size);
                map.put("list", list);
            } else {
                map.put("size", size);
            }
            String json = gson.toJson(map);
            sendMsg(DataUtils.replayVideoCatalogList, null, null, json, null, out, preview);
        } else if (orderName.equals(DataUtils.getPhotoAppointCatalog)) {
            // 获取指定目录的图片
            String filePath = jsonData.getCatalogName();
            sendFile(filePath, out, preview);
        } else if (orderName.equals(DataUtils.getVideoAppointCatalog)) {
            // 获取指定目录的视频
            String filePath = jsonData.getCatalogName();
            sendFile(filePath, out, preview);
        } else if (orderName.equals(DataUtils.returnVersion)) {// 查询新版本的回复

            Intent intent = new Intent();
            intent.setAction(DataUtils.UpdateAction);
            intent.putExtra("versionCode", jsonData.getVersionCode());
            MainService.this.sendBroadcast(intent);

        } else if (orderName.equals(DataUtils.readySendFile)) {// 准备接收更新文件

            if (null != jsonData.getFileName()) {
                if ("apk".equals(jsonData.getFormat())) {// 接收的文件为apk升级文件时
                    apkPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + jsonData.getFileName() + "." + jsonData.getFormat();
                    // 开启文件输出流
                    try {
                        fileOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(apkPath)));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

        } else if (orderName.equals(DataUtils.sendingFile)) {// 正在接收更新文件

            // 数据不为空的时候，将数据写入指定的文件中
            try {
                if (null != pab.getContent_byts() && null != fileOut) {
                    fileOut.write(pab.getContent_byts(), 0, pab.getContent_byts().length);
                    fileOut.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (orderName.equals(DataUtils.fileSendOK)) {// 更新文件接收完成，准备更新应用
            try {
                if (null != fileOut) {
                    // 该文件传送完成关闭输出流
                    fileOut.close();
                    fileOut = null;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(MainService.this);
                builder.setTitle("是否立即更新？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isRecording) {// 如果正在录制视频，停止录制视频
                            stopVideo();
                        }
                        File file = new File(apkPath);// 安装新版本应用
                        if (file.exists() && file.getName().endsWith(".apk")) {
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            intent.addCategory("android.intent.category.DEFAULT");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                            startActivity(intent);
                        }
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.create().show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int x = 0;

    //发送目录下的所有文件
    public void sendFile(String sPath, LinkedBlockingQueue<byte[]> out, LinkedBlockingQueue<byte[]> preview) {
        x = 0;
        File filePic = new File(sPath);
        File[] tempList = filePic.listFiles();
        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                try {
                    // 读取文件
                    String name = tempList[i].getName();
                    String fileName = name.substring(0, name.lastIndexOf("."));
                    String format = name.substring(name.lastIndexOf(".") + 1);
                    long fsize = tempList[i].length();
                    String code = StringUtil.getNewFlowNum() + "p";
                    JSONObject json = new JSONObject();
                    json.put("fileName", fileName);
                    json.put("format", format);
                    json.put("fileSize", fsize);
                    json.put("fileCode", code);
                    ++x;
                    sendMsg(DataUtils.readySendFile, null, code, json.toString(), null, out, preview);
                    Thread.sleep(5);
                    FileInputStream fis = new FileInputStream(tempList[i]);
                    int bufferSize = 4096;// 一次发送数据的长度
                    byte[] buf = new byte[bufferSize];
                    while (true) {
                        int read = 0;
                        if (fis != null) {
                            read = fis.read(buf);
                        }
                        if (read == -1) {
                            break;
                        }
                        byte byt[] = new byte[read];
                        System.arraycopy(buf, 0, byt, 0, read);
                        sendMsg(DataUtils.sendingFile, null, code, null, byt, out, preview);
                        ++x;
                        Thread.sleep(150);
                    }
                    fis.close();
                    sendMsg(DataUtils.fileSendOK, null, code, null, null, out, preview);
                    Thread.sleep(5);
                    ++x;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d("000000", "发送数量---" + x);
    }

    /**
     * 发送数据的方法
     *
     * @param orderName 指令
     * @param flowId
     * @param msgCode
     * @param msgString String类型的消息
     * @param msgByte   byte[]类型的消息
     * @param out       发送数据的列表
     * @param preview   发送预览数据的列表
     * @return
     */
    private boolean sendMsg(String orderName, String flowId, String msgCode, String msgString,
                            byte[] msgByte, LinkedBlockingQueue<byte[]> out, LinkedBlockingQueue<byte[]> preview) {
        boolean boo = false;
        ProtocolAgreementByte pab = new ProtocolAgreementByte();
        pab.setOrderName(orderName);
        if (!StringUtil.checkNull(flowId)) {
            pab.setFlowId_byts(flowId.getBytes());
        }
        if (!StringUtil.checkNull(msgCode)) {
            pab.setClientCodeStr(msgCode);
        }
        if (!StringUtil.checkNull(msgString)) {
            pab.setContentStr(msgString);
        }
        if (null != msgByte) {
            pab.setContent_byts(msgByte);
        }
        int flag = pab.assemblyData();
        if (flag == 1) {
            if (DataUtils.sendPreview.equals(orderName)) {
                boo = preview.offer(pab.getMsg());// 将预览数据添加到预览数据的链表中
            } else {
                boo = out.offer(pab.getMsg());// 将数据添加到发送数据的链表中
            }
        }
        return boo;
    }

    /**
     * 发送图片文件给服务器的方法
     *
     * @param photoPath 图片路径
     * @param orderName 指令
     * @param out       发送数据的列表
     * @param preview   发送预览数据的列表
     */
    private void sendFile(String photoPath, String orderName, LinkedBlockingQueue<byte[]> out, LinkedBlockingQueue<byte[]> preview) {

        byte[] data = FileUtils.lastPhoto(photoPath, 30);// 图片转为byte数组,并压缩至30%
        Toast.makeText(MainService.this, "最后一张照片压缩后的长度---" + data.length, Toast.LENGTH_SHORT).show();
        sendMsg(orderName, null, null, null, data, out, preview);
    }

    /**
     * 统一回复的方法
     *
     * @param orderName 需要回复的命令
     * @param i         需要回复的结果
     * @param token     需要回复的用户令牌
     * @param out       发送数据的列表
     * @param preview   发送预览数据的列表
     * @return 回复的结果
     */
    private boolean sendResult(String orderName, int i, String token, LinkedBlockingQueue<byte[]> out, LinkedBlockingQueue<byte[]> preview) {

        boolean boo = false;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("result", i);
            jsonObject.put("token", token);
            boo = sendMsg(orderName, null, null, jsonObject.toString(), null, out, preview);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return boo;
    }

    /**
     * 每秒传送5张照片实现预览效果
     */
    Runnable RunnableCallback2 = new Runnable() {
        @Override
        public void run() {
            //使用拍照的方式传送预览界面
            previewCallback2();
            handler.postDelayed(this, 200);//每隔200毫毛再次执行该回调
        }
    };

    /**
     * 使用拍照的方式传送预览界面
     */
    public void previewCallback2() {
        if (null != afterCamera) {
            afterCamera.autoFocus(null);
            afterCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    if (clientSocket.isReceive) {
                        sendMsg(DataUtils.sendPreview, null, null, null, data, commandData.out_client, commandData.out_preview_client);//发送给服务器
                    }
                    camera.startPreview();
                    Log.e(TAG, "=====传输成功=====");
                }
            }); // 拍照
        }
    }

    private long num = 0l;

    /**
     * 使用捕捉摄像头的方式传送预览界面
     */
    public void cameraPreviewCallback() {
        num = 0l;
        afterCamera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                Camera.Size size = afterCamera.getParameters().getPreviewSize(); // 获取预览大小
                final int w = size.width; // 宽度
                final int h = size.height;
                final YuvImage image = new YuvImage(data, ImageFormat.NV21, w, h, null);
                ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
                if (!image.compressToJpeg(new Rect(0, 0, w, h), 30, os)) {
                }
                byte[] tmp = os.toByteArray();
                num++;
                if (num % 3 == 0) {// 隔2帧传送1帧的预览图片
                    sendMsg(DataUtils.sendPreview, null, null, null, tmp, commandData.out_client, commandData.out_preview_client);//发送给服务器
                }
            }
        });
    }

    private boolean isVoiceControl = false;
    protected static PowerManager pm = null;
    protected static PowerManager.WakeLock wakeLock = null;

    /**
     * 开启语音唤醒
     */
    private void startWakeUp() {
        // 唤醒功能打开步骤
        // 1) 创建唤醒事件管理器
        mWpEventManager = EventManagerFactory.create(MainService.this, "wp");

        // 2) 注册唤醒事件监听器
        mWpEventManager.registerListener(new EventListener() {
            @Override
            public void onEvent(String name, String params, byte[] data, int offset, int length) {
                Log.d(TAG, String.format("event: name=%s, params=%s", name, params));
                try {
                    JSONObject json = new JSONObject(params);
                    if ("wp.data".equals(name)) { // 每次唤醒成功, 将会回调name=wp.data的时间, 被激活的唤醒词在params的word字段
                        String word = json.getString("word");
                        //     txtLog.append("唤醒成功, 唤醒词: " + word + "\r\n");
                        //      Toast.makeText(MainService.this,"唤醒成功, 唤醒词: "+word , Toast.LENGTH_LONG).show();
                        ttcDemo.speak("有需要帮助吗?");
                        stopWakeUp();  //关闭唤醒
                        handler.sendEmptyMessageDelayed(8888, 2200);
//                        if(!currentView.equals("HomeActivity")) {
//                            Intent dialogIntent = new Intent(getBaseContext(), HomeActivity.class);
//                            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            getApplication().startActivity(dialogIntent);
//                        }
                    } else if ("wp.exit".equals(name)) {
                        //      txtLog.append("唤醒已经停止: " + params + "\r\n");
                    }
                } catch (JSONException e) {
                    throw new AndroidRuntimeException(e);
                }
            }
        });
        openWakeUp();
    }

    private void openWakeUp() {
        // 3) 通知唤醒管理器, 启动唤醒功能
        HashMap params = new HashMap();
        params.put("kws-file", "assets:///WakeUp.bin"); // 设置唤醒资源, 唤醒资源请到 http://yuyin.baidu.com/wake#m4 来评估和导出
        mWpEventManager.send("wp.start", new JSONObject(params).toString(), null, 0, 0);
        //   txtLog.setText(DESC_TEXT);
    }

    private void stopWakeUp() {
        // 停止唤醒监听
        mWpEventManager.send("wp.stop", null, null, 0, 0);
    }

    /**
     * 开启轨迹服务
     */
    private void startTrace() {
        Log.d(TAG, "运行-----startTrace1");
        appData.initLBSTrace();
        Log.d(TAG, "运行-----startTrace2");
        initOnStartTraceListener();
        Log.d(TAG, "运行-----startTrace3");
        // 通过轨迹服务客户端client开启轨迹服务
        Log.d(TAG, "运行-----appData.getTrace()=" + appData.getTrace());
        appData.getClient().startTrace(appData.getTrace(), startTraceListener);
        Log.d(TAG, "运行-----startTrace4");
        Log.d(TAG, "运行-----startTrace5=" + appData.getEntityName());
    }

    /**
     * 初始化OnStartTraceListener
     */
    private void initOnStartTraceListener() {
        // 初始化startTraceListener
        startTraceListener = new OnStartTraceListener() {

            // 开启轨迹服务回调接口（arg0 : 消息编码，arg1 : 消息内容，详情查看类参考）
            public void onTraceCallback(int arg0, String arg1) {
                //    sendServiceMsg(Utils.show_ToastInfo,"开启轨迹服务回调接口消息 [消息编码 : " + arg0 + "，消息内容 : " + arg1 + "]");
                //    mHandler.obtainMessage(arg0, "开启轨迹服务回调接口消息 [消息编码 : " + arg0 + "，消息内容 : " + arg1 + "]").sendToTarget();
                Log.d(TAG, "开启轨迹服务回调接口消息 [消息编码 : " + arg0 + "，消息内容 : " + arg1 + "]");
            }

            // 轨迹服务推送接口（用于接收服务端推送消息，arg0 : 消息类型，arg1 : 消息内容，详情查看类参考）
            public void onTracePushCallback(byte arg0, String arg1) {
                // TODO Auto-generated method stub
                if (0x03 == arg0 || 0x04 == arg0) {
                    try {
                        JSONObject dataJson = new JSONObject(arg1);
                        if (null != dataJson) {
                            String mPerson = dataJson.getString("monitored_person");
                            String action = dataJson.getInt("action") == 1 ? "进入" : "离开";
                            String date = StringUtil.timestamp2Str(new Timestamp(dataJson.getInt("time")), "yyyy-MM-dd HH:mm:ss");
                            long fenceId = dataJson.getLong("fence_id");
//                            mHandler.obtainMessage(-1,
//                                    "监控对象[" + mPerson + "]于" + date + " [" + action + "][" + fenceId + "号]围栏")
//                                    .sendToTarget();
                            //        sendServiceMsg(Utils.show_ToastInfo, "监控对象[" + mPerson + "]于" + date + " [" + action + "][" + fenceId + "号]围栏");
                        }
                    } catch (JSONException e) {
                        //     sendServiceMsg(Utils.show_ToastInfo,  "轨迹服务推送接口消息 [消息类型 : " + arg0 + "，消息内容 : " + arg1 + "]");
//                        mHandler.obtainMessage(-1, "轨迹服务推送接口消息 [消息类型 : " + arg0 + "，消息内容 : " + arg1 + "]")
//                                .sendToTarget();
                    }
                } else {
                    //   sendServiceMsg(Utils.show_ToastInfo,"轨迹服务推送接口消息 [消息类型 : " + arg0 + "，消息内容 : " + arg1 + "]");
//                    mHandler.obtainMessage(-1, "轨迹服务推送接口消息 [消息类型 : " + arg0 + "，消息内容 : " + arg1 + "]").sendToTarget();
                }
            }
        };
    }

    public static String currentView = "HomeActivity";

    /**
     * 改变预览窗口大小的广播
     */
    private class ServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String order = intent.getStringExtra("order");
            String data = intent.getStringExtra("data");
            String action = intent.getAction();
            if (action.equals(DataUtils.ServiceViewAction)) {// 改变悬浮窗口的大小
                setViewSize(intent.getIntExtra("size", 0));// 根据接收的数据改变控件的大小
            } else if (action.equals(MainService.Action)) {
                if (order.equals(DataUtils.speakChinese)) {
                    ttcDemo.speak(data);
                } else if (order.equals(DataUtils.openWakeUp)) {
                    isVoiceControl = false;
                    openWakeUp();
                } else if (order.equals(DataUtils.closeWakeUp)) {
                    isVoiceControl = true;
                    stopWakeUp();
                } else if (order.equals(DataUtils.currentView)) {
                    currentView = data;
                    if (data.equals("HomeActivity")) {
                        voiceLevel = 1;
                    } else {
                        voiceLevel = 2;
                    }
                } else if (order.equals("openVoiceControl")) {
                    ttcDemo.speak("有需要帮助吗?");
                    stopWakeUp();  //关闭唤醒
                    handler.sendEmptyMessageDelayed(8888, 2200);
                }
            }
        }
    }

    int voiceLevel = 1;

    public void sendUIMsg(String order, String data) {
        // 发送广播
        Intent intent = new Intent();
        intent.putExtra("order", order);
        intent.putExtra("data", data);
        intent.setAction(DataUtils.broadcastUI);
        sendBroadcast(intent);
    }
}
