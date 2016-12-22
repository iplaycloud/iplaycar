package com.iplay.car.common.utils;

import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2016/8/11.
 */
public class FormatUtil {

    /**
     * 将缓存的视频文件夹的数据转存到存储视频文件的路径下
     *
     * @param recAudioFile 缓存的视频文件
     * @param path1        子文件夹一；（前录视频文件夹：/before 或者后录视频文件夹：/after ）
     * @param isLock       是否存储为锁定文件；（锁定文件以LOK开头，未锁定文件以VID开头）
     */
    public static void videoRename(File recAudioFile, String path1, String isLock) {
        Date date = new Date();

        // 存储视频文件的父路径--- CameraVideo/前录视频或后录视频/年月日
        String path = DataUtils.videoPath + path1 + "/" + new SimpleDateFormat("yyyyMMdd").format(date) + "/";

        // 视频文件名 --- 是否为锁定文件 + 年月日时分秒 + .3gp
        String fileName = isLock + new SimpleDateFormat("yyyyMMddHHmmss")
                .format(date) + ".3gp";
        File out = new File(path);
        if (!out.exists()) {
            out.mkdirs();
        }
        out = new File(path, fileName);
        Log.d("FormatUtil", "视频文件的位置："+out.getPath());
        if (recAudioFile.exists())
            recAudioFile.renameTo(out);

    }

    /**
     * 用以计时操作的相关方法
     *
     * @param num
     * @return
     */
    public static String format(int num) {

        String s = num + "";
        if (s.length() == 1) {
            s = "0" + s;
        }
        return s;
    }
}
