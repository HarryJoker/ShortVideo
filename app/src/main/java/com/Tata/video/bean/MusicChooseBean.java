package com.Tata.video.bean;

/**
 * Created by cxf on 2018/6/20.
 * 选择音乐的实体类
 */

public class MusicChooseBean {

    private String path;
    private String title;
    private String name;
    private String artist;
    private long duration;
    private String durationString;

    public MusicChooseBean() {
    }

    public MusicChooseBean(String path, String title, String name, String artist, long duration) {
        this.path = path;
        this.title = title;
        this.name = name;
        this.artist = artist;
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getDurationString() {
        return durationString;
    }

    public void setDurationString(String durationString) {
        this.durationString = durationString;
    }


    /**
     * 把一个long类型的总毫秒数转成时长
     */
    public static String castDurationString(long duration) {
        int hours = (int) (duration / (1000 * 60 * 60));
        int minutes = (int) ((duration % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) ((duration % (1000 * 60)) / 1000);
        String s = "";
        if (hours > 0) {
            if (hours < 10) {
                s += "0" + hours + ":";
            } else {
                s += hours + ":";
            }
        }
        if (minutes > 0) {
            if (minutes < 10) {
                s += "0" + minutes + ":";
            } else {
                s += minutes + ":";
            }
        } else {
            s += "00" + ":";
        }
        if (seconds > 0) {
            if (seconds < 10) {
                s += "0" + seconds;
            } else {
                s += seconds;
            }
        } else {
            s += "00";
        }
        return s;
    }

}
