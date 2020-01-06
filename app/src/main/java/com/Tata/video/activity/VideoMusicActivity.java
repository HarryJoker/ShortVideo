//package com.Tata.video.activity;
//
//import android.animation.Animator;
//import android.animation.AnimatorListenerAdapter;
//import android.animation.ObjectAnimator;
//import android.animation.TimeInterpolator;
//import android.app.Dialog;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Handler;
//import android.os.Message;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.text.Editable;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.util.SparseArray;
//import android.view.KeyEvent;
//import android.view.View;
//import android.view.animation.AccelerateDecelerateInterpolator;
//import android.view.inputmethod.EditorInfo;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.EditText;
//import android.widget.TextView;
//
//import com.alibaba.fastjson.JSON;
//import com.Tata.video.Constants;
//import com.Tata.video.R;
//import com.Tata.video.adapter.MusicAdapter;
//import com.Tata.video.adapter.MusicClassAdapter;
//import com.Tata.video.bean.MusicBean;
//import com.Tata.video.bean.MusicClassBean;
//import com.Tata.video.custom.RefreshAdapter;
//import com.Tata.video.custom.RefreshView;
//import com.Tata.video.http.HttpCallback;
//import com.Tata.video.http.HttpUtil;
//import com.Tata.video.interfaces.OnItemClickListener;
//import com.Tata.video.utils.DialogUitl;
//import com.Tata.video.utils.L;
//import com.Tata.video.utils.MusicMediaPlayerUtil;
//import com.Tata.video.utils.MusicUtil;
//import com.Tata.video.utils.ScreenDimenUtil;
//import com.Tata.video.utils.ToastUtil;
//import com.Tata.video.utils.WordUtil;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
///**
// * Created by cxf on 2018/7/26.
// */
//
//public class VideoMusicActivity extends AudioAbsActivity implements OnItemClickListener<MusicClassBean>, MusicAdapter.ActionListener, View.OnClickListener {
//
//    private RecyclerView mMusicClass;
//    private RefreshView mSearchRefreshView;
//    private RefreshView mHotRefreshView;
//    private RefreshView mCollectRefreshView;
//    private RefreshView mClassRefreshView;
//    private RefreshView mMyMusicRefreshView;
//    private MusicAdapter mSearchAdapter;
//    private MusicAdapter mHotAdapter;
//    private MusicAdapter mCollectAdapter;
//    private MusicAdapter mClassAdapter;
//    private MusicAdapter mMyMusicAdapter;
//    private List<MusicAdapter> mMusicAdapterList;
//    private int mFrom;
//    private InputMethodManager imm;
//    private EditText mEditText;
//    private Handler mHandler;
//    private static final int WHAT = 0;
//    private MusicMediaPlayerUtil mMusicMediaPlayerUtil;
//    private View mClassGroup;
//    private TextView mClassTitle;
//    private String mClassId;
//    private int mScreenWidth;
//    private ObjectAnimator mShowClassAnimator;
//    private ObjectAnimator mHideClassAnimator;
//    private SparseArray<View> mSparseArray;
//    private static final int HOT = 0;
//    private static final int COLLECT = 1;
//    private static final int MY = 2;
//    private int mCurKey = HOT;
//
//    @Override
//    protected int getLayoutId() {
//        return R.layout.activity_video_music_2;
//    }
//
//    @Override
//    protected void main() {
//        super.main();
//        setTitle(WordUtil.getString(R.string.choose_music));
//        View bottomGroup = findViewById(R.id.bottom_group);
//        mFrom = getIntent().getIntExtra(Constants.FROM, 0);
//        if (mFrom == Constants.VIDEO_FROM_EDIT) {
//            bottomGroup.setVisibility(View.GONE);
//        } else {
//            bottomGroup.findViewById(R.id.btn_link).setOnClickListener(this);
//            bottomGroup.findViewById(R.id.btn_start_record).setOnClickListener(this);
//        }
//        findViewById(R.id.btn_clear).setOnClickListener(this);
//        findViewById(R.id.btn_hot_music).setOnClickListener(this);
//        findViewById(R.id.btn_my_collect).setOnClickListener(this);
//        findViewById(R.id.btn_class_back).setOnClickListener(this);
//        findViewById(R.id.btn_my_music).setOnClickListener(this);
//        findViewById(R.id.btn_upload_local_music).setOnClickListener(this);
//        mMusicAdapterList = new ArrayList<>();
//        mClassTitle = (TextView) findViewById(R.id.classTitle);
//        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        mEditText = (EditText) findViewById(R.id.search_input);
//        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                    HttpUtil.cancel(HttpUtil.SEARCH_MUSIC);
//                    if (mHandler != null) {
//                        mHandler.removeMessages(WHAT);
//                    }
//                    imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
//                    if (mSearchRefreshView != null) {
//                        if (mSearchRefreshView.getVisibility() != View.VISIBLE) {
//                            mSearchRefreshView.setVisibility(View.VISIBLE);
//                        }
//                        mSearchRefreshView.initData();
//                    }
//                    return true;
//                }
//                return false;
//            }
//        });
//        mEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                HttpUtil.cancel(HttpUtil.SEARCH_MUSIC);
//                if (!TextUtils.isEmpty(s)) {
//                    if (mHandler != null) {
//                        mHandler.removeMessages(WHAT);
//                        mHandler.sendEmptyMessageDelayed(WHAT, 500);
//                    }
//                } else {
//                    doStopMusic();
//                    if (mSearchAdapter != null) {
//                        mSearchAdapter.clearData();
//                    }
//                    if (mSearchRefreshView != null && mSearchRefreshView.getVisibility() == View.VISIBLE) {
//                        mSearchRefreshView.setVisibility(View.INVISIBLE);
//                    }
//                }
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//        mHandler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                if (mEditText != null) {
//                    String s = mEditText.getText().toString().trim();
//                    if (!TextUtils.isEmpty(s)) {
//                        doStopMusic();
//                        if (mSearchRefreshView != null) {
//                            if (mSearchRefreshView.getVisibility() != View.VISIBLE) {
//                                mSearchRefreshView.setVisibility(View.VISIBLE);
//                            }
//                            mSearchRefreshView.initData();
//                        }
//                    }
//                }
//            }
//        };
//        mSearchRefreshView = (RefreshView) findViewById(R.id.searchRefreshView);
//        mSearchRefreshView.setNoDataLayoutId(R.layout.view_no_data_search);
//        mSearchRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
//        mSearchRefreshView.setDataHelper(new RefreshView.DataHelper<MusicBean>() {
//            @Override
//            public RefreshAdapter<MusicBean> getAdapter() {
//                if (mSearchAdapter == null) {
//                    mSearchAdapter = new MusicAdapter(mContext);
//                    mSearchAdapter.setActionListener(VideoMusicActivity.this);
//                    mSearchAdapter.setRefreshView(mSearchRefreshView);
//                    mMusicAdapterList.add(mSearchAdapter);
//                }
//                return mSearchAdapter;
//            }
//
//            @Override
//            public void loadData(int p, HttpCallback callback) {
//                searchMusic(p, callback);
//            }
//
//            @Override
//            public List<MusicBean> processData(String[] info) {
//                return JSON.parseArray(Arrays.toString(info), MusicBean.class);
//            }
//
//            @Override
//            public void onRefresh(List<MusicBean> list) {
//
//            }
//
//            @Override
//            public void onNoData(boolean noData) {
//
//            }
//
//            @Override
//            public void onLoadDataCompleted(int dataCount) {
//                if (dataCount < 20) {
//                    mSearchRefreshView.setLoadMoreEnable(false);
//                } else {
//                    mSearchRefreshView.setLoadMoreEnable(true);
//                }
//            }
//        });
//        mHotRefreshView = (RefreshView) findViewById(R.id.hotRefreshView);
//        mHotRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
//        mHotRefreshView.setDataHelper(new RefreshView.DataHelper<MusicBean>() {
//            @Override
//            public RefreshAdapter<MusicBean> getAdapter() {
//                if (mHotAdapter == null) {
//                    mHotAdapter = new MusicAdapter(mContext);
//                    mHotAdapter.setActionListener(VideoMusicActivity.this);
//                    mHotAdapter.setRefreshView(mHotRefreshView);
//                    mMusicAdapterList.add(mHotAdapter);
//                }
//                return mHotAdapter;
//            }
//
//            @Override
//            public void loadData(int p, HttpCallback callback) {
//                HttpUtil.getHotMusicList(callback);
//            }
//
//            @Override
//            public List<MusicBean> processData(String[] info) {
//                return JSON.parseArray(Arrays.toString(info), MusicBean.class);
//            }
//
//            @Override
//            public void onRefresh(List<MusicBean> list) {
//
//            }
//
//            @Override
//            public void onNoData(boolean noData) {
//
//            }
//
//            @Override
//            public void onLoadDataCompleted(int dataCount) {
//
//            }
//        });
//        mCollectRefreshView = (RefreshView) findViewById(R.id.collectRefreshView);
//        mCollectRefreshView.setNoDataLayoutId(R.layout.view_no_data_collect);
//        mCollectRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
//        mCollectRefreshView.setDataHelper(new RefreshView.DataHelper<MusicBean>() {
//            @Override
//            public RefreshAdapter<MusicBean> getAdapter() {
//                if (mCollectAdapter == null) {
//                    mCollectAdapter = new MusicAdapter(mContext);
//                    mCollectAdapter.setActionListener(VideoMusicActivity.this);
//                    mCollectAdapter.setRefreshView(mCollectRefreshView);
//                    mMusicAdapterList.add(mCollectAdapter);
//                }
//                return mCollectAdapter;
//            }
//
//            @Override
//            public void loadData(int p, HttpCallback callback) {
//                HttpUtil.getMusicCollectList(p, callback);
//            }
//
//            @Override
//            public List<MusicBean> processData(String[] info) {
//                return JSON.parseArray(Arrays.toString(info), MusicBean.class);
//            }
//
//            @Override
//            public void onRefresh(List<MusicBean> list) {
//                onStopMusic();
//            }
//
//            @Override
//            public void onNoData(boolean noData) {
//
//            }
//
//            @Override
//            public void onLoadDataCompleted(int dataCount) {
//                if (dataCount < 20) {
//                    mCollectRefreshView.setLoadMoreEnable(false);
//                } else {
//                    mCollectRefreshView.setLoadMoreEnable(true);
//                }
//            }
//        });
//        mMyMusicRefreshView = (RefreshView) findViewById(R.id.myMusicRefreshView);
//        mMyMusicRefreshView.setNoDataLayoutId(R.layout.view_no_data_my_music);
//        mMyMusicRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
//        mMyMusicRefreshView.setDataHelper(new RefreshView.DataHelper<MusicBean>() {
//            @Override
//            public RefreshAdapter<MusicBean> getAdapter() {
//                if (mMyMusicAdapter == null) {
//                    mMyMusicAdapter = new MusicAdapter(mContext);
//                    mMyMusicAdapter.setActionListener(VideoMusicActivity.this);
//                    mMyMusicAdapter.setRefreshView(mMyMusicRefreshView);
//                    mMusicAdapterList.add(mMyMusicAdapter);
//                }
//                return mMyMusicAdapter;
//            }
//
//            @Override
//            public void loadData(int p, HttpCallback callback) {
//                HttpUtil.getMyMusic(p, callback);
//            }
//
//            @Override
//            public List<MusicBean> processData(String[] info) {
//                return JSON.parseArray(Arrays.toString(info), MusicBean.class);
//            }
//
//            @Override
//            public void onRefresh(List<MusicBean> list) {
//                onStopMusic();
//            }
//
//            @Override
//            public void onNoData(boolean noData) {
//
//            }
//
//            @Override
//            public void onLoadDataCompleted(int dataCount) {
//                if (dataCount < 20) {
//                    mMyMusicRefreshView.setLoadMoreEnable(false);
//                } else {
//                    mMyMusicRefreshView.setLoadMoreEnable(true);
//                }
//            }
//        });
//        mScreenWidth = ScreenDimenUtil.getInstance().getScreenWdith();
//        mClassGroup = findViewById(R.id.classGroup);
//        mClassGroup.post(new Runnable() {
//            @Override
//            public void run() {
//                mClassGroup.setX(mScreenWidth);
//            }
//        });
//        mClassRefreshView = (RefreshView) findViewById(R.id.classRefreshView);
//        mClassRefreshView.setNoDataLayoutId(R.layout.view_no_data_default);
//        mClassRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
//        mClassRefreshView.setDataHelper(new RefreshView.DataHelper<MusicBean>() {
//            @Override
//            public RefreshAdapter<MusicBean> getAdapter() {
//                if (mClassAdapter == null) {
//                    mClassAdapter = new MusicAdapter(mContext);
//                    mClassAdapter.setActionListener(VideoMusicActivity.this);
//                    mClassAdapter.setRefreshView(mClassRefreshView);
//                    mMusicAdapterList.add(mClassAdapter);
//                }
//                return mClassAdapter;
//            }
//
//            @Override
//            public void loadData(int p, HttpCallback callback) {
//                HttpUtil.getMusicList(mClassId, p, callback);
//            }
//
//            @Override
//            public List<MusicBean> processData(String[] info) {
//                return JSON.parseArray(Arrays.toString(info), MusicBean.class);
//            }
//
//            @Override
//            public void onRefresh(List<MusicBean> list) {
//
//            }
//
//            @Override
//            public void onNoData(boolean noData) {
//
//            }
//
//            @Override
//            public void onLoadDataCompleted(int dataCount) {
//                if (dataCount < 20) {
//                    mClassRefreshView.setLoadMoreEnable(false);
//                } else {
//                    mClassRefreshView.setLoadMoreEnable(true);
//                }
//            }
//        });
//        TimeInterpolator interpolator = new AccelerateDecelerateInterpolator();
//        mShowClassAnimator = ObjectAnimator.ofFloat(mClassGroup, "x", 0);
//        mShowClassAnimator.setDuration(250);
//        mShowClassAnimator.setInterpolator(interpolator);
//        mShowClassAnimator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                if (mClassRefreshView != null) {
//                    mClassRefreshView.initData();
//                }
//            }
//        });
//        mHideClassAnimator = ObjectAnimator.ofFloat(mClassGroup, "x", mScreenWidth);
//        mHideClassAnimator.setDuration(250);
//        mHideClassAnimator.setInterpolator(interpolator);
//        mHideClassAnimator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                if (mClassAdapter != null) {
//                    mClassAdapter.clearData();
//                }
//            }
//        });
//        mMusicMediaPlayerUtil = new MusicMediaPlayerUtil();
//        mMusicClass = (RecyclerView) findViewById(R.id.musicClass);
//        mMusicClass.setHasFixedSize(true);
//        mMusicClass.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
//        mSparseArray = new SparseArray<>();
//        mSparseArray.put(HOT, mHotRefreshView);
//        mSparseArray.put(COLLECT, mCollectRefreshView);
//        mSparseArray.put(MY, findViewById(R.id.my_music_group));
//        mHotRefreshView.initData();
//        getMusicClass();
//    }
//
//    /**
//     * 获取音乐分类列表
//     */
//    private void getMusicClass() {
//        HttpUtil.getMusicClassList(new HttpCallback() {
//            @Override
//            public void onSuccess(int code, String msg, String[] info) {
//                if (code == 0 && info.length > 0) {
//                    List<MusicClassBean> list = JSON.parseArray(Arrays.toString(info), MusicClassBean.class);
//                    MusicClassAdapter musicClassAdapter = new MusicClassAdapter(mContext, list);
//                    musicClassAdapter.setOnItemClickListener(VideoMusicActivity.this);
//                    mMusicClass.setAdapter(musicClassAdapter);
//                }
//            }
//        });
//    }
//
//    /**
//     * 点击音乐分类
//     */
//    @Override
//    public void onItemClick(MusicClassBean bean, int position) {
//        doStopMusic();
//        mClassTitle.setText(bean.getTitle());
//        mClassId = bean.getId();
//        if (mClassGroup.getVisibility() != View.VISIBLE) {
//            mClassGroup.setVisibility(View.VISIBLE);
//        }
//        mShowClassAnimator.start();
//    }
//
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btn_start_record:
//                forwardVideoRecord(null);
//                break;
//            case R.id.btn_clear:
//                clear();
//                break;
//            case R.id.btn_hot_music:
//                toggleHotMusic(HOT);
//                break;
//            case R.id.btn_my_collect:
//                toggleHotMusic(COLLECT);
//                break;
//            case R.id.btn_class_back:
//                hideClassGroup();
//                break;
//            case R.id.btn_my_music:
//                toggleHotMusic(MY);
//                break;
//            case R.id.btn_upload_local_music:
//                forwardLocalMusic();
//                break;
//            case R.id.btn_link:
//                forwardLinkActivity();
//                break;
//        }
//    }
//
//    private void forwardLinkActivity(){
//        startActivity(new Intent(mContext,LinkActivity.class));
//    }
//
//    private void forwardLocalMusic(){
//        startActivity(new Intent(mContext,ChooseMusicActivity.class));
//    }
//
//    private void toggleHotMusic(int key) {
//        if (mCurKey == key) {
//            return;
//        }
//        doStopMusic();
//        View v1 = mSparseArray.get(mCurKey);
//        if (v1.getVisibility() == View.VISIBLE) {
//            v1.setVisibility(View.INVISIBLE);
//        }
//        View v2 = mSparseArray.get(key);
//        if (v2.getVisibility() != View.VISIBLE) {
//            v2.setVisibility(View.VISIBLE);
//        }
//        mCurKey = key;
//        if (key == COLLECT) {
//            mCollectRefreshView.initData();
//        } else if (key == MY) {
//            mMyMusicRefreshView.initData();
//        }
//    }
//
//    /**
//     * 前往视频录制
//     */
//    private void forwardVideoRecord(MusicBean bean) {
//        if (mFrom == Constants.VIDEO_FROM_EDIT) {
//            Intent intent = new Intent();
//            intent.putExtra(Constants.MUSIC_BEAN, bean);
//            setResult(RESULT_OK, intent);
//            finish();
//        } else {
//            Intent intent = new Intent(mContext, VideoRecordActivity.class);
//            intent.putExtra(Constants.MUSIC_BEAN, bean);
//            startActivity(intent);
//        }
//    }
//
//    /**
//     * 清空输入框
//     */
//    private void clear() {
//        HttpUtil.cancel(HttpUtil.SEARCH_MUSIC);
//        String s = mEditText.getText().toString();
//        if (TextUtils.isEmpty(s)) {
//            return;
//        }
//        mEditText.setText("");
//        imm.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
//        mEditText.requestFocus();
//    }
//
//
//    /**
//     * 隐藏分类
//     */
//    private void hideClassGroup() {
//        doStopMusic();
//        mHideClassAnimator.start();
//    }
//
//    /**
//     * 搜索音乐
//     */
//    private void searchMusic(int p, HttpCallback callback) {
//        String key = mEditText.getText().toString().trim();
//        if (!TextUtils.isEmpty(key)) {
//            HttpUtil.searchMusic(key, p, callback);
//        } else {
//            ToastUtil.show(WordUtil.getString(R.string.please_input_content));
//        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        mShowClassAnimator.cancel();
//        mHideClassAnimator.cancel();
//        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
//        super.onBackPressed();
//    }
//
//    @Override
//    protected void onDestroy() {
//        HttpUtil.cancel(HttpUtil.GET_MUSIC_CLASS_LIST);
//        HttpUtil.cancel(HttpUtil.GET_MUSIC_COLLECT_LIST);
//        HttpUtil.cancel(HttpUtil.GET_HOT_MUSIC_LIST);
//        HttpUtil.cancel(HttpUtil.GET_MY_MUSIC);
//        HttpUtil.cancel(HttpUtil.GET_MUSIC_LIST);
//        HttpUtil.cancel(HttpUtil.SET_MUSIC_COLLECT);
//        HttpUtil.cancel(HttpUtil.SEARCH_MUSIC);
//        if (mHandler != null) {
//            mHandler.removeCallbacksAndMessages(null);
//            mHandler = null;
//        }
//        if (mSearchRefreshView != null) {
//            mSearchRefreshView.setDataHelper(null);
//        }
//        if (mSearchAdapter != null) {
//            mSearchAdapter.setActionListener(null);
//        }
//        if (mHotRefreshView != null) {
//            mHotRefreshView.setDataHelper(null);
//        }
//        if (mHotAdapter != null) {
//            mHotAdapter.setActionListener(null);
//        }
//        if (mCollectRefreshView != null) {
//            mCollectRefreshView.setDataHelper(null);
//        }
//        if (mCollectAdapter != null) {
//            mCollectAdapter.setActionListener(null);
//        }
//        if (mClassRefreshView != null) {
//            mClassRefreshView.setDataHelper(null);
//        }
//        if (mClassAdapter != null) {
//            mClassAdapter.setActionListener(null);
//        }
//        if (mMusicMediaPlayerUtil != null) {
//            mMusicMediaPlayerUtil.destroy();
//        }
//        super.onDestroy();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (mMusicMediaPlayerUtil != null) {
//            mMusicMediaPlayerUtil.pausePlay();
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (mMusicMediaPlayerUtil != null) {
//            mMusicMediaPlayerUtil.resumePlay();
//        }
//    }
//
//    /**
//     * 下载并播放音乐
//     */
//    @Override
//    public void onPlayMusic(final MusicAdapter adapter, final MusicBean bean, final int position) {
//        String path = MusicUtil.getInstance().getMusicPath(bean.getId());
//        if (TextUtils.isEmpty(path)) {
//            final Dialog loadingMusicDialog = DialogUitl.loadingDialog(mContext, WordUtil.getString(R.string.downloading));
//            loadingMusicDialog.show();
//            MusicUtil.getInstance().downloadMusic(bean, new MusicUtil.MusicDownLoadCallback() {
//                @Override
//                public void onDownloadSuccess(String filePath) {
//                    loadingMusicDialog.dismiss();
//                    L.e("#下载音乐----成功--->" + filePath);
//                    bean.setLocalPath(filePath);
//                    adapter.expand(position);
//                    if (mMusicMediaPlayerUtil != null) {
//                        mMusicMediaPlayerUtil.startPlay(filePath);
//                    }
//                }
//
//                @Override
//                public void onProgress(int progress) {
//                    L.e("#下载音乐----progress--->" + progress);
//                }
//            });
//        } else {
//            bean.setLocalPath(path);
//            adapter.expand(position);
//            if (mMusicMediaPlayerUtil != null) {
//                mMusicMediaPlayerUtil.startPlay(path);
//            }
//        }
//    }
//
//
//    /**
//     * 暂停音乐播放
//     */
//    @Override
//    public void onStopMusic() {
//        if (mMusicMediaPlayerUtil != null) {
//            mMusicMediaPlayerUtil.stopPlay();
//        }
//    }
//
//    /**
//     * 点击使用按钮
//     */
//    @Override
//    public void onUseClick(MusicBean bean) {
//        forwardVideoRecord(bean);
//    }
//
//    @Override
//    public void onCollect(MusicAdapter adapter, int musicId, int isCollect) {
//        for (MusicAdapter musicAdapter : mMusicAdapterList) {
//            if (musicAdapter != null) {
//                musicAdapter.collectChanged(adapter, musicId, isCollect);
//            }
//        }
//    }
//
//    /**
//     * 切换分类时候暂停音乐播放
//     */
//    private void doStopMusic() {
//        for (MusicAdapter adapter : mMusicAdapterList) {
//            if (adapter != null) {
//                adapter.collapse();
//            }
//        }
//        if (mMusicMediaPlayerUtil != null) {
//            mMusicMediaPlayerUtil.stopPlay();
//        }
//    }
//
//}
