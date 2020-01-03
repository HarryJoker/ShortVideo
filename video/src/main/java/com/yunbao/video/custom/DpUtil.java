package com.yunbao.video.custom;

import android.content.Context;


/**
 * Created by cxf on 2017/8/9.
 * dp转px工具类
 */

public class DpUtil {

    public static int dp2px(Context context, int dpVal) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (scale * dpVal + 0.5f);
    }
}
