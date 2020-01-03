package com.Tata.video.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.Tata.video.AppConfig;
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.bean.ConfigBean;
import com.Tata.video.custom.VerticalImageSpan;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.interfaces.CommonCallback;
import com.Tata.video.utils.DpUtil;
import com.Tata.video.utils.MD5Util;
import com.Tata.video.utils.ToastUtil;

/**
 * Created by cxf on 2018/8/6.
 * 分红
 */

public class CashActivity2 extends AbsActivity implements View.OnClickListener {

    private TextView mCoin;
    private TextView mMinCashCoin;
    private double mRmbVal;
    private double mMinCashCoinVal;
    private RadioButton mBtnWx;
    private RadioButton mBtnZfb;
    private EditText mMoneyInput;
    private EditText mAccountInput;
    private int mType = 1;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_cash_2;
    }

    @Override
    protected void main() {
        mCoin = (TextView) findViewById(R.id.coin);
        mMinCashCoin = (TextView) findViewById(R.id.min_cash_coin);
        Intent intent = getIntent();
        mRmbVal = intent.getDoubleExtra(Constants.RMB, 0);
        mCoin.setText(String.valueOf(mRmbVal));
        int width = DpUtil.dp2px(24);
        mBtnWx = (RadioButton) findViewById(R.id.btn_wx);
        SpannableStringBuilder builder = new SpannableStringBuilder("  微信");
        Drawable wxDrawable = ContextCompat.getDrawable(mContext, R.mipmap.icon_cash_wx);
        wxDrawable.setBounds(0, 0, width, width);
        VerticalImageSpan imageSpan = new VerticalImageSpan(wxDrawable);
        builder.setSpan(imageSpan, 0, 1, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        mBtnWx.setText(builder);

        mBtnZfb = (RadioButton) findViewById(R.id.btn_zfb);
        SpannableStringBuilder builder2 = new SpannableStringBuilder("  支付宝");
        Drawable wxDrawable2 = ContextCompat.getDrawable(mContext, R.mipmap.icon_cash_zfb);
        wxDrawable2.setBounds(0, 0, width, width);
        VerticalImageSpan imageSpan2 = new VerticalImageSpan(wxDrawable2);
        builder2.setSpan(imageSpan2, 0, 1, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        mBtnZfb.setText(builder2);
        mBtnWx.setOnClickListener(this);
        mBtnZfb.setOnClickListener(this);
        findViewById(R.id.btn_cash).setOnClickListener(this);
        findViewById(R.id.btn_cash_record).setOnClickListener(this);
        mMoneyInput = (EditText) findViewById(R.id.money_input);
        mAccountInput = (EditText) findViewById(R.id.account_input);
        HttpUtil.getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean configBean) {
                mMinCashCoinVal = configBean.getBonus_min_cash();
                mMinCashCoin.setText(getResources().getString(R.string.最低可提现金额) + mMinCashCoinVal + getResources().getString(R.string.元));
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_wx:
                mType = 1;
                break;
            case R.id.btn_zfb:
                mType = 2;
                break;
            case R.id.btn_cash:
                setCash();
                break;
            case R.id.btn_cash_record:
                forwardHtml();
                break;
        }
    }


    private void setCash() {
        String money = mMoneyInput.getText().toString().trim();
        if (TextUtils.isEmpty(money)) {
            ToastUtil.show(getResources().getString(R.string.请输入提现金额));
            return;
        }
        double moneyVal = 0;
        try {
            moneyVal = Double.parseDouble(money);
            if (moneyVal > mRmbVal) {
                ToastUtil.show(getResources().getString(R.string.余额不足));
                return;
            }
            if (moneyVal < mMinCashCoinVal) {
                ToastUtil.show(getResources().getString(R.string.提现金额不能小于) + mMinCashCoinVal + getResources().getString(R.string.元));
                return;
            }
        } catch (Exception e) {
            ToastUtil.show(getResources().getString(R.string.请输入正确的金额));
            return;
        }
        String account = mAccountInput.getText().toString().trim();
        if (TextUtils.isEmpty(account)) {
            ToastUtil.show(getResources().getString(R.string.请输入提现账号));
            return;
        }
        String s = MD5Util.getMD5(account) + Constants.SIGN_1 + AppConfig.getInstance().getUid() + AppConfig.getInstance().getToken() + Constants.SIGN_2 + AppConfig.getInstance().getConfig().getDecryptSign() + Constants.SIGN_3;
        s = MD5Util.getMD5(s);
        HttpUtil.setBonus(money, account, s, mType, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                ToastUtil.show(msg);
                finish();
            }
        });
    }

    private void forwardHtml() {
        Intent intent = new Intent(mContext, WebActivity.class);
        AppConfig config = AppConfig.getInstance();
        String url = AppConfig.HOST + "/index.php?g=Appapi&m=Bonus&a=bonuslist&uid=" + config.getUid() + "&token=" + config.getToken();
        intent.putExtra(Constants.URL, url);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.SET_BONUS);
        super.onDestroy();
    }
}
