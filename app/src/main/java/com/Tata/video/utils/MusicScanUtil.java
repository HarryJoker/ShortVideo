package com.Tata.video.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.Tata.video.AppContext;
import com.Tata.video.bean.MusicChooseBean;
import com.Tata.video.interfaces.CommonCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/6/20.
 */

public class MusicScanUtil {

    private ContentResolver mContentResolver;
    private Handler mHandler;
    private CommonCallback<List<MusicChooseBean>> mCallback;
    private boolean mStop;

    public MusicScanUtil() {
        mContentResolver = AppContext.sInstance.getContentResolver();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                List<MusicChooseBean> videoList = (List<MusicChooseBean>) msg.obj;
                if (mCallback != null) {
                    mCallback.callback(videoList);
                }
            }
        };
    }

    public void getLocalMusicList(CommonCallback<List<MusicChooseBean>> callback) {
        if (callback == null) {
            return;
        }
        mCallback = callback;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mHandler != null) {
                    List<MusicChooseBean> musicList = getAllMusic();
                    Message msg = Message.obtain();
                    msg.obj = musicList;
                    mHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    private List<MusicChooseBean> getAllMusic() {
        List<MusicChooseBean> musicList = new ArrayList<>();
        String[] mediaColumns = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };
        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    mediaColumns, null, null, null);
            if (cursor != null) {
                while (!mStop && cursor.moveToNext()) {
                    String musicTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String musicPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    File file = new File(musicPath);
                    boolean canRead = file.canRead();
                    long length = file.length();
                    if (!canRead || length == 0) {
                        continue;
                    }
                    long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                    String musicName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                    if (TextUtils.isEmpty(musicName) || !musicName.endsWith(".mp3")) {
                        continue;
                    }
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    if ("<unknown>".equals(artist)) {
                        artist= "Unkown";
                    }
                    MusicChooseBean bean = new MusicChooseBean();
                    bean.setTitle(musicTitle);
                    bean.setPath(musicPath);
                    bean.setDuration(duration);
                    bean.setName(musicName);
                    bean.setArtist(artist);
                    musicList.add(bean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return musicList;
    }

    public void release() {
        mStop = true;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        mCallback = null;
    }

}
