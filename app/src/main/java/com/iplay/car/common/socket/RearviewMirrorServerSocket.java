package com.iplay.car.common.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.iplay.car.common.protocol.ProtocolAgreementByte;
import com.iplay.car.common.utils.CommandData;
import com.iplay.car.common.utils.DataUtils;

/**
 * 直连的工具类
 */
public class RearviewMirrorServerSocket {
    private static final String TAG = "RearviewMirrorServerSocket";
    private boolean isStart = false;// 是否启动工具类
    private ServerSocket mServerSocket;
    private Socket socket;
    private RecieveRemoteThread mThread;
    private InputStream inputStream;
    private OutputStream outputStream;
    public static boolean isReceive = false;
    public int state = 0;
    private Context context;
    private CommandData commandData;
    private Handler myHandler;
    private boolean ifReadStar = false; // 是否读到开始标记
    private int begin = 0; // 开始标记号
    private int zbSize = 0; // 待粘包数据大小

    public void init(Context context, CommandData commandData,
                     Handler myHandler) {
        try {
            this.context = context;
            this.commandData = commandData;
            this.myHandler = myHandler;
            // 建立一个ServerSocket,用于监听客户端Socket的连接请求
            mServerSocket = new ServerSocket(6316);
        } catch (IOException e) {
            e.printStackTrace();
        }
        isStart = true;
        mThread = new RecieveRemoteThread();
        mThread.start();
    }

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
            if (null != socket) {
                socket.close();
                socket = null;
            }
//            if (null != mServerSocket) {
//                mServerSocket.close();
//                mServerSocket = null;
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {

        isStart = false;
        close();

        try {
            if (null != mServerSocket) {
                mServerSocket.close();
                mServerSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 子线程，循环从mServerSocket中取出socket，并socket中取出数据，封装好后发送给mHandler
     */
    private class RecieveRemoteThread extends Thread {
        @Override
        public void run() {
            try {
                while (isStart) {
                    // 每当接收到客户端socket的请求，服务器端也对应产生一个socket
                    Socket client = mServerSocket.accept();
                    // 清空链表数据
                    commandData.msg_server.clear();
                    commandData.ing_server.clear();
                    commandData.read_server.clear();
                    commandData.out_server.clear();
                    commandData.out_preview_server.clear();
                    close();
                    x = 0;
                    l = 0l;
                    socket = client;
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();
                    state = 1;
                    // 启动接收数据线程
                    isReceive = true;
                    ReceiveData rd = new ReceiveData();
                    rd.init();
                    rd.start();

                    // 启动粘包线程
                    ReadData readData = new ReadData();
                    readData.start();

                    // 启动发送数据线程
                    SendData sd = new SendData();
                    sd.init();
                    sd.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 统一回复,1 处理成功,2处理失败
    private boolean uniformReply(int result) {
        boolean boo = false;
        try {
            ProtocolAgreementByte pab = new ProtocolAgreementByte();
            pab.setOrderName(DataUtils.returnResult);
            JSONObject json = new JSONObject();
            json.put("result", result);
            pab.setContentStr(json.toString());
            int flag = pab.assemblyData();
            if (flag == 1) {
                boo = commandData.out_server.offer(pab.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return boo;
    }

    // 接收数据的线程,保证接收数据的及时性
    private class ReceiveData extends Thread {
        public void init() {
        }

        @Override
        public void run() {
            try {
                while (isReceive) {
                    byte[] m_out_bytes = new byte[8192];
                    //从输入流读取一个数据块
                    int read = inputStream.read(m_out_bytes);
                    if (read > 0) {
                        byte[] out = new byte[read];
                        System.arraycopy(m_out_bytes, 0, out, 0, read);
                        commandData.msg_server.offer(out);// 把数据直接存入链表(先进先出),保证接收数据的及时性
                    } else {
                        Thread.sleep(200);
                    }
                }
            } catch (Exception e) {
                isReceive = false;
                state = 0;
                e.printStackTrace();
            }
        }
    }

    /**
     * 取出链表中的数据，进行分包粘包处理，
     */
    private class ReadData extends Thread {
        private int star = 0; //连续读到开始或结束标记符 35(#)的个数
        private int record_i = 0; //记录读取到的 开始或结束标记符 35(#)的位置

        public void run() {
            try {
                while (isReceive) {
                    //从链表中取出一个数据块
                    byte[] out = commandData.msg_server.poll();
                    if (null != out) {
                        int read = out.length;
                        //从数据块中循环取出字节
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
                                    // 确认开始标记 或结束标记(4个#号) 结束
                                    if (!ifReadStar) {
                                        //确认开始标记结束
                                        ifReadStar = true;
                                        //记录协议开始位置
                                        begin = record_i - 3;
                                        if (begin < 0) {
                                            begin = 0;
                                        }
                                    } else {
                                        // 确认结束标记结束
                                        ifReadStar = false;
                                        // 保存一个完整协议
                                        if (zbSize == 0) {
                                            //需要粘包的数据大小为0,说明在本数据块内协议完整
                                            // 保存待处理的协议,可供下一步处理
                                            byte[] byt = new byte[record_i
                                                    - begin + 1];
                                            System.arraycopy(out, begin, byt,
                                                    0, record_i - begin + 1);
                                            commandData.read_server.offer(byt);
                                        } else {
                                            //有需要粘包的数据,本数据块的协议数据沾上暂存的数据组成一个完整的数据包
                                            // 粘包
                                            byte[] zb_byte = new byte[zbSize
                                                    + i + 1];

                                            boolean boo = true;
                                            int topClass = 0;
                                            while (boo) {
                                                //从待处理粘包链表取出数据
                                                byte[] b_peek = commandData.ing_server
                                                        .poll();
                                                if (null != b_peek) {

                                                    System.arraycopy(b_peek, 0,
                                                            zb_byte, topClass,
                                                            b_peek.length);
                                                    topClass = topClass
                                                            + b_peek.length;
                                                } else {
                                                    //待处理粘包链表 没有需要粘包的数据了
                                                    boo = false;
                                                }
                                            }
                                            System.arraycopy(out, 0, zb_byte,
                                                    topClass, i + 1);

                                            commandData.read_server.offer(zb_byte);

                                            zbSize = 0;
                                        }
                                        // 拼好一次协议就发个消息个handler,有数据需要处理
                                        if (!commandData.isServerHasData()) {
                                            commandData.setServerHasData(true); //设置为有数据处理
                                            myHandler.sendEmptyMessage(1102);
                                        }
                                    }
                                    //开始或结束标记(4个#)读完,初始值 置0
                                    star = 0;
                                    record_i = 0;
                                }
                            } else {
                                //没有读到连续的 开始或结束表示,初始值 置0
                                star = 0;
                                record_i = 0;
                            }
                            if (i + 1 == read) {
                                // 读取到最后一个数据,需要判断是否需要 粘包
                                if (ifReadStar) {
                                    //开始符（4个#）读取完,协议没结束 保存到待粘包处理链表
                                    record_i = -1;
                                    byte[] byt = new byte[i - begin + 1];
                                    System.arraycopy(out, begin, byt, 0, i
                                            - begin + 1);
                                    commandData.ing_server.offer(byt);
                                    //计算需要粘包的大小
                                    zbSize = zbSize + i - begin + 1;
                                } else {
                                    //本数据块已经读完结束标记(4个#)
                                    if (star != 0) {
                                        //上个协议读取完整,但是多读入了下个协议的开始标记star为多读入的标记个数,把读到的开始标记(#)暂存到待粘包数据链表
                                        record_i = -1;
                                        byte[] byt = new byte[star];
                                        for (int k = 0; k < star; k++) {
                                            byt[k] = 35;
                                        }
                                        zbSize = zbSize + star;
                                        commandData.ing_server.offer(byt);
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

    private long l = 0l;
    private int x = 0;

    // 发送数据的线程
    private class SendData extends Thread {
        public void init() {
        }

        @Override
        public void run() {
            try {
                while (isReceive) {
                    byte[] byt = commandData.out_server.poll();// 获取并移除此队列的头。
                    if (null == byt) {
                        byt = commandData.out_preview_server.poll();// 预览的数据
                    }
                    if (null != byt) {
                        ProtocolAgreementByte pab = new ProtocolAgreementByte();
                        int t = pab.analyseData(byt);
                        Message msg = myHandler.obtainMessage();
                        msg.obj = byt;
                        msg.what = 999;
//                        myHandler.sendMessage(msg);

                        if (pab.getOrderName().equals(DataUtils.readySendFile) || pab.getOrderName().equals(DataUtils.sendingFile) || pab.getOrderName().equals(DataUtils.fileSendOK)) {
                            l += byt.length;
                            ++x;
                        }
                        Log.d("000000", pab.getOrderName() + "-解析数据情况--" + t + "--总长度--" + l + "--单次长度--" + byt.length + "队列长度--" + commandData.out_server.size() + "数量--" + x);
                        if (null != byt) {
                            outputStream.write(byt);
                            outputStream.flush();
                            Thread.sleep(10);
                        }
                    }
//                    else {
//                        Thread.sleep(500);
//                    }
                }
            } catch (Exception e) {
                state = 0;
                isReceive = false;
                e.printStackTrace();
            }
        }
    }
}