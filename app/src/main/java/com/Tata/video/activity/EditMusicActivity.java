package com.Tata.video.activity;

import android.app.Dialog;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.Tata.video.AppConfig;
import com.Tata.video.AppContext;
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.bean.ConfigBean;
import com.Tata.video.bean.MusicChooseBean;
import com.Tata.video.custom.RangeSlider;
import com.Tata.video.cut.Mp3Cutter;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.interfaces.CommonCallback;
import com.Tata.video.upload.VideoTxUpload;
import com.Tata.video.utils.DateFormatUtil;
import com.Tata.video.utils.DialogUitl;
import com.Tata.video.utils.L;
import com.Tata.video.utils.ToastUtil;
import com.Tata.video.utils.WordUtil;
import com.tencent.cos.COSClient;
import com.tencent.cos.COSConfig;
import com.tencent.cos.common.COSEndPoint;
import com.ywl5320.libmusic.WlMusic;
import com.ywl5320.listener.OnPreparedListener;

import java.io.File;

/**
 * Created by cxf on 2018/8/6.
 */

public class EditMusicActivity extends AudioAbsActivity implements View.OnClickListener {

    private String mPath;
    private String mTitle;
    private long mDuration;
    private TextView mCutTip;
    private RangeSlider mRangeSlider;
    private String mCutMusicTip;
    private WlMusic mWlMusic;
    private long mStart1;
    private long mEnd1;
    private int mStart;
    private int mEnd;
    private boolean mPaused;
    private String mCutResultName;
    private String mCutResultPath;
    private Dialog mCutDialog;
    private Mp3Cutter mMp3Cutter;
    private Dialog mUploadDialog;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_edit_music;
    }

    @Override
    protected void main() {
        super.main();
        setTitle(WordUtil.getString(R.string.edit_music));
        findViewById(R.id.btn_cut).setOnClickListener(this);
        Intent intent = getIntent();
        mPath = intent.getStringExtra(Constants.MUSIC_PATH);
        mTitle = intent.getStringExtra(Constants.MUSIC_TITLE);
        TextView musicTitle = (TextView) findViewById(R.id.music_title);
        musicTitle.setText(mTitle);
        mDuration = intent.getLongExtra(Constants.MUSIC_DURATION, 0);
        if (TextUtils.isEmpty(mPath) || mDuration == 0) {
            return;
        }
        mCutMusicTip = WordUtil.getString(R.string.cut_music_tip);
        mCutTip = (TextView) findViewById(R.id.cut_tip);
        showCutTime(0, mDuration);
        mRangeSlider = (RangeSlider) findViewById(R.id.bgm_range_slider);
        mRangeSlider.setRangeChangeListener(new RangeSlider.OnRangeChangeListener() {
            @Override
            public void onKeyDown(int type) {

            }

            @Override
            public void onKeyUp(int type, int leftPinIndex, int rightPinIndex) {
                long startTime = mDuration * leftPinIndex / 100;
                long endTime = mDuration * rightPinIndex / 100;
                showCutTime(startTime, endTime);
                mStart1 = startTime;
                mEnd1 = endTime;
                mStart = (int) (startTime / 1000);
                mEnd = (int) (endTime / 1000);
                mWlMusic.playNext(mPath);
            }
        });
        mStart = 0;
        mStart1 = 0;
        mEnd = (int) (mDuration / 1000);
        mEnd1 = mDuration;
        mWlMusic = WlMusic.getInstance();
        mWlMusic.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared() {
                mWlMusic.playCutAudio(mStart, mEnd);
            }
        });
        mWlMusic.setSource(mPath);
        mWlMusic.prePared();
    }


    private void showCutTime(long startTime, long endTime) {
        String cutTime = String.format("%.2f", (endTime - startTime) / 1000f) + "s";
        mCutTip.setText(mCutMusicTip + cutTime);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWlMusic != null) {
            mWlMusic.pause();
        }
        mPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPaused) {
            mPaused = false;
            if (mWlMusic != null) {
                mWlMusic.resume();
            }
        }
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.GET_CONFIG);
        HttpUtil.cancel(HttpUtil.ADD_MY_MUSIC);
        if (mWlMusic != null) {
            mWlMusic.stop();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (mWlMusic != null) {
            mWlMusic.stop();
        }
        mCutDialog = DialogUitl.loadingDialog(mContext,getResources().getString(R.string.剪辑中));
        mCutDialog.show();
        if (mMp3Cutter == null) {
            mMp3Cutter = new Mp3Cutter(new File(mPath));
        }
        if (!TextUtils.isEmpty(mCutResultPath)) {
            File file = new File(mCutResultPath);
            if (file.exists()) {
                file.delete();
            }
        }
        mCutResultName = DateFormatUtil.getCurTimeString() + ".mp3";
        mCutResultPath = AppConfig.VIDEO_MUSIC_PATH + mCutResultName;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mMp3Cutter.generateNewMp3ByTime(mCutResultPath, mStart1, mEnd1);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mCutDialog != null) {
                                mCutDialog.dismiss();
                            }
                            File cutResult = new File(mCutResultPath);
                            if (cutResult.exists() && cutResult.length() > 0) {
                                DialogUitl.showSimpleDialog(mContext, R.string.确定将剪辑的音乐上传至音乐库+"？", new DialogUitl.SimpleDialogCallback() {
                                    @Override
                                    public void onComfirmClick() {
                                        uploadMusic();
                                    }

                                    @Override
                                    public void onCancelClick() {
                                        File file = new File(mCutResultPath);
                                        if (file.exists()) {
                                            file.delete();
                                        }
                                    }
                                });
                            } else {
                                ToastUtil.show(WordUtil.getString(R.string.cut_music_failed));
                            }
                        }
                    });
                }
            }
        }).start();
    }


    private void uploadMusic() {
        mUploadDialog = DialogUitl.loadingDialog(mContext, getResources().getString(R.string.上传中));
        mUploadDialog.show();
        HttpUtil.getCreateNonreusableSignature(null, mCutResultName, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    final String videoSign = obj.getString("videosign");
                    final String bucketName = obj.getString("bucketname");
                    String appid = obj.getString("appid");
                    COSConfig cosConfig = new COSConfig();
                    cosConfig.setEndPoint(COSEndPoint.COS_SH);
                    final COSClient client = new COSClient(AppContext.sInstance, appid, cosConfig, bucketName);
                    ConfigBean configBean = AppConfig.getInstance().getConfig();
                    if (configBean != null) {
                        doUpload(client, configBean.getTxvideofolder() + "/" + mCutResultName, videoSign, bucketName);
                    } else {
                        HttpUtil.getConfig(new CommonCallback<ConfigBean>() {
                            @Override
                            public void callback(ConfigBean obj) {
                                doUpload(client, obj.getTxvideofolder() + "/" + mCutResultName, videoSign, bucketName);
                            }
                        });
                    }
                }
            }
        });
    }

    private void doUpload(COSClient client, String txvideofolder, String videoSign, String bucketName) {
        VideoTxUpload.getInstance().uploadFile(client, txvideofolder, mCutResultPath, videoSign, bucketName, new VideoTxUpload.OnSuccessCallback() {
            @Override
            public void onUploadSuccess(String url) {
                L.e("#上传音乐成功----->" + url);
                String length = MusicChooseBean.castDurationString(mEnd1 - mStart1);
                HttpUtil.addMyMusic(mTitle, length, url, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0) {
                            ToastUtil.show(R.string.音乐已上传至后台审核+"，\n"+R.string.审核通过则自动加入音乐库);
                        }
                    }

                    @Override
                    public void onFinish() {
                        if (mUploadDialog != null) {
                            mUploadDialog.dismiss();
                        }
                    }
                });
            }
        });
    }

}
