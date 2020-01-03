package com.Tata.video.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.Tata.video.AppConfig;
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.adapter.CommentReplyAdapter;
import com.Tata.video.bean.CommentBean;
import com.Tata.video.bean.UserBean;
import com.Tata.video.custom.AtEditText;
import com.Tata.video.custom.RefreshLayout;
import com.Tata.video.event.ReplyCommentEvent;
import com.Tata.video.event.VisibleHeightEvent;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.interfaces.OnItemClickListener;
import com.Tata.video.presenter.GlobalLayoutPresenter;
import com.Tata.video.utils.ScreenDimenUtil;
import com.Tata.video.utils.ToastUtil;
import com.Tata.video.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2017/9/7.
 */

public class ReplyActivity extends AbsActivity implements RefreshLayout.OnRefreshListener, View.OnClickListener {

    private View mRootView;
    private RefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private View mLoading;
    private AtEditText mAtEditText;
    private CommentBean mCommentBean;
    private CommentReplyAdapter mAdapter;
    private int mPage = 1;
    private InputMethodManager imm;
    private UserBean mCurReplyUser;
    private String mParentId = "0";
    private String mCurCommentId = "0";
    private String mVideoId;
    private String mUid;
    private GlobalLayoutPresenter mPresenter;
    private boolean mPaused;
    private int mOriginalHeight;
    private int mContentHeight;
    private int mStatusBarHeight;
    private int mCurHeight;
    private String mReplyString;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_reply;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.watch_reply));
        initView();
        initData();
    }

    private void initView() {
        mRootView = findViewById(R.id.root);
        mRefreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setScorllView(mRecyclerView);
        mLoading = findViewById(R.id.loading_reply);
        mAtEditText = (AtEditText) findViewById(R.id.comment_edit);
        mAtEditText.setActionListener(new AtEditText.ActionListener() {
            @Override
            public void onAtClick() {
                forwardAtFriendsActivity();
            }

            @Override
            public void onContainsUid() {
                ToastUtil.show(WordUtil.getString(R.string.you_have_at_him));
            }

            @Override
            public void onContainsName() {
                ToastUtil.show(WordUtil.getString(R.string.you_have_at_him_2));
            }
        });
        mAtEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendComment();
                    return true;
                }
                return false;
            }
        });
        findViewById(R.id.btn_at).setOnClickListener(this);
        //findViewById(R.id.btn_face).setOnClickListener(this);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        Intent intent = getIntent();
        mUid = AppConfig.getInstance().getUid();
        mCommentBean = intent.getParcelableExtra(Constants.COMMENT_BEAN);
        mVideoId = intent.getStringExtra(Constants.VIDEO_ID);
        mCurReplyUser = mCommentBean.getUserinfo();
        mCurCommentId = mCommentBean.getCommentid();
        mParentId = mCommentBean.getId();
        mAtEditText.setHint(getResources().getString(R.string.reply) + mCurReplyUser.getUser_nicename());
        mPresenter = new GlobalLayoutPresenter(this, mRootView);
        mPresenter.addLayoutListener();
        ScreenDimenUtil screenDimenUtil = ScreenDimenUtil.getInstance();
        mOriginalHeight = screenDimenUtil.getScreenHeight();
        mContentHeight = screenDimenUtil.getContentHeight();
        mStatusBarHeight = screenDimenUtil.getStatusBarHeight();
        mCurHeight = mOriginalHeight;
        EventBus.getDefault().register(this);
        mReplyString = WordUtil.getString(R.string.reply);
    }

    private void initData() {
        HttpUtil.getReplys(mCommentBean.getId(), String.valueOf(mPage), mCallback);
    }

    private HttpCallback mCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                if (info.length > 0) {
                    List<CommentBean> list = JSON.parseArray(Arrays.toString(info), CommentBean.class);
                    if (mAdapter == null) {
                        mAdapter = new CommentReplyAdapter(ReplyActivity.this, mCommentBean, list);
                        mAdapter.setOnItemClickListener(new OnItemClickListener<CommentBean>() {
                            @Override
                            public void onItemClick(CommentBean bean, int position) {
                                if (mUid.equals(bean.getUid())) {
                                    return;
                                }
                                mCurReplyUser = bean.getUserinfo();
                                mCurCommentId = bean.getCommentid();
                                mParentId = bean.getId();
                                mAtEditText.setHint(mReplyString + mCurReplyUser.getUser_nicename());
                                mAtEditText.requestFocus();
                                imm.showSoftInput(mAtEditText, InputMethodManager.SHOW_FORCED);
                            }
                        });
                        mAdapter.setRefreshLayout(mRefreshLayout);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                }

            }
        }

        @Override
        public void onFinish() {
            if (mLoading.getVisibility() == View.VISIBLE) {
                mLoading.setVisibility(View.INVISIBLE);
            }
        }
    };


    private HttpCallback mLoadMoreCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                if (info.length > 0) {
                    List<CommentBean> list = JSON.parseArray(Arrays.toString(info), CommentBean.class);
                    mAdapter.insertList(list);
                    if (list.size() > 0) {

                    }
                } else {
                    ToastUtil.show(WordUtil.getString(R.string.no_more_data));
                    mPage--;
                }
            } else {
                ToastUtil.show(WordUtil.getString(R.string.no_more_data));
                mPage--;
            }

        }

        @Override
        public void onFinish() {
            mRefreshLayout.completeLoadMore();
        }
    };


    @Override
    public void onRefresh() {
        mPage = 1;
        initData();
    }

    @Override
    public void onLoadMore() {
        mPage++;
        HttpUtil.getReplys(mCommentBean.getId(), String.valueOf(mPage), mLoadMoreCallback);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                finish();
                break;
            case R.id.btn_at:
                forwardAtFriendsActivity();
                break;
//            case R.id.btn_face:
//                break;

        }
    }

    /**
     * 召唤好友
     */
    private void forwardAtFriendsActivity() {
        startActivityForResult(new Intent(mContext, AtFriendsActivity.class), Constants.AT_FRIENDS_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == Constants.AT_FRIENDS_CODE && resultCode == RESULT_OK) {
            String uid = intent.getStringExtra(Constants.UID);
            String username = intent.getStringExtra(Constants.USER_NICE_NAME);
            if(mAtEditText!=null){
                mAtEditText.addAtSpan(uid, username);
            }
        }
    }


    private void sendComment() {
        if (!AppConfig.getInstance().isLogin()) {
            ToastUtil.show(WordUtil.getString(R.string.please_login));
            return;
        }
        if (mUid.equals(mCurReplyUser.getId())) {
            ToastUtil.show(WordUtil.getString(R.string.cannot_reply_self));
            return;
        }
        String content = mAtEditText.getText().toString().trim();
        if ("".equals(content)) {
            ToastUtil.show(WordUtil.getString(R.string.comment_tips_null));
        }
        imm.hideSoftInputFromWindow(mAtEditText.getWindowToken(), 0); //强制隐藏键盘
        HttpUtil.setComment(mCurReplyUser.getId(), mVideoId, content, mCurCommentId, mParentId, mAtEditText.getAtUserInfo(), mSetCommentCallback);
        mAtEditText.setText("");
    }

    private HttpCallback mSetCommentCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                if (info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    EventBus.getDefault().post(new ReplyCommentEvent(
                            mCommentBean.getId(),
                            obj.getString("comments"),
                            obj.getIntValue("replys")));
                    ToastUtil.show(msg);
                    finish();
                }
            }

        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVisibleHeightEvent(VisibleHeightEvent e) {
        if (!mPaused && mRootView != null) {
            int visibleHeight = e.getVisibleHeight();
            if (visibleHeight >= mContentHeight) {
                mCurHeight = mOriginalHeight;
            } else {
                mCurHeight = visibleHeight + mStatusBarHeight;
            }
            ViewGroup.LayoutParams params = mRootView.getLayoutParams();
            params.height = mCurHeight;
            mRootView.requestLayout();
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
        HttpUtil.cancel(HttpUtil.GET_REPLYS);
        HttpUtil.cancel(HttpUtil.SET_COMMENT);
        HttpUtil.cancel(HttpUtil.SET_COMMENT_LIKE);
        if (mPresenter != null) {
            mPresenter.removeLayoutListener();
            mPresenter.release();
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
