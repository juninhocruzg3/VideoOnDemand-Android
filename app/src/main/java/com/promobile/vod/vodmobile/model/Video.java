package com.promobile.vod.vodmobile.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by CRUZ JR, A.C.V. on 13/08/15.
 * Esta classe implementa a abstração de um Vídeo
 */
public class Video {
    private String id;
    private double rating;
    private int likeCount;
    private String commentCount;
    private int ratingCount;
    private long duration;
    private int favoriteCount;
    private String title;
    private String updated;
    private int viewCount;
    private String tags;
    private String path;
    private String thumb;
    private String description;
    private String url;

    private int image;

    public Video(String id, double rating, int likeCount, String commentCount, int ratingCount,
                        long duration, int favoriteCount, String title, String updated, int viewCount,
                        String tags, String path, String thumb, String description, String url, int image) {
        this.id = id;
        this.rating = rating;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.ratingCount = ratingCount;
        this.duration = duration;
        this.favoriteCount = favoriteCount;
        this.title = title;
        this.updated = updated;
        this.viewCount = viewCount;
        this.tags = tags;
        this.path = path;
        this.thumb = thumb;
        this.description = description;
        this.url = url;
        this.image = image;
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

    public static Video getVideoFromJsonObject(JSONObject jsonObject, int defaultImageId) {
        Video video = null;

        try {
            String id = jsonObject.getJSONObject("_id").getString("$id");
            double rating = 0;  //jsonObject.getString("rating") == null? 0 : jsonObject.getInt("rating");
            int likeCount = 0;  //jsonObject.getString("likeCount") == null? 0 : jsonObject.getInt("likeCount");
            String commentCount = jsonObject.getString("commentCount");
            int ratingCount = 0;    //jsonObject.getString("ratingCount") == null? 0 : jsonObject.getInt("ratingCount");
            long duration = 0;  //jsonObject.getString("duration") == null? 0 : jsonObject.getLong("duration");
            int favoriteCount = 0;  //jsonObject.getString("favoriteCount") == null? 0 : jsonObject.getInt("favoriteCount");
            String title = jsonObject.getString("title");
            String updated = jsonObject.getString("updated");
            int viewCount = 0; //jsonObject.getString("viewCount") == null? 0 : jsonObject.getInt("viewCount");
            String tags = jsonObject.getString("tags");
            String path = jsonObject.getString("path");
            String thumb = jsonObject.getString("thumb");
            String description = jsonObject.getString("description");
            String url = jsonObject.getString("url");

            video = new Video(id, rating, likeCount, commentCount, ratingCount, duration, favoriteCount, title,
                                        updated, viewCount, tags, path, thumb, description, url, defaultImageId);
        } catch (JSONException e) {
            Log.e("Video.getVideoFromJson", "Erro ao traduzir JSON: " + e.getMessage());
        }

        if(video != null)
            Log.d("VideoFormJson", video.toString());
        else
            Log.d("VideoFormJson", "Video é nulo");

        return video;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Vídeo: " + title + " | Descrição: " + description + " | id " + id;
    }
}
