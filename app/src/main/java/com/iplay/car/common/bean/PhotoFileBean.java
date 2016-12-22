package com.iplay.car.common.bean;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/7/27.
 * 图片文件数据类
 */
public class PhotoFileBean {
    private String name;// 父文件夹名
    private ArrayList<String> list;// 文件夹下图片的路径集合

    public ArrayList<String> getList() {
        return list;
    }

    public void setList(ArrayList<String> list) {
        this.list = list;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
