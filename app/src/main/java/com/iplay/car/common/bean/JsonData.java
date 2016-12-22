package com.iplay.car.common.bean;

/**
 * Created by Administrator on 2016/8/20.
 */
public class JsonData {
    private int flag;//开关标识，1为开、2为关
    private String token;// 用户的令牌
    private String catalogName; //目录路径名
    private String hotName;// 热点名称
    private String hotPwd;// 热点密码
    private int versionCode;// 版本号
    private String fileName;// 文件名
    private String format;// 文件类型
    private String fileSize;// 文件大小
    private String fileCode;// 文件号

    public String getHotName() {
        return hotName;
    }

    public void setHotName(String hotName) {
        this.hotName = hotName;
    }

    public String getHotPwd() {
        return hotPwd;
    }

    public void setHotPwd(String hotPwd) {
        this.hotPwd = hotPwd;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileCode() {
        return fileCode;
    }

    public void setFileCode(String fileCode) {
        this.fileCode = fileCode;
    }
}
