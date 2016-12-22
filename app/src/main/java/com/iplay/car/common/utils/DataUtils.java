package com.iplay.car.common.utils;

import android.os.Environment;

/**
 * Created by Administrator on 2016/8/9.
 */
public class DataUtils {
    public static final String NAME = "mirror";// 轻量存储的文件名
    public static final String photoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "CameraPic";
    //        public static final String photoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "Pictures";
    // 视频文件的父路径
    public static final String videoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "CameraVideo";
    public static final String BEFORE = "/before";// 前录视频的文件夹名称
    public static final String AFTER = "/after";// 后录视频的文件夹名称
    public static final String before_hit = videoPath + BEFORE + "/hit";// 前录视频的碰撞文件路径
    public static final String after_hit = videoPath + AFTER + "/hit";// 后录视频的碰撞文件路径
    public static final String LOCK = "LOK";// 锁定文件
    public static final String UNLOCK = "VID";// 未锁定文件
    public static final String MainServiceAction = "com.xctx.rearviewMirror.voice.service.MainService";//MainService的ACTION
    public static final String ServiceViewAction = "service.view";// 改变悬浮窗口的广播
    public static final String ServiceViewShowAction = "service.view.show";// 改变录制状态按钮的状态的广播
    public static final String ServiceVideoAction = "service.video";// 开始、结束录制视频的广播
    public static final String ServicePhotoAction = "service.photo";// 拍照的广播
    public static final String ServiceRemoveViewAction = "service.remove.view.photo";// 移除悬浮窗口的广播
    public static final String RecodeReceiverAction = "DrivingRecodeActivity.RecodeReceiver";// 行车记录界面的广播
    public static final String UpdateAction = "update.action";//应用更新页面的广播的action
    public static final String gotoClass = "goto.class";//跳转界面的action


    public static final String videoIsRecording = "videoIsRecording";// 存储是否持续录像的标签
    public static final String videoTime = "videoTime";// 存储录像时长的标签
    public static final String videoLock = "videoLock";// 存储录像是否锁定的标签
    public static final String lastPhotoPath = "lastPhotoPath";// 存储最后一张照片的路径的标签
    public static final String hotName = "hotName";// 存储热点名的标签
    public static final String hotPassword = "hotPassword";// 存储热点密码的标签
    public static final String impactFlag = "impactFlag";// 存储停车监控开关的标签
    public static final String ip = "112.74.131.61";// 服务器的IP地址
    public static final int port = 3887;// 连接服务器的端口


    public static final String sendID = "A001";// 客服端登入指令
    public static final String getToPhoto = "A011";// App控制设备拍照
    public static final String sendToPhoto = "B011";// 控制拍照回复
    public static final String getLastPhoto = "A012";// App获取最近一次拍照
    public static final String sendLastPhoto = "B012";// 获取照片回复
    public static final String getPreview = "A013";// 打开关闭 预览
    public static final String sendPreview = "B013";// 预览图片数据
    public static String shortDistanceConnection = "A016"; //短距离连接
    public static String setHotPwd = "A017";  //设置热点，及热点名称
    public static String replayShortDistanceConnection = "B016"; //短距离连接的回复
    public static final String sendVersion = "A018"; //查询新版本
    public static final String returnVersion = "B018"; //查询新版本的回复
    public static final String sendUpdate = "A019"; //更新新版本

    public static final String getToVideo = "A20";// App控制设备开始、停止录制视频
    public static final String sendToVideo = "B20";// App控制设备开始、停止录制视频的回复


    public static final String getPhotoCatalogList = "C001"; //获取照片目录列表
    public static final String getVideoCatalogList = "C002"; //获取视频目录列表
    public static final String replayPhotoCatalogList = "D001"; //回复目录列表
    public static final String replayVideoCatalogList = "D002"; //回复目录列表
    public static final String getPhotoAppointCatalog = "C003"; //获取指定目录的图片
    public static final String getVideoAppointCatalog = "C004"; //获取指定目录的视频
    public static String readySendFile = "9002"; // 准备文件传送
    public static String sendingFile = "9003";  //文件传送中
    public static String fileSendOK = "9004";  //文件传输完成
    public static final String returnResult = "2001";// 统一回复指令；1 ：处理成功、2 ：处理失败、3 ：未插SD卡、4 ：
    public static final String broadcastUI = "com.xctx.rearviewMirror.ui";
    public static final String openVoiceControl = "openVoiceControl";
    public static final String speakChinese = "speakChinese";
    public static final String openWakeUp = "openWakeUp";
    public static final String closeWakeUp = "closeWakeUp";
    public static final String currentView = "currentView";
    public static final String ttcSpeakFinish = "ttcSpeakFinish";
    public static final String returnUI = "returnUI";
    public static final String selectFunction = "selectFunction";
}