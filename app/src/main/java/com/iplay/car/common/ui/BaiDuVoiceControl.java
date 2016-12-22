package com.iplay.car.common.ui;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import com.baidu.speech.VoiceRecognitionService;
import com.iplay.car.common.base.BaseActivity;
import com.iplay.car.voice.service.MainService;
import com.iplay.car.common.utils.Constant;
import com.iplay.car.common.utils.DataUtils;
import com.iplay.car.common.utils.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaiDuVoiceControl extends BaseActivity implements RecognitionListener {
    private static final String TAG = "BaseVoiceControl";
    private static final int REQUEST_UI = 1;
    public static final int STATUS_None = 0;
    public static final int STATUS_WaitingReady = 2;
    public static final int STATUS_Ready = 3;
    public static final int STATUS_Speaking = 4;
    public static final int STATUS_Recognition = 5;
    private SpeechRecognizer speechRecognizer;
    private int status = STATUS_None;
    private long speechEndTime = -1;
    private static final int EVENT_ERROR = 11;
    private String mSampleDirPath;
    private static final String SAMPLE_DIR_NAME = "easr";
    private UiReceiver uiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 注册广播接收器
        uiReceiver = new UiReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(DataUtils.broadcastUI);
        this.registerReceiver(uiReceiver, filter);
    }

    public void init(){
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this, new ComponentName(this, VoiceRecognitionService.class));
        speechRecognizer.setRecognitionListener(this);
        if (mSampleDirPath == null) {
            String sdcardPath = Environment.getExternalStorageDirectory()
                    .toString();
            mSampleDirPath = sdcardPath + "/" + SAMPLE_DIR_NAME+"/";
        }
        FileUtils.makeDir(mSampleDirPath);
        FileUtils.copyFromAssetsToSdcard(this, false,"s_1", mSampleDirPath+"s_1");
        FileUtils.copyFromAssetsToSdcard(this, false,"s_2_InputMethod", mSampleDirPath+"s_2_InputMethod");
    }

    public void begin(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(BaiDuVoiceControl.this);
        boolean api = sp.getBoolean("api", false);
        if (api) {
            switch (status) {
                case STATUS_None:
                    start();
                    status = STATUS_WaitingReady;
                    break;
                case STATUS_WaitingReady:
                    cancel();
                    status = STATUS_None;
                    break;
                case STATUS_Ready:
                    cancel();
                    status = STATUS_None;
                    break;
                case STATUS_Speaking:
                    stop();
                    status = STATUS_Recognition;
                    break;
                case STATUS_Recognition:
                    cancel();
                    status = STATUS_None;
                    break;
            }
        } else {
            start();
        }
    }

    @Override
    protected void onDestroy() {
        speechRecognizer.destroy();
        this.unregisterReceiver(uiReceiver);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            onResults(data.getExtras());
        }
    }

    public void bindParams(Intent intent) {
//        intent.putExtra(Constant.EXTRA_SOUND_START, R.raw.bdspeech_recognition_start);
//        intent.putExtra(Constant.EXTRA_SOUND_END, R.raw.bdspeech_speech_end);
//        intent.putExtra(Constant.EXTRA_SOUND_SUCCESS, R.raw.bdspeech_recognition_success);
//        intent.putExtra(Constant.EXTRA_SOUND_ERROR, R.raw.bdspeech_recognition_error);
//        intent.putExtra(Constant.EXTRA_SOUND_CANCEL, R.raw.bdspeech_recognition_cancel);
//            intent.putExtra(Constant.EXTRA_INFILE, "res:///com/xctx/android/voice/16k_test.pcm");

        intent.putExtra(Constant.EXTRA_OUTFILE, "/sdcard/outfile.pcm");
        intent.putExtra(Constant.EXTRA_GRAMMAR, "assets:///baidu_speech_grammar.bsg");
        intent.putExtra(Constant.EXTRA_SAMPLE,16000);
        intent.putExtra(Constant.EXTRA_LANGUAGE, "cmn-Hans-CN");
        intent.putExtra(Constant.EXTRA_NLU, "enable");
        intent.putExtra(Constant.EXTRA_VAD, "search");
        intent.putExtra(Constant.EXTRA_PROP, 20000);
        intent.putExtra(Constant.EXTRA_OFFLINE_ASR_BASE_FILE_PATH, "/sdcard/easr/s_1");
        //    intent.putExtra(Constant.EXTRA_LICENSE_FILE_PATH, "/sdcard/easr/license-tmp-20150530.txt");
        intent.putExtra(Constant.EXTRA_OFFLINE_LM_RES_FILE_PATH, "/sdcard/easr/s_2_InputMethod");
        intent.putExtra(Constant.EXTRA_OFFLINE_SLOT_DATA, buildTestSlotData());
    }

    private String buildTestSlotData() {
        JSONObject slotData = new JSONObject();
        JSONArray name = new JSONArray().put("李涌泉").put("郭下纶");
        JSONArray song = new JSONArray().put("七里香").put("发如雪");
        JSONArray artist = new JSONArray().put("周杰伦").put("李世龙");
        JSONArray app = new JSONArray().put("手机百度").put("百度地图");
        JSONArray usercommand = new JSONArray().put("关灯").put("开门");
        try {
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_NAME, name);
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_SONG, song);
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_ARTIST, artist);
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_APP, app);
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_USERCOMMAND, usercommand);
        } catch (JSONException e) {

        }
        return slotData.toString();
    }

    private void start() {
        print("点击了“开始”");
        Intent intent = new Intent();
        bindParams(intent);
        boolean api = true;
        if (api) {
            speechEndTime = -1;
            speechRecognizer.startListening(intent);
        } else {
            intent.setAction("com.baidu.action.RECOGNIZE_SPEECH");
            startActivityForResult(intent, REQUEST_UI);
        }
    }

    public void stop() {
        speechRecognizer.stopListening();
        print("点击了“说完了”");
    }

    public void cancel() {
        speechRecognizer.cancel();
        status = STATUS_None;
        print("点击了“取消”");
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        status = STATUS_Ready;
        print("准备就绪，可以开始说");
    }

    @Override
    public void onBeginningOfSpeech() {
        status = STATUS_Speaking;
        print("准备就绪，可以开始说话");
    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        speechEndTime = System.currentTimeMillis();
        status = STATUS_Recognition;
        print("检测到用户的已经开始说话");
    }

    int overtime = 0;
    @Override
    public void onError(int error) {
        status = STATUS_None;
        StringBuilder sb = new StringBuilder();
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                sb.append("音频问题");
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                sb.append("没有语音输入");
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                sb.append("其它客户端错");
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                sb.append("权限不足");
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                sb.append("网络问题");
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                sb.append("没有匹配的识别结");
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                sb.append("引擎");
                break;
            case SpeechRecognizer.ERROR_SERVER:
                sb.append("服务端错误");
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                sb.append("连接超时");
                break;
        }
      //  sb.append(":" + error);
        print("识别失败" + sb.toString());
        if(overtime <= 3) {
            if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT || error == SpeechRecognizer.ERROR_NO_MATCH) {
                overtime = overtime+1;
                sb = new StringBuilder();
//                sb.append("我没听清,重说一遍:");
                sb.append("我没听清,虫说一遍:");
            }
            this.speakChinese(sb.toString());
            myHandler.sendEmptyMessageDelayed(8888, sb.toString().length() * 800);
        }else{
            this.speakChinese("下次再见,谢谢");
            sendServiceMsg(DataUtils.openWakeUp, ""); //打开语音唤醒
            overtime = 0;
        }
    }

    @Override
    public abstract void onResults(Bundle results);

    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> nbest = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (nbest.size() > 0) {
            print("~临时识别结果" + Arrays.toString(nbest.toArray(new String[0])));
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        switch (eventType) {
            case EVENT_ERROR:
                String reason = params.get("reason") + "";
                print("EVENT_ERROR, " + reason);
                break;
            case VoiceRecognitionService.EVENT_ENGINE_SWITCH:
                int type = params.getInt("engine_type");
                print("*引擎切换到" + (type == 0 ? "在线" : "离线"));
                break;
        }
    }

    private void print(String msg) {
        Log.d(TAG, "----" + msg);
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

    /**
     * 获取广播数据
     *
     * @author longli
     *
     */
    public class UiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String order = intent.getStringExtra("order");
            String data = intent.getStringExtra("data");
            String action = intent.getAction();
            if (action.equals(DataUtils.broadcastUI)) {
                if (order.equals(DataUtils.openVoiceControl)) {
                    init();
                    begin();
                }
            }
        }
    }

    public void speakChinese( String data){
        sendServiceMsg(DataUtils.speakChinese,data);
    }

    public void sendServiceMsg(String order, String data) {
        // 发送广播
        Intent intent = new Intent();
        intent.putExtra("order", order);
        intent.putExtra("data", data);
        intent.setAction(MainService.Action);
        sendBroadcast(intent);
    }

    protected Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 8888) {
                 begin();
            }
        }
    };

//    public void onResults(Bundle results) {
//        ArrayList<String> nbest = results
//                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//        String res = nbest.get(0).toString();
//        System.out.println("*********识别结果**************=" + res);
//        //     speakChinese("您说的是:" + res + "?");
//        if (res.contains("音乐")) {
//            doStartApplicationWithPackageName("cn.kuwo.kwmusichd");
//            sendServiceMsg(DataUtils.openWakeUp, ""); //打开语音唤醒
//            sendServiceMsg(DataUtils.currentView, "cn.kuwo.kwmusichd"); //设置当前页面
//        } else if (res.contains("导航")) {
//            //跳转到导航
//            gotoActivity(R.id.home_menu_navigation);
//            sendServiceMsg(DataUtils.openWakeUp, ""); //打开语音唤醒
//            sendServiceMsg(DataUtils.currentView, "home_menu_navigation"); //设置当前页面
//        }else if(res.contains("蓝牙")){
//            gotoActivity(R.id.home_menu_bluetooth);
//            sendServiceMsg(DataUtils.openWakeUp, ""); //打开语音唤醒
//            sendServiceMsg(DataUtils.currentView, "home_menu_bluetooth"); //设置当前页面
//        }else if(res.contains("照片")||res.contains("图片")){
//            gotoActivity(R.id.home_menu_picture);
//            sendServiceMsg(DataUtils.openWakeUp, ""); //打开语音唤醒
//            sendServiceMsg(DataUtils.currentView, "home_menu_picture"); //设置当前页面
//        }else if(res.contains("视频")){
//            gotoActivity(R.id.home_menu_video);
//            sendServiceMsg(DataUtils.openWakeUp, ""); //打开语音唤醒
//            sendServiceMsg(DataUtils.currentView, "home_menu_video"); //设置当前页面
//        }else if(res.contains("音乐")||res.contains("歌曲")||res.contains("听歌")){
//            gotoActivity(R.id.home_menu_music);
//            sendServiceMsg(DataUtils.openWakeUp, ""); //打开语音唤醒
//            sendServiceMsg(DataUtils.currentView, "home_menu_music"); //设置当前页面
//        }else if(res.contains("行车记录")){
//            gotoActivity(R.id.home_menu_record);
//            sendServiceMsg(DataUtils.openWakeUp, ""); //打开语音唤醒
//            sendServiceMsg(DataUtils.currentView, "home_menu_record"); //设置当前页面
//        }else if(res.contains("设置")){
//            gotoActivity(R.id.home_menu_set);
//            sendServiceMsg(DataUtils.openWakeUp, ""); //打开语音唤醒
//            sendServiceMsg(DataUtils.currentView, "home_menu_set"); //设置当前页面
//        } else {
//            //   myHandler.sendEmptyMessageDelayed(8888, res.length() * 900+1000);
//            myHandler.sendEmptyMessageDelayed(8888, 1);
//        }
//    }
}