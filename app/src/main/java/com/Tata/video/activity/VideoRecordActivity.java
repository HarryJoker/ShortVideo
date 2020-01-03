package com.Tata.video.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXRecordCommon;
import com.tencent.ugc.TXUGCRecord;
import com.Tata.video.AppContext;
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.bean.MusicBean;
import com.Tata.video.custom.AnimImageView;
import com.Tata.video.custom.record.RecordProgressView;
import com.Tata.video.custom.video.BeautyHolder;
import com.Tata.video.utils.DialogUitl;
import com.Tata.video.utils.FrameAnimUtil;
import com.Tata.video.utils.L;
import com.Tata.video.utils.ToastUtil;
import com.Tata.video.utils.VideoEditWrap;
import com.Tata.video.utils.WordUtil;

import java.io.File;
import java.util.Locale;

import cn.tillusory.sdk.TiSDKManager;
import cn.tillusory.sdk.TiSDKManagerBuilder;
import cn.tillusory.sdk.bean.TiDistortionEnum;
import cn.tillusory.sdk.bean.TiFilterEnum;
import cn.tillusory.sdk.bean.TiRockEnum;
import cn.tillusory.sdk.bean.TiRotation;


/**
 * Created by cxf on 2018/6/19.
 * 视频录制activity
 */

public class VideoRecordActivity extends AbsActivity {

    private static final int MIN_DURATION = 5000;//最小录制时间5s
    private static final int MAX_DURATION = 15000;//最大录制时间15s
    private TXUGCRecord mTXCameraRecord;//录制器
    private TiSDKManager mTiSDKManager;//萌颜的各种工具
    private AudioManager mAudioManager;//音频管理器
    private TXRecordCommon.TXUGCCustomConfig mCustomConfig;
    private TXRecordCommon.TXRecordResult mTXRecordResult;//录制结束后 录制的结果
    private TXCloudVideoView mVideoView;//预览的SurfaceView
    private RecordProgressView mRecordProgressView;//进度条
    private TextView mProgressTime;//显示当前录制时长的TextView
    private AnimImageView mBtnRecord;//录制按钮
    private View mBtnNext;//下一步 按钮
    private ImageView mBtnFlash;//闪光灯按钮
    private Dialog mStopRecordDialog;//停止录制的时候的dialog
    private long mLastClickTime;//上次点击录制按钮的时间
    private boolean mStartRecord;//是否开始了录制
    private boolean mClickPauseRecord;//是否手动停止了录制
    private boolean mPaused;//生命周期的暂停
    private boolean mDestroyed;//是否destory
    private boolean mIsFlashOpen;//闪光灯是否打开
    private boolean mIsFrontCamera = true;//是否是前置摄像头
    private String mCoverPath;//封面图的保存路径
    private String mVideoPath;//视频的保存路径
    private boolean mIsReachMinRecordDuration;//是否达到最小录制时间
    private boolean mIsReachMaxRecordDuration;//是否达到最大录制时间
    //美颜管理类
    private BeautyHolder mBeautyHolder;
    private MusicBean mMusicBean;//背景音乐
    //各种美颜效果
    private int mMeibai = 100;//美白
    private int mMoPi = 100;//磨皮
    private int mBaoHe = 100;//饱和
    private int mFengNen = 100;//粉嫩
    private int mBigEye = 20;//大眼
    private int mFace = 20;//瘦脸
    private String mTieZhi = "";//贴纸
    private TiDistortionEnum mTiDistortionEnum = TiDistortionEnum.NO_DISTORTION;

    private boolean mStartPreview;

    @Override
    protected int getLayoutId() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        return R.layout.activity_video_record;
    }

    @Override
    protected void main() {
        mVideoView = (TXCloudVideoView) findViewById(R.id.video_view);
        mVideoView.enableHardwareDecode(true);
        mBtnRecord = (AnimImageView) findViewById(R.id.btn_record);
        mBtnRecord.setImgList(FrameAnimUtil.getVideoRecordBtnAnim());
        mRecordProgressView = (RecordProgressView) findViewById(R.id.record_progress_view);
        mRecordProgressView.setMaxDuration(MAX_DURATION);
        mRecordProgressView.setMinDuration(MIN_DURATION);
        mProgressTime = (TextView) findViewById(R.id.progress_time);
        mBtnNext = findViewById(R.id.btn_next);
        mBtnNext.setAlpha(0.5f);
        mBtnFlash = (ImageView) findViewById(R.id.btn_flash);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mBeautyHolder = new BeautyHolder(mContext, ((ViewGroup) findViewById(R.id.root)), findViewById(R.id.content));
        mBeautyHolder.setEffectListener(mEffectListener);
        mMusicBean = getIntent().getParcelableExtra(Constants.MUSIC_BEAN);
        mVideoPath = VideoEditWrap.getInstance().generateVideoOutputPath();//视频保存的路径
        mCoverPath = mVideoPath.replace(".mp4", ".jpg");//视频封面图保存的路径
        initCameraRecord();
    }

    /**
     * 加载萌颜sdk的预处理
     */
    private void initTiSdk() {
        mTiSDKManager = new TiSDKManagerBuilder().build();
        //使用萌颜的美颜，美型效果
        mTiSDKManager.setBeautyEnable(true);
        mTiSDKManager.setFaceTrimEnable(true);
        mTiSDKManager.setSkinWhitening(mMeibai);//美白
        mTiSDKManager.setSkinBlemishRemoval(mMoPi);//磨皮
        mTiSDKManager.setSkinSaturation(mBaoHe);//饱和
        mTiSDKManager.setSkinTenderness(mFengNen);//粉嫩
        mTiSDKManager.setEyeMagnifying(mBigEye);//大眼
        mTiSDKManager.setChinSlimming(mFace);//瘦脸
        mTiSDKManager.setSticker(mTieZhi);//贴纸
        mTiSDKManager.setDistortionEnum(mTiDistortionEnum);
        //不使用萌颜的滤镜，抖音效果
        mTiSDKManager.setFilterEnum(TiFilterEnum.NO_FILTER);
        mTiSDKManager.setRockEnum(TiRockEnum.NO_ROCK);
    }

    private void initCameraRecord() {
        mTXCameraRecord = TXUGCRecord.getInstance(AppContext.sInstance);
        mTXCameraRecord.setHomeOrientation(TXLiveConstants.VIDEO_ANGLE_HOME_DOWN);
        mTXCameraRecord.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
        mTXCameraRecord.setRecordSpeed(TXRecordCommon.RECORD_SPEED_NORMAL);
        mTXCameraRecord.setAspectRatio(TXRecordCommon.VIDEO_ASPECT_RATIO_9_16);
        mCustomConfig = new TXRecordCommon.TXUGCCustomConfig();
        mCustomConfig.videoResolution = TXRecordCommon.VIDEO_RESOLUTION_540_960;
        mCustomConfig.minDuration = MIN_DURATION;
        mCustomConfig.maxDuration = MAX_DURATION;
        mCustomConfig.videoBitrate = 2400;
        mCustomConfig.videoGop = 3;
        mCustomConfig.videoFps = 20;
        mCustomConfig.isFront = mIsFrontCamera;
        mTXCameraRecord.setVideoRecordListener(mITXVideoRecordListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mStartPreview) {
            return;
        }
        mStartPreview = true;
        initTiSdk();
        mTXCameraRecord.setVideoProcessListener(mVideoCustomProcessListener);
        mTXCameraRecord.startCameraCustomPreview(mCustomConfig, mVideoView);
        if (!mIsFrontCamera) {
            mTXCameraRecord.switchCamera(mIsFrontCamera);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mStartRecord && !mClickPauseRecord) {
            pauseRecord();
        }
        if (mIsFlashOpen) {
            toggleFlash();
        }
        if (mTXCameraRecord != null) {
            mTXCameraRecord.setVideoProcessListener(null);
            mTXCameraRecord.stopCameraPreview();
            mStartPreview = false;
        }
        if (mTiSDKManager != null) {
            mTiSDKManager.destroy();
        }
    }

    @Override
    protected void onDestroy() {
        destroyRecord();
        super.onDestroy();
    }

    /**
     * 录制 暂停
     */
    private void switchRecord() {
        long currentClickTime = System.currentTimeMillis();
        if (currentClickTime - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = currentClickTime;
        if (mStartRecord) {
            if (mClickPauseRecord) {
                resumeRecord();
            } else {
                pauseRecord();
            }
            mClickPauseRecord = !mClickPauseRecord;
        } else {
            startRecord();
        }
    }


    /**
     * 开始录制
     */
    private void startRecord() {
        if (mTXCameraRecord == null) {
            initCameraRecord();
        }
        mStartRecord = true;
        int result = mTXCameraRecord.startRecord(mVideoPath, mCoverPath);
        if (result != TXRecordCommon.START_RECORD_OK) {
            if (result == TXRecordCommon.START_RECORD_ERR_NOT_INIT) {
                ToastUtil.show(R.string.别着急+","+R.string.画面还没出来);
            } else if (result == TXRecordCommon.START_RECORD_ERR_IS_IN_RECORDING) {
                ToastUtil.show(getResources().getString(R.string.还有录制的任务没有结束));
            } else if (result == TXRecordCommon.START_RECORD_ERR_VIDEO_PATH_IS_EMPTY) {
                ToastUtil.show(getResources().getString(R.string.传入的视频路径为空));
            } else if (result == TXRecordCommon.START_RECORD_ERR_API_IS_LOWER_THAN_18) {
                ToastUtil.show(getResources().getString(R.string.版本太低));
            }
            return;
        }
        if (mMusicBean != null) {
            int bgmDuration = mTXCameraRecord.setBGM(mMusicBean.getLocalPath());
            mTXCameraRecord.playBGMFromTime(0, bgmDuration);
            mTXCameraRecord.setBGMVolume(1);//背景音为1最大
            mTXCameraRecord.setMicVolume(0);//原声音为0
        }
        requestAudioFocus();
        mBtnRecord.startAnim();
    }

    /**
     * 暂停录制
     */
    private void pauseRecord() {
        if (mTXCameraRecord != null) {
            if (mMusicBean != null) {
                mTXCameraRecord.pauseBGM();
            }
            mTXCameraRecord.pauseRecord();
        }
        abandonAudioFocus();
        mBtnRecord.stopAnim();
    }

    /**
     * 恢复录制
     */
    private void resumeRecord() {
        int startResult = mTXCameraRecord.resumeRecord();
        if (startResult != TXRecordCommon.START_RECORD_OK) {
            ToastUtil.show(WordUtil.getString(R.string.record_failed));
            return;
        }
        if (mMusicBean != null) {
            mTXCameraRecord.resumeBGM();
        }
        requestAudioFocus();
        mBtnRecord.startAnim();
    }

    /**
     * 结束录制,会触发 onRecordComplete
     */
    private void stopRecord() {
        if (mTXCameraRecord != null) {
            mTXCameraRecord.stopBGM();
            mTXCameraRecord.stopRecord();
        }
        abandonAudioFocus();
        mBtnRecord.setEnabled(false);
    }

    /**
     * 录制结束时候显示处理中的弹窗
     */
    private void showProccessDialog() {
        if (mStopRecordDialog == null) {
            mStopRecordDialog = DialogUitl.loadingDialog(mContext, WordUtil.getString(R.string.processing));
            mStopRecordDialog.show();
        }
    }

    /**
     * 隐藏处理中的弹窗
     */
    private void hideProccessDialog() {
        if (mStopRecordDialog != null) {
            mStopRecordDialog.dismiss();
        }
    }

    /**
     * 回收资源
     */
    private void destroyRecord() {
        if (mDestroyed) {
            return;
        }
        mDestroyed = true;
        if (mBtnRecord != null) {
            mBtnRecord.release();
        }
        if (mTXCameraRecord != null) {
            mTXCameraRecord.stopBGM();
            mTXCameraRecord.stopCameraPreview();
            mTXCameraRecord.setVideoProcessListener(null);
            mTXCameraRecord.setVideoRecordListener(null);
            mTXCameraRecord.setFilter(null);
            mTXCameraRecord.setSpecialRatio(0);
            mTXCameraRecord.getPartsManager().deleteAllParts();
            mTXCameraRecord.release();
            mTXCameraRecord = null;
        }
        if (mRecordProgressView != null) {
            mRecordProgressView.release();
        }
        if (mBeautyHolder != null) {
            mBeautyHolder.setEffectListener(null);
            mBeautyHolder.release();
        }
        if (mTiSDKManager != null) {
            mTiSDKManager.destroy();
        }
        abandonAudioFocus();
    }


    @Override
    public void onBackPressed() {
        if (mIsFlashOpen) {
            toggleFlash();
        }
        if (mStartRecord) {
            if (!mClickPauseRecord) {
                pauseRecord();
            }
            if (!TextUtils.isEmpty(mVideoPath)) {
                File file = new File(mVideoPath);
                if (file.exists()) {
                    file.delete();
                }
            }
            if (!TextUtils.isEmpty(mCoverPath)) {
                File file = new File(mCoverPath);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
        destroyRecord();
        finish();
    }


    public void recordClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera://镜头
                toggleCamera();
                break;
            case R.id.btn_flash://闪光灯
                toggleFlash();
                break;
            case R.id.btn_upload://选择本地视频
                forwardChooseActivity();
                break;
            case R.id.btn_record://录制按钮
                switchRecord();
                break;
            case R.id.btn_beauty://美颜
                showBeauty();
                break;
            case R.id.btn_next://下一步
                toNext();
                break;
        }
    }

    private void toNext() {
        mBtnNext.setEnabled(false);
        mBtnRecord.setEnabled(false);
        showProccessDialog();
        stopRecord();
    }

    /**
     * 打开美颜面板
     */
    private void showBeauty() {
        if (mBeautyHolder != null) {
            mBeautyHolder.show();
        }
    }

    /**
     * 切换摄像头
     */
    private void toggleCamera() {
        if (mIsFlashOpen) {//如果闪光灯开启的话，关闭闪光灯
            toggleFlash();
        }
        mIsFrontCamera = !mIsFrontCamera;
        if (mTXCameraRecord != null) {
            mTXCameraRecord.switchCamera(mIsFrontCamera);
        }
    }

    /**
     * 切换闪光灯
     */
    private void toggleFlash() {
        if (!mIsFrontCamera) {//不是前置摄像头的话，才可以打开闪光灯
            if (mIsFlashOpen) {
                mBtnFlash.setImageResource(R.mipmap.icon_record_flash_close);
            } else {
                mBtnFlash.setImageResource(R.mipmap.icon_record_flash_open);
            }
            mIsFlashOpen = !mIsFlashOpen;
            mTXCameraRecord.toggleTorch(mIsFlashOpen);
        }
    }

    /**
     * 前往选择本地视频
     */
    private void forwardChooseActivity() {
        startActivityForResult(new Intent(mContext, VideoChooseActivity.class), Constants.VIDEO_CHOOSE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.VIDEO_CHOOSE_CODE && resultCode == RESULT_OK) {
            String path = data.getStringExtra(Constants.VIDEO_PATH);
            if (!TextUtils.isEmpty(path)) {
                mMusicBean = null;
                long duration = data.getLongExtra(Constants.VIDEO_DURATION, 0);
                forwardCompress(path, duration, Constants.VIDEO_FROM_CHOOSE);
            }
        }
    }


    /**
     * 前往压缩流程
     */
    private void forwardCompress(String path, long duration, int from) {
        Intent intent = new Intent(mContext, VideoCompressActivity.class);
        intent.putExtra(Constants.VIDEO_PATH, path);
        intent.putExtra(Constants.VIDEO_DURATION, duration);
        intent.putExtra(Constants.MUSIC_BEAN, mMusicBean);
        intent.putExtra(Constants.FROM, from);
        startActivity(intent);
        finish();
    }


    /**
     * 放弃音频的焦点
     */
    private void abandonAudioFocus() {
        if (mAudioManager == null) {
            return;
        }
        try {
            mAudioManager.abandonAudioFocus(mOnAudioFocusListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取音频焦点
     */
    private void requestAudioFocus() {
        if (mAudioManager == null) {
            return;
        }
        try {
            mAudioManager.requestAudioFocus(mOnAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {
            try {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    pauseRecord();
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    pauseRecord();
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {

                } else {
                    pauseRecord();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    //录制时候预处理的回调，美颜用
    private TXUGCRecord.VideoCustomProcessListener mVideoCustomProcessListener = new TXUGCRecord.VideoCustomProcessListener() {
        @Override
        public int onTextureCustomProcess(int i, int i1, int i2) {
            return mTiSDKManager.renderTexture2D(i, i1, i2, TiRotation.CLOCKWISE_ROTATION_0, false);
        }

        @Override
        public void onDetectFacePoints(float[] floats) {

        }

        @Override
        public void onTextureDestroyed() {

        }
    };


    //录制的回调
    private TXRecordCommon.ITXVideoRecordListener mITXVideoRecordListener = new TXRecordCommon.ITXVideoRecordListener() {
        @Override
        public void onRecordEvent(int event, Bundle bundle) {
            L.e("#onRecordComplete----event--->   " + event);
            if (event == TXRecordCommon.EVT_ID_PAUSE) {
                mRecordProgressView.clipComplete();
            } else if (event == TXRecordCommon.EVT_CAMERA_CANNOT_USE) {
                ToastUtil.show(R.string.摄像头打开失败+"，"+R.string.请检查权限);
            } else if (event == TXRecordCommon.EVT_MIC_CANNOT_USE) {
                ToastUtil.show(R.string.麦克风打开失败+"，"+R.string.请检查权限);
            }
        }

        @Override
        public void onRecordProgress(long milliSecond) {
            if (mRecordProgressView != null) {
                mRecordProgressView.setProgress((int) milliSecond);
            }
            if (mProgressTime != null) {
                mProgressTime.setText(String.format(Locale.CHINA, "00:%02d", Math.round(milliSecond / 1000f)));
            }
            if (milliSecond >= MIN_DURATION) {
                if (!mIsReachMinRecordDuration) {
                    mIsReachMinRecordDuration = true;
                    mBtnNext.setEnabled(true);
                    mBtnNext.setAlpha(1f);
                }
            }
            if (milliSecond >= MAX_DURATION) {
                if (!mIsReachMaxRecordDuration) {
                    mIsReachMaxRecordDuration = true;
                    mBtnRecord.setEnabled(false);
                    mBtnNext.setEnabled(false);
                    showProccessDialog();
                }
            }
        }

        @Override
        public void onRecordComplete(TXRecordCommon.TXRecordResult result) {
            hideProccessDialog();
            mTXRecordResult = result;
            long time = System.currentTimeMillis();
            L.e("#onRecordComplete", "#时间戳------->1   " + time);
            L.e("#onRecordComplete", "retCode------->     " + mTXRecordResult.retCode);
            if (mTXRecordResult.retCode < 0) {
                L.e("#onRecordComplete", "#时间戳------->2   " + time);
                ToastUtil.show(WordUtil.getString(R.string.record_failed));
                return;//不写这个的话，有时候会执行下面的else
            } else {
                L.e("#onRecordComplete", "#时间戳------->3   " + time);
                long duration = 0;
                if (mTXCameraRecord != null) {
                    duration = mTXCameraRecord.getPartsManager().getDuration();
                    mTXCameraRecord.getPartsManager().deleteAllParts();
                }
                L.e("#录制完成。跳转编辑页面------->");
                if (!TextUtils.isEmpty(mCoverPath)) {
                    File file = new File(mCoverPath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
                forwardCompress(mVideoPath, duration, Constants.VIDEO_FROM_RECORD);
            }
        }
    };

    /**
     * 各种美颜效果的回调
     */
    private BeautyHolder.EffectListener mEffectListener = new BeautyHolder.EffectListener() {

        /**
         * 设置滤镜
         */
        @Override
        public void onFilterChanged(Bitmap bitmap) {
            if (mTXCameraRecord != null) {
                mTXCameraRecord.setSpecialRatio(0.7f);//置滤镜效果程度，从0到1，越大滤镜效果越明显，默认取值0.5
                mTXCameraRecord.setFilter(bitmap);
            }
        }

        @Override
        public void onMeiBaiChanged(int progress) {
            if (mTiSDKManager != null) {
                mMeibai = progress;
                mTiSDKManager.setSkinWhitening(progress);
            }
        }

        @Override
        public void onMoPiChanged(int progress) {
            if (mTiSDKManager != null) {
                mMoPi = progress;
                mTiSDKManager.setSkinBlemishRemoval(progress);
            }
        }

        @Override
        public void onBaoHeChanged(int progress) {
            if (mTiSDKManager != null) {
                mBaoHe = progress;
                mTiSDKManager.setSkinSaturation(progress);
            }
        }

        @Override
        public void onFengNenChanged(int progress) {
            if (mTiSDKManager != null) {
                mFengNen = progress;
                mTiSDKManager.setSkinTenderness(progress);
            }
        }

        @Override
        public void onBigEyeChanged(int progress) {
            if (mTiSDKManager != null) {
                mBigEye = progress;
                mTiSDKManager.setEyeMagnifying(progress);
            }
        }

        @Override
        public void onFaceChanged(int progress) {
            if (mTiSDKManager != null) {
                mFace = progress;
                mTiSDKManager.setChinSlimming(progress);
            }
        }

        @Override
        public void onTieZhiChanged(String tieZhiName) {
            if (mTiSDKManager != null) {
                mTieZhi = tieZhiName;
                mTiSDKManager.setSticker(tieZhiName);
            }
        }

        @Override
        public void onHaHaChanged(TiDistortionEnum tiDistortionEnum) {
            if (mTiSDKManager != null) {
                mTiDistortionEnum = tiDistortionEnum;
                mTiSDKManager.setDistortionEnum(tiDistortionEnum);
            }
        }
    };
}
