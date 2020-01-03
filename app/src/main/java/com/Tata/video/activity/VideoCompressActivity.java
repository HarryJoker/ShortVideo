package com.Tata.video.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;
import com.tencent.ugc.TXVideoInfoReader;
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.fragment.VideoProcessFragment;
import com.Tata.video.utils.L;
import com.Tata.video.utils.ToastUtil;
import com.Tata.video.utils.VideoEditWrap;
import com.Tata.video.utils.WordUtil;

/**
 * Created by cxf on 2018/6/21.
 * 录制视频后预处理
 */

public class VideoCompressActivity extends AbsActivity implements TXVideoEditer.TXVideoProcessListener,
        TXVideoEditer.TXThumbnailListener {

    private static final String TAG = "VideoCompressActivity";
    private TXVideoEditer mTXVideoEditer;
    private String mVideoPath;
    private long mDuration;
    private Handler mHandler;
    private static final int VIDEO_LOAD_SUCCESS = 1;//视频信息读取成功
    private static final int VIDEO_LOAD_ERROR = 0;//视频信息读取失败
    private VideoProcessFragment mVideoProcessFragment;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_compress;
    }

    @Override
    protected void main() {
        Intent intent = getIntent();
        mVideoPath = intent.getStringExtra(Constants.VIDEO_PATH);
        mDuration = intent.getLongExtra(Constants.VIDEO_DURATION, 0);
        if (TextUtils.isEmpty(mVideoPath) || mDuration <= 0) {
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction tx = fragmentManager.beginTransaction();
        mVideoProcessFragment = new VideoProcessFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.VIDEO_PROCESS_DES, WordUtil.getString(R.string.video_compressing));
        mVideoProcessFragment.setArguments(bundle);
        mVideoProcessFragment.setActionListener(new VideoProcessFragment.ActionListener() {
            @Override
            public void onCancelClick() {
                cancelCompress();
            }
        });
        tx.add(R.id.replaced, mVideoProcessFragment).commit();
        mTXVideoEditer = VideoEditWrap.getInstance().createVideoEditer(mContext, mVideoPath);
        VideoEditWrap.getInstance().clearBitmapList();
        mTXVideoEditer.setVideoProcessListener(this);
        mTXVideoEditer.setThumbnailListener(this);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case VIDEO_LOAD_SUCCESS:
                        L.e(TAG, "#handleMessage----->success");
                        startCompress();
                        break;
                    case VIDEO_LOAD_ERROR:
                        ToastUtil.show(WordUtil.getString(R.string.video_load_error));
                        finish();
                        break;
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                TXVideoEditConstants.TXVideoInfo info = TXVideoInfoReader.getInstance().getVideoFileInfo(mVideoPath);
                L.e(TAG, "#info----->" + info);
                if (mHandler != null) {
                    if (info != null) {
                        L.e(TAG, "#info----->success");
                        mHandler.sendEmptyMessage(VIDEO_LOAD_SUCCESS);
                    } else {// error 发生错误x
                        mHandler.sendEmptyMessage(VIDEO_LOAD_ERROR);
                        L.e(TAG, "#info----->error");
                    }
                }

            }
        }).start();
    }

    private void startCompress() {
        L.e(TAG, "#startCompress----->1");
        if (mTXVideoEditer == null) {
            return;
        }
        L.e(TAG, "#startCompress----->2");

        int thumbnailCount = (int) Math.floor(mDuration / 1000f);
        TXVideoEditConstants.TXThumbnail thumbnail = new TXVideoEditConstants.TXThumbnail();
        thumbnail.count = thumbnailCount;
        thumbnail.width = 100;
        thumbnail.height = 100;
        mTXVideoEditer.setThumbnail(thumbnail);
        mTXVideoEditer.processVideo();
    }


    private void cancelCompress() {
        if (mTXVideoEditer != null) {
            mTXVideoEditer.cancel();
        }
        ToastUtil.show(WordUtil.getString(R.string.cancel_compress));
        finish();
    }

    /**
     * 前往视频编辑页面
     */
    private void forwardVideoEdit() {
        L.e("#前往视频编辑页面------->");
        Intent intent = new Intent(mContext, VideoEditActivity.class);
        intent.putExtras(getIntent().getExtras());
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (mTXVideoEditer != null) {
            mTXVideoEditer.setVideoProcessListener(null);
            mTXVideoEditer.setThumbnailListener(null);
            mTXVideoEditer = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        cancelCompress();
    }

    /*******
     * TXVideoProcessListener  回调
     *******/
    @Override
    public void onProcessProgress(float progress) {
        if (mVideoProcessFragment != null && mVideoProcessFragment.isAdded()) {
            mVideoProcessFragment.setProgress(progress);
        }
    }

    @Override
    public void onProcessComplete(TXVideoEditConstants.TXGenerateResult result) {
        if (result.retCode == TXVideoEditConstants.GENERATE_RESULT_OK) {
            L.e(TAG, "#startCompress----->完成");
            forwardVideoEdit();
        } else {
            L.e("#视频压缩出现错误------->" + result.descMsg);
            ToastUtil.show(WordUtil.getString(R.string.video_compress_failed));
            finish();
        }
    }

    /*******
     * TXVideoProcessListener  回调 end
     *******/

    /*******
     * TXThumbnailListener  回调
     *******/
    @Override
    public void onThumbnail(int index, long timeMs, Bitmap bitmap) {
        VideoEditWrap.getInstance().addVideoBitmap(bitmap);
    }
    /*******
     * TXThumbnailListener  回调 end
     *******/
}
