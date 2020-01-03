package com.Tata.video.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.Tata.video.AppConfig;
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.activity.ReportActivity;
import com.Tata.video.adapter.VideoShareAdapter;
import com.Tata.video.bean.ConfigBean;
import com.Tata.video.bean.ShareBean;
import com.Tata.video.bean.UserBean;
import com.Tata.video.bean.VideoBean;
import com.Tata.video.custom.ImageTextView;
import com.Tata.video.event.VideoDeleteEvent;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.interfaces.CommonCallback;
import com.Tata.video.interfaces.OnItemClickListener;
import com.Tata.video.utils.DialogUitl;
import com.Tata.video.utils.DownloadUtil;
import com.Tata.video.utils.SharedSdkUitl;
import com.Tata.video.utils.ToastUtil;
import com.Tata.video.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

import cn.sharesdk.framework.Platform;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by cxf on 2018/6/12.
 */

public class VideoShareFragment extends DialogFragment implements View.OnClickListener, OnItemClickListener<ShareBean> {

    private Context mContext;
    private View mRootView;
    private RecyclerView mRecyclerView;
    private ConfigBean mConfigBean;
    private VideoBean mVideoBean;
    private SharedSdkUitl mSharedSdkUitl;
    private ActionListener mActionListener;
    private ImageTextView mBtnReport;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mContext = getActivity();
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.fragment_video_share, null);
        Dialog dialog = new Dialog(mContext, R.style.dialog2);
        dialog.setContentView(mRootView);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mVideoBean = getArguments().getParcelable(Constants.VIDEO_BEAN);
        mRootView.findViewById(R.id.btn_close).setOnClickListener(this);
        mBtnReport = (ImageTextView) mRootView.findViewById(R.id.btn_report);
        mBtnReport.setOnClickListener(this);
        mRootView.findViewById(R.id.btn_copy).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_black).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_save).setOnClickListener(this);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mSharedSdkUitl = new SharedSdkUitl();
        HttpUtil.getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean configBean) {
                mConfigBean = configBean;
                List<ShareBean> list = mSharedSdkUitl.getShareList(configBean.getShare_type());
                if (list != null && list.size() > 0) {
                    VideoShareAdapter adapter = new VideoShareAdapter(mContext, list);
                    adapter.setOnItemClickListener(VideoShareFragment.this);
                    mRecyclerView.setAdapter(adapter);
                }
            }
        });
        if (mVideoBean != null) {
            String videoUid = mVideoBean.getUid();
            if (!TextUtils.isEmpty(videoUid) && videoUid.equals(AppConfig.getInstance().getUid())) {
                mBtnReport.setImageResource(R.mipmap.icon_share_delete);
                mBtnReport.setText(WordUtil.getString(R.string.delete));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                close();
                break;
            case R.id.btn_report:
                report();
                break;
            case R.id.btn_copy:
                copy();
                break;
            case R.id.btn_black:
                black();
                break;
            case R.id.btn_save:
                save();
                break;
        }
    }

    private void close() {
        dismiss();
    }

    private void report() {
        if (mVideoBean != null) {
            String videoId = mVideoBean.getId();//视频id
            String videoUid = mVideoBean.getUid();//视频发布者的uid
            if (TextUtils.isEmpty(videoId) || TextUtils.isEmpty(videoUid)) {
                return;
            }
            if (!videoUid.equals(AppConfig.getInstance().getUid())) {
                Intent intent = new Intent(mContext, ReportActivity.class);
                intent.putExtra(Constants.VIDEO_ID, videoId);
                startActivity(intent);
            } else {
                final Dialog dialog = DialogUitl.loadingDialog(mContext, WordUtil.getString(R.string.processing));
                dialog.show();
                HttpUtil.deleteVideo(videoId, new HttpCallback() {

                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        dismiss();
                        EventBus.getDefault().post(new VideoDeleteEvent(mVideoBean));
                    }
                });
            }
        }
    }

    private void copy() {
        if (mVideoBean != null) {
            ClipboardManager cm = (ClipboardManager) mContext.getSystemService(CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("text", mVideoBean.getHref());
            cm.setPrimaryClip(clipData);
            ToastUtil.show(getString(R.string.clip_success));
            dismiss();
        }
    }

    private void black() {

    }

    private void save() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED
                    ) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constants.REQUEST_FILE_PERMISSION);
            } else {
                download();
            }
        } else {
            download();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_FILE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    download();
                } else {
                    ToastUtil.show(getString(R.string.storage_permission_refused));
                }
                break;
        }
    }

    private void download() {
        String href = mVideoBean.getHref();
        if (href == null) {
            return;
        }
        String fileName = href.substring(href.lastIndexOf("/"));
        final Dialog dialog = DialogUitl.loadingDialog(mContext, WordUtil.getString(R.string.downloading));
        dialog.show();
        new DownloadUtil().download("tag", AppConfig.VIDEO_PATH, fileName, mVideoBean.getHref(), new DownloadUtil.Callback() {
            @Override
            public void onSuccess(File file) {
                ToastUtil.show(WordUtil.getString(R.string.download_success));
                dialog.dismiss();
                DownloadUtil.saveVideoInfo(mContext, file.getAbsolutePath());
            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onError(Throwable e) {
                ToastUtil.show(WordUtil.getString(R.string.download_fail));
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onItemClick(ShareBean bean, int position) {
        UserBean u = mVideoBean.getUserinfo();
        if (mConfigBean != null && u != null) {
            mSharedSdkUitl.share(
                    bean.getType(),//分享的类型
                    mConfigBean.getVideo_share_title(),//分享的标题
                    u.getUser_nicename() + mConfigBean.getVideo_share_des(),//分享的话术
                    mVideoBean.getThumb(),//图片
                    AppConfig.HOST + "/index.php?g=appapi&m=video&a=index&videoid=" + mVideoBean.getId(),//链接
                    new SharedSdkUitl.ShareListener() {
                        @Override
                        public void onSuccess(Platform platform) {
                            //ToastUtil.show(WordUtil.getString(R.string.share_success));
                            if (mActionListener != null) {
                                mActionListener.onShareSuccess();
                            }
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

                        }
                    });
        }
    }

    public interface ActionListener {
        void onShareSuccess();
    }

    public void setActionListener(ActionListener listener) {
        mActionListener = listener;
    }

}
