package com.Tata.video.upload;

import android.os.Handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.cos.COSClient;
import com.tencent.cos.COSConfig;
import com.tencent.cos.common.COSEndPoint;
import com.tencent.cos.model.COSRequest;
import com.tencent.cos.model.COSResult;
import com.tencent.cos.model.PutObjectRequest;
import com.tencent.cos.model.PutObjectResult;
import com.tencent.cos.task.listener.IUploadTaskListener;
import com.Tata.video.AppConfig;
import com.Tata.video.AppContext;
import com.Tata.video.bean.ConfigBean;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.utils.L;

/**
 * Created by cxf on 2018/5/21.
 */

public class VideoTxUpload implements UploadStrategy {

    private static VideoTxUpload sInstance;
    private Handler mHandler;

    private VideoTxUpload() {
        mHandler=new Handler();
    }

    public static VideoTxUpload getInstance() {
        if (sInstance == null) {
            synchronized (VideoTxUpload.class) {
                if (sInstance == null) {
                    sInstance = new VideoTxUpload();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void upload(final VideoUploadBean bean, final UploadCallback callback) {
        final String videoName = bean.getVideoName();
        final String imgName = bean.getImgName();
        HttpUtil.getCreateNonreusableSignature(imgName, videoName, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    final String imgSign = obj.getString("imgsign");
                    final String videoSign = obj.getString("videosign");
                    final String bucketName = obj.getString("bucketname");
                    COSConfig cosConfig = new COSConfig();
                    cosConfig.setEndPoint(COSEndPoint.COS_SH);
                    final COSClient client = new COSClient(AppContext.sInstance, obj.getString("appid"), cosConfig, bucketName);
                    final ConfigBean configBean = AppConfig.getInstance().getConfig();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            uploadFile(client, configBean.getTxvideofolder() + "/" + videoName, bean.getVideoPath(), videoSign, bucketName, new OnSuccessCallback() {
                                @Override
                                public void onUploadSuccess(String url) {
                                    bean.setVideoName(url);
                                    uploadFile(client, configBean.getTximgfolder() + "/" + imgName, bean.getImgPath(), imgSign, bucketName, new OnSuccessCallback() {
                                        @Override
                                        public void onUploadSuccess(String url) {
                                            bean.setImgName(url);
                                            callback.onSuccess(bean);
                                        }
                                    });
                                }
                            });
                        }
                    }).start();
                }
            }

        });
    }


    /**
     * 上传文件
     *
     * @param cosPath
     * @param srcPath
     * @param sign
     */
    public void uploadFile(COSClient client, String cosPath, String srcPath, String sign, String bucketName, final OnSuccessCallback callback) {
        PutObjectRequest putObjectRequest = new PutObjectRequest();
        putObjectRequest.setBucket(bucketName);
        putObjectRequest.setCosPath(cosPath);
        putObjectRequest.setSrcPath(srcPath);
        putObjectRequest.setSign(sign);
        putObjectRequest.setListener(new IUploadTaskListener() {
            @Override
            public void onSuccess(COSRequest cosRequest, COSResult cosResult) {
                final PutObjectResult result = (PutObjectResult) cosResult;
                if (result != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            L.e("#上传视频---上传结果---->", result.access_url);//加速域名
                            if (callback != null) {
                                callback.onUploadSuccess(result.access_url);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailed(COSRequest COSRequest, final COSResult cosResult) {
                L.e("#上传视频---->", "#上传出错： ret =" + cosResult.code + "; msg =" + cosResult.msg);
            }

            @Override
            public void onProgress(COSRequest cosRequest, final long currentSize, final long totalSize) {
                int progress = (int) ((float) currentSize / totalSize * 100f);
                L.e("#上传视频---进度--->" + progress);
            }

            @Override
            public void onCancel(COSRequest cosRequest, COSResult cosResult) {
            }
        });
        client.putObject(putObjectRequest);
    }

    public interface OnSuccessCallback {
        void onUploadSuccess(String url);
    }
}
