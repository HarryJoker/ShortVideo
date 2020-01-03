package com.Tata.video.activity;

import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.Tata.video.AppConfig;
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.adapter.SearchAdapter;
import com.Tata.video.bean.SearchBean;
import com.Tata.video.custom.RefreshAdapter;
import com.Tata.video.custom.RefreshView;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.interfaces.OnItemClickListener;
import com.Tata.video.utils.WordUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2017/8/10.
 * 我的 关注
 */

public class FollowActivity extends AbsActivity {

    private SearchAdapter mAdapter;
    private RefreshView mRefreshView;
    private String mToUid;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_follow;
    }

    @Override
    protected void main() {
        mToUid = getIntent().getStringExtra(Constants.TO_UID);
        if (TextUtils.isEmpty(mToUid)) {
            return;
        }
        mRefreshView = (RefreshView) findViewById(R.id.refreshView);
        if (mToUid.equals(AppConfig.getInstance().getUid())) {
            setTitle(WordUtil.getString(R.string.my_follow));
            mRefreshView.setNoDataLayoutId(R.layout.view_no_data_follow);
        } else {
            setTitle(WordUtil.getString(R.string.my_follow_2));
            mRefreshView.setNoDataLayoutId(R.layout.view_no_data_follow_2);
        }

        mRefreshView.setDataHelper(new RefreshView.DataHelper<SearchBean>() {
            @Override
            public RefreshAdapter<SearchBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new SearchAdapter(mContext);
                    mAdapter.setOnItemClickListener(new OnItemClickListener<SearchBean>() {
                        @Override
                        public void onItemClick(SearchBean bean, int position) {
                            OtherUserActivity.forwardOtherUser(mContext, bean.getId());
                        }
                    });
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                HttpUtil.getFollowsList(mToUid, p, "", callback);
            }

            @Override
            public List<SearchBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), SearchBean.class);
            }

            @Override
            public void onRefresh(List<SearchBean> list) {

            }

            @Override
            public void onNoData(boolean noData) {

            }

            @Override
            public void onLoadDataCompleted(int dataCount) {

            }
        });
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRefreshView.initData();
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.GET_FOLLOWS_LIST);
        super.onDestroy();
    }
}
