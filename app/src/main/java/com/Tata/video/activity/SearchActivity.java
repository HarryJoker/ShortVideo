package com.Tata.video.activity;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.Tata.video.R;
import com.Tata.video.adapter.SearchAdapter;
import com.Tata.video.bean.SearchBean;
import com.Tata.video.custom.RefreshAdapter;
import com.Tata.video.custom.RefreshView;
import com.Tata.video.event.FollowEvent;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.interfaces.OnItemClickListener;
import com.Tata.video.utils.ToastUtil;
import com.Tata.video.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2017/8/10.
 * 主页搜索页面
 */

public class SearchActivity extends AbsActivity implements View.OnClickListener {

    private EditText mEditText;
    private SearchAdapter mAdapter;
    private InputMethodManager imm;
    private RefreshView mRefreshView;
    private boolean mPaused;
    private Handler mHandler;
    private static final int WHAT = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_search;
    }

    @Override
    protected void main() {
        mRefreshView = (RefreshView) findViewById(R.id.refreshView);
        mRefreshView.setNoDataLayoutId(R.layout.view_no_data_search);
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
                search(callback);
            }

            @Override
            public List<SearchBean> processData(String[] info) {
                List<SearchBean> list = JSON.parseArray(Arrays.toString(info), SearchBean.class);
                if (list.size() < 20) {
                    if (mRefreshView != null) {
                        mRefreshView.setLoadMoreEnable(false);
                    }
                } else {
                    if (mRefreshView != null) {
                        mRefreshView.setLoadMoreEnable(true);
                    }
                }
                return list;
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
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mEditText = (EditText) findViewById(R.id.search_input);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    HttpUtil.cancel(HttpUtil.GET_USER_SEARCH);
                    if (mHandler != null) {
                        mHandler.removeMessages(WHAT);
                    }
                    imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                    mRefreshView.initData();
                    return true;
                }
                return false;
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                HttpUtil.cancel(HttpUtil.GET_USER_SEARCH);
                if (mHandler != null) {
                    mHandler.removeMessages(WHAT);
                    if (!TextUtils.isEmpty(s)) {
                        mHandler.sendEmptyMessageDelayed(WHAT, 500);
                    } else {
                        if (mAdapter != null) {
                            mAdapter.clearData();
                        }
                        if (mRefreshView != null) {
                            mRefreshView.setLoadMoreEnable(false);
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        findViewById(R.id.btn_clear).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        EventBus.getDefault().register(this);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (mRefreshView != null) {
                    mRefreshView.initData();
                }
            }
        };
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_clear:
                clear();
                break;
            case R.id.btn_cancel:
                onBackPressed();
                break;
        }
    }

    private void clear() {
        mEditText.setText("");
        imm.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
        mEditText.requestFocus();
        if (mAdapter != null) {
            mAdapter.clearData();
        }
        if (mRefreshView != null) {
            mRefreshView.setLoadMoreEnable(false);
        }
    }

    @Override
    public void onBackPressed() {
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        super.onBackPressed();
    }


    private void search(HttpCallback callback) {
        String key = mEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(key)) {
            //HttpUtil.getUserSearch(key, callback);
        } else {
            ToastUtil.show(WordUtil.getString(R.string.please_input_content));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPaused = false;
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.GET_USER_SEARCH);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        EventBus.getDefault().unregister(this);
        if (mRefreshView != null) {
            mRefreshView.setDataHelper(null);
        }
        super.onDestroy();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFollowEvent(FollowEvent e) {
        if (mPaused && mAdapter != null) {
            mAdapter.updateItem(e.getTouid(), e.getIsAttention());
        }
    }

}
