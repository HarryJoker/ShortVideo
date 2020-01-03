package com.Tata.video.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.Tata.video.R;
import com.Tata.video.bean.UserBean;
import com.Tata.video.bean.VideoBean;
import com.Tata.video.custom.RefreshAdapter;
import com.Tata.video.glide.ImgLoader;

/**
 * Created by cxf on 2018/6/7.
 */

public class NearVideoAdapter extends RefreshAdapter<VideoBean> {

    public NearVideoAdapter(Context context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_list_near, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, int position) {
        ((Vh) vh).setData(mList.get(position), position);
    }


    class Vh extends RecyclerView.ViewHolder {

        ImageView mThumb;
        ImageView mAvatar;
        TextView mName;
        TextView mDistance;
        VideoBean mBean;
        int mPosition;


        public Vh(View itemView) {
            super(itemView);
            mThumb = (ImageView) itemView.findViewById(R.id.thumb);
            mAvatar = (ImageView) itemView.findViewById(R.id.avatar);
            mName = (TextView) itemView.findViewById(R.id.name);
            mDistance = (TextView) itemView.findViewById(R.id.distance);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(mBean, mPosition);
                    }
                }
            });
        }

        void setData(VideoBean bean, int position) {
            mBean = bean;
            mPosition = position;
            ImgLoader.display(bean.getThumb(), mThumb);
            UserBean u = bean.getUserinfo();
            ImgLoader.display(u.getAvatar(), mAvatar);
            mName.setText(u.getUser_nicename());
            mDistance.setText(bean.getDistance());
        }
    }
}
