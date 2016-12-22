package com.iplay.car.common.bean;

import java.util.List;

/**
 * Created by Administrator on 2016/7/27.
 *  文件数据类
 */
public class FileBean {
    private String name;// 父文件夹名
    private List<String> list;// 文件夹下文件的路径集合

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
