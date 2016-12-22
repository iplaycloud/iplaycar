/*
 * Copyright (c) 2016. Vv <iplaycloud@gmail.com><http://www.v-sounds.com/>
 *
 * This file is part of AndroidReview (Android面试复习)
 *
 * AndroidReview is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  AndroidReview is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * along with AndroidReview.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.iplay.car.common.base.system;


import com.orhanobut.logger.Logger;
import com.iplay.car.common.base.BaseApplication;

/**
 * Author : iplay on 2015/7/20 20:51
 * Mail：iplaycloud@gmail.com
 * Description：
 */
public class AppContext extends BaseApplication {

    private static final String ApplicationID = "fe9c961af197bda25ff5702844b3feb7";
    private static AppContext instance;

    /**
     * 获得当前app运行的AppContext
     *
     * @return
     */
    public static AppContext getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        //初始化Log系统
        Logger.init("xctx")               // default PRETTYLOGGER or use just init()
                .setMethodCount(1)            // default 2
                .hideThreadInfo();           // default shown
        //异常捕获收集
        //CrashWoodpecker.fly().to(this);
    }


}
