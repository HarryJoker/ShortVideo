package com.Tata.video.fragment;

import android.support.v7.widget.GridLayoutManager;

import com.alibaba.fastjson.JSON;
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.activity.OtherUserActivity;
import com.Tata.video.activity.VideoPlayActivity;
import com.Tata.video.adapter.UserWorkAdapter;
import com.Tata.video.bean.VideoBean;
import com.Tata.video.custom.ItemDecoration;
import com.Tata.video.custom.RefreshAdapter;
import com.Tata.video.custom.RefreshView;
import com.Tata.video.event.NeedRefreshWorkEvent;
import com.Tata.video.event.VideoDeleteEvent;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.interfaces.OnItemClickListener;
import com.Tata.video.utils.VideoStorge;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/6/10.
 * 个人中心  作品
 */

public class UserWorkFragment extends UserItemFragment implements OnItemClickListener<VideoBean> {

    private RefreshView mRefreshView;
    private UserWorkAdapter mAdapter;
    private boolean mNeedRefresh;
    private boolean mPaused;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_user_video_work;
    }

    @Override
    protected void main() {
        super.main();
        mRefreshView = (RefreshView) mRootView.findViewById(R.id.refreshView);
        if (!mIsMainUserCenter) {
            mRefreshView.setNoDataLayoutId(R.layout.view_no_data_user_work);
        }
        mRefreshView.setDataHelper(new RefreshView.DataHelper<VideoBean>() {

            @Override
            public RefreshAdapter<VideoBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new UserWorkAdapter(mContext);
                    mAdapter.setOnItemClickListener(UserWorkFragment.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                if (!Constants.NOT_LOGIN_UID.equals(mUid)) {
                    HttpUtil.getHomeVideo(mUid, p, callback);
                }
            }

            @Override
            public List<VideoBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), VideoBean.class);
            }

            @Override
            public void onRefresh(List<VideoBean> list) {
                VideoStorge.getInstance().put(mHashCode, list);
            }

            @Override
            public void onNoData(boolean noData) {

            }

            @Override
            public void onLoadDataCompleted(int dataCount) {
                if (dataCount <= 0) {
                    mRefreshView.setLoadMoreEnable(false);
                } else {
                    mRefreshView.setLoadMoreEnable(true);
                }
                if (mIsMainUserCenter && dataCount > 0) {
                    ((UserFragment) getParentFragment()).onWorkCountChanged(dataCount);
                }
            }
        });
        mRefreshView.setLayoutManager(new GridLayoutManager(mContext, 3, GridLayoutManager.VERTICAL, false));
        ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 2, 2);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        mRefreshView.setItemDecoration(decoration);
        if (mIsMainUserCenter) {
            mRefreshView.initData();
            EventBus.getDefault().register(this);
        }
        if (mContext instanceof OtherUserActivity) {
            mRefreshView.initData();
        }
    }

    @Override
    public void onItemClick(VideoBean bean, int position) {
        if (mRefreshView != null && bean != null && bean.getUserinfo() != null) {
            VideoPlayActivity.forwardVideoPlay(mContext, mHashCode, position, mRefreshView.getPage(), bean.getUserinfo(), bean.getIsattent());
        }
    }


    @Override
    public void loadData() {
//        if (mFirst) {
//            mFirst = false;
//
//        }
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }

    @Override
    public void onDestroy() {
        if (mIsMainUserCenter) {
            EventBus.getDefault().unregister(this);
        }
        HttpUtil.cancel(HttpUtil.GET_HOME_VIDEO);
        super.onDestroy();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNeedRefreshWorkEvent(NeedRefreshWorkEvent e) {
        if (mPaused) {
            mNeedRefresh = true;
        } else {
            if (mRefreshView != null) {
                mRefreshView.initData();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoDeleteEvent(VideoDeleteEvent e) {
        mNeedRefresh = true;
    }


    @Override
    public void onLoginUserChanged(String uid) {
        mUid = uid;
//        if (mRefreshView != null) {
//            mRefreshView.initData();
//        }
        mNeedRefresh = true;
    }

    @Override
    public void clearData() {
        if (mAdapter != null) {
            mAdapter.clearData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPaused) {
            mPaused = false;
            if (mNeedRefresh && mRefreshView != null) {
                mNeedRefresh = false;
                mRefreshView.initData();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
    }

}
