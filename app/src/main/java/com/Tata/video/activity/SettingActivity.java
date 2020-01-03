package com.Tata.video.activity;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.Tata.video.AppConfig;
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.bean.ConfigBean;
import com.Tata.video.event.LogoutEvent;
import com.Tata.video.event.NeedRefreshEvent;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.interfaces.CommonCallback;
import com.Tata.video.utils.DialogUitl;
import com.Tata.video.utils.GlideCatchUtil;
import com.Tata.video.utils.ToastUtil;
import com.Tata.video.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by cxf on 2018/6/14.
 */

public class SettingActivity extends AbsActivity {

    private Handler mHandler;
    private TextView mCacheSize;
    private String mCurVersion;
    private TextView mVersion;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.setting));
        mHandler = new Handler();
        mCacheSize = (TextView) findViewById(R.id.cache_size);
        mVersion = (TextView) findViewById(R.id.version);
        mCacheSize.setText(getCacheSize());
        mCurVersion = AppConfig.getInstance().getVersion();

        ConfigBean configBean = AppConfig.getInstance().getConfig();
        if (configBean == null) {
            HttpUtil.getConfig(new CommonCallback<ConfigBean>() {
                @Override
                public void callback(ConfigBean configBean) {
                    showUpdate(configBean);
                }
            });
        } else {
            showUpdate(configBean);
        }
    }

    private void showUpdate(ConfigBean configBean) {
        String des;
        if (TextUtils.isEmpty(mCurVersion) || mCurVersion.equals(configBean.getApk_ver())) {
            des = "(" + WordUtil.getString(R.string.no_update) + ")";
        } else {
            des = "(" + WordUtil.getString(R.string.can_update) + ")";
        }
        mVersion.setText(mCurVersion + des);
    }

    public void settingClick(View v) {
        switch (v.getId()) {
            case R.id.btn_black_list:
                forwardBlackList();
                break;
            case R.id.btn_auth:
                forwardMyAuth();
                break;
            case R.id.btn_about:
                forwardAboutUs();
                break;
            case R.id.btn_check_update:
                checkUpdate();
                break;
            case R.id.btn_clear_cache:
                clearCache();
                break;
            case R.id.btn_logout:
                logout();
                break;
        }
    }


    private void forwardBlackList() {
        startActivity(new Intent(mContext, BlackActivity.class));
    }


    /**
     * 我的认证
     */
    private void forwardMyAuth() {
        Intent intent = new Intent(mContext, WebUploadImgActivity.class);
        AppConfig appConfig = AppConfig.getInstance();
        intent.putExtra(Constants.URL, AppConfig.HOST + "/index.php?g=Appapi&m=Auth&a=index&uid=" + appConfig.getUid() + "&token=" + appConfig.getToken());
        startActivity(intent);
    }

    /**
     * 关于我们
     */
    private void forwardAboutUs() {
        Intent intent = new Intent(mContext, WebUploadImgActivity.class);
        AppConfig appConfig = AppConfig.getInstance();
        String url = AppConfig.HOST + "/index.php?g=Appapi&m=About&a=index&uid=" + appConfig.getUid() + "&token=" + appConfig.getToken() + "&version=" + appConfig.getVersion();
        intent.putExtra(Constants.URL, url);
        startActivity(intent);
    }

    /**
     * 检查更新
     */
    private void checkUpdate() {
        HttpUtil.getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(final ConfigBean bean) {
                if (mCurVersion.equals(bean.getApk_ver())) {
                    ToastUtil.show(WordUtil.getString(R.string.latest_version));
                } else {
                    DialogUitl.showSimpleDialog(mContext, bean.getApk_des(), new DialogUitl.SimpleDialogCallback() {
                        @Override
                        public void onComfirmClick() {
                            String apkUrl = AppConfig.getInstance().getConfig().getApk_url();
                            if (TextUtils.isEmpty(apkUrl)) {
                                ToastUtil.show(WordUtil.getString(R.string.apk_url_not_exist));
                            } else {
                                try {
                                    Intent intent = new Intent();
                                    intent.setAction("android.intent.action.VIEW");
                                    intent.setData(Uri.parse(apkUrl));
                                    startActivity(intent);
                                } catch (Exception e) {
                                    ToastUtil.show(WordUtil.getString(R.string.apk_url_not_exist));
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 清除缓存
     */
    private void clearCache() {
        final Dialog dialog = DialogUitl.loadingDialog(mContext, getString(R.string.clear_ing));
        dialog.show();
        GlideCatchUtil.getInstance().clearImageAllCache();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    dialog.dismiss();
                }
                mCacheSize.setText(getCacheSize());
            }
        }, 2000);
    }

    private String getCacheSize() {
        String cache = GlideCatchUtil.getInstance().getCacheSize();
        if ("0.0Byte".equalsIgnoreCase(cache)) {
            cache = getString(R.string.no_cache);
        }
        return cache;
    }

    private void logout() {
        AppConfig.getInstance().logout();
        AppConfig.getInstance().logoutJPush();
        EventBus.getDefault().post(new NeedRefreshEvent());
        EventBus.getDefault().post(new LogoutEvent());
        finish();
    }

    @Override
    protected void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }
}
