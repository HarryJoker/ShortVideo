package com.Tata.video.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.Tata.video.AppConfig;
import com.Tata.video.R;
import com.Tata.video.activity.LoginActivity;
import com.Tata.video.activity.OtherUserActivity;
import com.Tata.video.bean.FansMsgBean;
import com.Tata.video.bean.UserBean;
import com.Tata.video.custom.RefreshAdapter;
import com.Tata.video.event.NeedRefreshEvent;
import com.Tata.video.glide.ImgLoader;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.interfaces.CommonCallback;
import com.Tata.video.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by cxf on 2018/7/21.
 */

public class FansMsgAdapter extends RefreshAdapter<FansMsgBean> {

    private String mFollowYouString;
    private String mFollowing;
    private String mFollow;

    public FansMsgAdapter(Context context) {
        super(context);
        mFollowYouString = WordUtil.getString(R.string.follow_you);
        mFollowing = WordUtil.getString(R.string.following);
        mFollow = WordUtil.getString(R.string.follow);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_list_fans_msg, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        ((Vh) holder).setData(mList.get(position), position, payload);
    }

    private void updateItem(int positon, int attention) {
        FansMsgBean bean = mList.get(positon);
        if (bean != null) {
            bean.setIsattention(attention);
            notifyItemChanged(positon, "payload");
        }
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mAvatar;
        TextView mName;
        TextView mTime;
        RadioButton mBtnFollow;
        View mLine;
        FansMsgBean mBean;
        int mPosition;

        public Vh(View itemView) {
            super(itemView);
            mAvatar = (ImageView) itemView.findViewById(R.id.avatar);
            mName = (TextView) itemView.findViewById(R.id.name);
            mTime = (TextView) itemView.findViewById(R.id.time);
            mLine = itemView.findViewById(R.id.line);
            mBtnFollow = (RadioButton) itemView.findViewById(R.id.btn_follow);
            mBtnFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (AppConfig.getInstance().isLogin()) {
                        HttpUtil.setAttention(mBean.getUid(), new CommonCallback<Integer>() {
                            @Override
                            public void callback(Integer isAttention) {
                                updateItem(mPosition, isAttention);
                                EventBus.getDefault().post(new NeedRefreshEvent());
                            }
                        });
                    } else {
                        mBtnFollow.setChecked(false);
                        LoginActivity.forwardLogin(mContext);
                    }
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OtherUserActivity.forwardOtherUser(mContext, mBean.getUid());
                }
            });
        }

        void setData(FansMsgBean bean, int position, Object payload) {
            mBean = bean;
            mPosition = position;
            if (bean != null) {
                UserBean u = bean.getUserInfo();
                if (payload == null) {
                    ImgLoader.display(u.getAvatar(), mAvatar);
                    mName.setText(Html.fromHtml(u.getUser_nicename() + "  <font color='#969696'>" + mFollowYouString + "</font>"));
                }
                mTime.setText(bean.getAddtime());
                if (bean.getIsattention() == 1) {
                    mBtnFollow.setChecked(true);
                    mBtnFollow.setText(mFollowing);
                } else {
                    mBtnFollow.setChecked(false);
                    mBtnFollow.setText(mFollow);
                }
            }
            if (position == mList.size() - 1) {
                if (mLine.getVisibility() == View.VISIBLE) {
                    mLine.setVisibility(View.INVISIBLE);
                }
            } else {
                if (mLine.getVisibility() != View.VISIBLE) {
                    mLine.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
