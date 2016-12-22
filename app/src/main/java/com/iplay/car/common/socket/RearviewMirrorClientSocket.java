package com.iplay.car.common.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Handler;
import android.os.Message;

import com.iplay.car.common.utils.CommandData;

/**
 * Created by Administrator on 2016/8/18.
 * 连接服务器的工具类
 */
public class RearviewMirrorClientSocket {
    private Socket clientS;
    public boolean isReceive = false;
    private CommandData commandData;
    private Handler myHandler;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean ifReadStar = false; // 是否读到开始标记
    private int begin = 0; // 开始标记号
    private int zbSize = 0; // 待粘包数据大小

    private String IP;// 服务器的IP地址
    private int ports;// 连接服务器的端口

    private void close() {
        try {
            if (null != inputStream) {
                inputStream.close();
                inputStream = null;
            }
            if (null != outputStream) {
                outputStream.close();
                outputStream = null;
            }
            if (null != clientS) {
                clientS.close();
                clientS = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        close();
    }

    // 连接到Socket服务端
    public boolean connected(String SERVER_IP_ADDRESS, int port, Handler myHandler, CommandData commandData) {
        this.myHandler = myHandler;
        this.commandData = commandData;
        this.IP = SERVER_IP_ADDRESS;
        this.ports = port;

        new Thread() {
            @Override
            public void run() {

                try {
                    clientS = new Socket(IP, ports);
                    //启动数据接收线程，启动数据发送线程
                    inputStream = clientS.getInputStream();
                    outputStream = clientS.getOutputStream();
                    isReceive = true;
                    ReceiveData rd = new ReceiveData();
                    rd.start();
                    ReadData read = new ReadData();
                    read.start();
                    SendData sd = new SendData();
                    sd.start();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        return isReceive;
    }

    // 接收数据的线程
    private class ReceiveData extends Thread {

        @Override
        public void run() {
            try {
                while (isReceive) {
                    byte[] m_out_bytes = new byte[8192];
                    int read = inputStream.read(m_out_bytes);
                    if (read > 0) {
                        byte[] out = new byte[read];
                        System.arraycopy(m_out_bytes, 0, out, 0, read);
                        commandData.msg_client.offer(out);// 把数据直接存入链表
                    } else {
                        Thread.sleep(300);
                    }
                }
            } catch (Exception e) {
                isReceive = false;
                e.printStackTrace();
            }
        }
    }

    /**
     * 取出链表中的数据，进行分包处理，
     */

    private class ReadData extends Thread {
        private int star = 0;
        private int record_i = 0;

        public void run() {
            try {
                while (isReceive) {
                    byte[] out = commandData.msg_client.poll();
                    if (null != out) {
                        int read = out.length;
                        for (int i = 0; i < read; i++) {
                            // 判断是否是开始标记或结束标记
                            if (out[i] == 35) {
                                if (star == 0) {
                                    star = 1;
                                    record_i = i;
                                } else {
                                    if (record_i == (i - 1)) {
                                        star = star + 1;
                                        record_i = i;
                                    } else {
                                        star = 1;
                                        record_i = i;
                                    }
                                }
                                if (star == 4) {
                                    if (!ifReadStar) {
                                        // 确认开始标记结束
                                        ifReadStar = true;
                                        begin = record_i - 3;
                                        if (begin < 0) {
                                            begin = 0;
                                        }
                                    } else {
                                        // 确认结束标记结束
                                        ifReadStar = false;
                                        // 保存完整协议
                                        if (zbSize == 0) {
                                            // 保存待处理的粘包
                                            byte[] byt = new byte[record_i
                                                    - begin + 1];
                                            System.arraycopy(out, begin, byt,
                                                    0, record_i - begin + 1);
                                            commandData.read_client.offer(byt);
                                        } else {
                                            // 粘包
                                            byte[] zb_byte = new byte[zbSize
                                                    + i + 1];

                                            boolean boo = true;
                                            int topClass = 0;
                                            while (boo) {
                                                byte[] b_peek = commandData.ing_client
                                                        .poll();
                                                if (null != b_peek) {

                                                    System.arraycopy(b_peek, 0,
                                                            zb_byte, topClass,
                                                            b_peek.length);
                                                    topClass = topClass
                                                            + b_peek.length;
                                                } else {
                                                    boo = false;
                                                }
                                            }
                                            System.arraycopy(out, 0, zb_byte,
                                                    topClass, i + 1);

                                            commandData.read_client.offer(zb_byte);

                                            zbSize = 0;
                                        }
                                        // 拼好一次协议就发个消息个handler
                                        if (!commandData.isClientHasData()) {
                                            commandData.setClientHasData(true);
                                            myHandler.sendEmptyMessage(1101);
                                        }
                                    }
                                    star = 0;
                                    record_i = 0;
                                }
                            } else {
                                star = 0;
                                record_i = 0;
                            }
                            if (i + 1 == read) {
                                // 读取到最后一个数据,需要判断是否需要 粘包
                                if (ifReadStar) {
                                    record_i = -1;
                                    byte[] byt = new byte[i - begin + 1];
                                    System.arraycopy(out, begin, byt, 0, i
                                            - begin + 1);
                                    commandData.ing_client.offer(byt);
                                    zbSize = zbSize + i - begin + 1;
                                } else {
                                    if (star != 0) {
                                        record_i = -1;
                                        byte[] byt = new byte[star];
                                        for (int k = 0; k < star; k++) {
                                            byt[k] = 35;
                                        }
                                        zbSize = zbSize + star;
                                        commandData.ing_client.offer(byt);
                                    }
                                }
                                begin = 0;
                            }
                        }
                    } else {
                        Thread.sleep(300);
                    }
                }
            } catch (Exception e) {
                isReceive = false;
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送数据的线程
     */
    private class SendData extends Thread {
        @Override
        public void run() {
            try {
                while (isReceive) {
                    byte[] byt = commandData.out_client.poll();// 回复的数据
                    if (null == byt) {
                        byt = commandData.out_preview_client.poll();// 预览的数据
                    }
                    if (null != byt) {

                        Message msg = myHandler.obtainMessage();
                        msg.obj = byt;
                        msg.what = 999;
                        myHandler.sendMessage(msg);
                        outputStream.write(byt);
                    } else {
                        Thread.sleep(300);
                    }
                }
            } catch (Exception e) {
                myHandler.sendEmptyMessage(1002);
                isReceive = false;
                e.printStackTrace();
            }
        }
    }
}