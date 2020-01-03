package com.Tata.video.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.Tata.video.AppConfig;
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.bean.ConfigBean;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.interfaces.CommonCallback;
import com.Tata.video.utils.DialogUitl;
import com.Tata.video.utils.DpUtil;
import com.Tata.video.utils.MD5Util;
import com.Tata.video.utils.ToastUtil;

/**
 * Created by cxf on 2017/9/30.
 * 邀请码输入框
 */

public class InviteFragment extends DialogFragment implements View.OnClickListener {

    private Context mContext;
    private View mRootView;
    private EditText mPwdEditText;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mContext = getContext();
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.fragment_invite, null);
        Dialog dialog = new Dialog(mContext, R.style.dialog2);
        dialog.setContentView(mRootView);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = DpUtil.dp2px(300);
        params.height = DpUtil.dp2px(180);
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPwdEditText = (EditText) mRootView.findViewById(R.id.pwd_text);
        mPwdEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 6) {
                    onInput(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mRootView.findViewById(R.id.btn_close).setOnClickListener(this);
    }


    private void onInput(final String content) {
        final Dialog dialog = DialogUitl.loadingDialog(mContext);
        dialog.show();
        HttpUtil.getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean configBean) {
                String s = MD5Util.getMD5(content) + Constants.SIGN_1 + AppConfig.getInstance().getUid() + AppConfig.getInstance().getToken() + Constants.SIGN_2 + configBean.getDecryptSign() + Constants.SIGN_3;
                s = MD5Util.getMD5(s);
                HttpUtil.setDistribut(content, s, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0) {
                            dismiss();
                            ToastUtil.show(getResources().getString(R.string.设置成功));
                        } else {
                            ToastUtil.show(msg);
                        }
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }


    @Override
    public void onDestroy() {
        HttpUtil.cancel(HttpUtil.GET_CONFIG);
        HttpUtil.cancel(HttpUtil.SET_DISTRIBUT);
        super.onDestroy();
    }
}
