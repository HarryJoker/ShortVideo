package com.Tata.video.custom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.Tata.video.AppConfig;
import com.Tata.video.R;
import com.Tata.video.bean.MusicBean;
import com.Tata.video.bean.UserBean;
import com.Tata.video.bean.VideoBean;
import com.Tata.video.event.NeedRefreshEvent;
import com.Tata.video.glide.ImgLoader;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.utils.FrameAnimUtil;
import com.Tata.video.utils.ScreenDimenUtil;
import com.Tata.video.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by cxf on 2018/6/5.
 */

public class VideoPlayWrap extends FrameLayout implements View.OnClickListener {

    private String mTag;
    private Context mContext;
    private VideoBean mVideoBean;
    private FrameLayout mContainer;
    private View mCover;
    private ImageView mCoverImg;
    private VideoPlayView mPlayView;
    private int mScreenWidth;
    private ImageView mAvatar;//头像
    private ImageView mBtnFollow;//关注按钮
    private FrameAnimImageView mBtnZan;//点赞按钮
    private TextView mZanNum;//点赞数
    private TextView mCommentNum;//评论数
    private TextView mShareNum;//分享数
    private TextView mTitle;//标题
    private TextView mName;//昵称
    private TextView mMusicTitle;//音乐标题
    private MusicAnimLayout mMusicAnimLayout;
    private boolean mUsing;//是否在使用中
    private ActionListener mActionListener;
    private String mMusicSuffix;
    private static final String SPACE = "            ";
    private static int sFollowAnimHashCode;
    private ValueAnimator mFollowAnimator;


    public VideoPlayWrap(Context context) {
        this(context, null);
    }

    public VideoPlayWrap(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoPlayWrap(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTag = String.valueOf(this.hashCode()) + HttpUtil.GET_VIDEO_INFO;
        mContext = context;
        mScreenWidth = ScreenDimenUtil.getInstance().getScreenWdith();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_video_wrap, this, false);
        addView(view);
        mContainer = (FrameLayout) view.findViewById(R.id.container);
        mCover = view.findViewById(R.id.cover);
        mCoverImg = (ImageView) view.findViewById(R.id.coverImg);
        mAvatar = (ImageView) view.findViewById(R.id.avatar);
        mBtnFollow = (ImageView) view.findViewById(R.id.btn_follow);
        mBtnZan = (FrameAnimImageView) view.findViewById(R.id.btn_zan);
        mZanNum = (TextView) view.findViewById(R.id.zan);
        mCommentNum = (TextView) view.findViewById(R.id.comment);
        mShareNum = (TextView) view.findViewById(R.id.share);
        mTitle = (TextView) view.findViewById(R.id.title);
        mName = (TextView) view.findViewById(R.id.name);
        mMusicTitle = (TextView) view.findViewById(R.id.music_title);
        mMusicAnimLayout = (MusicAnimLayout) view.findViewById(R.id.music_anim);
        mAvatar.setOnClickListener(this);
        mBtnFollow.setOnClickListener(this);
        view.findViewById(R.id.btn_zan).setOnClickListener(this);
        view.findViewById(R.id.btn_comment).setOnClickListener(this);
        view.findViewById(R.id.btn_share).setOnClickListener(this);
        mMusicSuffix = WordUtil.getString(R.string.music_suffix);
        mFollowAnimator = ValueAnimator.ofFloat(1f, 1.4f, 0.2f);
        mFollowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                mBtnFollow.setScaleX(v);
                mBtnFollow.setScaleY(v);
            }
        });
        mFollowAnimator.setDuration(1000);
        mFollowAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mFollowAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mBtnFollow.setVisibility(INVISIBLE);
            }
        });
    }

    /**
     * 加载数据
     */
    public void loadData(VideoBean bean) {
        mUsing = true;
        if (bean == null) {
            return;
        }
        mVideoBean = bean;
        ImgLoader.displayBitmap(bean.getThumb(), new ImgLoader.BitmapCallback() {
            @Override
            public void callback(Bitmap bitmap) {
                if (mCoverImg != null && mCover != null && mCover.getVisibility() == View.VISIBLE) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mCoverImg.getLayoutParams();
                    float width = bitmap.getWidth();
                    float height = bitmap.getHeight();
                    if (width >= height) {
                        params.height = (int) (mScreenWidth * height / width);
                    } else {
                        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    }
                    mCoverImg.setLayoutParams(params);
                    mCoverImg.requestLayout();
                    mCoverImg.setImageBitmap(bitmap);
                } else {
                    bitmap.recycle();
                }
            }
        });
        mZanNum.setText(bean.getLikes());
        mCommentNum.setText(bean.getComments());
        mShareNum.setText(bean.getShares());
        int isAttent = bean.getIsattent();
        if (isAttent == 1 || AppConfig.getInstance().getUid().equals(mVideoBean.getUid())) {
            if (mBtnFollow.getVisibility() == VISIBLE) {
                mBtnFollow.setVisibility(INVISIBLE);
            }
        } else {
            if (mBtnFollow.getVisibility() != VISIBLE) {
                mBtnFollow.setVisibility(VISIBLE);
            }
            mBtnFollow.setScaleX(1f);
            mBtnFollow.setScaleY(1f);
            mBtnFollow.setImageResource(R.mipmap.icon_video_unfollow);
        }
        int islike = bean.getIslike();
        if (islike == 1) {
            mBtnZan.setImageResource(R.mipmap.icon_video_zan_12);
        } else {
            mBtnZan.setImageResource(R.mipmap.icon_video_zan_01);
        }

        mTitle.setText(bean.getTitle());
        UserBean u = bean.getUserinfo();
        if (u != null) {
            ImgLoader.display(u.getAvatar(), mAvatar);
            mName.setText("@" + u.getUser_nicename());
            if (mVideoBean.getMusic_id() != 0) {
                MusicBean musicBean = mVideoBean.getMusicinfo();
                if (musicBean != null) {
                    mMusicAnimLayout.setImageUrl(musicBean.getImg_url());
                    String title = musicBean.getTitle();
                    mMusicTitle.setText(title + SPACE + title + SPACE + title);
                }
            } else {
                mMusicAnimLayout.setImageUrl(u.getAvatar());
                String title = "@" + u.getUser_nicename() + mMusicSuffix;
                mMusicTitle.setText(title + SPACE + title + SPACE + title);
            }
        }
        getVideoInfo();
    }

    /**
     * 暂停音乐播放的动画
     */
    public void pauseMusicAnim() {
        if (mMusicAnimLayout != null) {
            mMusicAnimLayout.pauseAnim();
        }
    }

    /**
     * 恢复音乐播放的动画
     */
    public void startMusicAnim() {
        if (mMusicAnimLayout != null) {
            mMusicAnimLayout.startAnim();
        }
    }

    /**
     * 显示背景图
     */
    public void showBg() {
        if (mCover.getVisibility() != VISIBLE) {
            mCover.setVisibility(VISIBLE);
        }
    }

    /**
     * 隐藏背景图
     */
    public void hideBg() {
        if (mCover.getVisibility() == VISIBLE) {
            mCover.setVisibility(INVISIBLE);
        }
    }

    public void removePlayView() {
        if (mContainer.getChildCount() > 0) {
            if (mPlayView != null) {
                mContainer.removeView(mPlayView);
                mPlayView = null;
            }
        }
        showBg();
        if (mMusicAnimLayout != null) {
            mMusicAnimLayout.cancelAnim();
        }
    }

    public void addPlayView(VideoPlayView playView) {
        mPlayView = playView;
        playView.setPlayWrap(this);
        ViewGroup parent = (ViewGroup) playView.getParent();
        if (parent != null) {
            parent.removeView(playView);
        }
        mContainer.addView(playView);
    }

    public void play() {
        if (mPlayView != null) {
            mPlayView.play(mVideoBean.getHref());
        }
    }

    public void clearData() {
        mUsing = false;
        HttpUtil.cancel(mTag);
        if (mCoverImg != null) {
            mCoverImg.setImageDrawable(null);
        }
        if (mAvatar != null) {
            mAvatar.setImageDrawable(null);
        }
    }

    /**
     * 获取单个视频信息，主要是该视频关于自己的信息 ，如是否关注，是否点赞等
     */
    public void getVideoInfo() {
        if (mUsing && mVideoBean != null) {
            HttpUtil.getVideoInfo(mVideoBean.getId(), mTag, mGetVideoInfoCallback);
        }
    }

    private HttpCallback mGetVideoInfoCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0 && info.length > 0) {
                JSONObject obj = JSON.parseObject(info[0]);
                String likes = obj.getString("likes");
                String comments = obj.getString("comments");
                String shares = obj.getString("shares");
                int isattent = obj.getIntValue("isattent");
                int islike = obj.getIntValue("islike");
                if (mVideoBean != null) {
                    mVideoBean.setLikes(likes);
                    mVideoBean.setComments(comments);
                    mVideoBean.setShares(shares);
                    mVideoBean.setIsattent(isattent);
                    mVideoBean.setIslike(islike);
                }

                if (mZanNum != null) {
                    mZanNum.setText(likes);
                }
                if (mCommentNum != null) {
                    mCommentNum.setText(comments);
                }
                if (mShareNum != null) {
                    mShareNum.setText(shares);
                }
                if (isattent == 1 || AppConfig.getInstance().getUid().equals(mVideoBean.getUid())) {
                    if (mBtnFollow.getVisibility() == VISIBLE) {
                        mBtnFollow.setVisibility(INVISIBLE);
                    }
                } else {
                    if (mBtnFollow.getVisibility() != VISIBLE) {
                        mBtnFollow.setVisibility(VISIBLE);
                    }
                    mBtnFollow.setScaleX(1f);
                    mBtnFollow.setScaleY(1f);
                    mBtnFollow.setImageResource(R.mipmap.icon_video_unfollow);
                }
                if (islike == 1) {
                    mBtnZan.setImageResource(R.mipmap.icon_video_zan_12);
                } else {
                    mBtnZan.setImageResource(R.mipmap.icon_video_zan_01);
                }
            }
        }
    };


    /**
     * 修改评论数
     *
     * @param comments
     */
    public void setCommentNum(String comments) {
        if (mVideoBean != null) {
            mVideoBean.setComments(comments);
        }
        if (mCommentNum != null) {
            mCommentNum.setText(comments);
        }
        EventBus.getDefault().post(new NeedRefreshEvent());
    }

    /**
     * 修改点赞数
     *
     * @param isLike 自己是否点赞
     * @param likes  点赞数
     */
    public void setLikes(int isLike, String likes) {
        if (mVideoBean != null) {
            mVideoBean.setIslike(isLike);
            mVideoBean.setLikes(likes);
        }
        if (isLike == 1) {
            mBtnZan.setSource(FrameAnimUtil.getVideoZanAnim())
                    .setFrameScaleType(FrameAnimImageView.FIT_WIDTH)
                    .setDuration(30).startAnim();
        } else {
            mBtnZan.setSource(FrameAnimUtil.getVideoCancelZanAnim())
                    .setFrameScaleType(FrameAnimImageView.FIT_WIDTH)
                    .setDuration(30).startAnim();
        }
        if (mZanNum != null) {
            mZanNum.setText(likes);
        }
        EventBus.getDefault().post(new NeedRefreshEvent());
    }

    /**
     * 修改分享数
     *
     * @param shares
     */
    public void setShares(String shares) {
        if (mVideoBean != null) {
            mVideoBean.setShares(shares);
        }
        if (mShareNum != null) {
            mShareNum.setText(shares);
        }
        EventBus.getDefault().post(new NeedRefreshEvent());
    }


    /**
     * 修改是否关注
     *
     * @param isAttent
     */
    public void setIsAttent(int isAttent) {
        if (mVideoBean != null && mBtnFollow != null) {
            mVideoBean.setIsattent(isAttent);
            if (isAttent == 1) {
                if (sFollowAnimHashCode == this.hashCode()) {
                    sFollowAnimHashCode = 0;
                    //执行关注动画
                    mBtnFollow.setImageResource(R.mipmap.icon_video_follow);
                    mFollowAnimator.start();
                } else {
                    if (mBtnFollow.getVisibility() == VISIBLE) {
                        mBtnFollow.setVisibility(INVISIBLE);
                    }
                }
            } else {
                if (mBtnFollow.getVisibility() != VISIBLE) {
                    mBtnFollow.setVisibility(VISIBLE);
                }
                mBtnFollow.setScaleX(1f);
                mBtnFollow.setScaleY(1f);
                mBtnFollow.setImageResource(R.mipmap.icon_video_unfollow);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (mActionListener != null && mVideoBean != null) {
            switch (v.getId()) {
                case R.id.btn_zan:
                    if (mVideoBean != null && mVideoBean.getIslike() == 0 && !mBtnZan.isAnimating()) {
                        mActionListener.onZanClick(this, mVideoBean);
                    }
                    break;
                case R.id.btn_comment:
                    mActionListener.onCommentClick(this, mVideoBean);
                    break;
                case R.id.btn_share:
                    mActionListener.onShareClick(this, mVideoBean);
                    break;
                case R.id.btn_follow:
                    sFollowAnimHashCode = this.hashCode();
                    mActionListener.onFollowClick(this, mVideoBean);
                    break;
                case R.id.avatar:
                    mActionListener.onAvatarClick(this, mVideoBean);
                    break;
            }
        }
    }

    public VideoBean getVideoBean() {
        return mVideoBean;
    }

    @Override
    protected void onDetachedFromWindow() {
        HttpUtil.cancel(mTag);
        super.onDetachedFromWindow();
    }

    public void release() {
        if (mAvatar != null) {
            mAvatar.setImageDrawable(null);
        }
        if (mCoverImg != null) {
            mCoverImg.setImageDrawable(null);
        }
        if (mBtnZan != null) {
            mBtnZan.release();
        }
        if (mFollowAnimator != null) {
            mFollowAnimator.cancel();
        }
    }


    public interface ActionListener {
        //点击点赞
        void onZanClick(VideoPlayWrap wrap, VideoBean bean);

        //点击评论
        void onCommentClick(VideoPlayWrap wrap, VideoBean bean);

        //点击关注
        void onFollowClick(VideoPlayWrap wrap, VideoBean bean);

        //点击头像
        void onAvatarClick(VideoPlayWrap wrap, VideoBean bean);

        //点击分享
        void onShareClick(VideoPlayWrap wrap, VideoBean bean);
    }

    public void setActionListener(ActionListener listener) {
        mActionListener = listener;
    }


}
