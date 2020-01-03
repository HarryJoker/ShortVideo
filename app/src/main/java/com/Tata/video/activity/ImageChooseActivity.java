package com.Tata.video.activity;

import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.adapter.ImageChooseAdapter;
import com.Tata.video.bean.ImageChooseBean;
import com.Tata.video.custom.ItemDecoration;
import com.Tata.video.interfaces.CommonCallback;
import com.Tata.video.utils.ImageUtil;
import com.Tata.video.utils.ToastUtil;
import com.Tata.video.utils.WordUtil;

import java.io.File;
import java.util.List;

/**
 * Created by cxf on 2018/7/16.
 */

public class ImageChooseActivity extends AbsActivity {

    private RecyclerView mRecyclerView;
    private ImageChooseAdapter mAdapter;
    private ImageUtil mImageUtil;
    private View mNoData;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_image_choose;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.choose_image));
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 4, GridLayoutManager.VERTICAL, false));
        ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 1, 1);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        mRecyclerView.addItemDecoration(decoration);
        mNoData = findViewById(R.id.no_data);
        mImageUtil = new ImageUtil();
        mImageUtil.getLocalImageList(new CommonCallback<List<ImageChooseBean>>() {
            @Override
            public void callback(List<ImageChooseBean> list) {
                if (list.size() == 0) {
                    if (mNoData.getVisibility() != View.VISIBLE) {
                        mNoData.setVisibility(View.VISIBLE);
                    }
                } else {
                    mAdapter = new ImageChooseAdapter(mContext, list);
                    mRecyclerView.setAdapter(mAdapter);
                }
            }
        });
    }

    public void chooseImageClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                onBackPressed();
                break;
            case R.id.btn_send:
                sendImage();
                break;
        }
    }

    private void sendImage() {
        if (mAdapter != null) {
            File file = mAdapter.getSelectedFile();
            if (file != null && file.exists()) {
                Intent intent = new Intent();
                intent.putExtra(Constants.SELECT_IMAGE_PATH, file.getAbsolutePath());
                setResult(RESULT_OK, intent);
                finish();
            } else {
                ToastUtil.show(WordUtil.getString(R.string.please_choose_image));
            }
        } else {
            ToastUtil.show(WordUtil.getString(R.string.no_image));
        }
    }


    @Override
    protected void onDestroy() {
        mImageUtil.release();
        super.onDestroy();
    }


}
