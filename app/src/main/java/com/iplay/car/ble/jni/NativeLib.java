package com.iplay.car.ble.jni;

/**
 * Description :
 * Created by iplay on 2016/12/21.
 * E-mail : iplaycloud@gmail.com
 */
public class NativeLib {

    static {
        System.loadLibrary("native-lib");
    }

    public native int openUart(int fd, int flags);
    public native byte[] readUart(int fd, int count);
    public native int writeUart(int fd, byte[] buf, int count);
    public native void closeUart(int fd);
}