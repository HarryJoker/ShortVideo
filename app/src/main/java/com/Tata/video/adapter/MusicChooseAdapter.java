package com.Tata.video.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.Tata.video.R;
import com.Tata.video.bean.MusicChooseBean;
import com.Tata.video.interfaces.OnItemClickListener;

import java.util.List;

/**
 * Created by cxf on 2018/8/6.
 */

public class MusicChooseAdapter extends RecyclerView.Adapter<MusicChooseAdapter.Vh> {

    private List<MusicChooseBean> mList;
    private LayoutInflater mInflater;
    private OnItemClickListener<MusicChooseBean> mOnItemClickListener;
    private View.OnClickListener mOnClickListener;

    public MusicChooseAdapter(Context context, List<MusicChooseBean> list) {
        mList = list;
        mInflater = LayoutInflater.from(context);
        mOnClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null) {
                    MusicChooseBean bean = (MusicChooseBean) tag;
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(bean, 0);
                    }
                }
            }
        };
    }

    public void setOnItemClickListener(OnItemClickListener<MusicChooseBean> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public Vh onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_list_choose_music, parent, false));
    }

    @Override
    public void onBindViewHolder(Vh vh, int position) {
        vh.setData(mList.get(position),position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class Vh extends RecyclerView.ViewHolder {

        TextView mTitle;
        TextView mArtist;
        View mLine;
        MusicChooseBean mBean;
        int mPosition;

        public Vh(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mArtist = (TextView) itemView.findViewById(R.id.artist);
            mLine = itemView.findViewById(R.id.line);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(MusicChooseBean bean, int position) {
            mBean = bean;
            itemView.setTag(bean);
            mTitle.setText(bean.getTitle());
            mArtist.setText(bean.getArtist());
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
