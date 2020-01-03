package com.Tata.video.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Tata.video.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import cn.qqtheme.framework.entity.Province;
import cn.qqtheme.framework.picker.AddressPicker;


/**
 * Created by cxf on 2017/8/8.
 */

public class DialogUitl {

    //第三方登录的时候用显示的dialog
    public static Dialog loginAuthDialog(Context context) {
        Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.dialog_login_loading);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    /**
     * 用于网络请求等耗时操作的LoadingDialog
     */
    public static Dialog loadingDialog(Context context, String text) {
        Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (!"".equals(text)) {
            TextView titleView = (TextView) dialog.findViewById(R.id.text);
            titleView.setText(text);
        }
        return dialog;
    }

    public static Dialog loadingDialog(Context context) {
        return loadingDialog(context, "");
    }

    public static void showSimpleDialog(Context context, String msg, final SimpleDialogCallback callback) {
        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.dialog_simple);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        TextView msgTextView = (TextView) dialog.findViewById(R.id.msg);
        msgTextView.setText(msg);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (callback != null) {
                    if (v.getId() == R.id.btn_confirm) {
                        callback.onComfirmClick();
                    } else {
                        callback.onCancelClick();
                    }
                }
            }
        };
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(listener);
        dialog.findViewById(R.id.btn_confirm).setOnClickListener(listener);
        dialog.show();
    }

    public static void showStringArrayDialog(Context context, String[] array, final StringArrayDialogCallback callback) {
        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.dialog_string_array);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        LinearLayout container = (LinearLayout) dialog.findViewById(R.id.container);
        View.OnClickListener itemListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) v;
                if (callback != null) {
                    callback.onItemClick(textView.getText().toString(), (int) v.getTag());
                }
                dialog.dismiss();
            }
        };
        for (int i = 0, length = array.length; i < length; i++) {
            TextView textView = new TextView(context);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DpUtil.dp2px(40)));
            textView.setTextColor(0xff000000);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setText(array[i]);
            textView.setTag(i);
            textView.setOnClickListener(itemListener);
            container.addView(textView);
        }
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 用户中心更多
     */
    public static void showUserMoreDialog(Context context, String[] array, int[] colors, final StringArrayDialogCallback callback) {
        final Dialog dialog = new Dialog(context, R.style.dialog2);
        dialog.setContentView(R.layout.dialog_string_array2);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
        LinearLayout container = (LinearLayout) dialog.findViewById(R.id.container);
        View.OnClickListener itemListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) v;
                if (callback != null) {
                    callback.onItemClick(textView.getText().toString(), (int) v.getTag());
                }
                dialog.dismiss();
            }
        };
        for (int i = 0, length = array.length; i < length; i++) {
            TextView textView = new TextView(context);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DpUtil.dp2px(50)));
            int color = 0xff000000;
            if (colors != null && i < colors.length) {
                color = colors[i];
            }
            textView.setTextColor(color);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            textView.setGravity(Gravity.CENTER);
            textView.setText(array[i]);
            textView.setTag(i);
            textView.setOnClickListener(itemListener);
            container.addView(textView);
            if (i != length - 1) {
                View v = new View(context);
                v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DpUtil.dp2px(1)));
                v.setBackgroundColor(0xff76787A);
                container.addView(v);
            }
        }
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static void showCityChooseDialog(Activity activity, ArrayList<Province> list,
                                            String province, String city, String district, AddressPicker.OnAddressPickListener listener) {
        AddressPicker picker = new AddressPicker(activity, list);
        picker.setTextColor(0xff636363);
        picker.setDividerColor(0xffededed);
        picker.setAnimationStyle(R.style.bottomToTopAnim);
        picker.setCancelTextColor(0xff1227fa);
        picker.setSubmitTextColor(0xff1227fa);
        picker.setTopLineColor(0xffececec);
        picker.setTopBackgroundColor(0xffececec);
        picker.setHeight(DpUtil.dp2px(250));
        picker.setOffset(5);
        picker.setHideProvince(false);
        picker.setHideCounty(false);
        picker.setColumnWeight(3 / 9.0f, 3 / 9.0f, 3 / 9.0f);
        if (TextUtils.isEmpty(province)) {
            province = "Beijing";
        }
        if (TextUtils.isEmpty(city)) {
            city = "Beijing";
        }
        if (TextUtils.isEmpty(district)) {
            district = "Dongcheng District";
        }
        picker.setSelectedItem(province, city, district);
        picker.setOnAddressPickListener(listener);
        picker.show();
    }

    public static void showDatePickerDialog(Context context, final DataPickerCallback callback) {
        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.dialog_date_picker);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.datePicker);
        final Calendar c = Calendar.getInstance();
        datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker view, int year, int month, int dayOfMonth) {
                c.set(year, month, dayOfMonth);
            }
        });
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.btn_confirm) {
                    if (callback != null) {
                        if (c.getTime().getTime() > new Date().getTime()) {
                            ToastUtil.show(WordUtil.getString(R.string.please_input_right_date));
                        } else {
                            String result = DateFormat.format("yyyy-MM-dd", c).toString();
                            callback.onComfirmClick(result);
                            dialog.dismiss();
                        }
                    }
                } else {
                    dialog.dismiss();
                }
            }
        };
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(listener);
        dialog.findViewById(R.id.btn_confirm).setOnClickListener(listener);
        dialog.show();
    }


    public interface DataPickerCallback {
        void onComfirmClick(String date);
    }

    public static abstract class SimpleDialogCallback {

        public abstract void onComfirmClick();

        public void onCancelClick() {

        }
    }

    public interface StringArrayDialogCallback {
        void onItemClick(String text, int position);
    }

}
