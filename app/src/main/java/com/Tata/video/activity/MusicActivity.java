package com.Tata.video.activity;

import android.app.Dialog;
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
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.adapter.MusicAdapter;
import com.Tata.video.bean.MusicBean;
import com.Tata.video.custom.RefreshAdapter;
import com.Tata.video.custom.RefreshView;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.utils.DialogUitl;
import com.Tata.video.utils.L;
import com.Tata.video.utils.MusicUtil;
import com.Tata.video.utils.ToastUtil;
import com.Tata.video.utils.WordUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/6/20.
 */

public class MusicActivity extends AbsActivity implements  View.OnClickListener {

    private RefreshView mRefreshView;
    private MusicAdapter mAdapter;
    private InputMethodManager imm;
    private EditText mEditText;
    private List<MusicBean> mTopList;
    private int mFrom;
    private Handler mHandler;
    private static final int WHAT = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_music;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.choose_music));
        View btnRecord = findViewById(R.id.btn_start_record);
        mFrom = getIntent().getIntExtra(Constants.FROM, 0);
        if (mFrom == Constants.VIDEO_FROM_EDIT) {
            btnRecord.setVisibility(View.INVISIBLE);
        } else {
            btnRecord.setOnClickListener(this);
        }
        mRefreshView = (RefreshView) findViewById(R.id.refreshView);
        mRefreshView.setNoDataLayoutId(R.layout.view_no_data_search);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new RefreshView.DataHelper<MusicBean>() {
            @Override
            public RefreshAdapter<MusicBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new MusicAdapter(mContext);
                   // mAdapter.setActionListener(MusicActivity.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                searchMusic(p, callback);
            }

            @Override
            public List<MusicBean> processData(String[] info) {
                List<MusicBean> list = JSON.parseArray(Arrays.toString(info), MusicBean.class);
                if (list.size() > 0) {
                    mRefreshView.setLoadMoreEnable(true);
                }
                return list;
            }

            @Override
            public void onRefresh(List<MusicBean> list) {
            }
            @Override
            public void onNoData(boolean noData) {

            }

            @Override
            public void onLoadDataCompleted(int dataCount) {

            }
        });
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mEditText = (EditText) findViewById(R.id.search_input);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    HttpUtil.cancel(HttpUtil.GET_MUSIC_LIST);
                    HttpUtil.cancel(HttpUtil.SEARCH_MUSIC);
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
                HttpUtil.cancel(HttpUtil.GET_MUSIC_LIST);
                HttpUtil.cancel(HttpUtil.SEARCH_MUSIC);
                if (mHandler != null) {
                    mHandler.removeMessages(WHAT);
                    if (!TextUtils.isEmpty(s)) {
                        mHandler.sendEmptyMessageDelayed(WHAT, 500);
                    } else {
                        showTopList();
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
                if (mRefreshView != null) {
                    mRefreshView.initData();
                }
            }
        };
        getTopList();
    }

    private void getTopList() {
        HttpUtil.getMusicList("",1,new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    mTopList = JSON.parseArray(Arrays.toString(info), MusicBean.class);
                    mRefreshView.refreshLocalData(mTopList);
                }
            }
        });
    }

    public void onUseClick(final MusicBean bean) {
        String path = MusicUtil.getInstance().getMusicPath(bean.getId());
        if (TextUtils.isEmpty(path)) {
            final Dialog loadingMusicDialog = DialogUitl.loadingDialog(mContext, WordUtil.getString(R.string.downloading));
            loadingMusicDialog.show();
            MusicUtil.getInstance().downloadMusic(bean, new MusicUtil.MusicDownLoadCallback() {
                @Override
                public void onDownloadSuccess(String filePath) {
                    loadingMusicDialog.dismiss();
                    L.e("#下载音乐----成功--->" + filePath);
                    bean.setLocalPath(filePath);
                    forwardVideoRecord(bean);
                }

                @Override
                public void onProgress(int progress) {
                    L.e("#下载音乐----progress--->" + progress);
                }
            });
        } else {
            bean.setLocalPath(path);
            forwardVideoRecord(bean);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_record:
                forwardVideoRecord(null);
                break;
            case R.id.btn_clear:
                clear();
                break;
        }
    }

    private void clear() {
        String s=mEditText.getText().toString();
        if(TextUtils.isEmpty(s)){
            return;
        }
        mEditText.setText("");
        imm.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
        mEditText.requestFocus();
        showTopList();
    }

    private void showTopList() {
        if (mTopList != null) {
            mRefreshView.refreshLocalData(mTopList);
        } else {
            getTopList();
        }
        mRefreshView.setLoadMoreEnable(false);
    }

    /**
     * 前往视频录制
     */
    private void forwardVideoRecord(MusicBean bean) {
        if (mFrom == Constants.VIDEO_FROM_EDIT) {
            Intent intent = new Intent();
            intent.putExtra(Constants.MUSIC_BEAN, bean);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Intent intent = new Intent(mContext, VideoRecordActivity.class);
            intent.putExtra(Constants.MUSIC_BEAN, bean);
            startActivity(intent);
            finish();
        }
    }

    private void searchMusic(int p, HttpCallback callback) {
        String key = mEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(key)) {
            HttpUtil.searchMusic(key, p, callback);
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
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.GET_MUSIC_LIST);
        HttpUtil.cancel(HttpUtil.SEARCH_MUSIC);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (mRefreshView != null) {
            mRefreshView.setDataHelper(null);
        }
        if (mAdapter != null) {
            mAdapter.setActionListener(null);
        }
        super.onDestroy();
    }
}
