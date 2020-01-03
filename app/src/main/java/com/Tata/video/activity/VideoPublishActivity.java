package com.Tata.video.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.Tata.video.AppConfig;
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.adapter.VideoShareAdapter;
import com.Tata.video.bean.ConfigBean;
import com.Tata.video.bean.ShareBean;
import com.Tata.video.bean.UserBean;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.interfaces.CommonCallback;
import com.Tata.video.interfaces.OnItemClickListener;
import com.Tata.video.upload.UploadStrategy;
import com.Tata.video.upload.VideoQnUpload;
import com.Tata.video.upload.VideoTxUpload;
import com.Tata.video.upload.VideoUploadBean;
import com.Tata.video.upload.VideoUploadManager;
import com.Tata.video.utils.DialogUitl;
import com.Tata.video.utils.SharedSdkUitl;
import com.Tata.video.utils.ToastUtil;
import com.Tata.video.utils.WordUtil;

import java.io.File;
import java.util.List;

import cn.sharesdk.framework.Platform;

/**
 * Created by cxf on 2018/6/26.
 */

public class VideoPublishActivity extends AbsActivity implements OnItemClickListener<ShareBean>, View.OnClickListener {

    private TXCloudVideoView mVideoView;
    private TXLivePlayer mPlayer;
    private String mVideoPath;
    private String mCoverPath;
    private VideoUploadBean mVideoUploadBean;
    private boolean mPaused;
    private boolean mStartPlay;
    private RecyclerView mRecyclerView;
    private TextView mShareTo;
    private ConfigBean mConfigBean;
    private SharedSdkUitl mSharedSdkUitl;
    private Dialog mPublishDialog;
    private EditText mEditTitle;
    private TextView mLength;
    private String mShareType;
    private int mMusicId;
    private int mSaveType;
    private int mLink;
    private String mLinkType;
    private String mLinkVideoId;

    @Override
    protected int getLayoutId() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        return R.layout.activity_video_publish;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.video_publish));
        Intent intent = getIntent();
        mVideoPath = intent.getStringExtra(Constants.VIDEO_PATH);
        mCoverPath = intent.getStringExtra(Constants.VIDEO_COVER_PATH);
        mSaveType = intent.getIntExtra(Constants.SAVE_TYPE, Constants.SAVE_TYPE_ALL);
        mLink = intent.getIntExtra(Constants.LINK, 0);
        mLinkType = intent.getStringExtra(Constants.LINK_TYPE);
        mLinkVideoId = intent.getStringExtra(Constants.LINK_VIDEO_ID);
        if (mVideoPath == null || mCoverPath == null) {
            return;
        }
        mMusicId = intent.getIntExtra(Constants.VIDEO_MUSIC_ID, 0);
        mVideoUploadBean = new VideoUploadBean(mVideoPath, mCoverPath);
        ((TextView) findViewById(R.id.city)).setText(AppConfig.getInstance().getCity());
        mShareTo = (TextView) findViewById(R.id.share_to);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mSharedSdkUitl = new SharedSdkUitl();
        HttpUtil.getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean configBean) {
                mConfigBean = configBean;
                List<ShareBean> list = mSharedSdkUitl.getShareList(configBean.getShare_type());
                if (list != null && list.size() > 0) {
                    VideoShareAdapter adapter = new VideoShareAdapter(mContext, list, true, true);
                    adapter.setOnItemClickListener(VideoPublishActivity.this);
                    mRecyclerView.setAdapter(adapter);
                    if (list.size() > 4) {
                        mShareTo.setText(WordUtil.getString(R.string.share_to_2));
                    }
                }
            }
        });
        findViewById(R.id.btn_publish).setOnClickListener(this);
        mEditTitle = (EditText) findViewById(R.id.edit_title);
        mLength = (TextView) findViewById(R.id.length);
        mEditTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mLength.setText(s.length() + "/50");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        initPlayer();
    }

    private void initPlayer() {
        mVideoView = (TXCloudVideoView) findViewById(R.id.video_view);
        mPlayer = new TXLivePlayer(mContext);
        mPlayer.setConfig(new TXLivePlayConfig());
        mPlayer.setPlayerView(mVideoView);
        mPlayer.enableHardwareDecode(false);
        mPlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
        mPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
        mPlayer.setPlayListener(mPlayListener);
        if (!TextUtils.isEmpty(mVideoPath)) {
            int result = mPlayer.startPlay(mVideoPath, TXLivePlayer.PLAY_TYPE_LOCAL_VIDEO);
            if (result == 0) {
                mStartPlay = true;
            }
        }
    }

    private ITXLivePlayListener mPlayListener = new ITXLivePlayListener() {
        @Override
        public void onPlayEvent(int e, Bundle bundle) {
            if (e == TXLiveConstants.PLAY_EVT_PLAY_END) {
                onReplay();
            }
        }

        @Override
        public void onNetStatus(Bundle bundle) {

        }
    };

    /**
     * 循环播放
     */
    private void onReplay() {
        if (mStartPlay && mPlayer != null) {
            mPlayer.seek(0);
            mPlayer.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
        if (mStartPlay && mPlayer != null) {
            mPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPaused) {
            mPaused = false;
            if (mStartPlay && mPlayer != null) {
                mPlayer.resume();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.stopPlay(true);
        }
        if (mVideoView != null) {
            mVideoView.onDestroy();
        }
        if (mSharedSdkUitl != null) {
            mSharedSdkUitl.cancelListener();
        }
    }

    @Override
    public void onItemClick(ShareBean bean, int position) {
        if (bean.isChecked()) {
            mShareType = bean.getType();
        } else {
            mShareType = null;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_publish) {
            publishVideo();
        }
    }

    /**
     * 把视频和封面图片上传到云存储
     */
    private void publishVideo() {
        if (mConfigBean == null) {
            return;
        }
        mPublishDialog = DialogUitl.loadingDialog(mContext, WordUtil.getString(R.string.video_publish_ing));
        mPublishDialog.show();
        UploadStrategy strategy = null;
        if (mConfigBean.getCloudtype() == VideoUploadManager.UPLOAD_QN) {//七牛云
            strategy = VideoQnUpload.getInstance();
        } else if (mConfigBean.getCloudtype() == VideoUploadManager.UPLOAD_TX) {//腾讯云
            strategy = VideoTxUpload.getInstance();
        }
        if (strategy == null || mVideoUploadBean == null) {
            return;
        }
        VideoUploadManager.getInstance().upload(mVideoUploadBean, strategy, new VideoUploadManager.OnUploadSuccess() {
            @Override
            public void onSuccess(VideoUploadBean bean) {
                if (mSaveType == Constants.SAVE_TYPE_PUB) {//仅发布
                    String videoPath = mVideoUploadBean.getVideoPath();
                    if (!TextUtils.isEmpty(videoPath)) {
                        File file = new File(videoPath);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }
                saveUploadInfo();
            }
        });
    }


    /**
     * 把上传后的视频信息保存到数据库
     */
    private void saveUploadInfo() {
        final String title = mEditTitle.getText().toString();//分享的标题
        HttpUtil.uploadVideo(title, mVideoUploadBean.getImgName(), mVideoUploadBean.getVideoName(), mMusicId, mLink, mLinkType, mLinkVideoId, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (mPublishDialog != null) {
                    mPublishDialog.dismiss();
                }
                if (code == 0 && info.length > 0) {
                    ToastUtil.show(msg);
//                    if (mLink == 1) {
//                        ToastUtil.show(R.string.发布成功);
//                    } else {
//                        ToastUtil.show(WordUtil.getString(R.string.publish_success));
//                    }
                    JSONObject obj = JSON.parseObject(info[0]);
                    if (mShareType == null) {
                        finish();
                    } else {
                        share(title, obj.getString("id"), obj.getString("thumb_s"));
                    }
                }else{
                    ToastUtil.show(getResources().getString(R.string.发布失败));
                }
            }
        });
    }

    /**
     * 分享
     */
    private void share(final String title, final String videoId, final String videoThumb) {
        if (mConfigBean == null || mSharedSdkUitl == null) {
            return;
        }
        UserBean u = AppConfig.getInstance().getUserBean();
        if (u == null) {
            return;
        }
        String des = u.getUser_nicename() + mConfigBean.getVideo_share_des();
        mSharedSdkUitl.share(mShareType, title, des, videoThumb, AppConfig.HOST + "/index.php?g=appapi&m=video&a=index&videoid=" + videoId, new SharedSdkUitl.ShareListener() {
            @Override
            public void onSuccess(Platform platform) {
                //ToastUtil.show(WordUtil.getString(R.string.share_success));
            }

            @Override
            public void onError(Platform platform) {
                //ToastUtil.show(WordUtil.getString(R.string.share_fail));
            }

            @Override
            public void onCancel(Platform platform) {
                //ToastUtil.show(WordUtil.getString(R.string.share_cancel));
            }

            @Override
            public void onShareFinish() {
                finish();
            }
        });
    }

}
