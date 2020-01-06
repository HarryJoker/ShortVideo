package com.Tata.video.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.bean.MusicBean;
import com.Tata.video.custom.video.FilterHolder;
import com.Tata.video.custom.video.MusicHolder;
import com.Tata.video.fragment.VideoProcessFragment;
import com.Tata.video.utils.DialogUitl;
import com.Tata.video.utils.L;
import com.Tata.video.utils.ToastUtil;
import com.Tata.video.utils.VideoEditWrap;
import com.Tata.video.utils.WordUtil;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;
import com.tencent.ugc.TXVideoInfoReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by cxf on 2018/6/21.
 * 录制或选择视频完成后进行编辑
 */

public class VideoEditActivity extends AbsActivity implements View.OnClickListener {

    private static final String TAG = "VideoEditActivity";
    private static final int STATUS_NONE = 0;
    private static final int STATUS_PLAY = 1;
    private static final int STATUS_PAUSE = 2;
    private static final int STATUS_PREVIEW_AT_TIME = 3;
    private int mFrom;//录制后进来的，还是选择视频后进来的
    private TXVideoEditer mTXVideoEditer;
    private String mVideoPath;
    private long mVideoDuration;
    private MusicBean mMusicBean;
    private boolean mPaused;
    private ImageView mBtnPlay;
    private int mPLayStatus = STATUS_NONE;
    private long mPreviewAtTime;
    private long mCutStartTime;
    private long mCutEndTime;
    private View mBtnNext;
    private VideoProcessFragment mVideoProcessFragment;
    private String mGenerateVideoPath;//生成视频的路径
    //美颜管理类
//    private SpecialHolder mSpecialHolder;
    //滤镜管理类
    private FilterHolder mFilterHolder;
    //音乐管理类
    private MusicHolder mMusicHolder;
    private int mSaveType;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_edit;
    }

    @Override
    protected void main() {
        //初始化editer
        TXVideoEditer editer = VideoEditWrap.getInstance().getTXVideoEditer();
        if (editer == null || editer.getTXVideoInfo() == null) {
            ToastUtil.show(WordUtil.getString(R.string.video_edit_status_error));
            finish();
            return;
        }
        mTXVideoEditer = editer;
        mTXVideoEditer.setTXVideoPreviewListener(mPreviewListener);
        mTXVideoEditer.setVideoGenerateListener(mTXVideoGenerateListener);
        mTXVideoEditer.setTXVideoReverseListener(mReverseListener);
        Intent intent = getIntent();
        mFrom = intent.getIntExtra(Constants.FROM, 0);
        mVideoPath = intent.getStringExtra(Constants.VIDEO_PATH);
        mMusicBean = intent.getParcelableExtra(Constants.MUSIC_BEAN);
        mVideoDuration = intent.getLongExtra(Constants.VIDEO_DURATION, 0);
        ViewGroup container = (ViewGroup) findViewById(R.id.container);
        View content = findViewById(R.id.content);
        mBtnNext = findViewById(R.id.btn_next);
        mBtnPlay = (ImageView) findViewById(R.id.btn_play);
        findViewById(R.id.root).setOnClickListener(this);
//        mSpecialHolder = new SpecialHolder(mContext, container, content, mVideoDuration);
//        mSpecialHolder.setEffectListener(mEffectListener);
        mFilterHolder = new FilterHolder(mContext, container, content);
        mFilterHolder.setFilterEffectListener(mFilterEffectListener);
        mMusicHolder = new MusicHolder(mContext, container, content, mMusicBean);
        mMusicHolder.setMusicChangeListener(mMusicChangeListener);
        //开启预览
        FrameLayout layout = (FrameLayout) findViewById(R.id.video_layout);
        TXVideoEditConstants.TXPreviewParam param = new TXVideoEditConstants.TXPreviewParam();
        param.videoView = layout;
        param.renderMode = TXVideoEditConstants.PREVIEW_RENDER_MODE_FILL_EDGE;
        mTXVideoEditer.initWithPreview(param);
        mCutStartTime = 0;
        mCutEndTime = mVideoDuration;
        startPlay(mCutStartTime, mCutEndTime);
        if (mMusicBean != null) {
            mTXVideoEditer.setBGM(mMusicBean.getLocalPath());
            mTXVideoEditer.setVideoVolume(0);
            mTXVideoEditer.setBGMVolume(0.8f);
        } else {
            mTXVideoEditer.setVideoVolume(0.8f);
        }
    }

    /**
     * 预览的播放进度监听
     */
    private TXVideoEditer.TXVideoPreviewListener mPreviewListener = new TXVideoEditer.TXVideoPreviewListener() {
        @Override
        public void onPreviewProgress(int timeMs) {
//            if (mSpecialHolder != null) {
//                mSpecialHolder.onVideoPreview(timeMs);
//            }
        }

        @Override
        public void onPreviewFinished() {
//            if (mSpecialHolder == null || mSpecialHolder.isShowCut()) {
//
//            }
            if (mPLayStatus == STATUS_PLAY) {
                startPlay(mCutStartTime, mCutEndTime);
            }
        }
    };

    /**
     * 倒放监听
     */
    private TXVideoEditer.TXVideoReverseListener mReverseListener = new TXVideoEditer.TXVideoReverseListener() {
        @Override
        public void onReverseComplete(TXVideoEditConstants.TXGenerateResult result) {
            if (result.retCode == TXVideoEditConstants.GENERATE_RESULT_OK) {
//                if (mSpecialHolder != null) {
//                    mSpecialHolder.reverse();
//                }
                if (mTXVideoEditer != null) {
                    startPlay(mCutStartTime, mCutEndTime);
                }
            }
        }
    };

    private void startPlay(long startTime, long endTime) {
        if (mTXVideoEditer != null) {
            mTXVideoEditer.startPlayFromTime(startTime, endTime);
            mPLayStatus = STATUS_PLAY;
        }
        if (mBtnPlay != null && mBtnPlay.getVisibility() == View.VISIBLE) {
            mBtnPlay.setVisibility(View.INVISIBLE);
        }
    }

    private void pausePlay() {
        if (mPLayStatus == STATUS_PLAY) {
            mPLayStatus = STATUS_PAUSE;
            if (mTXVideoEditer != null) {
                mTXVideoEditer.pausePlay();
            }
        }
        if (mBtnPlay != null && mBtnPlay.getVisibility() != View.VISIBLE) {
            mBtnPlay.setVisibility(View.VISIBLE);
        }
    }

    private void resumePlay() {
        if (mPLayStatus == STATUS_PAUSE) {
            mPLayStatus = STATUS_PLAY;
            if (mTXVideoEditer != null) {
                mTXVideoEditer.resumePlay();
            }
        }
        if (mBtnPlay != null && mBtnPlay.getVisibility() == View.VISIBLE) {
            mBtnPlay.setVisibility(View.INVISIBLE);
        }
    }

    private void previewAtTime(long timeMs) {
        if (mPLayStatus != STATUS_PREVIEW_AT_TIME) {
            mPLayStatus = STATUS_PREVIEW_AT_TIME;
            if (mTXVideoEditer != null) {
                mTXVideoEditer.pausePlay();
            }
            if (mBtnPlay != null && mBtnPlay.getVisibility() != View.VISIBLE) {
                mBtnPlay.setVisibility(View.VISIBLE);
            }
        }
        mTXVideoEditer.previewAtTime(timeMs);
        mPreviewAtTime = timeMs;
    }


    public void editClick(View v) {
        switch (v.getId()) {
            case R.id.btn_special://特效
                showSpecial();
                break;
            case R.id.btn_cover://封面
                ToastUtil.show(getResources().getString(R.string.敬请期待));
                break;
            case R.id.btn_filter://滤镜
                showFilter();
                break;
            case R.id.btn_music://音乐
                chooseMusic();
                break;
            case R.id.btn_music_volume://音量
                showMusic();
                break;
            case R.id.btn_next://下一步
                toNext();
                break;
        }
    }


    private void toNext() {
        DialogUitl.showUserMoreDialog(mContext, new String[]{
                WordUtil.getString(R.string.save_type_all),
                WordUtil.getString(R.string.save_type_save),
                WordUtil.getString(R.string.save_type_pub)
        }, new int[]{
                0xff1271FB, 0xff1271FB, 0xff1271FB
        }, new DialogUitl.StringArrayDialogCallback() {
            @Override
            public void onItemClick(String text, int position) {
                switch (position) {
                    case 0:
                        mSaveType = Constants.SAVE_TYPE_ALL;
                        break;
                    case 1:
                        mSaveType = Constants.SAVE_TYPE_SAVE;
                        break;
                    case 2:
                        mSaveType = Constants.SAVE_TYPE_PUB;
                        break;
                }
                generateVideo();
            }
        });
    }

    /**
     * 生成视频
     */
    private void generateVideo() {
        L.e(TAG, "#toNext------->生成视频");
        if (mTXVideoEditer == null) {
            return;
        }
        if (mCutEndTime - mCutStartTime < 1000) {
            ToastUtil.show(WordUtil.getString(R.string.video_duration_too_short));
        }
        mTXVideoEditer.stopPlay();
        mPLayStatus = STATUS_NONE;
        mBtnNext.setEnabled(false);
        mGenerateVideoPath = VideoEditWrap.getInstance().generateVideoOutputPath();
        mVideoProcessFragment = new VideoProcessFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.VIDEO_PROCESS_DES, WordUtil.getString(R.string.video_generating));
        mVideoProcessFragment.setArguments(bundle);
        mVideoProcessFragment.setActionListener(new VideoProcessFragment.ActionListener() {
            @Override
            public void onCancelClick() {
                cancelGenerateVideo();
            }
        });
        mVideoProcessFragment.show(getSupportFragmentManager(), "VideoProcessFragment");
        mTXVideoEditer.setCutFromTime(mCutStartTime, mCutEndTime);
        mTXVideoEditer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_720P, mGenerateVideoPath);
    }

    //生成视频的回调
    private TXVideoEditer.TXVideoGenerateListener mTXVideoGenerateListener = new TXVideoEditer.TXVideoGenerateListener() {

        @Override
        public void onGenerateProgress(float v) {
            if (mVideoProcessFragment != null && mVideoProcessFragment.isAdded()) {
                mVideoProcessFragment.setProgress(v);
            }
        }

        @Override
        public void onGenerateComplete(TXVideoEditConstants.TXGenerateResult result) {
            L.e(TAG, "#onGenerateComplete------->");
            if (result.retCode == TXVideoEditConstants.GENERATE_RESULT_OK) {
                L.e(TAG, "#onGenerateComplete------->生成视频成功");
                saveGenerateVideoInfo();
                ToastUtil.show(WordUtil.getString(R.string.generate_video_success));
                if (mSaveType == Constants.SAVE_TYPE_SAVE) {//仅保存
                    finish();
                    return;
                }
                generateCoverFile();
            } else {
                onGenerateFailure();
            }
        }
    };


    private void onGenerateFailure() {
        ToastUtil.show(WordUtil.getString(R.string.generate_video_failed));
        mBtnNext.setEnabled(true);
        if (mVideoProcessFragment != null && mVideoProcessFragment.isAdded()) {
            mVideoProcessFragment.dismiss();
        }
    }

    /**
     * 生成视频的封面图
     */
    private void generateCoverFile() {
        L.e(TAG, "#generateCoverFile------->生成视频的封面图");
        new AsyncTask<Void, String, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                File outputVideo = new File(mGenerateVideoPath);
                if (!outputVideo.exists()) {
                    return null;
                }
                if (mFrom == Constants.VIDEO_FROM_RECORD) {
                    if (!TextUtils.isEmpty(mVideoPath)) {
                        File file = new File(mVideoPath);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }
                Bitmap bitmap = TXVideoInfoReader.getInstance().getSampleImage(0, mGenerateVideoPath);
                if (bitmap == null) {
                    return null;
                }
                String coverFilePath = mGenerateVideoPath.replace(".mp4", ".jpg");
                File file = new File(coverFilePath);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return coverFilePath;
            }

            @Override
            protected void onPostExecute(String coverFilePath) {
                L.e(TAG, "#generateCoverFile------->coverFilePath  " + coverFilePath);
                if (!TextUtils.isEmpty(coverFilePath)) {
                    Intent intent = new Intent(mContext, VideoPublishActivity.class);
                    intent.putExtra(Constants.VIDEO_PATH, mGenerateVideoPath);
                    intent.putExtra(Constants.VIDEO_COVER_PATH, coverFilePath);
                    intent.putExtra(Constants.SAVE_TYPE, mSaveType);
                    Intent intent1 = getIntent();
                    int link = intent1.getIntExtra(Constants.LINK, 0);
                    String type = intent1.getStringExtra(Constants.LINK_TYPE);
                    String videoId = intent1.getStringExtra(Constants.LINK_VIDEO_ID);
                    L.e(TAG, "#type------>" + type);
                    L.e(TAG, "#videoId------>" + videoId);
                    intent.putExtra(Constants.LINK, link);
                    intent.putExtra(Constants.LINK_TYPE, type);
                    intent.putExtra(Constants.LINK_VIDEO_ID, videoId);
                    if (mMusicBean != null) {
                        intent.putExtra(Constants.VIDEO_MUSIC_ID, mMusicBean.getId());
                    }
                    startActivity(intent);
                    finish();
                    L.e(TAG, "#generateCoverFile------->跳转到发布！！");
                } else {
                    onGenerateFailure();
                }
            }

        }.execute();
    }

    /**
     * 把新生成的视频保存到ContentProvider,在选择上传的时候能找到
     */
    private void saveGenerateVideoInfo() {
        try {
            File videoFile = new File(mGenerateVideoPath);
            String fileName = videoFile.getName();
            long currentTimeMillis = System.currentTimeMillis();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.TITLE, fileName);
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.DATE_MODIFIED, currentTimeMillis);
            values.put(MediaStore.MediaColumns.DATE_ADDED, currentTimeMillis);
            values.put(MediaStore.MediaColumns.DATA, mGenerateVideoPath);
            values.put(MediaStore.MediaColumns.SIZE, videoFile.length());
            values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, currentTimeMillis);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
            values.put(MediaStore.Video.VideoColumns.DURATION, mCutEndTime - mCutStartTime);
            getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 取消生成视频
     */
    private void cancelGenerateVideo() {
        L.e(TAG, "#cancelGenerateVideo------->取消生成视频");
        if (mVideoProcessFragment != null && mVideoProcessFragment.isAdded()) {
            mVideoProcessFragment.dismiss();
        }
        mBtnNext.setEnabled(true);
        if (mTXVideoEditer != null) {
            mTXVideoEditer.cancel();
        }
        ToastUtil.show(WordUtil.getString(R.string.cancel_generate_video));
    }


    /**
     * 打开特效面板
     */
    private void showSpecial() {
//        if (mSpecialHolder != null) {
//            mSpecialHolder.show();
//        }
    }

    /**
     * 打开滤镜面板
     */
    private void showFilter() {
        if (mFilterHolder != null) {
            mFilterHolder.show();
        }
    }

    /**
     * 打开音乐编辑面板
     */
    private void showMusic() {
        if (mMusicHolder != null) {
            mMusicHolder.show();
        }
    }

    /**
     * 选择更换背景音乐
     */
    private void chooseMusic() {
//        Intent intent = new Intent(mContext, VideoMusicActivity.class);
//        intent.putExtra(Constants.FROM, Constants.VIDEO_FROM_EDIT);
//        startActivityForResult(intent, 100);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK && mTXVideoEditer != null) {
            MusicBean bean = data.getParcelableExtra(Constants.MUSIC_BEAN);
            if (bean != null) {
                String bgmPath = bean.getLocalPath();
                if (!TextUtils.isEmpty(bgmPath)) {
                    mTXVideoEditer.setBGM(bgmPath);
                    mTXVideoEditer.setBGMVolume(0.8f);
                    mMusicBean = bean;
                    if (mMusicHolder != null) {
                        mMusicHolder.setBgmMusic(bean);
                    }
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
        if (mPLayStatus == STATUS_PLAY && mTXVideoEditer != null) {
            mTXVideoEditer.pausePlay();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPaused) {
            mPaused = false;
            if (mPLayStatus == STATUS_PLAY && mTXVideoEditer != null) {
                mTXVideoEditer.resumePlay();
            }
        }
    }

    @Override
    protected void onDestroy() {
        VideoEditWrap.getInstance().release();
        if (mFilterHolder != null) {
            mFilterHolder.release();
        }
        if (mMusicHolder != null) {
            mMusicHolder.release();
        }
        super.onDestroy();
    }

//    @Override
//    public void onBackPressed() {
//        DialogUitl.showSimpleDialog(mContext, WordUtil.getString(R.string.give_up_edit), new DialogUitl.SimpleDialogCallback() {
//            @Override
//            public void onComfirmClick() {
//                VideoEditActivity.super.onBackPressed();
//            }
//        });
//
//    }

    private FilterHolder.FilterEffectListener mFilterEffectListener = new FilterHolder.FilterEffectListener() {
        @Override
        public void onFilterChanged(Bitmap bitmap) {
            if (mTXVideoEditer != null) {
                mTXVideoEditer.setFilter(bitmap);
            }
        }
    };

    private MusicHolder.MusicChangeListener mMusicChangeListener = new MusicHolder.MusicChangeListener() {
        @Override
        public void onOriginalChanged(float value) {
            if (mTXVideoEditer != null) {
                mTXVideoEditer.setVideoVolume(value);
            }
        }

        @Override
        public void onBgmChanged(float value) {
            if (mTXVideoEditer != null) {
                mTXVideoEditer.setBGMVolume(value);
            }
        }

        @Override
        public void onBgmCancelClick() {
            if (mTXVideoEditer != null) {
                mTXVideoEditer.setBGM(null);
                mMusicBean = null;
            }
        }

        @Override
        public void onBgmCutTimeChanged(long startTime, long endTime) {
            if (mTXVideoEditer != null) {
                mTXVideoEditer.setBGMStartTime(startTime, endTime);
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.root) {
            switch (mPLayStatus) {
                case STATUS_PLAY:
                    pausePlay();
                    break;
                case STATUS_PAUSE:
                    resumePlay();
                    break;
                case STATUS_PREVIEW_AT_TIME:
                    if (mPreviewAtTime > mCutStartTime && mPreviewAtTime < mCutEndTime) {
                        startPlay(mPreviewAtTime, mCutEndTime);
                    } else {
                        startPlay(mCutStartTime, mCutEndTime);
                    }
                    break;
            }
        }
    }

//    private SpecialHolder.EffectListener mEffectListener = new SpecialHolder.EffectListener() {
//        @Override
//        public void onSeekChanged(long currentTimeMs) {
//            previewAtTime(currentTimeMs);
//        }
//
//        @Override
//        public void onCutTimeChanged(long startTimeMs, long endTimeMs) {
//            mCutStartTime = startTimeMs;
//            mCutEndTime = endTimeMs;
//            if (mTXVideoEditer != null) {
//                mTXVideoEditer.setCutFromTime(startTimeMs, endTimeMs);
//            }
//        }
//
//        @Override
//        public void onOtherSpecialStart(int effect, long currentTimeMs) {
//            if (mPLayStatus == STATUS_NONE || mPLayStatus == STATUS_PREVIEW_AT_TIME) {
//                startPlay(mPreviewAtTime, mCutEndTime);
//            } else if (mPLayStatus == STATUS_PAUSE) {
//                resumePlay();
//            }
//            if (mTXVideoEditer != null) {
//                mTXVideoEditer.startEffect(effect, currentTimeMs);
//            }
//        }
//
//        @Override
//        public void onOtherSpecialEnd(int effect, long currentTimeMs) {
//            pausePlay();
//            if (mTXVideoEditer != null) {
//                mTXVideoEditer.stopEffect(effect, currentTimeMs);
//            }
//        }
//
//        @Override
//        public void onOtherSpecialCancel(long currentTimeMs) {
//            if (mTXVideoEditer != null) {
//                mTXVideoEditer.previewAtTime(currentTimeMs);
//                mTXVideoEditer.deleteLastEffect();
//            }
//        }
//
//        @Override
//        public void onTimeDaoFangChanged(boolean add) {
//            if (mTXVideoEditer != null) {
//                if (add) {
//                    mTXVideoEditer.stopPlay();
//                    mTXVideoEditer.setReverse(true);
//                } else {
//                    mTXVideoEditer.stopPlay();
//                    mTXVideoEditer.setReverse(false);
//                    startPlay(mCutStartTime, mCutEndTime);
//                    if (mSpecialHolder != null) {
//                        mSpecialHolder.reverse();
//                    }
//                }
//            }
//        }
//
//        @Override
//        public void onTimeFanFuChanged(boolean add, long startTime) {
//            if (mTXVideoEditer != null) {
//                if (add) {
//                    mTXVideoEditer.previewAtTime(startTime);
//                    TXVideoEditConstants.TXRepeat repeat = new TXVideoEditConstants.TXRepeat();
//                    repeat.startTime = startTime;
//                    repeat.endTime = startTime + 2000;
//                    repeat.repeatTimes = 3;
//                    mTXVideoEditer.setRepeatPlay(Arrays.asList(repeat));
//                } else {
//                    mTXVideoEditer.setRepeatPlay(null);
//                    mTXVideoEditer.stopPlay();
//                    startPlay(mCutStartTime, mCutEndTime);
//                }
//
//            }
//        }
//
//        @Override
//        public void onTimeMdzChanged(boolean add, long startTime) {
//            if (mTXVideoEditer != null) {
//                if (add) {
//                    mTXVideoEditer.previewAtTime(startTime);
//                    TXVideoEditConstants.TXSpeed speed = new TXVideoEditConstants.TXSpeed();
//                    speed.startTime = startTime;
//                    speed.endTime = mVideoDuration;
//                    speed.speedLevel = TXVideoEditConstants.SPEED_LEVEL_SLOW;
//                    mTXVideoEditer.setSpeedList(Arrays.asList(speed));
//                } else {
//                    mTXVideoEditer.setSpeedList(null);
//                }
//            }
//        }
//
//        @Override
//        public boolean onHideClicked() {
//            if (mPLayStatus == STATUS_PREVIEW_AT_TIME) {
//                startPlay(mPreviewAtTime, mCutEndTime);
//                return false;
//            }
//            if (mPLayStatus == STATUS_PAUSE) {
//                resumePlay();
//                return false;
//            }
//            if (mPLayStatus == STATUS_PLAY) {
//                return true;
//            }
//            return false;
//        }
//    };
}
