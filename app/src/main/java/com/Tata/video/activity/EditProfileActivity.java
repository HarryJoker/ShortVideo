package com.Tata.video.activity;

import android.app.Dialog;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.Tata.video.AppConfig;
import com.Tata.video.Constants;
import com.Tata.video.R;
import com.Tata.video.bean.UserBean;
import com.Tata.video.event.NeedRefreshEvent;
import com.Tata.video.fragment.ChooseImgFragment;
import com.Tata.video.glide.ImgLoader;
import com.Tata.video.http.HttpCallback;
import com.Tata.video.http.HttpUtil;
import com.Tata.video.interfaces.CommonCallback;
import com.Tata.video.utils.CityUtil;
import com.Tata.video.utils.DialogUitl;
import com.Tata.video.utils.ToastUtil;
import com.Tata.video.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;

import cn.qqtheme.framework.entity.City;
import cn.qqtheme.framework.entity.County;
import cn.qqtheme.framework.entity.Province;
import cn.qqtheme.framework.picker.AddressPicker;

/**
 * Created by cxf on 2018/6/14.
 */

public class EditProfileActivity extends AbsActivity {

    private ImageView mAvatar;
    private EditText mEditName;
    private TextView mBirthday;
    private TextView mGender;
    private EditText mSign;
    private TextView mSignLength;
    private View mBtnSave;
    private UserBean mUserBean;
    private ChooseImgFragment mFragment;
    private String mNewAvatar;
    private String mNewAvatarThumb;
    private TextView mArea;
    private String mChooseProvince;
    private String mChoosedCity;
    private String mChoosedDistrict;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_profile;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.edit_profile));
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mEditName = (EditText) findViewById(R.id.edit_name);
        mBirthday = (TextView) findViewById(R.id.birthday);
        mGender = (TextView) findViewById(R.id.gender);
        mArea = (TextView) findViewById(R.id.area);
        mSignLength = (TextView) findViewById(R.id.sign_length);
        mSign = (EditText) findViewById(R.id.sign);
        mSign.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSignLength.setText(s.length() + "/20");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBtnSave = findViewById(R.id.btn_save);
        mFragment = new ChooseImgFragment();
        mFragment.setOnCompleted(new CommonCallback<File>() {
            @Override
            public void callback(final File file) {
                HttpUtil.updateAvatar(file, new HttpCallback() {

                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0 && info.length > 0) {
                            ImgLoader.display(file, mAvatar);
                            JSONObject obj = JSON.parseObject(info[0]);
                            mNewAvatar = obj.getString("avatar");
                            mNewAvatarThumb = obj.getString("avatar_thumb");
                            UserBean bean = AppConfig.getInstance().getUserBean();
                            bean.setAvatar(mNewAvatar);
                            bean.setAvatar_thumb(mNewAvatarThumb);
                        }
                    }

                    @Override
                    public boolean showLoadingDialog() {
                        return true;
                    }

                    @Override
                    public Dialog createLoadingDialog() {
                        return DialogUitl.loadingDialog(mContext);
                    }
                });
            }
        });
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(mFragment, "ChooseImgFragment").commit();

        HttpUtil.getBaseInfo(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    UserBean u = JSON.parseObject(info[0], UserBean.class);
                    mUserBean = u;
                    ImgLoader.display(u.getAvatar(), mAvatar);
                    mEditName.setText(u.getUser_nicename());
                    if (!TextUtils.isEmpty(u.getBirthday())) {
                        mBirthday.setText(u.getBirthday());
                    }
                    mGender.setText(Constants.GENDER_MAP.get(u.getSex()));
                    if (!TextUtils.isEmpty(u.getSignature())) {
                        mSign.setText(u.getSignature());
                    }
                    String province = mUserBean.getProvince();
                    String city = mUserBean.getCity();
                    String district = mUserBean.getArea();
                    if (!TextUtils.isEmpty(province) && !TextUtils.isEmpty(city) && !city.equals(R.string.城市未填写)) {
                        String result = province + city;
                        if (!TextUtils.isEmpty(district)) {
                            result += district;
                        }
                        mArea.setText(result);
                    }
                }
            }
        });
    }

    public void editProfileClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                save();
                break;
            case R.id.btn_avatar:
                editAvatar();
                break;
            case R.id.btn_birth_day:
                editBirthday();
                break;
            case R.id.btn_gender:
                editGender();
                break;
            case R.id.btn_area:
                chooseCity();
                break;
        }
    }


    private void chooseCity() {
        final ArrayList<Province> list = CityUtil.getInstance().getCityList();
        if (list == null || list.size() == 0) {
            final Dialog loading = DialogUitl.loadingDialog(mContext);
            loading.show();
            CityUtil.getInstance().getCityListFromAssets(new CommonCallback<ArrayList<Province>>() {
                @Override
                public void callback(ArrayList<Province> newList) {
                    loading.dismiss();
                    if (newList != null) {
                        showChooseCityDialog(newList);
                    }
                }
            });
        } else {
            showChooseCityDialog(list);
        }
    }

    private void showChooseCityDialog(ArrayList<Province> list) {
        String province = mChooseProvince;
        String city = mChoosedCity;
        String district = mChoosedDistrict;
        if (TextUtils.isEmpty(province)) {
            province = AppConfig.getInstance().getProvince();
        }
        if (TextUtils.isEmpty(city)) {
            city = AppConfig.getInstance().getCity();
        }
        if (TextUtils.isEmpty(district)) {
            district = AppConfig.getInstance().getDistrict();
        }
        DialogUitl.showCityChooseDialog(this, list, province, city, district, new AddressPicker.OnAddressPickListener() {
            @Override
            public void onAddressPicked(Province province, City city, County county) {
                mChooseProvince = province.getAreaName();
                mChoosedCity = city.getAreaName();
                mChoosedDistrict = county.getAreaName();
                mArea.setText(mChooseProvince + mChoosedCity + mChoosedDistrict);
            }
        });
    }


    private void editAvatar() {
        DialogUitl.showStringArrayDialog(mContext, new String[]{getResources().getString(R.string.相机), getResources().getString(R.string.相册)}, new DialogUitl.StringArrayDialogCallback() {
            @Override
            public void onItemClick(String text, int position) {
                if (mFragment != null) {
                    if (position == 0) {
                        mFragment.forwardCamera();
                    } else {
                        mFragment.forwardAlumb();
                    }
                }
            }
        });
    }

    private void editBirthday() {
        DialogUitl.showDatePickerDialog(mContext, new DialogUitl.DataPickerCallback() {
            @Override
            public void onComfirmClick(String date) {
                mBirthday.setText(date);
            }
        });

    }

    private void editGender() {
        DialogUitl.showStringArrayDialog(mContext, new String[]{getResources().getString(R.string.man), getResources().getString(R.string.woman)}, new DialogUitl.StringArrayDialogCallback() {
            @Override
            public void onItemClick(String text, int position) {
                mGender.setText(text);
            }
        });
    }

    private void save() {
        JSONObject obj = null;
        String nickname = mEditName.getText().toString().trim();
        if (TextUtils.isEmpty(nickname)) {
            ToastUtil.show(WordUtil.getString(R.string.please_input_nickname));
            return;
        } else {
            if (!nickname.equals(mUserBean.getUser_nicename())) {
                obj = new JSONObject();
                obj.put(Constants.USER_NICE_NAME, nickname);
            }
        }
        String birthday = mBirthday.getText().toString();
        if (TextUtils.isEmpty(birthday)) {
            birthday = "";
        }
        if (!birthday.equals(mUserBean.getBirthday())) {
            if (obj == null) {
                obj = new JSONObject();
            }
            obj.put(Constants.BIRTHDAY, birthday);
        }
        String genderString = mGender.getText().toString();
        if (!TextUtils.isEmpty(genderString)) {
            int index = Constants.GENDER_MAP.indexOfValue(genderString);
            if (index >= 0) {
                int gender = Constants.GENDER_MAP.keyAt(index);
                if (gender != mUserBean.getSex()) {
                    if (obj == null) {
                        obj = new JSONObject();
                    }
                    obj.put(Constants.SEX, gender);
                }
            }
        }
        if (!TextUtils.isEmpty(mChooseProvince) && !mChooseProvince.equals(mUserBean.getProvince())) {
            if (obj == null) {
                obj = new JSONObject();
            }
            obj.put(Constants.PROVINCE, mChooseProvince);
        }
        if (!TextUtils.isEmpty(mChoosedCity) && !mChoosedCity.equals(mUserBean.getCity())) {
            if (obj == null) {
                obj = new JSONObject();
            }
            obj.put(Constants.CITY, mChoosedCity);
        }
        if (!TextUtils.isEmpty(mChoosedDistrict) && !mChoosedDistrict.equals(mUserBean.getArea())) {
            if (obj == null) {
                obj = new JSONObject();
            }
            obj.put(Constants.AREA, mChoosedDistrict);
        }
        String sign = mSign.getText().toString();
        if (TextUtils.isEmpty(sign)) {
            sign = "";
        }
        if (!sign.equals(mUserBean.getSignature())) {
            if (obj == null) {
                obj = new JSONObject();
            }
            obj.put(Constants.SINGATURE, sign);
        }
        if (obj != null) {
            final String json = obj.toJSONString();
            mBtnSave.setClickable(false);
            HttpUtil.updateFields(json, new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0) {
                        ToastUtil.show(WordUtil.getString(R.string.update_success));
                        EventBus.getDefault().post(new NeedRefreshEvent());
                        finish();
                    }
                }
            });
        } else {
            if ((mNewAvatar != null && !mNewAvatar.equals(mUserBean.getAvatar())) ||
                    (mNewAvatarThumb != null && !mNewAvatarThumb.equals(mUserBean.getAvatar_thumb()))) {
                EventBus.getDefault().post(new NeedRefreshEvent());
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if ((mNewAvatar != null && !mNewAvatar.equals(mUserBean.getAvatar())) ||
                (mNewAvatarThumb != null && !mNewAvatarThumb.equals(mUserBean.getAvatar_thumb()))) {
            EventBus.getDefault().post(new NeedRefreshEvent());
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.GET_BASE_INFO);
        HttpUtil.cancel(HttpUtil.UPDATE_FIELDS);
        super.onDestroy();
    }
}
