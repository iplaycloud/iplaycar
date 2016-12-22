package com.iplay.car.common.bean;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/7/29.
 * <p/>
 * 视频文件----文件夹类
 */
public class VideoFileBean {
    private String name;
    private ArrayList<VideoFile> list;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<VideoFile> getList() {
        return list;
    }

    public void setList(ArrayList<VideoFile> list) {
        this.list = list;
    }

}
