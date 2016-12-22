package com.iplay.car.navigation.app;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.LocationMode;
import com.baidu.trace.Trace;
import com.iplay.car.common.utils.CommandData;

/**
 * Created by Administrator on 2016/10/13.
 */
public class  AppData extends Application {
    private Context mContext;
    /**
     * 轨迹服务
     */
    private Trace trace = null;
    /**
     * 轨迹服务客户端
     */
    private LBSTraceClient client = null;

    /**
     * 鹰眼服务ID，开发者创建的鹰眼服务对应的服务ID
     */
    private int serviceId = 126798;

    /**
     * entity标识
     */
    private String entityName = "myTraceTest";

    /**
     * 轨迹服务类型（0 : 不建立socket长连接， 1 : 建立socket长连接但不上传位置数据，2 : 建立socket长连接并上传位置数据）
     */
    private int traceType = 2;
    private CommandData commandData;// 数据类

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        mContext = getApplicationContext();
    }

    public void initLBSTrace(){
        // 初始化轨迹服务客户端
        client = new LBSTraceClient(mContext);
        Log.d("MainService", "运行-----client="+client);
        // 初始化轨迹服务
        setTrace(new Trace(mContext, serviceId, entityName, traceType));
        Log.d("MainService", "运行-----mContext="+mContext);
        Log.d("MainService", "运行-----serviceId="+serviceId);
        Log.d("MainService", "运行-----entityName="+entityName);
        Log.d("MainService", "运行-----traceType="+traceType);
        // 设置定位模式
        client.setLocationMode(LocationMode.High_Accuracy);
    }

    public LBSTraceClient getClient() {
        return client;
    }

    public void setClient(LBSTraceClient client) {
        this.client = client;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public int getTraceType() {
        return traceType;
    }

    public void setTraceType(int traceType) {
        this.traceType = traceType;
    }

    public Trace getTrace() {
        return trace;
    }

    public void setTrace(Trace trace) {
        this.trace = trace;
    }

    public CommandData getCommandData() {
        return commandData;
    }

    public void setCommandData(CommandData commandData) {
        this.commandData = commandData;
    }
}
