package com.iplay.car.common.utils;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Administrator on 2016/8/18.
 * 数据类
 */
public class CommandData {
    public LinkedBlockingQueue<byte[]> msg_client = new LinkedBlockingQueue<byte[]>();// 存放接收到的数据
    public LinkedBlockingQueue<byte[]> ing_client = new LinkedBlockingQueue<byte[]>(); //存储接收到的数据--还未进行粘包处理的数据
    public LinkedBlockingQueue<byte[]> read_client = new LinkedBlockingQueue<byte[]>(); // 存储已分好的数据包
    public LinkedBlockingQueue<byte[]> out_client = new LinkedBlockingQueue<byte[]>();// 存储需要发送的数据
    public LinkedBlockingQueue<byte[]> out_preview_client = new LinkedBlockingQueue<byte[]>();// 存储需要发送的预览数据数据

    private boolean isClientHasData = false; // 是否有数据传过来

    public boolean isClientHasData() {
        return isClientHasData;
    }

    public void setClientHasData(boolean isClientHasData) {
        this.isClientHasData = isClientHasData;
    }

    // 直连与设备交互的数据
    public LinkedBlockingQueue<byte[]> msg_server = new LinkedBlockingQueue<byte[]>();// 存放接收到的数据
    public LinkedBlockingQueue<byte[]> ing_server = new LinkedBlockingQueue<byte[]>(); //存储接收到的数据--还未进行粘包处理的数据
    public LinkedBlockingQueue<byte[]> read_server = new LinkedBlockingQueue<byte[]>(); // 存储已分好的数据包
    public LinkedBlockingQueue<byte[]> out_server = new LinkedBlockingQueue<byte[]>();// 存储需要发送的数据
    public LinkedBlockingQueue<byte[]> out_preview_server = new LinkedBlockingQueue<byte[]>();// 存储需要发送的预览数据数据

    private boolean isServerHasData = false;// 直连时，设备是否有数据传送过来

    public boolean isServerHasData() {
        return isServerHasData;
    }

    public void setServerHasData(boolean isServerHasData) {
        this.isServerHasData = isServerHasData;
    }
}