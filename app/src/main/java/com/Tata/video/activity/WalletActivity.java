package com.Tata.video.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.bean.ConfigBean;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.interfaces.CommonCallback;
import com.Tata.video.utils.WordUtil;

/**
 * Created by cxf on 2018/8/6.
 */

public class WalletActivity extends AbsActivity implements View.OnClickListener {

    private TextView mCoinName;
    private TextView mCoin;
    private TextView mCoin2;
    private TextView mRmb;
    private double mCoinVal;
    private double mRmbVal;
    private double mTicket;//可提现数
    private String mCoinNameString;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_wallet;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.my_wallet));
        //findViewById(R.id.btn_coin).setOnClickListener(this);
        findViewById(R.id.btn_rmb).setOnClickListener(this);
        mCoinName = (TextView) findViewById(R.id.coin_name);
        mCoin = (TextView) findViewById(R.id.coin);
        mCoin2 = (TextView) findViewById(R.id.coin2);
        mRmb = (TextView) findViewById(R.id.rmb);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.btn_coin://提现
//                forwardCash();
//                break;
            case R.id.btn_rmb://分红
                forwardCash2();
                break;
        }
    }

    private void forwardCash() {
        Intent intent = new Intent(mContext, CashActivity.class);
        intent.putExtra(Constants.COIN, mCoinVal);
        intent.putExtra(Constants.TICKET, mTicket);
        startActivity(intent);
    }

    private void forwardCash2() {
        Intent intent = new Intent(mContext, CashActivity2.class);
        intent.putExtra(Constants.RMB, mRmbVal);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TextUtils.isEmpty(mCoinNameString)) {
            HttpUtil.getConfig(new CommonCallback<ConfigBean>() {
                @Override
                public void callback(final ConfigBean configBean) {
                    mCoinNameString = configBean.getName_coin();
                    showData();
                }
            });
        } else {
            showData();
        }

    }

    private void showData() {
        mCoinName.setText(R.string.持有+"  (" + mCoinNameString + ")");
        HttpUtil.getMyWallet(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    String coinString = obj.getString("coin");
                    String rmbString = obj.getString("bonus");
                    mCoinVal = Double.parseDouble(coinString);
                    mRmbVal = Double.parseDouble(rmbString);
                    mCoin.setText(coinString);
                    mRmb.setText(rmbString);
                    mCoin2.setText(R.string.当前参与分红的 + mCoinNameString + "：" + coinString);
                    mTicket = obj.getDoubleValue("ticket");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.GET_MY_WALLET);
        super.onDestroy();
    }
}
