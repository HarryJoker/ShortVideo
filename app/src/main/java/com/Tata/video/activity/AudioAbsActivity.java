package com.Tata.video.activity;

import android.content.Context;
import android.media.AudioManager;
import android.view.KeyEvent;

/**
 * Created by cxf on 2018/7/19.
 */

public abstract class AudioAbsActivity extends AbsActivity {

    private AudioManager mAudioManager;

    @Override
    protected void main() {
        super.main();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * 调节音量
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(mAudioManager!=null){
            //int volume;
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                    //volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    //onVolumeChanged(volume,mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                    //volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    //onVolumeChanged(volume,mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    //protected abstract void onVolumeChanged(int curVolume, int maxVolume);

}
