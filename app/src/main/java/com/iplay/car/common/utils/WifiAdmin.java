package com.iplay.car.common.utils;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.Inet4Address;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2016/7/25.
 */
public class WifiAdmin {
    //定义一个wifiManager对象
    private WifiManager mWifiManager;
    //定义一个WifiInfo对象
    private WifiInfo mWifiInfo;
    //扫描出的网络连接列表
    private List<ScanResult> mWifiList;
    //网络连接列表
    private List<WifiConfiguration> mWifiConfigurations;
    WifiManager.WifiLock mWifiLock;

    public WifiAdmin(Context context) {
        //取得wifiManager对象
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //取得wifiInfo对象
        mWifiInfo = mWifiManager.getConnectionInfo();
    }

    /**
     * 关闭wifi
     */
    public boolean closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            return mWifiManager.setWifiEnabled(false);
        }
        return false;
    }

    /**
     * 检查wifi状态
     */
    public int checkState() {
        return mWifiManager.getWifiState();
    }

    //锁定wifiLock
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    //解锁wifilock
    public void releaseWifiLock() {
        //判断是否锁定
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    //创建一个wifiLock
    public void createWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("test");
    }

    //得到配置好的网络
    public List<WifiConfiguration> getConfiguration() {
        return mWifiConfigurations;
    }

    //指定配置好的网络进行连接
    public void connetionConfiguration(int index) {
        if (index > mWifiConfigurations.size()) {
            return;
        }
        //连接配置好指定ID的网络
        mWifiManager.enableNetwork(mWifiConfigurations.get(index).networkId, true);
    }

    public void startScan() {
        mWifiManager.startScan();
        //得到扫描结果
        mWifiList = mWifiManager.getScanResults();
        //得到配置好的网络连接
        mWifiConfigurations = mWifiManager.getConfiguredNetworks();
    }

    //得到网络列表
    public List<ScanResult> getWifiList() {
        return mWifiList;
    }

    //查看扫描结果
    public StringBuffer lookUpScan() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mWifiList.size(); i++) {
            sb.append("Index_" + new Integer(i + 1).toString() + ":");
            //将ScanResult信息转换成一个字符串包
            //其中把包括：BSSID,SSID,capabilities，freauency，level
            sb.append((mWifiList.get(i)).toString()).append("\n");
        }
        return sb;
    }

    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    public String getBSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    public int getIpAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    public int getNetWordId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    /**
     * 得到wifiInfo的所有信息
     */
    public String getWifiInfo() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }

    //添加一个网络并连接
    public void addNetWork(WifiConfiguration configuration) {
        int wcgId = mWifiManager.addNetwork(configuration);
        mWifiManager.enableNetwork(wcgId, true);
    }

    //断开指定ID的网络
    public void disConnectionWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    //打开wifi功能
    public boolean openWifi() {
        boolean bRet = true;
        if (!mWifiManager.isWifiEnabled()) {
            bRet = mWifiManager.setWifiEnabled(true);
        }
        return bRet;
    }

    /**
     * 提供一个外部接口，传入要连接的无线网络
     *///WifiCipherType
    public boolean connect(String SSID, String Password, WifiConnect.WifiCipherType Type) {
        if (!this.openWifi()) {
            return false;
        }
        // 状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
        while (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            try {
                //为了避免程序一直while循环，让它睡个100毫秒在检查.....
                Thread.currentThread();
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        }

        System.out.println("WifiAdmin#connect==连接结束");

        WifiConfiguration wifiConfig = createWifiInfo(SSID, Password, Type);
        //
        if (wifiConfig == null) {
            return false;
        }

        WifiConfiguration tempConfig = this.isExsits(SSID);

        int tempId = wifiConfig.networkId;
        if (tempConfig != null) {
            tempId = tempConfig.networkId;
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        int netID = mWifiManager.addNetwork(wifiConfig);

        // 断开连接
        mWifiManager.disconnect();
        // 重新连接
        //设置为true，使其他的连接断开
        boolean bRet = mWifiManager.enableNetwork(netID, true);
        mWifiManager.reconnect();
        return bRet;
    }

    //查看以前是否也配置过这个网络WifiConfiguration
    public WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager
                .getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;//existingConfig
            }
        }
        return null;//null
    }

    private WifiConfiguration createWifiInfo(String SSID, String Password,
                                             WifiConnect.WifiCipherType Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        if (Type == WifiConnect.WifiCipherType.WIFICIPHER_NOPASS) {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == WifiConnect.WifiCipherType.WIFICIPHER_WEP) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == WifiConnect.WifiCipherType.WIFICIPHER_WPA) {

            // config.preSharedKey = "\"" + Password + "\"";
            // config.hiddenSSID = true;
            // config.allowedAuthAlgorithms
            // .set(WifiConfiguration.AuthAlgorithm.OPEN);
            // config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            // config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            // config.allowedPairwiseCiphers
            // .set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            // config.status = WifiConfiguration.Status.ENABLED;

            // 修改之后配置
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);

        } else {
            return null;
        }
        return config;
    }

    /**
     * 判断扫描结果是否连接上
     */
    public boolean isConnect(ScanResult result) {
        if (result == null) {
            return false;
        }

        mWifiInfo = mWifiManager.getConnectionInfo();
        String g2 = "\"" + result.SSID + "\"";
        if (mWifiInfo.getSSID() != null && mWifiInfo.getSSID().endsWith(g2)) {
            return true;
        }
        return false;
    }

    /**
     * 将int类型的IP转换成字符串形式的IP
     */
    public String ipIntToString(int ip) {
        try {
            byte[] bytes = new byte[4];
            bytes[0] = (byte) (0xff & ip);
            bytes[1] = (byte) ((0xff00 & ip) >> 8);
            bytes[2] = (byte) ((0xff0000 & ip) >> 16);
            bytes[3] = (byte) ((0xff000000 & ip) >> 24);
            return Inet4Address.getByAddress(bytes).getHostAddress();
        } catch (Exception e) {
            return "";
        }
    }

    public int getConnNetId() {
        // result.SSID;
        mWifiInfo = mWifiManager.getConnectionInfo();
        return mWifiInfo.getNetworkId();
    }

    public boolean ConnectToNetID(int netID) {
        return mWifiManager.enableNetwork(netID, true);
    }

    private int getMaxPriority() {
        List<WifiConfiguration> localList = this.mWifiManager
                .getConfiguredNetworks();
        int i = 0;
        Iterator<WifiConfiguration> localIterator = localList.iterator();
        while (true) {
            if (!localIterator.hasNext())
                return i;
            WifiConfiguration localWifiConfiguration = (WifiConfiguration) localIterator
                    .next();
            if (localWifiConfiguration.priority <= i)
                continue;
            i = localWifiConfiguration.priority;
        }
    }

    public WifiConfiguration setMaxPriority(WifiConfiguration config) {
        int priority = getMaxPriority() + 1;
        if (priority > 99999) {
            priority = shiftPriorityAndSave();
        }

        config.priority = priority; // 2147483647;
        System.out.println("priority=" + priority);

        mWifiManager.updateNetwork(config);

        // 本机之前配置过此wifi热点，直接返回
        return config;
    }

    private int shiftPriorityAndSave() {
        List<WifiConfiguration> localList = this.mWifiManager
                .getConfiguredNetworks();
        sortByPriority(localList);
        int i = localList.size();
        for (int j = 0; ; ++j) {
            if (j >= i) {
                this.mWifiManager.saveConfiguration();
                return i;
            }
            WifiConfiguration localWifiConfiguration = (WifiConfiguration) localList
                    .get(j);
            localWifiConfiguration.priority = j;
            this.mWifiManager.updateNetwork(localWifiConfiguration);
        }
    }

    private void sortByPriority(List<WifiConfiguration> paramList) {
        Collections.sort(paramList, new SjrsWifiManagerCompare());
    }

    class SjrsWifiManagerCompare implements Comparator<WifiConfiguration> {
        public int compare(WifiConfiguration paramWifiConfiguration1,
                           WifiConfiguration paramWifiConfiguration2) {
            return paramWifiConfiguration1.priority
                    - paramWifiConfiguration2.priority;
        }
    }

    /**
     * 信号强度转换为字符串
     */
    public static String singlLevToStr(int level) {

        String resuString = "无信号";

        if (Math.abs(level) > 100) {
        } else if (Math.abs(level) > 80) {
            resuString = "弱";
        } else if (Math.abs(level) > 70) {
            resuString = "强";
        } else if (Math.abs(level) > 60) {
            resuString = "强";
        } else if (Math.abs(level) > 50) {
            resuString = "较强";
        } else {
            resuString = "极强";
        }
        return resuString;
    }

    /**
     * 添加到网络
     */
    public boolean addNetwork(WifiConfiguration wcg) {
        if (wcg == null) {
            return false;
        }
        //receiverDhcp = new ReceiverDhcp(ctx, mWifiManager, this, wlanHandler);
        //ctx.registerReceiver(receiverDhcp, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        int wcgID = mWifiManager.addNetwork(wcg);
        boolean b = mWifiManager.enableNetwork(wcgID, true);
        mWifiManager.saveConfiguration();
        System.out.println(b);
        return b;
    }

    public boolean connectSpecificAP(ScanResult scan) {
        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        boolean networkInSupplicant = false;
        boolean connectResult = false;
        // 重新连接指定AP
        mWifiManager.disconnect();
        for (WifiConfiguration w : list) {
            // 将指定AP名字转换
            // String str = convertToQuotedString(info.ssid);
            if (w.BSSID != null && w.BSSID.equals(scan.BSSID)) {
                connectResult = mWifiManager.enableNetwork(w.networkId, true);
                // mWifiManager.saveConfiguration();
                networkInSupplicant = true;
                break;
            }
        }
        if (!networkInSupplicant) {
            WifiConfiguration config = CreateWifiInfo(scan, "");
            connectResult = addNetwork(config);
        }

        return connectResult;
    }

    //然后是一个实际应用方法，只验证过没有密码的情况
    public WifiConfiguration CreateWifiInfo(ScanResult scan, String Password) {
        // Password="ultrapower2013";
        // deleteExsits(info.ssid);
        WifiConfiguration config = new WifiConfiguration();
        config.hiddenSSID = false;
        config.status = WifiConfiguration.Status.ENABLED;

        if (scan.capabilities.contains("WEP")) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);

            config.SSID = "\"" + scan.SSID + "\"";

            config.wepTxKeyIndex = 0;
            config.wepKeys[0] = Password;
            // config.preSharedKey = "\"" + SHARED_KEY + "\"";
        } else if (scan.capabilities.contains("PSK")) {
            //
            config.SSID = "\"" + scan.SSID + "\"";
            config.preSharedKey = "\"" + Password + "\"";
        } else if (scan.capabilities.contains("EAP")) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.SSID = "\"" + scan.SSID + "\"";
            config.preSharedKey = "\"" + Password + "\"";
        } else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

            config.SSID = "\"" + scan.SSID + "\"";
            // config.BSSID = info.mac;
            config.preSharedKey = null;
            //
        }

        return config;
    }

    //添加指定WIFI的配置信息,原列表不存在此SSID
    public int AddWifiConfig(List<ScanResult> wifiList, String ssid, String pwd) {
        int wifiId = -1;
        for (int i = 0; i < wifiList.size(); i++) {
            ScanResult wifi = wifiList.get(i);
            if (wifi.SSID.equals(ssid)) {
                Log.i("AddWifiConfig", "equals");
                WifiConfiguration wifiCong = new WifiConfiguration();
                wifiCong.SSID = "\"" + wifi.SSID + "\"";//\"转义字符，代表"
                wifiCong.preSharedKey = "\"" + pwd + "\"";//WPA-PSK密码
                wifiCong.hiddenSSID = false;
                wifiCong.status = WifiConfiguration.Status.ENABLED;
                wifiId = mWifiManager.addNetwork(wifiCong);//将配置好的特定WIFI密码信息添加,添加完成后默认是不激活状态，成功返回ID，否则为-1
                if (wifiId != -1) {
                    return wifiId;
                }
            }
        }
        return wifiId;
    }

    //连接指定Id的WIFI
    public boolean ConnectWifi(int wifiId) {
        for (int i = 0; i < mWifiConfigurations.size(); i++) {
            WifiConfiguration wifi = mWifiConfigurations.get(i);
            if (wifi.networkId == wifiId) {
                while (!(mWifiManager.enableNetwork(wifiId, true))) {//激活该Id，建立连接
                    //status:0--已经连接，1--不可连接，2--可以连接
                    Log.i("ConnectWifi","-----------7"+ String.valueOf(mWifiConfigurations.get(wifiId).status));
                }
                return true;
            }
        }
        return false;
    }
//得到Wifi配置好的信息
    public void getConfiguration1(){
           mWifiConfigurations = mWifiManager.getConfiguredNetworks();//得到配置好的网络信息
            for(int i =0;i<mWifiConfigurations.size();i++){
                  Log.i("getConfiguration",mWifiConfigurations.get(i).SSID);
                  Log.i("getConfiguration","--------8"+String.valueOf(mWifiConfigurations.get(i).networkId));
                }
        }


}
