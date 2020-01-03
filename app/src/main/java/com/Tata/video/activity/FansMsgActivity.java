package com.Tata.video.activity;

import android.support.v7.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSON;
import com.Tata.video.R;
import com.Tata.video.adapter.FansMsgAdapter;
import com.Tata.video.bean.FansMsgBean;
import com.Tata.video.custom.RefreshAdapter;
import com.Tata.video.custom.RefreshView;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.utils.WordUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/7/21.
 */

public class FansMsgActivity extends AbsActivity {

    private RefreshView mRefreshView;
    private FansMsgAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_fans_msg;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.fans));
        mRefreshView = (RefreshView) findViewById(R.id.refreshView);
        mRefreshView.setNoDataLayoutId(R.layout.view_no_data_fans);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new RefreshView.DataHelper<FansMsgBean>() {
            @Override
            public RefreshAdapter<FansMsgBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new FansMsgAdapter(mContext);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                HttpUtil.getFansMessages(p, callback);
            }

            @Override
            public List<FansMsgBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), FansMsgBean.class);
            }

            @Override
            public void onRefresh(List<FansMsgBean> list) {

            }
            @Override
            public void onNoData(boolean noData) {

            }

            @Override
            public void onLoadDataCompleted(int dataCount) {

            }
        });
        mRefreshView.initData();
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.GET_FANS_MESSAGES);
        super.onDestroy();
    }
}
