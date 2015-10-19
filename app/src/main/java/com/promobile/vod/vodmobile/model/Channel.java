package com.promobile.vod.vodmobile.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by CRUZ JR, A.C.V. on 19/10/15.
 * Esta classe implementa a abstração de um canal.
 */
public class Channel {
    public static int VIDEO_ID_MODE = 0;
    public static int VIDEO_INFO_MODE = 1;

    private String id;
    private String name;
    private String uploader;
    private String description;
    private ArrayList<Video> tracks;
    private int viewCount;

    public Channel(String id, String name, String uploader, String description, ArrayList<Video> tracks, int viewCount) {
        this();
        this.id = id;
        this.name = name;
        this.uploader = uploader;
        this.description = description;
        this.tracks = tracks;
        this.viewCount = viewCount;
    }

    public Channel() {
        super();
    }

    public static Channel getChannelFromJsonObject(JSONObject jsonObject) {
        Channel channel = null;

        try {
            String id = jsonObject.getJSONObject("_id").getString("$id");
            String name = jsonObject.getString("playlist_nome");
            String uploader = jsonObject.getString("playlist_uploader");
            String description = jsonObject.getString("playlist_descricao");
            JSONArray jsonArrayTracks = jsonObject.getJSONArray("playlist_track");
            ArrayList<Video> tracks = new ArrayList<>();
            for (int i = 0; i < jsonArrayTracks.length(); i++) {
                tracks.add(new Video(jsonArrayTracks.getJSONObject(i).getString("$id")));
            }
            int viewCount = jsonObject.getString("playlist_viewCount") == null? 0 : jsonObject.getInt("playlist_viewCount");

            channel = new Channel(id, name, uploader, description, tracks, viewCount);
        }
        catch(JSONException e) {
            Log.e("ChannelFromJson", "Erro ao obter Canal de JSON: " + e.getMessage());
        }

        return channel;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Video> getTracks() {
        return tracks;
    }

    public void setTracks(ArrayList<Video> tracks) {
        this.tracks = tracks;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
}
