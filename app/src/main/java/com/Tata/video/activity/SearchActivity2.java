package com.Tata.video.activity;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.adapter.FollowVideoAdapter;
import com.Tata.video.adapter.SearchAdapter;
import com.Tata.video.adapter.SearchHistoryAdapter;
import com.Tata.video.bean.SearchBean;
import com.Tata.video.bean.VideoBean;
import com.Tata.video.custom.ItemDecoration;
import com.Tata.video.custom.RefreshAdapter;
import com.Tata.video.custom.RefreshView;
import com.Tata.video.custom.ViewPagerIndicator;
import com.Tata.video.event.FollowEvent;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.interfaces.OnItemClickListener;
import com.Tata.video.utils.SharedPreferencesUtil;
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
 * Created by cxf on 2017/8/10.
 * 主页搜索页面
 */

public class SearchActivity2 extends AbsActivity implements View.OnClickListener, OnItemClickListener<VideoBean> {

    private static final int WHAT = 0;
    private EditText mEditText;
    private InputMethodManager imm;
    private Handler mHandler;
    private View mGroup;
    private ViewPagerIndicator mIndicator;
    private ViewPager mViewPager;
    private List<View> mViewList;
    private RefreshView mUserRefreshView;
    private RefreshView mVideoRefreshView;
    private SearchAdapter mSearchUserAdapter;
    private FollowVideoAdapter mVideoAdapter;
    private String mLastKey1;
    private String mLastKey2;
    private boolean mPaused;
    private RecyclerView mSearchHistory;
    private View mNoSearchHistory;
    private SearchHistoryAdapter mSearchHistoryAdapter;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_search2;
    }

    @Override
    protected void main() {
        mSearchHistory = (RecyclerView) findViewById(R.id.searchHistory);
        mSearchHistory.setHasFixedSize(true);
        mSearchHistory.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mSearchHistoryAdapter = new SearchHistoryAdapter(mContext);
        mSearchHistoryAdapter.setActionListener(new SearchHistoryAdapter.ActionListener() {
            @Override
            public void onItemClick(String s) {
                HttpUtil.cancel(HttpUtil.GET_USER_SEARCH);
                HttpUtil.cancel(HttpUtil.GET_VIDEO_SEARCH);
                if (mHandler != null) {
                    mHandler.removeMessages(WHAT);
                }
                mEditText.setText(s);
                mEditText.setSelection(s.length());
                if (mGroup.getVisibility() != View.VISIBLE) {
                    mGroup.setVisibility(View.VISIBLE);
                }
                imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                ((RefreshView) mViewPager.getChildAt(mViewPager.getCurrentItem())).initData();
            }

            @Override
            public void onListSizeChanged(int size) {
                if (size > 0) {
                    if (mNoSearchHistory.getVisibility() == View.VISIBLE) {
                        mNoSearchHistory.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (mNoSearchHistory.getVisibility() != View.VISIBLE) {
                        mNoSearchHistory.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onContentChanged(String searchHistory) {
                SharedPreferencesUtil.getInstance().saveSearchHistory(searchHistory);
            }
        });
        mSearchHistory.setAdapter(mSearchHistoryAdapter);
        mNoSearchHistory = findViewById(R.id.no_search_history);
        String s = SharedPreferencesUtil.getInstance().readSearchHistory();
        if (!TextUtils.isEmpty(s)) {
            String[] arr = s.split("/");
            List<String> list = new ArrayList<>();
            for (int i = 0, length = arr.length; i < length; i++) {
                list.add(arr[i]);
            }
            mSearchHistoryAdapter.insertList(list);
            mNoSearchHistory.setVisibility(View.INVISIBLE);
        }
        mGroup = findViewById(R.id.group);
        mIndicator = (ViewPagerIndicator) findViewById(R.id.indicator);
        mIndicator.setVisibleChildCount(2);
        mIndicator.setTitles(new String[]{WordUtil.getString(R.string.user), WordUtil.getString(R.string.video)});
        mIndicator.setChangeSize(false);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setOffscreenPageLimit(1);
        mIndicator.setViewPager(mViewPager);
        mViewList = new ArrayList<>();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mUserRefreshView = (RefreshView) inflater.inflate(R.layout.item_search, mViewPager, false);
        mUserRefreshView.setNoDataLayoutId(R.layout.view_no_data_search);
        mUserRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mUserRefreshView.setDataHelper(new RefreshView.DataHelper<SearchBean>() {

            @Override
            public RefreshAdapter<SearchBean> getAdapter() {
                if (mSearchUserAdapter == null) {
                    mSearchUserAdapter = new SearchAdapter(mContext);
                    mSearchUserAdapter.setOnItemClickListener(new OnItemClickListener<SearchBean>() {
                        @Override
                        public void onItemClick(SearchBean bean, int position) {
                            imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                            OtherUserActivity.forwardOtherUser(mContext, bean.getId());
                        }
                    });
                }
                return mSearchUserAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                if (p == 1) {
                    String key = mEditText.getText().toString().trim();
                    if (!TextUtils.isEmpty(key)) {
                        if (!key.equals(mLastKey1)) {
                            HttpUtil.getUserSearch(key, p, callback);
                            mLastKey1 = key;
                        }
                    }
                } else {
                    HttpUtil.getUserSearch(mLastKey1, p, callback);
                }
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
                    mUserRefreshView.setLoadMoreEnable(false);
                } else {
                    mUserRefreshView.setLoadMoreEnable(true);
                }
            }
        });
        mVideoRefreshView = (RefreshView) inflater.inflate(R.layout.item_search, mViewPager, false);
        mVideoRefreshView.setNoDataLayoutId(R.layout.view_no_data_search);
        mVideoRefreshView.setLayoutManager(new GridLayoutManager(mContext, 2, GridLayoutManager.VERTICAL, false));
        ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 2, 2);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        mVideoRefreshView.setItemDecoration(decoration);
        mVideoRefreshView.setDataHelper(new RefreshView.DataHelper<VideoBean>() {

            @Override
            public RefreshAdapter<VideoBean> getAdapter() {
                if (mVideoAdapter == null) {
                    mVideoAdapter = new FollowVideoAdapter(mContext);
                    mVideoAdapter.setOnItemClickListener(SearchActivity2.this);
                }
                return mVideoAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                if (p == 1) {
                    String key = mEditText.getText().toString().trim();
                    if (!TextUtils.isEmpty(key)) {
                        if (!key.equals(mLastKey2)) {
                            HttpUtil.getVideoSearch(key, p, callback);
                            mLastKey2 = key;
                        }
                    }
                } else {
                    HttpUtil.getVideoSearch(mLastKey2, p, callback);
                }
            }

            @Override
            public List<VideoBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), VideoBean.class);
            }

            @Override
            public void onRefresh(List<VideoBean> list) {
                VideoStorge.getInstance().put(Constants.VIDEO_SEARCH, list);
            }

            @Override
            public void onNoData(boolean noData) {

            }

            @Override
            public void onLoadDataCompleted(int dataCount) {
                if (dataCount < 20) {
                    mVideoRefreshView.setLoadMoreEnable(false);
                } else {
                    mVideoRefreshView.setLoadMoreEnable(true);
                }
            }
        });
        mViewList.add(mUserRefreshView);
        mViewList.add(mVideoRefreshView);
        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return mViewList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View v = mViewList.get(position);
                container.addView(v);
                return v;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mViewList.get(position));
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ((RefreshView) mViewPager.getChildAt(position)).initData();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mEditText = (EditText) findViewById(R.id.search_input);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    HttpUtil.cancel(HttpUtil.GET_USER_SEARCH);
                    HttpUtil.cancel(HttpUtil.GET_VIDEO_SEARCH);
                    if (mHandler != null) {
                        mHandler.removeMessages(WHAT);
                    }
                    String key = mEditText.getText().toString().trim();
                    if (!TextUtils.isEmpty(key)) {
                        if (mGroup.getVisibility() != View.VISIBLE) {
                            mGroup.setVisibility(View.VISIBLE);
                        }
                        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                        ((RefreshView) mViewPager.getChildAt(mViewPager.getCurrentItem())).initData();
                        mSearchHistoryAdapter.insertItem(key);
                    } else {
                        ToastUtil.show(WordUtil.getString(R.string.please_input_content));
                    }
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
                HttpUtil.cancel(HttpUtil.GET_VIDEO_SEARCH);
                if (!TextUtils.isEmpty(s)) {
                    if (mHandler != null) {
                        mHandler.removeMessages(WHAT);
                        mHandler.sendEmptyMessageDelayed(WHAT, 500);
                    }
                } else {
                    if (mGroup.getVisibility() == View.VISIBLE) {
                        mGroup.setVisibility(View.INVISIBLE);
                    }
                    if (mSearchUserAdapter != null) {
                        mSearchUserAdapter.clearData();
                    }
                    if (mVideoAdapter != null) {
                        mVideoAdapter.clearData();
                    }
                    mUserRefreshView.showLoading();
                    mVideoRefreshView.showLoading();
                    if (mViewPager.getCurrentItem() > 0) {
                        mViewPager.setCurrentItem(0, false);
                    }
                    mLastKey1 = null;
                    mLastKey2 = null;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        findViewById(R.id.btn_clear).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_search_history_clear).setOnClickListener(this);
        EventBus.getDefault().register(this);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                HttpUtil.cancel(HttpUtil.GET_USER_SEARCH);
                HttpUtil.cancel(HttpUtil.GET_VIDEO_SEARCH);
                String s = mEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(s)) {
                    if (mGroup.getVisibility() != View.VISIBLE) {
                        mGroup.setVisibility(View.VISIBLE);
                    }
                    ((RefreshView) mViewPager.getChildAt(mViewPager.getCurrentItem())).initData();
                } else {
                    if (mGroup.getVisibility() == View.VISIBLE) {
                        mGroup.setVisibility(View.INVISIBLE);
                    }
                }
            }
        };
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mEditText != null) {
                    imm.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
                    mEditText.requestFocus();
                }
            }
        }, 200);
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
            case R.id.btn_search_history_clear:
                clearSearchHistory();
                break;
        }
    }

    /**
     * 清空搜索历史
     */
    private void clearSearchHistory() {
        if (mSearchHistoryAdapter.clear()) {
            SharedPreferencesUtil.getInstance().saveSearchHistory("");
        }
    }

    private void clear() {
        String s = mEditText.getText().toString();
        if (!"".equals(s)) {
            mEditText.setText("");
            imm.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
            mEditText.requestFocus();
        }
    }

    @Override
    public void onBackPressed() {
        HttpUtil.cancel(HttpUtil.GET_USER_SEARCH);
        HttpUtil.cancel(HttpUtil.GET_VIDEO_SEARCH);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        super.onBackPressed();
    }


    @Override
    protected void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        EventBus.getDefault().unregister(this);
        VideoStorge.getInstance().remove(Constants.VIDEO_SEARCH);
        if (mUserRefreshView != null) {
            mUserRefreshView.setDataHelper(null);
        }
        if (mVideoRefreshView != null) {
            mVideoRefreshView.setDataHelper(null);
        }
        if (mSearchUserAdapter != null) {
            mSearchUserAdapter.setOnItemClickListener(null);
        }
        if (mVideoAdapter != null) {
            mVideoAdapter.setOnItemClickListener(null);
        }
        if (mViewPager != null) {
            mViewPager.clearOnPageChangeListeners();
        }
        super.onDestroy();
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFollowEvent(FollowEvent e) {
        if (mPaused && mSearchUserAdapter != null) {
            mSearchUserAdapter.updateItem(e.getTouid(), e.getIsAttention());
        }
    }

    @Override
    public void onItemClick(VideoBean bean, int position) {
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        if (mVideoRefreshView != null && bean != null && bean.getUserinfo() != null) {
            VideoPlayActivity.forwardVideoPlay(mContext, Constants.VIDEO_SEARCH, position, mVideoRefreshView.getPage(), bean.getUserinfo(), bean.getIsattent());
        }
    }
}
