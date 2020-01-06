package com.Tata.video.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.Tata.video.AppConfig;
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.bean.OffLineMsgEvent;
import com.Tata.video.bean.VideoBean;
import com.Tata.video.custom.MyViewPager;
import com.Tata.video.event.ChatRoomCloseEvent;
import com.Tata.video.event.JMessageLoginEvent;
import com.Tata.video.event.LogoutEvent;
import com.Tata.video.event.ShowInviteEvent;
import com.Tata.video.fragment.InviteFragment;
import com.Tata.video.fragment.MainFragment;
import com.Tata.video.fragment.UserFragment;
import com.Tata.video.interfaces.GlobalLayoutChangedListener;
import com.Tata.video.interfaces.VideoChangeListener;
import com.Tata.video.presenter.GlobalLayoutPresenter;
import com.Tata.video.utils.LocationUtil;
import com.Tata.video.utils.ToastUtil;
import com.Tata.video.utils.VideoStorge;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/6/11.
 */

public class MainActivity extends AudioAbsActivity implements ViewPager.OnPageChangeListener, GlobalLayoutChangedListener, VideoChangeListener {

    private MyViewPager mViewPager;
    private MainFragment mMainFragment;
    private UserFragment mUserFragment;
    private GlobalLayoutPresenter mPresenter;
    private boolean mShowInvite;

    @Override
    protected int getLayoutId() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        return R.layout.activity_main;
    }

    @Override
    protected void main() {
        super.main();
        mViewPager = (MyViewPager) findViewById(R.id.viewPager);
        mViewPager.addOnPageChangeListener(this);
        mMainFragment = new MainFragment();
        mUserFragment = new UserFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.IS_MAIN_USER_CENTER, false);
        mUserFragment.setArguments(bundle);
        mUserFragment.setOnBackClickListener(new UserFragment.OnBackClickListener() {
            @Override
            public void onBackClick() {
                if (mViewPager != null) {
                    mViewPager.setCurrentItem(0, true);
                }
            }
        });
        final List<Fragment> list = new ArrayList<>();
        list.add(mMainFragment);
        list.add(mUserFragment);
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return list.get(position);
            }

            @Override
            public int getCount() {
                return list.size();
            }
        });
        mPresenter = new GlobalLayoutPresenter(this, mViewPager);
        EventBus.getDefault().register(this);
        //HttpUtil.getConfig(null);
        startLocation();
        AppConfig.getInstance().loginJPush();
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//        L.e("#MainActivity", "onPageScrolled--------position------>" + position);
//        L.e("#MainActivity", "onPageScrolled--------positionOffset------>" + positionOffset);
//        L.e("#MainActivity", "onPageScrolled--------positionOffsetPixels------>" + positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            if (mMainFragment != null) {
                mMainFragment.hiddenChanged(false);
            }
        } else if (position == 1) {
            if (mMainFragment != null) {
                mMainFragment.hiddenChanged(true);
            }
            if (mUserFragment != null) {
                mUserFragment.loadData();
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void changeVideo(VideoBean videoBean) {
        if (videoBean != null && mUserFragment != null) {
            mUserFragment.setUserInfo(videoBean.getUserinfo(), videoBean.getIsattent());
        }
    }

    public void setCanScroll(boolean canScroll) {
        if (mViewPager != null) {
            mViewPager.setCanScroll(canScroll);
        }
    }

    @Override
    public void addLayoutListener() {
        if (mPresenter != null) {
            mPresenter.addLayoutListener();
        }
    }

    @Override
    public void removeLayoutListener() {
        if (mPresenter != null) {
            mPresenter.removeLayoutListener();
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (mViewPager != null) {
            mViewPager.clearOnPageChangeListeners();
        }
        removeLayoutListener();
        if (mPresenter != null) {
            mPresenter.release();
        }
        VideoStorge.getInstance().clear();
        LocationUtil.getInstance().stopLocation();
        super.onDestroy();
    }


    public void showUserInfo() {
        if (mViewPager != null) {
            mViewPager.setCurrentItem(1, true);
        }
    }

    public void mainClick(View v) {
        switch (v.getId()) {
            case R.id.btn_record:
                if (AppConfig.getInstance().isLogin()) {
                    checkVideoPermission();
                } else {
                    LoginActivity.forwardLogin(mContext);
                }
                break;
            case R.id.btn_search:
                forwardSearch();
                break;
        }
    }

    private void forwardSearch() {
        startActivity(new Intent(mContext, SearchActivity2.class));
    }

    /**
     * 开启定位
     */
    private void startLocation() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.REQUEST_LOCATION_PERMISSION);
            } else {
                LocationUtil.getInstance().startLocation();
            }
        } else {
            LocationUtil.getInstance().startLocation();
        }
    }


    /**
     * 检查并申请录制视频的权限
     */
    public void checkVideoPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        Constants.REQUEST_VIDEO_PERMISSION);
            } else {
                startVideoRecord();
            }
        } else {
            startVideoRecord();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (isAllGranted(permissions, grantResults)) {
            switch (requestCode) {
                case Constants.REQUEST_LOCATION_PERMISSION:
                    LocationUtil.getInstance().startLocation();
                    break;
                case Constants.REQUEST_VIDEO_PERMISSION:
                    startVideoRecord();
                    break;
            }
        }


    }

    //判断申请的权限有没有被允许
    private boolean isAllGranted(String[] permissions, int[] grantResults) {
        boolean isAllGranted = true;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                isAllGranted = false;
                showTip(permissions[i]);
                break;
            }
        }
        return isAllGranted;
    }

    //拒绝某项权限时候的提示
    private void showTip(String permission) {
        switch (permission) {
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                ToastUtil.show(getString(R.string.storage_permission_refused));
                break;
            case Manifest.permission.CAMERA:
                ToastUtil.show(getString(R.string.camera_permission_refused));
                break;
            case Manifest.permission.RECORD_AUDIO:
                ToastUtil.show(getString(R.string.record_audio_permission_refused));
                break;
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                ToastUtil.show(getString(R.string.location_permission_refused));
                break;
        }
    }

    /**
     * 开启短视频录制
     */
    private void startVideoRecord() {
//        startActivity(new Intent(mContext, VideoMusicActivity.class));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onJMessageLoginEvent(JMessageLoginEvent e) {
        refreshUnReadCount();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogoutEvent(LogoutEvent e) {
        if (mMainFragment != null) {
            mMainFragment.onLogout();
        }
        refreshUnReadCount();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChatRoomCloseEvent(ChatRoomCloseEvent e) {
        refreshUnReadCount();
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onChatMessageBean(ChatMessageBean e) {
//        refreshUnReadCount();
//    }

    /**
     * 接收到了离线消息
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOffLineMsgEvent(OffLineMsgEvent e) {
        refreshUnReadCount();
    }

    public void refreshUnReadCount() {
        if (mMainFragment != null) {
            mMainFragment.refreshUnReadCount();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onShowInviteEvent(ShowInviteEvent e){
        mShowInvite=true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mShowInvite){
            mShowInvite=false;
            InviteFragment fragment = new InviteFragment();
            fragment.show(getSupportFragmentManager(), "InviteFragment");
        }
    }

    @Override
    public void onBackPressed() {
        if(mViewPager!=null&&mViewPager.getCurrentItem()!=0){
            mViewPager.setCurrentItem(0,true);
            return;
        }
        super.onBackPressed();
    }
}
