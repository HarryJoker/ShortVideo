package com.Tata.video.utils;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.Tata.video.AppContext;
import com.Tata.video.R;

/**
 * Created by cxf on 2017/8/3.
 */

public class ToastUtil {

    private static Toast sToast;

    static {
        sToast = new Toast(AppContext.sInstance);
        sToast.setDuration(Toast.LENGTH_SHORT);
        sToast.setGravity(Gravity.CENTER, 0, 0);
        View view = LayoutInflater.from(AppContext.sInstance).inflate(R.layout.view_toast, null);
        sToast.setView(view);
    }

    public static void show(String s) {
        sToast.setText(s);
        sToast.show();
    }
}
