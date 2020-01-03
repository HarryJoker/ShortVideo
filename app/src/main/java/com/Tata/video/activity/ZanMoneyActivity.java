package com.Tata.video.activity;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.Tata.video.R;
import com.Tata.video.bean.ConfigBean;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.interfaces.CommonCallback;
import com.Tata.video.utils.ToastUtil;

/**
 * Created by cxf on 2018/8/8.
 */

public class ZanMoneyActivity extends AbsActivity implements View.OnClickListener {

    private EditText mEditText;
    private double mPraisePercent;
    private int mZanNumVal;
    private TextView mZanNum;
    private TextView mCoin;
    private View mBtnChange;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_zan_money;
    }

    @Override
    protected void main() {
        findViewById(R.id.btn_change).setOnClickListener(this);
        mZanNum = (TextView) findViewById(R.id.zan_num);
        mEditText = (EditText) findViewById(R.id.input);
        mCoin = (TextView) findViewById(R.id.coin);
        mBtnChange = findViewById(R.id.btn_change);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    mCoin.setText("");
                } else {
                    try {
                        long num = Long.parseLong(s.toString());
                        mCoin.setText(String.valueOf(num * mPraisePercent));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        HttpUtil.getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean configBean) {
                mPraisePercent = configBean.getPraise_percent();
                setTitle(R.string.点赞兑换 + configBean.getName_coin() + R.string.币);
                HttpUtil.getBaseInfo(new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0 && info.length > 0) {
                            JSONObject obj = JSON.parseObject(info[0]);
                            mZanNumVal = obj.getIntValue("praiseTotal");
                            mZanNum.setText(String.valueOf(mZanNumVal));
                            mEditText.setEnabled(true);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_change:
                change();
                break;
        }
    }

    /**
     * 兑换
     */
    private void change() {
        String num = mEditText.getText().toString();
        try {
            if (TextUtils.isEmpty(num)) {
                ToastUtil.show(getResources().getString(R.string.请输入要兑换的赞数));
            } else {
                mBtnChange.setClickable(false);
                final int zanNum = Integer.parseInt(num);
                HttpUtil.exchangeTicket(num, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0) {
                            mZanNumVal -= zanNum;
                            mZanNum.setText(String.valueOf(mZanNumVal));
                            mEditText.setText("");
                            mCoin.setText("");
                        }
                        ToastUtil.show(msg);
                    }

                    @Override
                    public void onFinish() {
                        mBtnChange.setClickable(true);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.EXCHANGE_TICKET);
        HttpUtil.cancel(HttpUtil.GET_BASE_INFO);
        HttpUtil.cancel(HttpUtil.GET_CONFIG);
        super.onDestroy();
    }
}
