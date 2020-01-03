package com.Tata.video.activity;

import android.content.Context;
import android.content.Intent;
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
import com.Tata.video.AppConfig;
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.adapter.ContactsAdapter;
import com.Tata.video.bean.SearchBean;
import com.Tata.video.custom.RefreshAdapter;
import com.Tata.video.custom.RefreshView;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.interfaces.OnItemClickListener;
import com.Tata.video.utils.ToastUtil;
import com.Tata.video.utils.WordUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/7/22.
 * 召唤好友
 */

public class AtFriendsActivity extends AbsActivity implements OnItemClickListener<SearchBean> {

    private View mTip;
    private RefreshView mRefreshView1;
    private RefreshView mRefreshView2;
    private ContactsAdapter mAdapter1;
    private ContactsAdapter mAdapter2;
    private View mResult;
    private EditText mEditText;
    private Handler mHandler;
    private static final int WHAT = 0;
    private InputMethodManager imm;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_at_friends;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.at_friends));
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mTip = findViewById(R.id.tip);
        mResult = findViewById(R.id.result);
        mEditText = (EditText) findViewById(R.id.search_input);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    HttpUtil.cancel(HttpUtil.GET_FOLLOWS_LIST);
                    if (mHandler != null) {
                        mHandler.removeMessages(WHAT);
                    }
                    imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                    if (mResult.getVisibility() != View.VISIBLE) {
                        mResult.setVisibility(View.VISIBLE);
                    }
                    mRefreshView2.initData();
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
                HttpUtil.cancel(HttpUtil.GET_FOLLOWS_LIST);
                if (mHandler != null) {
                    mHandler.removeMessages(WHAT);
                    if (!TextUtils.isEmpty(s)) {
                        mHandler.sendEmptyMessageDelayed(WHAT, 500);
                    } else {
                        if (mResult.getVisibility() == View.VISIBLE) {
                            mResult.setVisibility(View.INVISIBLE);
                        }
                        if (mAdapter2 != null) {
                            mAdapter2.clearData();
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (mRefreshView2 != null) {
                    if (mResult.getVisibility() != View.VISIBLE) {
                        mResult.setVisibility(View.VISIBLE);
                    }
                    mRefreshView2.initData();
                }
            }
        };
        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = mEditText.getText().toString();
                if (TextUtils.isEmpty(s)) {
                    return;
                }
                mEditText.setText("");
                mEditText.requestFocus();
                imm.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
                if (mResult.getVisibility() == View.VISIBLE) {
                    mResult.setVisibility(View.INVISIBLE);
                }
                if (mAdapter2 != null) {
                    mAdapter2.clearData();
                }
            }
        });

        mRefreshView2 = (RefreshView) findViewById(R.id.refreshView2);
        mRefreshView2.setNoDataLayoutId(R.layout.view_no_data_search);
        mRefreshView2.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView2.setDataHelper(new RefreshView.DataHelper<SearchBean>() {
            @Override
            public RefreshAdapter<SearchBean> getAdapter() {
                if (mAdapter2 == null) {
                    mAdapter2 = new ContactsAdapter(mContext);
                    mAdapter2.setOnItemClickListener(AtFriendsActivity.this);
                }
                return mAdapter2;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                search(p, callback);
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
                if (dataCount < 20) {
                    mRefreshView2.setLoadMoreEnable(false);
                } else {
                    mRefreshView2.setLoadMoreEnable(true);
                }
            }
        });

        mRefreshView1 = (RefreshView) findViewById(R.id.refreshView1);
        mRefreshView1.setNoDataLayoutId(R.layout.view_no_data_follow);
        mRefreshView1.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView1.setDataHelper(new RefreshView.DataHelper<SearchBean>() {
            @Override
            public RefreshAdapter<SearchBean> getAdapter() {
                if (mAdapter1 == null) {
                    mAdapter1 = new ContactsAdapter(mContext);
                    mAdapter1.setOnItemClickListener(AtFriendsActivity.this);
                }
                return mAdapter1;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                HttpUtil.getFollowsList(AppConfig.getInstance().getUid(), p, "", callback);
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
                if (!noData) {
                    mEditText.setEnabled(true);
                    if (mTip.getVisibility() != View.VISIBLE) {
                        mTip.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (mTip.getVisibility() == View.VISIBLE) {
                        mTip.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onLoadDataCompleted(int dataCount) {
                if (dataCount < 20) {
                    mRefreshView1.setLoadMoreEnable(false);
                } else {
                    mRefreshView1.setLoadMoreEnable(true);
                }
            }
        });

    }

    private void search(int p, HttpCallback callback) {
        String key = mEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(key)) {
            HttpUtil.getUserSearch(key, p, callback);
        } else {
            ToastUtil.show(WordUtil.getString(R.string.please_input_content));
        }
    }


    @Override
    public void onBackPressed() {
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRefreshView1 != null) {
            mRefreshView1.initData();
        }
    }


    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.GET_FOLLOWS_LIST);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (mRefreshView1 != null) {
            mRefreshView1.setDataHelper(null);
        }
        if (mAdapter1 != null) {
            mAdapter1.setOnItemClickListener(null);
        }
        if (mRefreshView2 != null) {
            mRefreshView2.setDataHelper(null);
        }
        if (mAdapter2 != null) {
            mAdapter2.setOnItemClickListener(null);
        }
        super.onDestroy();
    }

    @Override
    public void onItemClick(SearchBean bean, int position) {
        Intent intent = new Intent();
        intent.putExtra(Constants.UID, bean.getId());
        intent.putExtra(Constants.USER_NICE_NAME, bean.getUser_nicename());
        setResult(RESULT_OK, intent);
        finish();
    }
}
