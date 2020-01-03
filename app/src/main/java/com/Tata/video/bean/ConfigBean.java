package com.Tata.video.bean;

import android.text.TextUtils;

/**
 * Created by cxf on 2017/8/5.
 */

public class ConfigBean {
    private static final String SIGN = "PLGVI5oRwn:SJZbMyODETY3rq08fjaAvCN4Kkctu1sXiBlW6.9dFxzHeQ2hU7pm";
    private String apk_ver;
    private String apk_url;
    private String apk_des;
    private String wx_siteurl;
    private String app_android;
    private String video_share_des;
    private String video_share_title;
    private String share_title;
    private String share_des;
    private String name_coin;
    private String name_votes;
    private String enter_tip_level;
    private String maintain_switch;
    private String tximgfolder;
    private String txvideofolder;
    private String maintain_tips;
    private String[] live_time_coin;
    private String[] login_type;
    private String[][] live_type;
    private String[] share_type;
    private int cloudtype;
    private String qiniu_domain;
    private int private_letter_switch;
    private int private_letter_nums;
    private double draw_min_cash;
    private double bonus_min_cash;
    private double praise_percent;
    private String invite_tacket;
    private String signature;
    private String decryptSign;

    public String getApk_ver() {
        return apk_ver;
    }

    public void setApk_ver(String apk_ver) {
        this.apk_ver = apk_ver;
    }

    public String getApk_url() {
        return apk_url;
    }

    public void setApk_url(String apk_url) {
        this.apk_url = apk_url;
    }

    public String getWx_siteurl() {
        return wx_siteurl;
    }

    public String getApk_des() {
        return apk_des;
    }

    public void setApk_des(String apk_des) {
        this.apk_des = apk_des;
    }

    public void setWx_siteurl(String wx_siteurl) {
        this.wx_siteurl = wx_siteurl;
    }

    public String getApp_android() {
        return app_android;
    }

    public void setApp_android(String app_android) {
        this.app_android = app_android;
    }

    public String getShare_title() {
        return share_title;
    }

    public void setShare_title(String share_title) {
        this.share_title = share_title;
    }

    public String getShare_des() {
        return share_des;
    }

    public void setShare_des(String share_des) {
        this.share_des = share_des;
    }

    public String getName_coin() {
        return name_coin;
    }

    public void setName_coin(String name_coin) {
        this.name_coin = name_coin;
    }

    public String getName_votes() {
        return name_votes;
    }

    public void setName_votes(String name_votes) {
        this.name_votes = name_votes;
    }

    public String getEnter_tip_level() {
        return enter_tip_level;
    }

    public void setEnter_tip_level(String enter_tip_level) {
        this.enter_tip_level = enter_tip_level;
    }

    public String getMaintain_switch() {
        return maintain_switch;
    }

    public void setMaintain_switch(String maintain_switch) {
        this.maintain_switch = maintain_switch;
    }

    public String getMaintain_tips() {
        return maintain_tips;
    }

    public void setMaintain_tips(String maintain_tips) {
        this.maintain_tips = maintain_tips;
    }

    public String[] getLive_time_coin() {
        return live_time_coin;
    }

    public void setLive_time_coin(String[] live_time_coin) {
        this.live_time_coin = live_time_coin;
    }

    public String[] getLogin_type() {
        return login_type;
    }

    public void setLogin_type(String[] login_type) {
        this.login_type = login_type;
    }

    public String[][] getLive_type() {
        return live_type;
    }

    public void setLive_type(String[][] live_type) {
        this.live_type = live_type;
    }

    public String[] getShare_type() {
        return share_type;
    }

    public void setShare_type(String[] share_type) {
        this.share_type = share_type;
    }


    public String getVideo_share_des() {
        return video_share_des;
    }

    public void setVideo_share_des(String video_share_des) {
        this.video_share_des = video_share_des;
    }

    public String getVideo_share_title() {
        return video_share_title;
    }

    public void setVideo_share_title(String video_share_title) {
        this.video_share_title = video_share_title;
    }

    public String getTximgfolder() {
        return tximgfolder;
    }

    public void setTximgfolder(String tximgfolder) {
        this.tximgfolder = tximgfolder;
    }

    public String getTxvideofolder() {
        return txvideofolder;
    }

    public void setTxvideofolder(String txvideofolder) {
        this.txvideofolder = txvideofolder;
    }

    public int getCloudtype() {
        return cloudtype;
    }

    public void setCloudtype(int cloudtype) {
        this.cloudtype = cloudtype;
    }

    public String getQiniu_domain() {
        return qiniu_domain;
    }

    public void setQiniu_domain(String qiniu_domain) {
        this.qiniu_domain = qiniu_domain;
    }

    public int getPrivate_letter_switch() {
        return private_letter_switch;
    }

    public void setPrivate_letter_switch(int private_letter_switch) {
        this.private_letter_switch = private_letter_switch;
    }

    public int getPrivate_letter_nums() {
        return private_letter_nums;
    }

    public void setPrivate_letter_nums(int private_letter_nums) {
        this.private_letter_nums = private_letter_nums;
    }

    public double getDraw_min_cash() {
        return draw_min_cash;
    }

    public void setDraw_min_cash(double draw_min_cash) {
        this.draw_min_cash = draw_min_cash;
    }

    public double getBonus_min_cash() {
        return bonus_min_cash;
    }

    public void setBonus_min_cash(double bonus_min_cash) {
        this.bonus_min_cash = bonus_min_cash;
    }

    public double getPraise_percent() {
        return praise_percent;
    }

    public void setPraise_percent(double praise_percent) {
        this.praise_percent = praise_percent;
    }

    public String getInvite_tacket() {
        return invite_tacket;
    }

    public void setInvite_tacket(String invite_tacket) {
        this.invite_tacket = invite_tacket;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
        this.decryptSign = null;
    }

    public String getDecryptSign() {
        if (!TextUtils.isEmpty(this.decryptSign)) {
            if (this.decryptSign.length() > 2) {
                return this.decryptSign.substring(1, this.decryptSign.length() - 1);
            } else {
                return this.decryptSign;
            }
        }
        if (TextUtils.isEmpty(this.signature)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int strLen = SIGN.length();
        int codeLen = this.signature.length();
        for (int i = 0; i < codeLen; i++) {
            for (int j = 0; j < strLen; j++) {
                if (this.signature.charAt(i) == SIGN.charAt(j)) {
                    if (j - 1 < 0) {
                        sb.append(SIGN.charAt(strLen - 1));
                    } else {
                        sb.append(SIGN.charAt(j - 1));
                    }
                }
            }
        }
        this.decryptSign = sb.toString();
        if (this.decryptSign.length() > 2) {
            return this.decryptSign.substring(1, this.decryptSign.length() - 1);
        } else {
            return this.decryptSign;
        }
    }
}
