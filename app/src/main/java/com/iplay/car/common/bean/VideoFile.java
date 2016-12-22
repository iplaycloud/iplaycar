package com.iplay.car.common.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2016/8/4.
 * <p/>
 * 视频文件---文件类
 */
public class VideoFile implements Parcelable {
    private String name;//文件名
    private String path;//文件路径
    private String Date;//文件的创建时间
    private float size;//文件大小
    private String time;//视频的时长
    private boolean isLok;// 是否为锁定文件

    public VideoFile() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isLok() {
        return isLok;
    }

    public void setLok(boolean isLok) {
        this.isLok = isLok;
    }

    // —— 实现序列化的方法 —— //

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(path);
        dest.writeString(Date);
        dest.writeFloat(size);
        dest.writeString(time);
        dest.writeInt(isLok ? 1 : 0);
    }

    public VideoFile(Parcel parcel) {
        name = parcel.readString();
        path = parcel.readString();
        Date = parcel.readString();
        size = parcel.readFloat();
        time = parcel.readString();
        isLok = parcel.readInt() == 0 ? false : true;
    }

    public static final Creator<VideoFile> CREATOR = new Creator<VideoFile>() {
        @Override
        public VideoFile createFromParcel(Parcel source) {
            return new VideoFile(source);
        }

        @Override
        public VideoFile[] newArray(int size) {
            return new VideoFile[size];
        }
    };
}
