package com.iplay.car.voice.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizeBag;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/12.
 */
public class MainService1 extends Service {
    private static final String TAG = "MainService1";
    public static final String Action = "com.xctx.rearviewMirror.voice.service.MainService1";

    @Override
    public void onDestroy() {
        Log.d(TAG, "销毁");

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("1111", "绑定");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initialEnv();
        initialTts();
        ttcHandler.sendEmptyMessageDelayed(8888, 10000);
        return super.onStartCommand(intent, flags, startId);
    }

    private Handler ttcHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 8888){
                //测试说话
                // batchSpeak();
                speak();
            }
        }
    };

    private String mSampleDirPath;
    private static final String SAMPLE_DIR_NAME = "baiduTTS";
    private static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female.dat";
    private static final String SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male.dat";
    private static final String TEXT_MODEL_NAME = "bd_etts_text.dat";
    private static final String LICENSE_FILE_NAME = "temp_license";
    private static final String ENGLISH_SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female_en.dat";
    private static final String ENGLISH_SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male_en.dat";
    private static final String ENGLISH_TEXT_MODEL_NAME = "bd_etts_text_en.dat";
    //初始化语音合成资源文件
    private void initialEnv() {
        if (mSampleDirPath == null) {
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            mSampleDirPath = sdcardPath + "/" + SAMPLE_DIR_NAME;
        }
        makeDir(mSampleDirPath);
        copyFromAssetsToSdcard(false, SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, TEXT_MODEL_NAME, mSampleDirPath + "/" + TEXT_MODEL_NAME);
        copyFromAssetsToSdcard(false, LICENSE_FILE_NAME, mSampleDirPath + "/" + LICENSE_FILE_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_TEXT_MODEL_NAME);
    }

    class ssl implements SpeechSynthesizerListener {
        /*
        * @param arg0
        */
        @Override
        public void onSynthesizeStart(String utteranceId) {
            //     toPrint("onSynthesizeStart utteranceId=" + utteranceId);
            Toast.makeText(MainService1.this,"onSynthesizeStart utteranceId=" + utteranceId,Toast.LENGTH_LONG).show();
        }

        /**
         * 合成数据和进度的回调接口，分多次回调
         *
         * @param utteranceId
         * @param data 合成的音频数据。该音频数据是采样率为16K，2字节精度，单声道的pcm数据。
         * @param progress 文本按字符划分的进度，比如:你好啊 进度是0-3
         */
        @Override
        public void onSynthesizeDataArrived(String utteranceId, byte[] data, int progress) {
            // toPrint("onSynthesizeDataArrived");
            //  mHandler.sendMessage(mHandler.obtainMessage(UI_CHANGE_SYNTHES_TEXT_SELECTION, progress, 0));
        }

        /**
         * 合成正常结束，每句合成正常结束都会回调，如果过程中出错，则回调onError，不再回调此接口
         *
         * @param utteranceId
         */
        @Override
        public void onSynthesizeFinish(String utteranceId) {
            //    toPrint("onSynthesizeFinish utteranceId=" + utteranceId);
            Toast.makeText(MainService1.this,"onSynthesizeFinish utteranceId=" + utteranceId,Toast.LENGTH_LONG).show();
        }

        /**
         * 播放开始，每句播放开始都会回调
         *
         * @param utteranceId
         */
        @Override
        public void onSpeechStart(String utteranceId) {
            //    toPrint("onSpeechStart utteranceId=" + utteranceId);
            Toast.makeText(MainService1.this,"onSpeechStart utteranceId=" + utteranceId,Toast.LENGTH_LONG).show();
        }

        /**
         * 播放进度回调接口，分多次回调
         *
         * @param utteranceId
         * @param progress 文本按字符划分的进度，比如:你好啊 进度是0-3
         */
        @Override
        public void onSpeechProgressChanged(String utteranceId, int progress) {
            // toPrint("onSpeechProgressChanged");
            //   mHandler.sendMessage(mHandler.obtainMessage(UI_CHANGE_INPUT_TEXT_SELECTION, progress, 0));
        }

        /**
         * 播放正常结束，每句播放正常结束都会回调，如果过程中出错，则回调onError,不再回调此接口
         *
         * @param utteranceId
         */
        @Override
        public void onSpeechFinish(String utteranceId) {
            //   toPrint("onSpeechFinish utteranceId=" + utteranceId);
            Toast.makeText(MainService1.this,"onSpeechFinish utteranceId=" + utteranceId,Toast.LENGTH_LONG).show();
        }

        /**
         * 当合成或者播放过程中出错时回调此接口
         *
         * @param utteranceId
         * @param error 包含错误码和错误信息
         */
        @Override
        public void onError(String utteranceId, SpeechError error) {
            //    toPrint("onError error=" + "(" + error.code + ")" + error.description + "--utteranceId=" + utteranceId);
            Toast.makeText(MainService1.this,"onError error=" + "(" + error.code + ")" + error.description + "--utteranceId=" + utteranceId,Toast.LENGTH_LONG).show();
        }
    }

    private SpeechSynthesizer mSpeechSynthesizer;
    private void initialTts() {
        this.mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        this.mSpeechSynthesizer.setContext(MainService1.this);
        this.mSpeechSynthesizer.setSpeechSynthesizerListener(new ssl());
        // 文本模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mSampleDirPath + "/"
                + TEXT_MODEL_NAME);
        // 声学模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mSampleDirPath + "/"
                + SPEECH_FEMALE_MODEL_NAME);
        // 本地授权文件路径,如未设置将使用默认路径.设置临时授权文件路径，LICENCE_FILE_NAME请替换成临时授权文件的实际路径，仅在使用临时license文件时需要进行设置，如果在[应用管理]中开通了正式离线授权，不需要设置该参数，建议将该行代码删除（离线引擎）
        // 如果合成结果出现临时授权文件将要到期的提示，说明使用了临时授权文件，请删除临时授权即可。
        //   this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_LICENCE_FILE, mSampleDirPath + "/" + LICENSE_FILE_NAME);
        // 请替换为语音开发者平台上注册应用得到的App ID (离线授权)
        this.mSpeechSynthesizer.setAppId("8976532"/*这里只是为了让Demo运行使用的APPID,请替换成自己的id。*/);
        // 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
        this.mSpeechSynthesizer.setApiKey("FtylQVMTN6ieZBApAEn7Yatd",
                "0c4602e054a3a3a6a0b9525dcf3240f4"/*这里只是为了让Demo正常运行使用APIKey,请替换成自己的APIKey*/);
        // 发音人（在线引擎），可用参数为0,1,2,3。。。（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置Mix模式的合成策略
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 授权检测接口(只是通过AuthInfo进行检验授权是否成功。)
        // AuthInfo接口用于测试开发者是否成功申请了在线或者离线授权，如果测试授权成功了，可以删除AuthInfo部分的代码（该接口首次验证时比较耗时），不会影响正常使用（合成使用时SDK内部会自动验证授权）
        AuthInfo authInfo = this.mSpeechSynthesizer.auth(TtsMode.MIX);

        if (authInfo.isSuccess()) {
            //   toPrint("auth success");
            Toast.makeText(MainService1.this,"语音合成 auth success",Toast.LENGTH_LONG).show();
        } else {
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            //   toPrint("auth failed errorMsg=" + errorMsg);
            Toast.makeText(MainService1.this,"auth failed errorMsg=" + errorMsg,Toast.LENGTH_LONG).show();
        }

        // 初始化tts
        mSpeechSynthesizer.initTts(TtsMode.MIX);
        // 加载离线英文资源（提供离线英文合成功能）
        int result =
                mSpeechSynthesizer.loadEnglishModel(mSampleDirPath + "/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath
                        + "/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
        //   toPrint("loadEnglishModel result=" + result);
        Toast.makeText(MainService1.this,"loadEnglishModel result=" + result,Toast.LENGTH_LONG).show();
        //打印引擎信息和model基本信息
        //  printEngineInfo();
    }

    private void batchSpeak() {
        List<SpeechSynthesizeBag> bags = new ArrayList<SpeechSynthesizeBag>();
        bags.add(getSpeechSynthesizeBag("123456", "0"));
        bags.add(getSpeechSynthesizeBag("你好", "1"));
        bags.add(getSpeechSynthesizeBag("使用百度语音合成SDK", "2"));
        bags.add(getSpeechSynthesizeBag("hello", "3"));
        bags.add(getSpeechSynthesizeBag("这是一个demo工程", "4"));
        int result = this.mSpeechSynthesizer.batchSpeak(bags);
        if (result < 0) {
            //    toPrint("error,please look up error code in doc or URL:http://yuyin.baidu.com/docs/tts/122 ");
            Toast.makeText(MainService1.this,"error,please look up error code in doc or URL:http://yuyin.baidu.com/docs/tts/122 ",Toast.LENGTH_LONG).show();
        }
    }

    private void speak() {
        String text = "欢迎使用百度语音合成SDK,百度语音为你提供支持。";
        int result = this.mSpeechSynthesizer.speak(text);
        if (result < 0) {
            //  toPrint("error,please look up error code in doc or URL:http://yuyin.baidu.com/docs/tts/122 ");
            Toast.makeText(MainService1.this,"error,please look up error code in doc or URL:http://yuyin.baidu.com/docs/tts/122 ",Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(MainService1.this,text,Toast.LENGTH_LONG).show();
        }
    }

    private SpeechSynthesizeBag getSpeechSynthesizeBag(String text, String utteranceId) {
        SpeechSynthesizeBag speechSynthesizeBag = new SpeechSynthesizeBag();
        //需要合成的文本text的长度不能超过1024个GBK字节。
        speechSynthesizeBag.setText(text);
        speechSynthesizeBag.setUtteranceId(utteranceId);
        return speechSynthesizeBag;
    }

    private void makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 将sample工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
     *
     * @param isCover 是否覆盖已存在的目标文件
     * @param source
     * @param dest
     */
    private void copyFromAssetsToSdcard(boolean isCover, String source, String dest) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = getResources().getAssets().open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}