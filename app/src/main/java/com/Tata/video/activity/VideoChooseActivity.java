package com.Tata.video.activity;

import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.adapter.VideoChooseAdapter;
import com.Tata.video.bean.VideoChooseBean;
import com.Tata.video.custom.ItemDecoration;
import com.Tata.video.interfaces.CommonCallback;
import com.Tata.video.interfaces.OnItemClickListener;
import com.Tata.video.utils.VideoUtil;
import com.Tata.video.utils.WordUtil;

import java.util.List;

/**
 * Created by cxf on 2018/6/20.
 */

public class VideoChooseActivity extends AbsActivity implements OnItemClickListener<VideoChooseBean> {

    private RecyclerView mRecyclerView;
    private VideoUtil mVideoUtil;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_choose;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.local_video));
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 4, GridLayoutManager.VERTICAL, false));
        ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 1, 1);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        mRecyclerView.addItemDecoration(decoration);
        mVideoUtil = new VideoUtil();
        mVideoUtil.getLocalVideoList(new CommonCallback<List<VideoChooseBean>>() {
            @Override
            public void callback(List<VideoChooseBean> videoList) {
                VideoChooseAdapter adapter = new VideoChooseAdapter(mContext, videoList);
                adapter.setOnItemClickListener(VideoChooseActivity.this);
                mRecyclerView.setAdapter(adapter);
            }
        });
    }


    @Override
    public void onItemClick(VideoChooseBean bean, int position) {
        Intent intent = new Intent();
        intent.putExtra(Constants.VIDEO_PATH, bean.getVideoPath());
        intent.putExtra(Constants.VIDEO_DURATION, bean.getDuration());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        mVideoUtil.release();
        super.onDestroy();
    }

}
