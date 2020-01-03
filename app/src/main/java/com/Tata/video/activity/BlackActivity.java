package com.Tata.video.activity;

import android.app.Dialog;
import android.support.v7.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSON;
import com.Tata.video.R;
import com.Tata.video.adapter.BlackAdapter;
import com.Tata.video.bean.UserBean;
import com.Tata.video.custom.RefreshAdapter;
import com.Tata.video.custom.RefreshView;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.utils.DialogUitl;
import com.Tata.video.utils.WordUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/7/30.
 */

public class BlackActivity extends AbsActivity implements BlackAdapter.ActionListener {

    private RefreshView mRefreshView;
    private BlackAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_black;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.black_list));
        mRefreshView = (RefreshView) findViewById(R.id.refreshView);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setNoDataLayoutId(R.layout.view_no_data_default);
        mRefreshView.setDataHelper(new RefreshView.DataHelper<UserBean>() {
            @Override
            public RefreshAdapter<UserBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new BlackAdapter(mContext);
                    mAdapter.setActionListener(BlackActivity.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                HttpUtil.getBlackList(p, callback);
            }

            @Override
            public List<UserBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), UserBean.class);
            }

            @Override
            public void onRefresh(List<UserBean> list) {

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
    public void onItemRemoved(String toUid, final int position) {
        HttpUtil.setBlack(toUid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (mAdapter != null) {
                    mAdapter.removeItem(position);
                    if (mAdapter.getItemCount() == 0 && mRefreshView != null) {
                        mRefreshView.showNoData();
                    }
                }
            }

            @Override
            public boolean showLoadingDialog() {
                return true;
            }

            @Override
            public Dialog createLoadingDialog() {
                return DialogUitl.loadingDialog(mContext, WordUtil.getString(R.string.processing));
            }
        });
    }


    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.GET_BLACK_LIST);
        HttpUtil.cancel(HttpUtil.SET_BLACK);
        mRefreshView.setDataHelper(null);
        if (mAdapter != null) {
            mAdapter.setActionListener(null);
        }
        super.onDestroy();
    }

}
