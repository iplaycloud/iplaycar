package com.iplay.car.common.utils;

/**
 * Created by Administrator on 2016/7/25.
 */
public class WifiInfoList {
    private String ssid;
    private String capabilities;
    private int level;

    public String getSsid() {

        return ssid;
    }

    public void setSsid(String ssid) {

        this.ssid = ssid;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    private String bssid;

    public WifiInfoList(String bssid,String ssid,String capabilities,int level){
        this.bssid=bssid;
        this.capabilities=capabilities;
        this.level=level;
        this.ssid=ssid;

    }


}
