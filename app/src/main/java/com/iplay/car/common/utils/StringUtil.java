package com.iplay.car.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class StringUtil {
    /**
     * 比较两个byte数组数据是否相同,相同返回 true
     *
     * @param data1
     * @param data2
     * @param len
     * @return
     */
    public static boolean equalsByte(byte[] data1, byte[] data2, int len) {
        if (data1 == null && data2 == null) {
            return true;
        }
        if (data1 == null || data2 == null) {
            return false;
        }
        if (data1 == data2) {
            return true;
        }
        boolean bEquals = true;
        int i;
        for (i = 0; i < data1.length && i < data2.length && i < len; i++) {
            if (data1[i] != data2[i]) {
                bEquals = false;
                break;
            }
        }
        return bEquals;
    }

    /**
     * 压缩GZip
     *
     * @param data
     * @return
     */
    public static byte[] gZip(byte[] data) {
        byte[] b = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            gzip.write(data);
            gzip.finish();
            gzip.close();
            b = bos.toByteArray();
            bos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return b;
    }

    /**
     * 解压GZip
     *
     * @param data
     * @return
     */
    public static byte[] unGZip(byte[] data) {
        byte[] b = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            GZIPInputStream gzip = new GZIPInputStream(bis);
            byte[] buf = new byte[1024];
            int num = -1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, num);
            }
            b = baos.toByteArray();
            baos.flush();
            baos.close();
            gzip.close();
            bis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return b;
    }

    /**
     * Get XML String of utf-8
     *
     * @return XML-Formed string
     */
    public static Map<Integer, String> tokenMap = new HashMap<Integer, String>();

    public static String getUTF8XMLString(String xml) {
        // A StringBuffer Object
        StringBuffer sb = new StringBuffer();
        sb.append(xml);
        String xmString = "";
        String xmlUTF8 = "";
        try {
            xmString = new String(sb.toString().getBytes("UTF-8"));
            xmlUTF8 = URLEncoder.encode(xmString, "UTF-8");
            System.out.println("utf-8 编码：" + xmlUTF8);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // return to String Formed
        return xmlUTF8;
    }

    public static String[] splitString(String sInput, String sDelimiter) {
        if (StringUtil.checkNull(sInput)) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(sInput, sDelimiter);
        int size = st.countTokens();
        // System.out.println("size="+size);
        String output[] = new String[size];
        for (int i = 0; i < size; i++) {
            output[i] = st.nextToken();
        }
        return output;
    }

    static int flowNum = 0;

    public static String getNewFlowNum() {
        if (flowNum >= 999999) {
            flowNum = 0;
        }
        flowNum = flowNum + 1;
        String str = String.valueOf(flowNum);
        while (str.length() < 6) {
            str = "0" + str;
        }
        return "A" + str;
    }

    // prefix 为后缀
    public static String getNewflowcode(String prefix) {
        String flowcode = StringUtil.timeToString(new Date(), "yyyyMMddHHmmss")
                + StringUtil.getNewFlowNum() + prefix;
        return flowcode;
    }

    public static boolean checkNull(String sInput) {
        boolean flag = false;
        try {
            if ("".equals(sInput) || null == sInput || "null".equals(sInput)) {
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static Date StringTotime(String format) {
        try {
            if (checkNull(format)) {
                return null;
            }
            SimpleDateFormat localTime = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            Date d = localTime.parse(format);
            return d;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date StringTotime(String str, String format) {
        try {
            if (checkNull(str)) {
                return null;
            }
            SimpleDateFormat localTime = new SimpleDateFormat(format);
            Date d = localTime.parse(str);
            return d;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String timeToString(Date d, String format) {
        // System.out.println("d="+d);
        SimpleDateFormat localTime = new SimpleDateFormat(format);
        String date = localTime.format(d);
        return date;
    }

    public static String timeToString(Timestamp d, String format) {
        SimpleDateFormat localTime = new SimpleDateFormat(format);
        String date = localTime.format(d);
        return date;
    }

    /**
     * 时间戳转换为字符串
     *
     * @param time
     * @return
     */
    public static String timestamp2Str(Timestamp time, String format) {
        Date date = null;
        if (null != time) {
            date = new Date(time.getTime());
        }
        return timeToString(date, format);
    }

    // 将 UTF-8 编码的字符串转换为 GB2312 编码格式：F
    public static String utf8Togb2312(String str) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '+':
                    sb.append(' ');
                    break;
                case '%':
                    try {
                        sb.append((char) Integer.parseInt(
                                str.substring(i + 1, i + 3), 16));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException();
                    }
                    i += 2;
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        String result = sb.toString();
        String res = null;
        try {
            byte[] inputBytes = result.getBytes("8859_1");
            res = new String(inputBytes, "UTF-8");
        } catch (Exception e) {
        }
        return res;
    }

    public static String getTime(String time) {
        String date = "";
        if (!StringUtil.checkNull(time)) {
            if (time.length() > 10) {
                date = time.substring(0, 10);
            }
        }
        return date;
    }

    // 将 GB2312 编码格式的字符串转换为 UTF-8 格式的字符串：
    public static String gb2312ToUtf8(String str) {
        String urlEncode = "";
        try {
            urlEncode = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return urlEncode;
    }


    /**
     * 生成随机密码
     *
     * @param length
     * @return
     */
    public static String getRandomString(int length, String pw) {
        StringBuffer buffer = new StringBuffer(pw);
        StringBuffer sb = new StringBuffer();
        java.util.Random r = new java.util.Random();
        int range = buffer.length();
        for (int i = 0; i < length; i++) {
            sb.append(buffer.charAt(r.nextInt(range)));
        }
        return sb.toString();
    }

    /**
     * 保存文件
     *
     * @param path
     * @param file
     * @param filename
     * @return
     */
    public static int savefiles(String path, File file, String filename) {
        int result = 0;
        try {
            InputStream is = new java.io.FileInputStream(file);
            // 判断文件夹是否存在
            File f = new File(path);
            if (!f.exists() && !f.isDirectory()) {
                f.mkdirs();
            }
            java.io.OutputStream os = new java.io.FileOutputStream(path
                    + File.separator + filename);
            byte buffer[] = new byte[8192];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                os.write(buffer, 0, count);
            }
            os.close();
            is.close();
            result = 1;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * @param inStream
     * @return 字节数组
     * @throws Exception
     * @方法功能 InputStream 转为 byte
     */
    public static byte[] inputStream2Byte(InputStream inStream)
            throws Exception {
        // ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        // byte[] buffer = new byte[1024];
        // int len = -1;
        // while ((len = inStream.read(buffer)) != -1) {
        // outSteam.write(buffer, 0, len);
        // }
        // outSteam.close();
        // inStream.close();
        // return outSteam.toByteArray();
        int count = 0;
        while (count == 0) {
            count = inStream.available();
        }
        byte[] b = new byte[count];
        inStream.read(b);
        return b;
    }

    /**
     * @param b
     * @return InputStream
     * @throws Exception
     * @方法功能 byte 转为 InputStream
     */
    public static InputStream byte2InputStream(byte[] b) throws Exception {
        InputStream is = new ByteArrayInputStream(b);
        return is;
    }

    /**
     * @param number
     * @return 两位的字节数组
     * @功能 短整型与字节的转换
     */
    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }

    /**
     * @param b
     * @return 短整型
     * @功能 字节的转换与短整型
     */
    public static short byteToShort(byte[] b) {
        short s = 0;
        short s0 = (short) (b[0] & 0xff);// 最低位
        short s1 = (short) (b[1] & 0xff);
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }

    public static byte[] shortsTobytes(short[] shor, int len) {
        byte[] byt = new byte[len * 2];
        for (int i = 0; i < len; i++) {
            byte[] a = shortToByte(shor[i]);
            System.arraycopy(a, 0, byt, i * 2, 2);
        }
        return byt;
    }

    public static short[] bytesToShorts(byte[] b) {
        short[] s2 = new short[b.length / 2];
        for (int i = 0; i < b.length / 2; i++) {
            short s = 0;
            short s0 = (short) (b[i * 2] & 0xff);// 最低位
            short s1 = (short) (b[i * 2 + 1] & 0xff);
            s1 <<= 8;
            s = (short) (s0 | s1);
            s2[i] = s;
        }
        return s2;
    }

    /**
     * @param i
     * @return 四位的字节数组
     * @方法功能 整型与字节数组的转换
     */
    public static byte[] intToByte(int i) {
        byte[] bt = new byte[4];
        bt[0] = (byte) (0xff & i);
        bt[1] = (byte) ((0xff00 & i) >> 8);
        bt[2] = (byte) ((0xff0000 & i) >> 16);
        bt[3] = (byte) ((0xff000000 & i) >> 24);
        return bt;
    }

    /**
     * @param bytes
     * @return 整型
     * @方法功能 字节数组和整型的转换
     */
    public static int bytesToInt(byte[] bytes) {
        int num = bytes[0] & 0xFF;
        num |= ((bytes[1] << 8) & 0xFF00);
        num |= ((bytes[2] << 16) & 0xFF0000);
        num |= ((bytes[3] << 24) & 0xFF000000);
        return num;
    }

    /**
     * @param number
     * @return byte[]
     * @方法功能 字节数组和长整型的转换
     */
    public static byte[] longToByte(long number) {
        long temp = number;
        byte[] b = new byte[8];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Long(temp & 0xff).byteValue();
            // 将最低位保存在最低位
            temp = temp >> 8;
            // 向右移8位
        }
        return b;
    }

    /**
     * @param b
     * @return 长整型
     * @方法功能 字节数组和长整型的转换
     */
    public static long byteToLong(byte[] b) {
        long s = 0;
        long s0 = b[0] & 0xff;// 最低位
        long s1 = b[1] & 0xff;
        long s2 = b[2] & 0xff;
        long s3 = b[3] & 0xff;
        long s4 = b[4] & 0xff;// 最低位
        long s5 = b[5] & 0xff;
        long s6 = b[6] & 0xff;
        long s7 = b[7] & 0xff; // s0不变
        s1 <<= 8;
        s2 <<= 16;
        s3 <<= 24;
        s4 <<= 8 * 4;
        s5 <<= 8 * 5;
        s6 <<= 8 * 6;
        s7 <<= 8 * 7;
        s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;
        return s;
    }

    /**
     * 数值转换为时间格式（00:00）
     * @param i 时间数值
     * @return String 格式时间数据
     */
    public static String intToTime(int i) {

        if (i > 0 && i < 3600) {
            if (i > 59) {
                return format(i / 60) + ":" + format(i % 60);
            } else {
                return "00:" + format(i);
            }
        }
        return "00:00";
    }

    /**
     * 数值转换为时间格式（00）
     * 用以计时操作的相关方法
     * @param num 时间数据
     * @return String 格式时间数据
     */
    public static String format(int num) {

        String s = num + "";
        if (s.length() == 1) {
            s = "0" + s;
        }
        return s;
    }

}