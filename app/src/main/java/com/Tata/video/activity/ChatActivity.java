package com.Tata.video.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.Tata.video.AppConfig;
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.adapter.ChatAdapter;
import com.Tata.video.adapter.FacePagerAdapter;
import com.Tata.video.bean.ChatMessageBean;
import com.Tata.video.bean.ConfigBean;
import com.Tata.video.bean.UserBean;
import com.Tata.video.custom.AnimImageView;
import com.Tata.video.custom.ChatImageHolder;
import com.Tata.video.custom.MyImageView2;
import com.Tata.video.custom.TextRender;
import com.Tata.video.event.ChatRoomCloseEvent;
import com.Tata.video.event.FollowEvent;
import com.Tata.video.event.VisibleHeightEvent;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.interfaces.OnFaceClickListener;
import com.Tata.video.jpush.JMessageUtil;
import com.Tata.video.presenter.GlobalLayoutPresenter;
import com.Tata.video.utils.AsrUtil;
import com.Tata.video.utils.DateFormatUtil;
import com.Tata.video.utils.DialogUitl;
import com.Tata.video.utils.DpUtil;
import com.Tata.video.utils.FrameAnimUtil;
import com.Tata.video.utils.L;
import com.Tata.video.utils.MediaRecordUtil;
import com.Tata.video.utils.ScreenDimenUtil;
import com.Tata.video.utils.ToastUtil;
import com.Tata.video.utils.VoiceMediaPlayerUtil;
import com.Tata.video.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import cn.jpush.im.api.BasicCallback;

/**
 * Created by cxf on 2018/7/10.
 */

public class ChatActivity extends AudioAbsActivity {

    private static final int INPUT_NONE = 0;//无表情，无键盘
    private static final int INPUT_FACE = 1;//表情
    private static final int INPUT_KEY_BOARD = 2;//键盘
    private static final int INPUT_MORE = 3;//更多输入
    private static final int REQUEST_CODE_CHOOSE_IMAGE = 100;//选择图片
    private static final int REQUEST_CODE_PHOTO = 101;//拍照
    private static final int REQUEST_CODE_LOCATION = 102;//定位
    private static final int WHAT_END_RECORD_AUDIO = 201;//录音
    private static final int MAX_RECORD_AUDIO_DURATION = 60000;
    private UserBean mToUserBean;
    private String mToUserId;
    private ViewGroup mRoot1;
    private View mRootView;
    private MyImageView2 mBtnFace;
    private EditText mInput;
    private TextView mBtnRecordVoice;
    private RecyclerView mRecyclerView;
    private View mFaceGroup;
    private View mMoreGroup;
    private View mBtnHideSoftInput;
    private View mLoading;
    private ViewPager mViewPager;
    private RadioGroup mRadioGroup;
    private GlobalLayoutPresenter mPresenter;
    private boolean mPaused;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mContentHeight;
    private int mStatusBarHeight;
    private int mCurHeight;
    private int mInputStatus = INPUT_NONE;
    private InputMethodManager imm;
    private int mFaceHeight;
    private int mMoreHeight;
    private boolean mVoiceInput;
    private boolean mFaceShowing;
    private boolean mMoreShowing;
    private ChatAdapter mChatAdapter;
    private long mLastSendTime;//上一次发消息的时间
    private ChatMessageBean mCurMessageBean;//待发送的文本消息
    private File mCameraResult;//拍照的结果
    private long mLastClickTime;//上一次点击按钮的时间
    private boolean mAudioRecording;
    private MediaRecordUtil mMediaRecordUtil;
    private String mPressSay;
    private String mReleaseEnd;
    private String mPleaseSay;
    private Drawable mUnPressDrawable;
    private Drawable mPressedDrawable;
    private File mRecordVoiceFile;//录音文件
    private long mRecordVoiceDuration;//录音时长
    private VoiceMediaPlayerUtil mVoiceMediaPlayerUtil;
    private Handler mHandler;
    private ImageView mBtnVoice;
    private View mVoiceInputGroup;
    private View mVoiceInputTip;
    private TextView mVoiceInputTextView;
    private AnimImageView mBtnVoiceInput;
    private View mBtnVoiceInputClose;
    private boolean mVoiceToTextShowing;
    private View mBtnVoiceCancel;
    private View mBtnVoiceSend;
    private AsrUtil mAsrUtil;//语音识别
    private ObjectAnimator mVoiceInputShowAnimator;
    private ObjectAnimator mVoiceInputHideAnimator;
    private ChatImageHolder mChatImageHolder;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat;
    }

    @Override
    protected void main() {
        super.main();
        mToUserBean = getIntent().getParcelableExtra(Constants.USER_BEAN);
        if (mToUserBean == null) {
            return;
        }
        mToUserId = mToUserBean.getId();
        setTitle(mToUserBean.getUser_nicename());
        mRoot1 = (ViewGroup) findViewById(R.id.root1);
        mRootView = findViewById(R.id.root);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        List<ChatMessageBean> list = JMessageUtil.getInstance().getChatMessageList(mToUserId);
        mChatAdapter = new ChatAdapter(mContext, list, mToUserBean);
        mChatAdapter.setActionListener(mActionListener);
        mRecyclerView.setAdapter(mChatAdapter);
        mBtnFace = (MyImageView2) findViewById(R.id.btn_face);
        mInput = (EditText) findViewById(R.id.input);
        mInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendText();
                    return true;
                }
                return false;
            }
        });
        mBtnRecordVoice = (TextView) findViewById(R.id.btn_record_voice);
        mBtnRecordVoice.setOnTouchListener(mOnTouchListener);
        mFaceGroup = findViewById(R.id.face_group);
        mMoreGroup = findViewById(R.id.more_group);
        mBtnHideSoftInput = findViewById(R.id.btn_hide_soft_input);
        mLoading = findViewById(R.id.loading);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(new FacePagerAdapter(mContext, mOnFaceClickListener));
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        mPresenter = new GlobalLayoutPresenter(this, mRootView);
        mPresenter.addLayoutListener();
        ScreenDimenUtil screenDimenUtil = ScreenDimenUtil.getInstance();
        mScreenWidth = screenDimenUtil.getScreenHeight();
        mScreenHeight = screenDimenUtil.getScreenHeight();
        mContentHeight = screenDimenUtil.getContentHeight();
        mStatusBarHeight = screenDimenUtil.getStatusBarHeight();
        mCurHeight = mScreenHeight;
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mFaceHeight = DpUtil.dp2px(220);
        mMoreHeight = DpUtil.dp2px(154);
        EventBus.getDefault().register(this);
        mPressSay = WordUtil.getString(R.string.press_say);
        mReleaseEnd = WordUtil.getString(R.string.release_end);
        mPleaseSay = WordUtil.getString(R.string.please_say);
        mUnPressDrawable = ContextCompat.getDrawable(mContext, R.drawable.bg_press_say_unpressed);
        mPressedDrawable = ContextCompat.getDrawable(mContext, R.drawable.bg_press_say_pressed);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case WHAT_END_RECORD_AUDIO:
                        stopRecordVoice();
                        break;
                }
            }
        };
        mVoiceInputGroup = findViewById(R.id.voice_input_group);
        mVoiceInputTip = findViewById(R.id.voice_input_tip);
        mVoiceInputTextView = (TextView) findViewById(R.id.voice_input_text);
        mBtnVoice = (ImageView) findViewById(R.id.btn_voice);
        mBtnVoiceInput = (AnimImageView) findViewById(R.id.btn_voice_input);
        mBtnVoiceInput.setImgList(FrameAnimUtil.getChatVoiceAnimInput());
        mBtnVoiceInput.setOnTouchListener(mOnVoiceInputTouchListener);
        mBtnVoiceInputClose = findViewById(R.id.btn_voice_input_close);
        mBtnVoiceCancel = findViewById(R.id.btn_voice_cancel);
        mBtnVoiceSend = findViewById(R.id.btn_voice_send);
        mAsrUtil = new AsrUtil(mContext);
        mAsrUtil.setAsrCallback(new AsrUtil.AsrCallback() {
            @Override
            public void onResult(String result) {
                if (!TextUtils.isEmpty(result)) {
                    mVoiceInputTextView.setText(result);
                }
            }
        });
        TimeInterpolator interpolator = new AccelerateDecelerateInterpolator();
        mVoiceInputShowAnimator = ObjectAnimator.ofFloat(mVoiceInputGroup, "translationY", 0);
        mVoiceInputShowAnimator.setDuration(200);
        mVoiceInputShowAnimator.setInterpolator(interpolator);
        mVoiceInputHideAnimator = ObjectAnimator.ofFloat(mVoiceInputGroup, "translationY", DpUtil.dp2px(200));
        mVoiceInputHideAnimator.setDuration(200);
        mVoiceInputHideAnimator.setInterpolator(interpolator);
        mVoiceInputHideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mVoiceToTextShowing = false;
            }
        });

    }

    private View.OnTouchListener mOnVoiceInputTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent e) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    onVoiceInputDown();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    onVoiceInputUp();
                    break;
            }
            return true;
        }
    };

    /**
     * 语音识别按下
     */
    private void onVoiceInputDown() {
        mBtnVoiceInput.startAnim();
        if (mBtnVoiceCancel.getVisibility() == View.VISIBLE) {
            mBtnVoiceCancel.setVisibility(View.INVISIBLE);
        }
        if (mBtnVoiceSend.getVisibility() == View.VISIBLE) {
            mBtnVoiceSend.setVisibility(View.INVISIBLE);
        }
        if (mVoiceInputTip.getVisibility() == View.VISIBLE) {
            mVoiceInputTip.setVisibility(View.INVISIBLE);
        }
        if (mBtnVoiceInputClose.getVisibility() == View.VISIBLE) {
            mBtnVoiceInputClose.setVisibility(View.INVISIBLE);
        }
        mVoiceInputTextView.setText(mPleaseSay);
        if (mAsrUtil != null) {
            mAsrUtil.start();
        }
    }

    /**
     * 语音识别抬起
     */
    private void onVoiceInputUp() {
        mBtnVoiceInput.stopAnim();
        if (mAsrUtil != null) {
            mAsrUtil.stop();
        }
        String s = mVoiceInputTextView.getText().toString();
        if (!mPleaseSay.equals(s)) {
            if (mBtnVoiceCancel.getVisibility() != View.VISIBLE) {
                mBtnVoiceCancel.setVisibility(View.VISIBLE);
            }
            if (mBtnVoiceSend.getVisibility() != View.VISIBLE) {
                mBtnVoiceSend.setVisibility(View.VISIBLE);
            }
        } else {
            mVoiceInputTextView.setText("");
            if (mBtnVoiceInputClose.getVisibility() != View.VISIBLE) {
                mBtnVoiceInputClose.setVisibility(View.VISIBLE);
            }
            if (mVoiceInputTip.getVisibility() != View.VISIBLE) {
                mVoiceInputTip.setVisibility(View.VISIBLE);
            }
        }
    }


    private OnFaceClickListener mOnFaceClickListener = new OnFaceClickListener() {
        @Override
        public void onFaceClick(String str, int faceImageRes) {
            Editable editable = mInput.getText();
            editable.insert(mInput.getSelectionStart(), TextRender.getFaceImageSpan(str, faceImageRes));
        }

        @Override
        public void onFaceDeleteClick() {
            int selection = mInput.getSelectionStart();
            String text = mInput.getText().toString();
            if (selection > 0) {
                String text2 = text.substring(selection - 1, selection);
                if ("]".equals(text2)) {
                    int start = text.lastIndexOf("[", selection);
                    if (start >= 0) {
                        mInput.getText().delete(start, selection);
                    } else {
                        mInput.getText().delete(selection - 1, selection);
                    }
                } else {
                    mInput.getText().delete(selection - 1, selection);
                }

            }
        }
    };

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            RadioButton radioButton = (RadioButton) mRadioGroup.getChildAt(position);
            if (radioButton != null) {
                radioButton.setChecked(true);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent e) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startRecordVoice();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    stopRecordVoice();
                    break;
            }
            return true;
        }
    };

    /**
     * 聊天消息文件下载成功的回调
     */
    private ChatAdapter.ActionListener mActionListener = new ChatAdapter.ActionListener() {
        @Override
        public void onImageClick(String filePath, int x, int y, final int wdith, final int height, List<String> list, int position) {
            if (mChatImageHolder == null) {
                mChatImageHolder = new ChatImageHolder(mContext, mRoot1, mScreenWidth, mScreenHeight);
            }
            mChatImageHolder.show(filePath, x, y, wdith, height, list, position);
        }

        @Override
        public void onVoiceDownload(ChatMessageBean bean, String filePath) {
            if (mVoiceMediaPlayerUtil == null) {
                mVoiceMediaPlayerUtil = new VoiceMediaPlayerUtil(mContext);
                mVoiceMediaPlayerUtil.setMediaPlayCallback(new VoiceMediaPlayerUtil.MediaPlayCallback() {
                    @Override
                    public void onPlayStart(ChatMessageBean bean) {
                        if (mChatAdapter != null) {
                            mChatAdapter.updateVoiceItem(bean, true);
                        }
                    }

                    @Override
                    public void onPlayEnd(ChatMessageBean bean) {
                        if (mChatAdapter != null) {
                            mChatAdapter.updateVoiceItem(bean, false);
                        }
                    }

                    @Override
                    public void onPlayError(ChatMessageBean bean) {
                        if (mChatAdapter != null) {
                            mChatAdapter.updateVoiceItem(bean, false);
                        }
                    }
                });
            }
            mVoiceMediaPlayerUtil.startPlay(bean, filePath);
        }

        @Override
        public void onStopVoice() {
            if (mVoiceMediaPlayerUtil != null) {
                mVoiceMediaPlayerUtil.stopPlay();
            }
        }
    };

    /**
     * 开始录音
     */
    private void startRecordVoice() {
        mAudioRecording = true;
        mBtnRecordVoice.setBackground(mPressedDrawable);
        mBtnRecordVoice.setText(mReleaseEnd);
        if (mMediaRecordUtil == null) {
            mMediaRecordUtil = new MediaRecordUtil();
        }
        File dir = new File(AppConfig.VIDEO_MUSIC_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        mRecordVoiceFile = new File(dir, DateFormatUtil.getCurTimeString() + ".m4a");
        mMediaRecordUtil.startRecord(mRecordVoiceFile.getAbsolutePath());
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(WHAT_END_RECORD_AUDIO, MAX_RECORD_AUDIO_DURATION);
        }
    }

    /**
     * 结束录音
     */
    private void stopRecordVoice() {
        if (!mAudioRecording) {
            return;
        }
        mAudioRecording = false;
        if (mHandler != null) {
            mHandler.removeMessages(WHAT_END_RECORD_AUDIO);
        }
        mBtnRecordVoice.setBackground(mUnPressDrawable);
        mBtnRecordVoice.setText(mPressSay);
        mRecordVoiceDuration = mMediaRecordUtil.stopRecord();
        if (mRecordVoiceDuration < 2000) {
            ToastUtil.show(WordUtil.getString(R.string.record_audio_too_short));
            deleteVoiceFile();
        } else {
            mCurMessageBean = JMessageUtil.getInstance().createVoiceMessage(mToUserId, mRecordVoiceFile, mRecordVoiceDuration);
            if (mCurMessageBean != null) {
                sendMessage();
            } else {
                deleteVoiceFile();
            }
        }
    }

    private void deleteVoiceFile() {
        if (mRecordVoiceFile != null && mRecordVoiceFile.exists()) {
            mRecordVoiceFile.delete();
        }
        mRecordVoiceFile = null;
        mRecordVoiceDuration = 0;
    }

    public void chatClick(View v) {
        long curTime = System.currentTimeMillis();
        if (curTime - mLastClickTime < 500) {
            return;
        }
        mLastClickTime = curTime;
        switch (v.getId()) {
            case R.id.btn_voice:
                toggleVoiceInput();
                break;
            case R.id.btn_face:
                toggleFace();
                break;
            case R.id.btn_more:
                showMore();
                break;
            case R.id.btn_hide_soft_input:
                if (mVoiceToTextShowing) {
                    hideVoiceToText();
                } else {
                    hideSoftInput();
                }
                break;
            case R.id.btn_send:
                sendText();
                break;
            case R.id.btn_more_img:
                checkFilePermission();
                break;
            case R.id.btn_more_camera:
                checkCameraPermission();
                break;
            case R.id.btn_more_voice:
                checkAsrPermission();
                break;
            case R.id.btn_more_location:
                checkLocationPermission();
                break;
            case R.id.btn_voice_input_close:
                hideVoiceToText();
                break;
            case R.id.btn_voice_cancel:
                voiceInputCancel();
                break;
            case R.id.btn_voice_send:
                voiceInputSend();
                break;
            case R.id.btn_setting:
                openMoreWindow();
                break;
        }
    }

    /**
     * 打开更多的弹窗
     */
    private void openMoreWindow() {
        if (AppConfig.getInstance().isLogin()) {
            HttpUtil.checkBlack(mToUserId, mCheckBlackCallback);
        } else {
            LoginActivity.forwardLogin(mContext);
        }
    }

    private HttpCallback mCheckBlackCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0 && info.length > 0) {
                JSONObject obj = JSON.parseObject(info[0]);
                String black = obj.getIntValue("u2t") == 0 ? WordUtil.getString(R.string.black) : WordUtil.getString(R.string.cancel_black);
                DialogUitl.showUserMoreDialog(mContext,
                        new String[]{WordUtil.getString(R.string.report), black},
                        new int[]{0xff1271FB, 0xffff0000}, new DialogUitl.StringArrayDialogCallback() {
                            @Override
                            public void onItemClick(String text, int position) {
                                if (position == 0) {
                                    reportUser();
                                } else {
                                    setBlack();
                                }
                            }
                        }
                );
            }
        }
    };

    /**
     * 举报用户
     */
    private void reportUser() {
        String url = AppConfig.HOST + "/index.php?g=Appapi&m=Userreport&a=index&uid=" + AppConfig.getInstance().getUid() + "&token=" + AppConfig.getInstance().getToken() + "&touid=" + mToUserId;
        Intent intent = new Intent(mContext, WebUploadImgActivity.class);
        intent.putExtra(Constants.URL, url);
        startActivity(intent);
    }

    /**
     * 拉黑对方或取消拉黑
     */
    private void setBlack() {
        HttpUtil.setBlack(mToUserId, mSetBlackCallback);
    }

    private HttpCallback mSetBlackCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0 && info.length > 0) {
                int res = JSON.parseObject(info[0]).getIntValue("isblack");
                if (res == 1) {//已拉黑
                    ToastUtil.show(WordUtil.getString(R.string.set_black_success));
                    //拉黑的时候把关注取消
                    EventBus.getDefault().post(new FollowEvent(mToUserId, 0));
                } else if (res == 0) {//解除拉黑
                    ToastUtil.show(getString(R.string.cancel_black_success));
                }
            }
        }
    };


    private void voiceInputCancel() {
        mVoiceInputTextView.setText("");
        if (mVoiceInputTip.getVisibility() != View.VISIBLE) {
            mVoiceInputTip.setVisibility(View.VISIBLE);
        }
        if (mBtnVoiceInputClose.getVisibility() != View.VISIBLE) {
            mBtnVoiceInputClose.setVisibility(View.VISIBLE);
        }
        if (mBtnVoiceCancel.getVisibility() == View.VISIBLE) {
            mBtnVoiceCancel.setVisibility(View.INVISIBLE);
        }
        if (mBtnVoiceSend.getVisibility() == View.VISIBLE) {
            mBtnVoiceSend.setVisibility(View.INVISIBLE);
        }

    }

    private void voiceInputSend() {
        String s = mVoiceInputTextView.getText().toString().trim();
        if (!TextUtils.isEmpty(s)) {
            ChatMessageBean messageBean = JMessageUtil.getInstance().createTextMessage(mToUserId, s);
            if (messageBean != null) {
                mCurMessageBean = messageBean;
                sendMessage();
            } else {
                ToastUtil.show(WordUtil.getString(R.string.message_send_failed));
            }
            mVoiceInputTextView.setText("");
        }
        if (mVoiceInputTip.getVisibility() != View.VISIBLE) {
            mVoiceInputTip.setVisibility(View.VISIBLE);
        }
        if (mBtnVoiceInputClose.getVisibility() != View.VISIBLE) {
            mBtnVoiceInputClose.setVisibility(View.VISIBLE);
        }
        if (mBtnVoiceCancel.getVisibility() == View.VISIBLE) {
            mBtnVoiceCancel.setVisibility(View.INVISIBLE);
        }
        if (mBtnVoiceSend.getVisibility() == View.VISIBLE) {
            mBtnVoiceSend.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 显示语音转文字
     */
    private void showVoiceToText() {
        mVoiceToTextShowing = true;
        mVoiceInputShowAnimator.start();
    }

    /**
     * 隐藏语音转文字
     */
    private void hideVoiceToText() {
        mVoiceInputHideAnimator.start();
    }

    private void toggleVoiceInput() {
        if (mVoiceInput) {
            mVoiceInput = false;
            mMoreShowing = false;
            mFaceShowing = false;
            if (mBtnHideSoftInput != null && mBtnHideSoftInput.getVisibility() != View.VISIBLE) {
                mBtnHideSoftInput.setVisibility(View.VISIBLE);
            }
            if (mBtnRecordVoice != null && mBtnRecordVoice.getVisibility() == View.VISIBLE) {
                mBtnRecordVoice.setVisibility(View.INVISIBLE);
            }
            if (mInput != null && mInput.getVisibility() != View.VISIBLE) {
                mInput.setVisibility(View.VISIBLE);
                mInput.requestFocus();
            }
            imm.showSoftInput(mInput, InputMethodManager.SHOW_FORCED);
            mBtnFace.setImageResource(R.mipmap.icon_chat_face);
            mInputStatus = INPUT_KEY_BOARD;
            mBtnVoice.setImageResource(R.mipmap.icon_chat_voice);
        } else {
            checkAudioPermission();
        }
    }


    private void showRecordVoice() {
        mVoiceInput = true;
        mBtnVoice.setImageResource(R.mipmap.icon_chat_keyboard);
        if (mInput != null && mInput.getVisibility() == View.VISIBLE) {
            mInput.setVisibility(View.INVISIBLE);
        }
        if (mBtnRecordVoice != null && mBtnRecordVoice.getVisibility() != View.VISIBLE) {
            mBtnRecordVoice.setVisibility(View.VISIBLE);
        }
        if (mInputStatus == INPUT_KEY_BOARD || mInputStatus == INPUT_FACE || mInputStatus == INPUT_MORE) {
            hideSoftInput();
        }
    }

    private void showMore() {
        if (mVoiceInput) {
            if (mBtnRecordVoice != null && mBtnRecordVoice.getVisibility() == View.VISIBLE) {
                mBtnRecordVoice.setVisibility(View.INVISIBLE);
            }
            mBtnVoice.setImageResource(R.mipmap.icon_chat_voice);
            if (mInput != null && mInput.getVisibility() != View.VISIBLE) {
                mInput.setVisibility(View.VISIBLE);
                mInput.requestFocus();
            }
            mVoiceInput = false;
        }
        mMoreShowing = true;
        if (mInputStatus == INPUT_MORE) {
            return;
        }
        if (mInputStatus == INPUT_KEY_BOARD) {
            imm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
        }
        if (mFaceGroup != null && mFaceGroup.getVisibility() == View.VISIBLE) {
            mFaceGroup.setVisibility(View.INVISIBLE);
        }
        if (mMoreGroup != null && mMoreGroup.getVisibility() != View.VISIBLE) {
            mMoreGroup.setVisibility(View.VISIBLE);
        }
        if (mBtnHideSoftInput != null && mBtnHideSoftInput.getVisibility() != View.VISIBLE) {
            mBtnHideSoftInput.setVisibility(View.VISIBLE);
        }
        mCurHeight = mScreenHeight - mMoreHeight;
        requestLayout();
        mInputStatus = INPUT_MORE;
        mFaceShowing = false;
    }

    private void toggleFace() {
        if (mVoiceInput) {
            if (mBtnRecordVoice != null && mBtnRecordVoice.getVisibility() == View.VISIBLE) {
                mBtnRecordVoice.setVisibility(View.INVISIBLE);
            }
            mBtnVoice.setImageResource(R.mipmap.icon_chat_voice);
            if (mInput != null && mInput.getVisibility() != View.VISIBLE) {
                mInput.setVisibility(View.VISIBLE);
                mInput.requestFocus();
            }
            mVoiceInput = false;
        }
        mMoreShowing = false;
        if (mBtnHideSoftInput != null && mBtnHideSoftInput.getVisibility() != View.VISIBLE) {
            mBtnHideSoftInput.setVisibility(View.VISIBLE);
        }
        if (mMoreGroup != null && mMoreGroup.getVisibility() == View.VISIBLE) {
            mMoreGroup.setVisibility(View.INVISIBLE);
        }
        if (mInputStatus == INPUT_NONE || mInputStatus == INPUT_MORE) {
            if (mFaceGroup != null && mFaceGroup.getVisibility() != View.VISIBLE) {
                mFaceGroup.setVisibility(View.VISIBLE);
            }
            mCurHeight = mScreenHeight - mFaceHeight;
            requestLayout();
            mBtnFace.setImageResource(R.mipmap.icon_chat_keyboard);
            mInputStatus = INPUT_FACE;
            mFaceShowing = true;
        } else if (mInputStatus == INPUT_FACE) {
            mInput.requestFocus();
            imm.showSoftInput(mInput, InputMethodManager.SHOW_FORCED);
            mBtnFace.setImageResource(R.mipmap.icon_chat_face);
            mInputStatus = INPUT_KEY_BOARD;
        } else if (mInputStatus == INPUT_KEY_BOARD) {
            mFaceShowing = true;
            if (mFaceGroup != null && mFaceGroup.getVisibility() != View.VISIBLE) {
                mFaceGroup.setVisibility(View.VISIBLE);
            }
            mBtnFace.setImageResource(R.mipmap.icon_chat_keyboard);
            mInputStatus = INPUT_FACE;
            imm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
            mCurHeight = mScreenHeight - mFaceHeight;
            requestLayout();
        }
    }

    private void hideSoftInput() {
        mFaceShowing = false;
        mMoreShowing = false;
        if (mInputStatus == INPUT_KEY_BOARD) {
            imm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
        } else if (mInputStatus == INPUT_FACE) {
            mBtnFace.setImageResource(R.mipmap.icon_chat_face);
            mCurHeight = mScreenHeight;
            requestLayout();
            if (mFaceGroup != null && mFaceGroup.getVisibility() == View.VISIBLE) {
                mFaceGroup.setVisibility(View.INVISIBLE);
            }
            if (mBtnHideSoftInput != null && mBtnHideSoftInput.getVisibility() == View.VISIBLE) {
                mBtnHideSoftInput.setVisibility(View.INVISIBLE);
            }
            mInputStatus = INPUT_NONE;
        } else if (mInputStatus == INPUT_MORE) {
            mCurHeight = mScreenHeight;
            requestLayout();
            if (mMoreGroup != null && mMoreGroup.getVisibility() == View.VISIBLE) {
                mMoreGroup.setVisibility(View.INVISIBLE);
            }
            if (mBtnHideSoftInput != null && mBtnHideSoftInput.getVisibility() == View.VISIBLE) {
                mBtnHideSoftInput.setVisibility(View.INVISIBLE);
            }
            mInputStatus = INPUT_NONE;
        }
    }

    /**
     * 检查是否能够发送消息
     */
    private boolean isCanSendMsg() {
        if (!AppConfig.getInstance().isLoginIM()) {
            ToastUtil.show(getResources().getString(R.string.IM暂未接入)+getResources().getString(R.string.无法使用));
            return false;
        }
        long curTime = System.currentTimeMillis();
        if (curTime - mLastSendTime < 1500) {
            ToastUtil.show(WordUtil.getString(R.string.send_too_fast));
            return false;
        }
        mLastSendTime = curTime;
        return true;
    }

    /**
     * 发送文本信息
     */
    private void sendText() {
        String content = mInput.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            ToastUtil.show(WordUtil.getString(R.string.please_input_content));
            return;
        }
        ChatMessageBean messageBean = JMessageUtil.getInstance().createTextMessage(mToUserId, content);
        if (messageBean == null) {
            ToastUtil.show(WordUtil.getString(R.string.message_send_failed));
            return;
        }
        mCurMessageBean = messageBean;
        sendMessage();
    }

    /**
     * 发送消息
     */
    private void sendMessage() {
        if (!isCanSendMsg()) {
            return;
        }
        if (mCurMessageBean != null) {
            HttpUtil.checkBlack(mToUserId, mCallback);
        } else {
            ToastUtil.show(WordUtil.getString(R.string.message_send_failed));
        }
    }

    private HttpCallback mCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                if (info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    Integer t2u = obj.getInteger("t2u");
                    if (t2u == null || 1 == t2u) {
                        ToastUtil.show(getString(R.string.you_are_blacked));
                        if (mLoading.getVisibility() == View.VISIBLE) {
                            mLoading.setVisibility(View.INVISIBLE);
                        }
                        if (mCurMessageBean != null) {
                            JMessageUtil.getInstance().removeMessage(mToUserId, mCurMessageBean.getRawMessage());
                        }
                    } else {
                        ConfigBean config = AppConfig.getInstance().getConfig();
                        if (config.getPrivate_letter_switch() == 1) {
                            int isattent = obj.getIntValue("isattent");
                            int myMsgCount = mChatAdapter.getMyMessageCount();
                            if (isattent == 0 && myMsgCount >= config.getPrivate_letter_nums()) {
                                ToastUtil.show(WordUtil.getString(R.string.most_msg_tip) + config.getPrivate_letter_nums() + WordUtil.getString(R.string.most_msg_tip_2));
                                if (mLoading.getVisibility() == View.VISIBLE) {
                                    mLoading.setVisibility(View.INVISIBLE);
                                }
                                if (mCurMessageBean != null) {
                                    JMessageUtil.getInstance().removeMessage(mToUserId, mCurMessageBean.getRawMessage());
                                }
                                return;
                            }
                        }
                        if (mCurMessageBean != null) {
                            if (mCurMessageBean.getType() == ChatMessageBean.TYPE_TEXT) {
                                mInput.setText("");
                            }
                            mCurMessageBean.getRawMessage().setOnSendCompleteCallback(mOnSendCompleteCallback);
                            JMessageUtil.getInstance().sendMessage(mCurMessageBean);
                        } else {
                            ToastUtil.show(WordUtil.getString(R.string.message_send_failed));
                        }
                    }
                }
            } else {
                ToastUtil.show(msg);
            }
        }

        @Override
        public void onStart() {
            if (mLoading.getVisibility() != View.VISIBLE) {
                mLoading.setVisibility(View.VISIBLE);
            }
        }
    };


    private BasicCallback mOnSendCompleteCallback = new BasicCallback() {
        @Override
        public void gotResult(int responseCode, String responseDesc) {
            if (mLoading.getVisibility() == View.VISIBLE) {
                mLoading.setVisibility(View.INVISIBLE);
            }
            if (responseCode == 0) {
                //消息发送成功
                if (mChatAdapter != null) {
                    mChatAdapter.insertItem(mCurMessageBean);
                }
            } else {
                //消息发送失败
                ToastUtil.show(WordUtil.getString(R.string.message_send_failed));
                L.e("#极光IM---消息发送失败--->  responseDesc:" + responseDesc);
                if (mCurMessageBean != null) {
                    JMessageUtil.getInstance().removeMessage(mToUserId, mCurMessageBean.getRawMessage());
                }
            }
            if (mCurMessageBean.getType() == ChatMessageBean.TYPE_VOICE) {
                deleteVoiceFile();
            }
        }
    };


    /**
     * 检查文件读写的权限,这是在选择本地图片或文件时候用
     */
    private void checkFilePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    ) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.REQUEST_FILE_PERMISSION);
            } else {
                chooseImage();
            }
        } else {
            chooseImage();
        }
    }

    /**
     * 检查拍照的权限
     */
    private void checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA
                        },
                        Constants.REQUEST_CAMERA_PERMISSION);
            } else {
                takePhoto();
            }
        } else {
            takePhoto();
        }
    }

    /**
     * 检查录音的权限
     */
    private void checkAudioPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.RECORD_AUDIO
                        },
                        Constants.REQUEST_AUDIO_PERMISSION);
            } else {
                showRecordVoice();
            }
        } else {
            showRecordVoice();
        }
    }


    /**
     * 检查语音输入的权限
     */
    private void checkAsrPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.READ_PHONE_STATE
                        },
                        Constants.REQUEST_ASR_PERMISSION);
            } else {
                showVoiceToText();
            }
        } else {
            showVoiceToText();
        }
    }


    /**
     * 检查定位的权限
     */
    private void checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.REQUEST_LOCATION_PERMISSION);
            } else {
                forwardLocationActivity();
            }
        } else {
            forwardLocationActivity();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (isAllGranted(permissions, grantResults)) {
            switch (requestCode) {
                case Constants.REQUEST_FILE_PERMISSION:
                    chooseImage();
                    break;
                case Constants.REQUEST_CAMERA_PERMISSION:
                    takePhoto();
                    break;
                case Constants.REQUEST_LOCATION_PERMISSION:
                    forwardLocationActivity();
                    break;
                case Constants.REQUEST_AUDIO_PERMISSION:
                    showRecordVoice();
                    break;
                case Constants.REQUEST_ASR_PERMISSION:
                    showVoiceToText();
                    break;
            }
        }

    }

    //判断申请的权限有没有被允许
    private boolean isAllGranted(String[] permissions, int[] grantResults) {
        boolean isAllGranted = true;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                isAllGranted = false;
                showTip(permissions[i]);
                break;
            }
        }
        return isAllGranted;
    }

    //拒绝某项权限时候的提示
    private void showTip(String permission) {
        switch (permission) {
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                ToastUtil.show(getString(R.string.storage_permission_refused));
                break;
            case Manifest.permission.CAMERA:
                ToastUtil.show(getString(R.string.camera_permission_refused));
                break;
            case Manifest.permission.RECORD_AUDIO:
                ToastUtil.show(getString(R.string.record_audio_permission_refused));
                break;
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                ToastUtil.show(getString(R.string.location_permission_refused));
                break;
            case Manifest.permission.READ_PHONE_STATE:
                ToastUtil.show(getString(R.string.read_phone_status_permission_refused));
                break;
        }
    }


    /**
     * 选择图片
     */
    private void chooseImage() {
        startActivityForResult(new Intent(mContext, ImageChooseActivity.class), REQUEST_CODE_CHOOSE_IMAGE);
    }

    /**
     * 拍照
     */
    private void takePhoto() {
        File dir = new File(AppConfig.CAMERA_IMAGE_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        mCameraResult = new File(dir, DateFormatUtil.getCurTimeString() + ".png");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(mContext, AppConfig.FILE_PROVIDER, mCameraResult);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(mCameraResult);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_CODE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_CHOOSE_IMAGE://选择图片的结果
                    mCurMessageBean = JMessageUtil.getInstance().createImageMessage(mToUserId, intent.getStringExtra(Constants.SELECT_IMAGE_PATH));
                    sendMessage();
                    break;
                case REQUEST_CODE_PHOTO://拍照的结果
                    if (mCameraResult != null) {
                        String path = mCameraResult.getAbsolutePath();
                        if (!TextUtils.isEmpty(path)) {
                            mCurMessageBean = JMessageUtil.getInstance().createImageMessage(mToUserId, path);
                            sendMessage();
                        }
                        mCameraResult = null;
                    }
                    break;
                case REQUEST_CODE_LOCATION://选择位置的结果
                    double lat = intent.getDoubleExtra(Constants.LAT, 0);
                    double lng = intent.getDoubleExtra(Constants.LNG, 0);
                    int scale = intent.getIntExtra(Constants.SCALE, 0);
                    String address = intent.getStringExtra(Constants.ADDRESS);
                    if (lat > 0 && lng > 0 && scale > 0 && !TextUtils.isEmpty(address)) {
                        mCurMessageBean = JMessageUtil.getInstance().createLocationMessage(mToUserId, lat, lng, scale, address);
                        sendMessage();
                    } else {
                        ToastUtil.show(WordUtil.getString(R.string.get_location_failed));
                    }
                    break;
            }
        } else {
            if (requestCode == REQUEST_CODE_PHOTO) {
                ToastUtil.show(WordUtil.getString(R.string.cancel_photo));
            }
        }
    }

    /**
     * 前往发送位置Activity
     */
    private void forwardLocationActivity() {
        startActivityForResult(new Intent(mContext, LocationActivity.class), REQUEST_CODE_LOCATION);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
        if (mVoiceMediaPlayerUtil != null) {
            mVoiceMediaPlayerUtil.pausePlay();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPaused = false;
        if (mVoiceMediaPlayerUtil != null) {
            mVoiceMediaPlayerUtil.resumePlay();
        }
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.CHECK_BLACK);
        HttpUtil.cancel(HttpUtil.SET_BLACK);
        if (mVoiceInputShowAnimator != null) {
            mVoiceInputShowAnimator.cancel();
        }
        if (mVoiceInputHideAnimator != null) {
            mVoiceInputHideAnimator.cancel();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (mPresenter != null) {
            mPresenter.removeLayoutListener();
            mPresenter.release();
        }
        if (mChatAdapter != null) {
            mChatAdapter.setActionListener(null);
        }
        EventBus.getDefault().unregister(this);
        if (mMediaRecordUtil != null) {
            mMediaRecordUtil.release();
        }
        if (mVoiceMediaPlayerUtil != null) {
            mVoiceMediaPlayerUtil.destroy();
        }
        if (mAsrUtil != null) {
            mAsrUtil.release();
        }
        super.onDestroy();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVisibleHeightEvent(VisibleHeightEvent e) {
        if (!mPaused && mRootView != null) {
            int visibleHeight = e.getVisibleHeight();
            if (visibleHeight >= mContentHeight) {
                if (!mMoreShowing) {
                    if (!mFaceShowing) {
                        if (mFaceGroup != null && mFaceGroup.getVisibility() == View.VISIBLE) {
                            mFaceGroup.setVisibility(View.INVISIBLE);
                        }
                        if (mBtnHideSoftInput != null && mBtnHideSoftInput.getVisibility() == View.VISIBLE) {
                            mBtnHideSoftInput.setVisibility(View.INVISIBLE);
                        }
                        mInputStatus = INPUT_NONE;
                        mCurHeight = mScreenHeight;
                        requestLayout();
                    } else {
                        mInputStatus = INPUT_FACE;
                        mBtnFace.setImageResource(R.mipmap.icon_chat_keyboard);
                        mCurHeight = mScreenHeight - mFaceHeight;
                        requestLayout();
                    }
                } else {
                    mInputStatus = INPUT_MORE;
                    mCurHeight = mScreenHeight - mMoreHeight;
                    requestLayout();
                }
            } else {
                if (mBtnHideSoftInput != null && mBtnHideSoftInput.getVisibility() != View.VISIBLE) {
                    mBtnHideSoftInput.setVisibility(View.VISIBLE);
                }
                mInputStatus = INPUT_KEY_BOARD;
                mBtnFace.setImageResource(R.mipmap.icon_chat_face);
                mFaceShowing = false;
                mCurHeight = visibleHeight + mStatusBarHeight;
                requestLayout();
            }

        }
    }

    private void requestLayout() {
        ViewGroup.LayoutParams params = mRootView.getLayoutParams();
        params.height = mCurHeight;
        mRootView.requestLayout();
        if (mChatAdapter != null) {
            mChatAdapter.scrollToBottom();
        }
    }

    /**
     * 接收消息
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChatMessageBean(ChatMessageBean bean) {
        if (bean != null && !bean.isFromSelf() && mToUserId.equals(bean.getFrom()) && mChatAdapter != null) {
            mChatAdapter.insertItem(bean);
        }
    }

    public static void forwardChatRoom(Context context, UserBean bean) {
        AppConfig appConfig = AppConfig.getInstance();
        if (appConfig.isLogin() && appConfig.isLoginIM() && appConfig.getUserBean() != null) {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra(Constants.USER_BEAN, bean);
            context.startActivity(intent);
        } else {
            ToastUtil.show(WordUtil.getString(R.string.please_login_2));
        }
    }

    @Override
    public void onBackPressed() {
        if (mChatImageHolder != null && !mChatImageHolder.hide()) {
            return;
        }
        JMessageUtil.getInstance().markAllMessagesAsRead(mToUserId);
        if (mChatAdapter != null && mToUserBean != null) {
            ChatMessageBean bean = mChatAdapter.getLastMessage();
            if (bean != null) {
                JMessageUtil util = JMessageUtil.getInstance();
                String lastMessage = util.getMessageString(bean.getRawMessage());
                String lastTime = util.getMessageTimeString(bean.getRawMessage());
                EventBus.getDefault().post(new ChatRoomCloseEvent(mToUserId, lastMessage, lastTime, mToUserBean));
            }
        }
        super.onBackPressed();
    }
}
