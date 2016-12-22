package com.iplay.car.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.iplay.car.common.bean.CatalogBean;
import com.iplay.car.common.bean.PhotoFileBean;
import com.iplay.car.common.bean.VideoFile;
import com.iplay.car.common.bean.VideoFileBean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by Administrator on 2016/7/27.
 */
public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * 判断sd卡是否可用
     */
    public static boolean sdkIsOk() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡所有空间
     *
     * @return size(MB)
     */
    public static long getSDAllSize() {
        //取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        //获取单个数据块的大小(byte)
        long blockSize = sf.getBlockSize();
        //获取所有数据块数
        long allBlocks = sf.getBlockCount();
        return (allBlocks * blockSize) / 1024 / 1024;
    }

    /**
     * 获取SD卡剩余容量
     *
     * @return size(MB)
     */
    public static long getSDFreeSize() {
        // 取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());

        // 获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        // 空闲的数据块的数量
        long availableBlocks = sf.getAvailableBlocks();

        // 由于API18（Android4.3）以后getBlockSize过时并且改为了getBlockSizeLong
        // 因此这里需要根据版本号来使用那一套API
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
//        {
//           long blockSize = sf.getBlockSizeLong();
//           long totalBlocks = sf.getBlockCountLong();
//           long availableBlocks = sf.getAvailableBlocksLong();
//        }
//        else
//        {
//           long blockSize = sf.getBlockSize();
//           long totalBlocks = sf.getBlockCount();
//           long availableBlocks = sf.getAvailableBlocks();
//        }

        // 返回SD卡空闲大小
        return (availableBlocks * blockSize) / 1024 / 1024; // 单位MB
    }

    /**
     * 获取指定路径下照片文件的集合
     *
     * @param path 文件路径
     * @param type 文件类型
     * @return 照片数据集合
     */
    public static List<PhotoFileBean> getFileList(String path, String type) {

        File dir = new File(path);
        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组

        if (null != files) {
            List<PhotoFileBean> list = new ArrayList<PhotoFileBean>();

            for (int i = 0; i < files.length; i++) {

                String name = files[i].getName();//文件名
                Log.d(TAG, "1111----" + name);
                if (files[i].isDirectory()) { // 该文件为文件夹时
                    File[] photos = files[i].listFiles(); // 获取该文件夹下的文件
                    Log.d(TAG, "1111----" + photos.length);
                    if (null != photos) {
                        ArrayList<String> photoList = new ArrayList<String>();
                        for (int j = 0; j < photos.length; j++) {
                            if (photos[j].isFile()) { // 该文件为文件时
                                String photoName = photos[j].getName();// 获取文件名
                                if (photoName.endsWith(type)) {// 该文件为需要获取的文件
                                    photoList.add(photos[j].getAbsolutePath());// 将文件路径添加到集合中
                                }
                                Log.d(TAG, "1111----" + photoName);
                            }
                        }
                        if (photoList.size() > 0) {// 有需要的文件时，存储文件夹名、文件集合
                            PhotoFileBean photoFileBean = new PhotoFileBean();
                            photoFileBean.setName(name);
                            photoFileBean.setList(photoList);
                            list.add(photoFileBean);// 添加到集合中
                        }
                    }
                }

            }
            // 有需要的数据时，返回数据集合
            if (list.size() > 0) {
                return list;
            }
        }
        return null;
    }

    /**
     * 压缩图片
     *
     * @param imgPath 图片路径
     * @param pixelW  目标宽度
     * @param pixelH  目标高度
     * @return 压缩后的bitmap
     */
    public static Bitmap ratio(String imgPath, float pixelW, float pixelH) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true，即只读边不读内容
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        // Get bitmap info, but notice that bitmap is null now
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, newOpts);

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 想要缩放的目标尺寸
        float hh = pixelH;// 设置高度为240f时，可以明显看到图片缩小了
        float ww = pixelW;// 设置宽度为120f，可以明显看到图片缩小了
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) {
            be = 1;
        }
        newOpts.inSampleSize = be;//设置缩放比例
        // 开始压缩图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(imgPath, newOpts);
        // 压缩好比例大小后再进行质量压缩
//        return compress(bitmap, maxSize); // 这里再进行质量压缩的意义不大，反而耗资源，删除
        return bitmap;
    }

    /**
     * 获取指定路径下的指定类型的视频文件
     *
     * @param path 路径
     * @param type 获取视频文件的类型，1为全部，2为已锁定
     * @return 文件夹集合
     */
    public static List<VideoFileBean> getVideoList(String path, int type) {

        File dir = new File(path);
        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组

        List<VideoFileBean> list = new ArrayList<VideoFileBean>();// 文件夹的数据集合

        if (null != files) {

            for (int i = 0; i < files.length; i++) {

                String name = files[i].getName();//文件名

                ArrayList<VideoFile> data = new ArrayList<VideoFile>();// 视频文件的集合
                if (files[i].isDirectory()) { // 该文件为文件夹时
                    File[] videos = files[i].listFiles(); // 获取该文件夹下的文件

                    for (int j = 0; j < videos.length; j++) {
                        if (videos[j].isFile()) { // 该文件为文件时
                            String videoName = videos[j].getName();// 获取文件名
                            if (videoName.endsWith(".3gp") || videoName.endsWith(".mp4")) {// 该文件为需要获取的文件

                                if (type == 1) {// 获取全部视频文件
                                    data.add(createVideoFile(videos[j], videoName));
                                } else if (type == 2) {// 获取已锁定的视频文件
                                    if (videoName.startsWith(DataUtils.LOCK)) {
                                        data.add(createVideoFile(videos[j], videoName));
                                    }
                                } else if (type == 3) {
                                    // 获取所有未锁定的视频文件
                                    if (!videoName.startsWith(DataUtils.LOCK)) {
                                        data.add(createVideoFile(videos[j], videoName));
                                    }
                                }
                            }
                        }
                    }

                }
                // 视频文件数量大于0，才创建文件夹类，加入集合
                if (data.size() > 0) {
                    VideoFileBean videoFileBean = new VideoFileBean();//文件夹数据
                    videoFileBean.setName(name);// 文件夹名
                    videoFileBean.setList(data);// 视频文件集合
                    list.add(videoFileBean);
                }

            }
            if (list.size() > 0) {
                return list;
            }
        }

        return null;

    }

    /**
     * 获取指定路径下的视频文件夹集合
     *
     * @param path 指定路径
     * @param type 视频文件类型；1为所有视频、2为锁定视频
     * @return 视频文件夹集合
     */
    public static List<String> getVideoFolderList(String path, int type) {
        File folder = new File(path);
        if (folder.isDirectory()) {
            List<String> list = new ArrayList<>();
            File[] files = folder.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    File[] videos = files[i].listFiles();
                    if (videos.length > 0) {// 该文件夹下有文件
                        if (1 == type) {// 全部视频
                            list.add(files[i].getName());
                        } else if (2 == type) {// 锁定视频
                            for (int j = 0; j < videos.length; j++) {
                                if (videos[j].isFile()) {
                                    String name = videos[j].getName();
                                    if (name.startsWith(DataUtils.LOCK)) {
                                        list.add(files[i].getName());
                                        continue;
                                    }
                                }
                            }
                        }
                    } else {// 该文件夹下没有文件
                        files[i].delete();// 删除空文件夹
                    }
                }
            }
            return list;
        }
        return null;
    }

    /**
     * 获取指定路径的视频文件的集合
     *
     * @param path 路径
     * @param type 视频文件类型；1为所有视频、2为锁定视频
     * @return 视频文件的集合
     */
    public static ArrayList<VideoFile> getVideoFileList(String path, int type) {
        File folder = new File(path);
        if (folder.isDirectory()) {
            ArrayList<VideoFile> list = new ArrayList<>();
            File[] videos = folder.listFiles(); // 获取该文件夹下的文件
            for (int j = 0; j < videos.length; j++) {
                if (videos[j].isFile()) { // 该文件为文件时
                    String videoName = videos[j].getName();// 获取文件名
                    if (videoName.endsWith(".3gp") || videoName.endsWith(".mp4")) {// 该文件为需要获取的文件

                        if (type == 1) {// 获取全部视频文件
                            list.add(createVideoFile(videos[j], videoName));
                        } else if (type == 2) {// 获取已锁定的视频文件
                            if (videoName.startsWith(DataUtils.LOCK)) {
                                list.add(createVideoFile(videos[j], videoName));
                            }
                        }
                    }
                }
            }
            return list;
        }
        return null;
    }

    /**
     * 获取视频文件信息类对象
     *
     * @param file      视频文件
     * @param videoName 文件名
     * @return 视频文件信息类对象
     */
    public static VideoFile createVideoFile(File file, String videoName) {
        VideoFile videoFile = new VideoFile(); // 视频文件数据
        videoFile.setName(videoName); // 文件名
        videoFile.setLok(videoName.startsWith(DataUtils.LOCK));// 是否为锁定文件
        String videoPath = file.getAbsolutePath();// 路径
        videoFile.setPath(videoPath);

        double d = file.length() / 1024 / 1024.00;
        float size = (float) (Math.round(d * 100)) / 100;// 大小（MB）
        videoFile.setSize(size);
        long l = file.lastModified();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(l);
        String time = formatter.format(date);// 时间

        videoFile.setDate(time);
//        // 如果文件大小大于0时，获取缩略图
//        if (size > 0) {
//            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//            retriever.setDataSource(videoPath);
//            Bitmap bitmap = comp(retriever.getFrameAtTime(), pixelW, pixelH);// 缩略图
//
//            videoFile.setBitmap(bitmap);
//        }
        return videoFile;
    }

    /**
     * 获取缩略图
     *
     * @param videoPath 视频文件的路径
     * @param pixelW    缩放后的宽度
     * @param pixelH    缩放后的高度
     * @return 缩略图
     */
    public static Bitmap comp(String videoPath, float pixelW, float pixelH) {

        // 获取视频文件的缩略图
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        Bitmap image = retriever.getFrameAtTime();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if (baos.toByteArray().length / 1024 > 1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = pixelH;//这里设置高度为800f
        float ww = pixelW;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return bitmap;
    }

    /**
     * 压缩图片质量
     *
     * @param photoPath 图片路径
     * @param be        压缩比例
     * @return 压缩后图片的byte数组
     */
    public static byte[] lastPhoto(String photoPath, int be) {
        Bitmap image = BitmapFactory.decodeFile(photoPath);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, be, baos);
        if (baos.toByteArray().length / 1024 > 1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
        }

        return baos.toByteArray();
    }

    /*
    * 获取指定目录下的所有目录列表
    */
    public static List<CatalogBean> getCatalogListFromCatalog(String catalog) {
        List<CatalogBean> list = null;
        File file = new File(catalog);
        File[] tempList = file.listFiles();
        int size = 0;
        if (null != tempList) {
            size = tempList.length;
        }
        if (size > 0) {
            list = new ArrayList<CatalogBean>();
        }
        for (int i = 0; i < size; i++) {
            if (!tempList[i].isFile()) {
                CatalogBean cb = new CatalogBean();
                cb.setCatalogName(catalog + "/" + tempList[i].getName());
                list.add(cb);
            }
        }
        return list;
    }

    /**
     * 创建照片和视频文件夹
     */
    public static void create() {
        createFile(DataUtils.videoPath + DataUtils.BEFORE);
        createFile(DataUtils.videoPath + DataUtils.AFTER);
        createFile(DataUtils.photoPath);
        createFile(DataUtils.after_hit);
        createFile(DataUtils.before_hit);
    }

    /**
     * 指定路径的文件是否存在，不存在则创建文件
     *
     * @param filePath 文件路径
     */
    public static void createFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 获取指定路径的文件的大小
     *
     * @param path 文件路径
     * @return 文件的大小
     */
    public static long getDirSize(String path) {

        File file = new File(path);
        long size = 0;
        File files[] = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                size = size + getDirSize(files[i]);
            } else {
                size = size + files[i].length();
            }
        }
        return size;
    }

    /**
     * 获取指定文件的大小
     *
     * @param f 文件
     * @return 文件大小
     */
    public static long getDirSize(File f) {
        long size = 0;
        File files[] = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                size = size + getDirSize(files[i]);
            } else {
                size = size + files[i].length();
            }
        }

        return size;
    }

    /**
     * 删除指定路径的最早的普通视频文件
     *
     * @param path 文件路径
     * @return 是否删除成功
     */
    public static boolean deleteFile(String path) {

        boolean isOK = false;
        File file = new File(path);
        List<File> list = new ArrayList<>();
        getVIDFile(list, file);
        if (list.size() > 0) {
            int min = 0;// 最早的视频文件的位置
            String fileName = list.get(min).getName().substring(3);
            String twoName = null;
            for (int i = 1; i < list.size(); i++) {
                twoName = list.get(i).getName().substring(3);
                if (compareDate(fileName, twoName) == 1) {
                    min = i;
                    fileName = twoName;
                }
            }
            list.get(min).delete();// 删除最早的视频文件
            isOK = true;
        }
        return isOK;
    }

    /**
     * 获取指定路径的普通视频文件集合
     *
     * @param file 文件夹
     * @return 普通视频文件集合
     */
    public static void getVIDFile(List<File> list, File file) {

        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {//是文件夹时
                getVIDFile(list, files[i]);
            } else {// 是文件时
                if (files[i].getName().startsWith(DataUtils.UNLOCK)) {//是VID开头的普通视频文件
                    list.add(files[i]);
                }
            }
        }

    }

    /**
     * 比较两个日期的大小
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return 1大于2时返回1
     */
    public static int compareDate(String date1, String date2) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");

        try {
            java.util.Date d1 = df.parse(date1);
            java.util.Date d2 = df.parse(date2);
            if (d1.getTime() > d2.getTime()) {
                return 1;
            } else if (d1.getTime() < d2.getTime()) {
                return -1;
            } else {
                return 0;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    private static String FormetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     *
     * @param filePath 文件路径
     * @return 计算好的带B、KB、MB、GB的字符串
     */
    public static String getAutoFileOrFilesSize(String filePath) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getDirSize(file);
            } else {
                blockSize = getDirSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("获取文件大小", "获取失败!");
        }
        return FormetFileSize(blockSize);
    }

    public static void makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /*
     * 将sample工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
     *
     * @param isCover 是否覆盖已存在的目标文件
     * @param source
     * @param dest
    */
    @SuppressWarnings("unused")
    public static void copyFromAssetsToSdcard(Context context, boolean isCover, String source, String dest) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = context.getResources().getAssets().open(source);
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

    /**
     * 将目标文件移动到指定的路径，并修改为锁定为文件
     *
     * @param startPath 目标文件路径
     * @param goalPath  指定文件的父路径
     */
    public static void moveFile(String startPath, String goalPath) {
        try {
            File startFile = new File(startPath);
            if (startFile.exists()) { // 起始文件存在时
                createFile(goalPath);// 创建指定路径的文件夹
                String name = startFile.getName();
                name = name.replace(name.substring(0, DataUtils.LOCK.length()), DataUtils.LOCK);// 将视频文件标识为锁定文件
                InputStream inputStream = new FileInputStream(startFile);
                FileOutputStream fileOutputStream = new FileOutputStream(goalPath + "/" + name);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = inputStream.read(bytes)) != -1) {
                    fileOutputStream.write(bytes, 0, length);
                    fileOutputStream.flush();
                }
                if (null != fileOutputStream) {
                    fileOutputStream.close();
                    fileOutputStream = null;
                }
                if (null != inputStream) {
                    inputStream.close();
                    inputStream = null;
                }
                startFile.delete();// 删除源文件
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static long getVideoFilesAndSize(TreeSet<String> set, String path) {

        File file = new File(path);
        return getVideoFilesAndSize(set,file);

    }

    public static long getVideoFilesAndSize(TreeSet<String> set,File file){

        long size = 0l;
        File[] files = file.listFiles();
        for (File f : files){
            if (f.isDirectory()){
                size = size + getVideoFilesAndSize(set,f);
            }else{
                size = size + f.length();
                if (f.getName().startsWith(DataUtils.UNLOCK)){
                    set.add(f.getPath());
                }
            }
        }
        return size;
    }
}
