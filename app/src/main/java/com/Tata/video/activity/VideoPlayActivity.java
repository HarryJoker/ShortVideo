package com.Tata.video.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.Tata.video.AppConfig;
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.bean.UserBean;
import com.Tata.video.bean.VideoBean;
import com.Tata.video.custom.VideoPlayWrap;
import com.Tata.video.event.FollowEvent;
import com.Tata.video.event.NeedRefreshLikeEvent;
import com.Tata.video.fragment.UserFragment;
import com.Tata.video.fragment.VideoCommentFragment;
import com.Tata.video.fragment.VideoInputFragment;
import com.Tata.video.fragment.VideoPlayFragment;
import com.Tata.video.fragment.VideoShareFragment;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.interfaces.GlobalLayoutChangedListener;
import com.Tata.video.interfaces.VideoChangeListener;
import com.Tata.video.presenter.GlobalLayoutPresenter;
import com.Tata.video.utils.ToastUtil;
import com.Tata.video.utils.VideoStorge;
import com.Tata.video.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/6/7.
 */

public class VideoPlayActivity extends AudioAbsActivity implements ViewPager.OnPageChangeListener, GlobalLayoutChangedListener, VideoChangeListener {

    private ViewPager mViewPager;
    private VideoPlayFragment mVideoPlayFragment;
    private UserFragment mUserFragment;
    private UserBean mUserBean;
    private List<VideoBean> mVideoList;
    private int mPage;
    private int mPosition;
    private int mIsAttention;
    private FragmentManager mFragmentManager;
    private VideoCommentFragment mCommentFragment;
    private VideoShareFragment mShareFragment;
    private GlobalLayoutPresenter mPresenter;
    private long mLastClickTime;
    private boolean mIsSingleVideo;

    public static void forwardVideoPlay(Context context, String videoKey, int videoPosition, int videoPage, UserBean userBean, int isAttention) {
        Intent intent = new Intent(context, VideoPlayActivity.class);
        intent.putExtra(Constants.VIDEO_KEY, videoKey);
        intent.putExtra(Constants.VIDEO_POSITION, videoPosition);
        intent.putExtra(Constants.VIDEO_PAGE, videoPage);
        intent.putExtra(Constants.USER_BEAN, userBean);
        intent.putExtra(Constants.IS_ATTENTION, isAttention);
        context.startActivity(intent);
    }

    /**
     * 播放单个视频
     */
    public static void forwardSingleVideoPlay(Context context, VideoBean videoBean) {
        Intent intent = new Intent(context, VideoPlayActivity.class);
        intent.putExtra(Constants.SINGLE_VIDEO, true);
        intent.putExtra(Constants.VIDEO_BEAN, videoBean);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        return R.layout.activity_video_play;
    }

    @Override
    protected void main() {
        super.main();
        mFragmentManager = getSupportFragmentManager();
        Intent intent = getIntent();
        mIsSingleVideo = intent.getBooleanExtra(Constants.SINGLE_VIDEO, false);
        final List<Fragment> fragmentList = new ArrayList<>();
        if (mIsSingleVideo) {
            VideoBean videoBean = intent.getParcelableExtra(Constants.VIDEO_BEAN);
            mVideoList = Arrays.asList(videoBean);
            mUserBean = videoBean.getUserinfo();
            mIsAttention = videoBean.getIsattent();
        } else {
            String videoKey = intent.getStringExtra(Constants.VIDEO_KEY);
            mVideoList = VideoStorge.getInstance().get(videoKey);
            mUserBean = intent.getParcelableExtra(Constants.USER_BEAN);
            mIsAttention = intent.getIntExtra(Constants.IS_ATTENTION, 0);
        }
        mPage = intent.getIntExtra(Constants.VIDEO_PAGE, 1);
        mPosition = intent.getIntExtra(Constants.VIDEO_POSITION, 0);

        mVideoPlayFragment = new VideoPlayFragment();
        mVideoPlayFragment.setDataHelper(new VideoPlayFragment.DataHelper() {
            @Override
            public void initData(HttpCallback callback) {

            }

            @Override
            public void loadMoreData(int p, HttpCallback callback) {
//                if (mUserBean != null) {
//                    HttpUtil.getHomeVideo(mUserBean.getId(), p, callback);
//                }
            }

            @Override
            public int getInitPosition() {
                return mPosition;
            }

            @Override
            public List<VideoBean> getInitVideoList() {
                return mVideoList;
            }

            @Override
            public int getInitPage() {
                return mPage;
            }

        });
        mVideoPlayFragment.setActionListener(mActionListener);
        fragmentList.add(mVideoPlayFragment);
        mUserFragment = new UserFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.IS_MAIN_USER_CENTER, false);
        bundle.putParcelable(Constants.USER_BEAN, mUserBean);
        bundle.putInt(Constants.IS_ATTENTION, mIsAttention);
        mUserFragment.setArguments(bundle);
        mUserFragment.setOnBackClickListener(new UserFragment.OnBackClickListener() {
            @Override
            public void onBackClick() {
                if (mViewPager != null) {
                    mViewPager.setCurrentItem(0, true);
                }
            }
        });
        fragmentList.add(mUserFragment);

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }

            @Override
            public int getCount() {
                return fragmentList.size();
            }
        });
        mViewPager.addOnPageChangeListener(this);
        mPresenter = new GlobalLayoutPresenter(this, mViewPager);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (mVideoPlayFragment != null) {
            mVideoPlayFragment.onOuterPageSelected(position);
        }
        if (position == 1 && mUserFragment != null) {
            mUserFragment.loadData();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    public void openCommentWindow() {
        if (!canClick()) {
            return;
        }
        if (!AppConfig.getInstance().isLogin()) {
            ToastUtil.show(WordUtil.getString(R.string.please_login));
            return;
        }
        if (mVideoPlayFragment != null) {
            VideoBean bean = mVideoPlayFragment.getCurVideoBean();
            VideoPlayWrap wrap = mVideoPlayFragment.getCurWrap();
            if (bean != null && wrap != null) {
                VideoInputFragment videoInputFragment = new VideoInputFragment();
                videoInputFragment.setVideoPlayWrap(wrap);
                Bundle bundle = new Bundle();
                bundle.putParcelable(Constants.VIDEO_BEAN, bean);
                videoInputFragment.setArguments(bundle);
                FragmentManager manager = getSupportFragmentManager();
                videoInputFragment.show(manager, "VideoInputFragment");
            }
        }
    }


    private VideoPlayWrap.ActionListener mActionListener = new VideoPlayWrap.ActionListener() {
        @Override
        public void onZanClick(final VideoPlayWrap wrap, VideoBean bean) {
            if (!canClick()) {
                return;
            }
            if (AppConfig.getInstance().isLogin()) {
                if (AppConfig.getInstance().getUid().equals(bean.getUid())) {
                    ToastUtil.show(WordUtil.getString(R.string.cannot_zan_self));
                    return;
                }
                String videoId = bean.getId();
                if (!TextUtils.isEmpty(videoId)) {
                    HttpUtil.setVideoLike(videoId, new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (code == 0 && info.length > 0) {
                                JSONObject obj = JSON.parseObject(info[0]);
                                int islike = obj.getIntValue("islike");
                                wrap.setLikes(islike, obj.getString("likes"));
                                EventBus.getDefault().post(new NeedRefreshLikeEvent());
                            }
                        }
                    });
                }
            } else {
                LoginActivity.forwardLogin(mContext);
            }
        }

        @Override
        public void onCommentClick(VideoPlayWrap wrap, VideoBean bean) {
            if (!canClick()) {
                return;
            }
            ((GlobalLayoutChangedListener) mContext).addLayoutListener();
            mCommentFragment = new VideoCommentFragment();
            mCommentFragment.setVideoPlayWrap(wrap);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.VIDEO_ID, bean.getId());
            bundle.putString(Constants.UID, bean.getUid());
            bundle.putBoolean(Constants.FULL_SCREEN, false);
            mCommentFragment.setArguments(bundle);
            if (!mCommentFragment.isAdded()) {
                mCommentFragment.show(((VideoPlayActivity) mContext).getSupportFragmentManager(), "VideoShareFragment");
            }
        }

        @Override
        public void onFollowClick(final VideoPlayWrap wrap, VideoBean bean) {
            if (!canClick()) {
                return;
            }
            if (AppConfig.getInstance().isLogin()) {
                if (AppConfig.getInstance().getUid().equals(bean.getUid())) {
                    ToastUtil.show(WordUtil.getString(R.string.cannot_follow_self));
                    return;
                }
                final String touid = bean.getUid();
                if (!TextUtils.isEmpty(touid)) {
                    HttpUtil.setAttention(bean.getUid(), null);
                }
            } else {
                LoginActivity.forwardLogin(mContext);
            }
        }

        @Override
        public void onAvatarClick(VideoPlayWrap wrap, VideoBean bean) {
            if (!canClick()) {
                return;
            }
            if (mViewPager != null) {
                mViewPager.setCurrentItem(1, true);
            }
        }

        @Override
        public void onShareClick(final VideoPlayWrap wrap, final VideoBean bean) {
            if (!canClick()) {
                return;
            }
            mShareFragment = new VideoShareFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.VIDEO_BEAN, bean);
            mShareFragment.setArguments(bundle);
            mShareFragment.setActionListener(new VideoShareFragment.ActionListener() {
                @Override
                public void onShareSuccess() {
                    HttpUtil.setVideoShare(bean.getId(), new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (code == 0 && info.length > 0) {
                                JSONObject obj = JSON.parseObject(info[0]);
                                wrap.setShares(obj.getString("shares"));
                            }
                        }
                    });
                }
            });
            if (!mShareFragment.isAdded()) {
                mShareFragment.show(mFragmentManager, "VideoShareFragment");
            }
        }
    };


    @Override
    public void onDestroy() {
        if (mViewPager != null) {
            mViewPager.clearOnPageChangeListeners();
        }
        if (mVideoPlayFragment != null) {
            mVideoPlayFragment.setDataHelper(null);
            mVideoPlayFragment.setActionListener(null);
        }
        removeLayoutListener();
        if (mPresenter != null) {
            mPresenter.release();
        }
        HttpUtil.cancel(HttpUtil.GET_HOME_VIDEO);
        HttpUtil.cancel(HttpUtil.SET_VIDEO_LIKE);
        HttpUtil.cancel(HttpUtil.SET_VIDEO_SHARE);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFollowEvent(FollowEvent e) {
        String touid = e.getTouid();
        if (touid != null && mVideoPlayFragment != null) {
            mVideoPlayFragment.refreshVideoAttention(touid, e.getIsAttention());
        }
    }


    @Override
    public void changeVideo(VideoBean videoBean) {
        if (videoBean != null && mUserFragment != null) {
            mUserFragment.setUserInfo(videoBean.getUserinfo(), videoBean.getIsattent());
        }
    }


    private boolean canClick() {
        long timeStamp = System.currentTimeMillis();
        if (timeStamp - mLastClickTime < 1000) {
            return false;
        } else {
            mLastClickTime = timeStamp;
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        if(mViewPager!=null&&mViewPager.getCurrentItem()!=0){
            mViewPager.setCurrentItem(0,true);
            return;
        }
        if (mVideoPlayFragment != null) {
            mVideoPlayFragment.backDestroyPlayView();
        }
        super.onBackPressed();
    }
}
