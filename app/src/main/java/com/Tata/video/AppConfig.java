package com.Tata.video;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.Tata.video.bean.ConfigBean;
import com.Tata.video.bean.UserBean;
import com.Tata.video.jpush.JMessageUtil;
import com.Tata.video.jpush.JPushUtil;
import com.Tata.video.utils.L;
import com.Tata.video.utils.SharedPreferencesUtil;

/**
 * Created by cxf on 2017/8/4.
 */

public class AppConfig {

    private static final String TAG = "AppConfig";

    //域名
    public static final String HOST = "http://short-video.kinlink.cn";
/*"http://vedio.mie5123.cn"  "http://short-video.kinlink.cn"*/
    public static final String FILE_PROVIDER = "com.qianhong.phonelive.fileprovider";

    public static final String DCMI_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
    //保存视频的时候，在sd卡存储短视频的路径DCIM下
    public static final String VIDEO_PATH = DCMI_PATH + "/yunbao/video/";
    //下载贴纸的时候保存的路径
    public static final String VIDEO_TIE_ZHI_PATH = DCMI_PATH + "/yunbao/tieZhi/";
    //下载音乐的时候保存的路径
    public static final String VIDEO_MUSIC_PATH = DCMI_PATH + "/yunbao/music/";
    //拍照时图片保存路径
    public static final String CAMERA_IMAGE_PATH = DCMI_PATH + "/yunbao/camera/";
    //腾讯云存储远程上传的图片文件路径
    public static final String TX_COS_IMG_PATH = "dsp";
    //腾讯云存储远程上传的视频文件路径
    public static final String TX_COS_VIDEO_PATH = "dsp";
    //萌颜鉴权码
    public static final String BEAUTY_KEY = "95508c2aa7e347b99edca30d391851a4";

    private static AppConfig sInstance;

    private AppConfig() {

    }

    public static AppConfig getInstance() {
        if (sInstance == null) {
            synchronized (AppConfig.class) {
                if (sInstance == null) {
                    sInstance = new AppConfig();
                }
            }
        }
        return sInstance;
    }

    private String mUid = Constants.NOT_LOGIN_UID;//未登录的uid
    private String mToken;
    private ConfigBean mConfig;
    private double mLng;
    private double mLat;
    private String mProvince;//省
    private String mCity;//市
    private String mDistrict;//区
    private UserBean mUserBean;
    private String mVersion;
    private boolean mLoginIM;//IM是否登录了
    private String mJPushAppKey;//极光推送的AppKey
    private String mTxLocationKey;//腾讯定位，地图的AppKey

    public String getUid() {
        if (TextUtils.isEmpty(mUid)) {
            String[] uidAndToken = SharedPreferencesUtil.getInstance().readUidAndToken();
            if (uidAndToken != null && uidAndToken.length > 1) {
                mUid = uidAndToken[0];
                mToken = uidAndToken[1];
            } else {
                mUid = Constants.NOT_LOGIN_UID;
            }
        }
        return mUid;
    }

    public String getToken() {
        return mToken;
    }

    public ConfigBean getConfig() {
        return mConfig;
    }

    public void setConfig(ConfigBean config) {
        mConfig = config;
    }

    public double getLng() {
        return mLng;
    }

    public double getLat() {
        return mLat;
    }

    public String getProvince() {
        return mProvince;
    }

    public String getCity() {
        return mCity;
    }

    public String getDistrict() {
        return mDistrict;
    }

    public void setUserBean(UserBean bean) {
        mUserBean = bean;
    }

    public UserBean getUserBean() {
        if (mUserBean == null) {
            String userBeanJson = SharedPreferencesUtil.getInstance().readUserBeanJson();
            if (!TextUtils.isEmpty(userBeanJson)) {
                mUserBean = JSON.parseObject(userBeanJson, UserBean.class);
            } else {
                mUid = Constants.NOT_LOGIN_UID;
                mToken = null;
            }
        }
        return mUserBean;
    }


    /**
     * 判断是否登录过了
     *
     * @return
     */
    public boolean isLogin() {
        if (TextUtils.isEmpty(mToken) || TextUtils.isEmpty(mUid) || Constants.NOT_LOGIN_UID.equals(mUid)) {//未登录的uid为-1
            return false;
        }
        return true;
    }

    /**
     * 设置登录信息
     *
     * @param uid
     * @param token
     */
    public void login(String uid, String token) {
        L.e(TAG, "#uid------>" + uid);
        L.e(TAG, "#token------>" + token);
        mUid = uid;
        mToken = token;
        SharedPreferencesUtil.getInstance().saveUidAndToken(uid, token);
    }

    public void logout() {
        mUid = Constants.NOT_LOGIN_UID;
        mToken = null;
        SharedPreferencesUtil.getInstance().clear();
    }


    /**
     * 设置位置信息
     *
     * @param lng      经度
     * @param lat      纬度
     * @param province 省
     * @param city     市
     */
    public void setLocationInfo(double lng, double lat, String province, String city, String district) {
        mLng = lng;
        mLat = lat;
        mProvince = province;
        mCity = city;
        mDistrict = district;
    }

    /**
     * 登录JPush和JMessage
     */
    public void loginJPush() {
        if (isLogin()) {
            JPushUtil.getInstance().setAlias(mUid);
            JMessageUtil.getInstance().loginJMessage(mUid);
        }
    }

    /**
     * 登出JPush和JMessage
     */
    public void logoutJPush() {
        JPushUtil.getInstance().stopPush();
        JMessageUtil.getInstance().logoutEMClient();
        mLoginIM = false;
    }

    /**
     * 获取版本号
     */
    public String getVersion() {
        if (TextUtils.isEmpty(mVersion)) {
            try {
                PackageManager manager = AppContext.sInstance.getPackageManager();
                PackageInfo info = manager.getPackageInfo(AppContext.sInstance.getPackageName(), 0);
                mVersion = info.versionName;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mVersion;
    }

    /**
     * 获取MetaData中的极光AppKey
     *
     * @return
     */
    public String getJPushAppKey() {
        if (mJPushAppKey == null) {
            mJPushAppKey = getMetaDataString("JPUSH_APPKEY");
        }
        return mJPushAppKey;
    }


    /**
     * 获取MetaData中的腾讯定位，地图的AppKey
     *
     * @return
     */
    public String getTxLocationKey() {
        if (mTxLocationKey == null) {
            mTxLocationKey = getMetaDataString("TencentMapSDK");
        }
        return mTxLocationKey;
    }

    private String getMetaDataString(String key) {
        String res = null;
        try {
            ApplicationInfo appInfo = AppContext.sInstance.getPackageManager().getApplicationInfo(AppContext.sInstance.getPackageName(), PackageManager.GET_META_DATA);
            res = appInfo.metaData.getString(key);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return res;
    }

    public void setLoginIM(boolean loginIM) {
        mLoginIM = loginIM;
    }

    public boolean isLoginIM() {
        return mLoginIM;
    }
}
