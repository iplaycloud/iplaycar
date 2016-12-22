//
// Created by Administrator on 2016/6/8.
//

#include <jni.h>
#include <android/log.h>

#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <math.h>

#include <termios.h>
#include <linux/input.h>

#define UART_DEVICE     "/dev/ttyMT0" //uart设备文件名

#define  LOG_TAG    "GameMapApp"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

static const char *classPathName = "com/xctx/rearviewMirror/ble/jni/NativeLib";

int uart_init(const char *path, int flags)
{
    int fd;

    static struct termios  newtio;

    /*--打开uart设备文件--*/
    /*没有设置O_NONBLOCK，所以这里read和write是阻塞操作*/
    //fd = open(UART_DEVICE, O_RDWR|O_NOCTTY);
    fd = open(path, flags);
    if (fd < 0)
    {
        perror(UART_DEVICE);
        return fd;
    }
    else
    {
        printf("open %s successfully\n", UART_DEVICE);
    }

    /*--设置操作参数--*/
    /*获取当前操作模式参数*/
    memset(&newtio, 0, sizeof(newtio));

    /*波特率=115200 数据位=8 使能数据接收*/
    newtio.c_cflag = B115200 | CS8 | CLOCAL | CREAD;
    newtio.c_iflag = IGNPAR;
    /*newtio.c_oflag = OPOST | OLCUC; */
    /* 设定为正规模式 */
    /*newtio.c_lflag = ICANON;*/

    /*清空输入缓冲区和输出缓冲区*/
    tcflush(fd, TCIFLUSH);
    /*设置新的操作参数*/
    tcsetattr(fd, TCSANOW, &newtio);

    return fd;
}

static jint open_uart(JNIEnv *env, jobject thiz, jstring path, jint flags) {

    jint fd = 0;

    const char *str = env->GetStringUTFChars(path, 0);

    printf("%s", str);

    fd = uart_init(str, flags);

    return fd;
}

static jbyteArray read_uart(JNIEnv *env, jobject thiz, jint fd, jint count)
{
    int ret = 0;

    char buf[1024];

    if(count > 1024)
        count = 1024;

    ret = read(fd, buf, count);

    /*分配Java层的byte数组*/
    jbyteArray array = env->NewByteArray(ret);

    /*把buf数据拷贝到array中*/
    env->SetByteArrayRegion(array, 0, ret, (const jbyte *) buf);

    return array;
}

/*
 * 把Java的byte[] buf转换成char*
 * */
static jint write_uart(JNIEnv *env, jobject thiz, jint fd, jbyteArray byarray, jint count)
{
    int ret = 0;

    jsize len  = env->GetArrayLength(byarray);

    jbyte *jbarray = (jbyte *)malloc(len * sizeof(jbyte));

    env->GetByteArrayRegion(byarray,0,len,jbarray);

    char *dDate=(char*)jbarray;

    ret = write(fd, dDate, len);

    return ret;
}

static jint close_uart(JNIEnv *env, jobject thiz, jint fd) {

    close(fd);

}

/**
 *第一个变量name是Java中函数的名字。
 *第二个变量signature，用字符串是描述了函数的参数和返回值
 *第三个变量fnPtr是函数指针，指向C函数。
 */
static JNINativeMethod methods[] = {{"openUart",  "()I", (void *) open_uart},
                                    {"readUart",  "(II)[B", (void *) read_uart},
                                    {"writeUart",  "(I[BI)I", (void *) write_uart},
                                    {"closeUart",  "(I)", (void *) close_uart},
};

/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv *env, const char *className,
                                 JNINativeMethod *gMethods, int numMethods) {
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL) {
        //ALOGE("Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        //ALOGE("RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * Register native methods for all classes we know about.
 *
 * returns JNI_TRUE on success.
 */
static int registerNatives(JNIEnv *env) {
    if (!registerNativeMethods(env, classPathName, methods,
                               sizeof(methods) / sizeof(methods[0]))) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * This is called by the VM when the shared library is first loaded.
 */
typedef union {
    JNIEnv *env;
    void *venv;
} UnionJNIEnvToVoid;

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    UnionJNIEnvToVoid uenv;
    uenv.venv = NULL;
    jint result = -1;
    JNIEnv *env = NULL;

    // ALOGI("JNI_OnLoad");

    if (vm->GetEnv(&uenv.venv, JNI_VERSION_1_4) != JNI_OK) {
        //   ALOGE("ERROR: GetEnv failed");
        goto bail;
    }
    env = uenv.env;

    if (registerNatives(env) != JNI_TRUE) {
        // ALOGE("ERROR: registerNatives failed");
        goto bail;
    }

    result = JNI_VERSION_1_4;

    bail:
    return result;
}
