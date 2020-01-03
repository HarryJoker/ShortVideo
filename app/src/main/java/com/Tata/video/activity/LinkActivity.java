package com.Tata.video.activity;

import android.app.Dialog;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.Tata.video.AppConfig;
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.glide.ImgLoader;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.utils.DateFormatUtil;
import com.Tata.video.utils.DialogUitl;
import com.Tata.video.utils.DownloadUtil;
import com.Tata.video.utils.ToastUtil;
import com.Tata.video.utils.WordUtil;

import java.io.File;

/**
 * Created by cxf on 2018/8/13.
 */

public class LinkActivity extends AbsActivity implements View.OnClickListener {

    private EditText mEditText;
    private String mResultVideoUrl;
    private String mType;
    private String mVideoId;
    private String mTag;
    private Dialog mDialog;
    private View mBtnNext;
    private ImageView mImg;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_link;
    }

    @Override
    protected void main() {
        setTitle(R.string.输入链接);
        mBtnNext = findViewById(R.id.btn_next);
        mBtnNext.setOnClickListener(this);
        mEditText = findViewById(R.id.edit);
        mImg = (ImageView) findViewById(R.id.img);
        ImgLoader.display(R.mipmap.bg_link, mImg);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_next:
                toNext();
                break;
        }
    }

    private void toNext() {
        String url = mEditText.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            ToastUtil.show(getResources().getString(R.string.请输入视频链接));
            return;
        }
        mBtnNext.setClickable(false);
        mDialog = DialogUitl.loadingDialog(mContext, WordUtil.getString(R.string.downloading));
        mDialog.show();
        HttpUtil.getOutVideoUrl(url, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    mResultVideoUrl = obj.getString("video");
                    mType = obj.getString("type");
                    mVideoId = obj.getString("videoid");
                    downloadVideo();
                } else {
                    if (code == 1003) {
                        ToastUtil.show(getResources().getString(R.string.该视频已被使用));
                    } else {
                        ToastUtil.show(getResources().getString(R.string.获取视频链接失败));
                    }
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                }
            }

            @Override
            public void onFinish() {
                mBtnNext.setClickable(true);
            }
        });
    }


    private void downloadVideo() {
        String fileName = "android_" + DateFormatUtil.getCurTimeString() + ".mp4";
        mTag = fileName;
        new DownloadUtil().download(mTag, AppConfig.VIDEO_PATH, fileName, mResultVideoUrl, new DownloadUtil.Callback() {
            @Override
            public void onSuccess(File file) {
                ToastUtil.show(WordUtil.getString(R.string.download_success));
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                String videoPath = file.getAbsolutePath();
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(videoPath); //在获取前，设置文件路径（应该只能是本地路径）
                String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                retriever.release(); //释放
                long duration = 0;
                if (!TextUtils.isEmpty(durationStr)) {
                    duration = Long.parseLong(durationStr);
                }
                DownloadUtil.saveVideoInfo(mContext, videoPath, duration);
                Intent intent = new Intent(mContext, VideoCompressActivity.class);
                intent.putExtra(Constants.VIDEO_PATH, videoPath);
                intent.putExtra(Constants.VIDEO_DURATION, duration);
                intent.putExtra(Constants.FROM, Constants.VIDEO_FROM_CHOOSE);
                intent.putExtra(Constants.LINK, 1);
                intent.putExtra(Constants.LINK_TYPE, mType);
                intent.putExtra(Constants.LINK_VIDEO_ID, mVideoId);
                startActivity(intent);
            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onError(Throwable e) {
                ToastUtil.show(WordUtil.getString(R.string.download_fail));
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.GET_OUT_VIDEO_URL);
        if (!TextUtils.isEmpty(mTag)) {
            HttpUtil.cancel(mTag);
        }
        super.onDestroy();
    }
}
