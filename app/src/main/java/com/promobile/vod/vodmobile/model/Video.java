package com.promobile.vod.vodmobile.model;

/**
 * Created by CRUZ JR, A.C.V. on 13/08/15.
 */
public class Video {
    private String url;
    private String name;
    private long duration;

    private int image;

    public Video(String url, String name, long duration) {
        this.url = url;
        this.name = name;
        this.duration = duration;
    }

    public Video(String url, String name, long duration, int id_image) {
        this(url, name, duration);
        this.image = id_image;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getFormattedDuration() {
        long seconds = duration % 60;
        long minutes = (duration / 60) % 60;
        long hours = duration / 3600;

        if(hours > 0) {
            return hours + ":" + minutes + ":" + seconds + "s";
        }
        else {
            return minutes + ":" + seconds + "s";
        }
    }

    public int getImage() {
        return image;
    }
}
