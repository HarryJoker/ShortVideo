package com.Tata.video;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.utils.SharedPreferencesUtil;
import cn.sharesdk.framework.ShareSDK;

//    implementation'com.tencent.map.geolocation:TencentLocationSdk-openplatform:7.2.4'//腾讯定位
//    implementation 'com.tencent.map:tencent-map-vector-sdk:latest.release'          //腾讯地图

/**
 * Created by cxf on 2017/8/3.
 */

public class AppContext extends MultiDexApplication {

    public static AppContext sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
        //初始化腾讯bugly
//        CrashReport.initCrashReport(getApplicationContext());
        //初始化http
        HttpUtil.init();
        //初始化ShareSdk
        ShareSDK.initSDK(this);
        //初始化极光推送
//        JPushUtil.getInstance().init();
        //初始化极光IM
//        JMessageUtil.getInstance().init();
        //初始化萌颜
//        TiSDK.init(AppConfig.BEAUTY_KEY, this);
        //获取uid和token
        String[] uidAndToken = SharedPreferencesUtil.getInstance().readUidAndToken();
        if (uidAndToken != null) {
            AppConfig.getInstance().login(uidAndToken[0], uidAndToken[1]);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(this);
        super.attachBaseContext(base);
    }

    ActivityLifecycleCallbacks mActivityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            Log.d(activity.getClass().getSimpleName(), "onActivityCreated -------------------------------->");
        }

        @Override
        public void onActivityStarted(Activity activity) {
            Log.d(activity.getClass().getSimpleName(), "onActivityStarted -------------------------------->");
        }

        @Override
        public void onActivityResumed(Activity activity) {
            Log.d(activity.getClass().getSimpleName(), "onActivityResumed -------------------------------->");
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Log.d(activity.getClass().getSimpleName(), "onActivityPaused -------------------------------->");
        }

        @Override
        public void onActivityStopped(Activity activity) {
            Log.d(activity.getClass().getSimpleName(), "onActivityStopped -------------------------------->");
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            Log.d(activity.getClass().getSimpleName(), "onActivityDestroyed -------------------------------->");
        }
    };
}
