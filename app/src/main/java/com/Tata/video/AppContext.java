package com.Tata.video;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.Tata.video.http.HttpUtil;
import com.Tata.video.jpush.JMessageUtil;
import com.Tata.video.jpush.JPushUtil;
import com.Tata.video.utils.SharedPreferencesUtil;
import com.tencent.bugly.crashreport.CrashReport;

import cn.sharesdk.framework.ShareSDK;
import cn.tillusory.sdk.TiSDK;

/**
 * Created by cxf on 2017/8/3.
 */

public class AppContext extends MultiDexApplication {

    public static AppContext sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        //初始化腾讯bugly
        CrashReport.initCrashReport(getApplicationContext());
        //初始化http
        HttpUtil.init();
        //初始化ShareSdk
        ShareSDK.initSDK(this);
        //初始化极光推送
        JPushUtil.getInstance().init();
        //初始化极光IM
        JMessageUtil.getInstance().init();
        //初始化萌颜
        TiSDK.init(AppConfig.BEAUTY_KEY, this);
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
}
