package com.Tata.video.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSON;
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.adapter.AdminMsgAdapter;
import com.Tata.video.bean.AdminMsgBean;
import com.Tata.video.custom.RefreshAdapter;
import com.Tata.video.custom.RefreshView;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.interfaces.OnItemClickListener;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/7/27.
 */

public class AdminMsgActivity extends AbsActivity implements OnItemClickListener<AdminMsgBean> {

    private RefreshView mRefreshView;
    private AdminMsgAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_admin_msg;
    }

    @Override
    protected void main() {
        setTitle(getResources().getString(R.string.app_name)+ getResources().getString(R.string.Official));
        mRefreshView = (RefreshView) findViewById(R.id.refreshView);
        mRefreshView.setNoDataLayoutId(R.layout.view_no_data_default);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new RefreshView.DataHelper<AdminMsgBean>() {
            @Override
            public RefreshAdapter<AdminMsgBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new AdminMsgAdapter(mContext);
                    mAdapter.setOnItemClickListener(AdminMsgActivity.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                HttpUtil.getAdminMsgList(p, callback);
            }

            @Override
            public List<AdminMsgBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), AdminMsgBean.class);
            }

            @Override
            public void onRefresh(List<AdminMsgBean> list) {

            }

            @Override
            public void onNoData(boolean noData) {

            }

            @Override
            public void onLoadDataCompleted(int dataCount) {
                if (dataCount < 20) {
                    mRefreshView.setLoadMoreEnable(false);
                } else {
                    mRefreshView.setLoadMoreEnable(true);
                }
            }
        });
        mRefreshView.initData();
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.GET_ADMIN_MSG_LIST);
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdminMsgBean bean, int position) {
        Intent intent=new Intent(mContext,WebActivity.class);
        intent.putExtra(Constants.URL,bean.getUrl());
        startActivity(intent);
    }
}
