package com.Tata.video.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.Tata.video.R;

/**
 * Created by cxf on 2018/6/9.
 * 可以禁止滑动的ViewPager
 */

public class MyViewPager extends ViewPager {

    private boolean mCanScroll;

    public MyViewPager(Context context) {
        this(context, null);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MyViewPager);
        mCanScroll = ta.getBoolean(R.styleable.MyViewPager_canScroll, true);
        ta.recycle();
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mCanScroll) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mCanScroll) {
            return super.onTouchEvent(ev);
        } else {
            return true;
        }
    }

    public void setCanScroll(boolean canScroll) {
        mCanScroll = canScroll;
    }

}
