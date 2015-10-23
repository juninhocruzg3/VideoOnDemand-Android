package com.promobile.vod.vodmobile.connection;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.promobile.vod.vodmobile.model.Channel;
import com.promobile.vod.vodmobile.model.Video;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by CRUZ JR, A.C.V. on 14/10/15.
 * Esta classe gerencia a obtenção de dados da API fonte de informações de canais e vídeos do VOD.
 */
public class VodSource {
    public static final String URL_SERVER = "http://vod.icomp.ufam.edu.br";

    public interface VideosMostPopularListener {
        void onSucess(ArrayList<Video> arrayList);
        void onError(VolleyError error);
    }

    public interface ChannelsListListener {
        void onSucess(ArrayList<Channel> arrayList);
        void onError(VolleyError error);
    }

    public interface ChannelListener {
        void onSucess(Channel channel);
        void onError(VolleyError error);
    }

    public interface VideoListener {
        void onSucess(Video video);
        void onError(VolleyError error);
    }

    private final String SERVICE_VIDEOS_MOST_POPULAR = "/api/videos_most_popular";
    private final String SERVICE_CHANNELS_LIST = "/api/channels_list";
    private final String SERVICE_CHANNEL = "/api/channels/";
    private final String SERVICE_VIDEO = "/api/videos/";

    private static VodSource vodSource;

    private VideosMostPopularListener mostPopularListener;
    private ChannelsListListener channelsListListener;
    private ChannelListener channelListener;
    private VideoListener videoListener;

    private VolleyController volleyController;

    private VodSource() {
        volleyController = VolleyController.getInstance();
    }

    public static VodSource getInstance() {
        if(vodSource == null)
            vodSource = new VodSource();

        return vodSource;
    }

    public void getVideo(Video video, VideoListener videoListener) {
        this.videoListener = videoListener;
        VideoResponseListener videoResponseListener = new VideoResponseListener();

        StringRequest stringRequest = new StringRequest(URL_SERVER + SERVICE_VIDEO + video.getId(), videoResponseListener, videoResponseListener);

        // Time out set to 2000ms and retry number is set to 2
        stringRequest.setRetryPolicy(new DefaultRetryPolicy());

        // Adding request to request queue
        VolleyController.getInstance().addToRequestQueue(stringRequest, "VodSource");
    }

    public void getChannel(Channel channel, ChannelListener channelListener) {
        this.channelListener = channelListener;
        ChannelResponseListener channelResponseListener = new ChannelResponseListener(channel);

        StringRequest stringRequest = new StringRequest(URL_SERVER + SERVICE_CHANNEL + channel.getId(), channelResponseListener, channelResponseListener);

        // Time out set to 2000ms and retry number is set to 2
        stringRequest.setRetryPolicy(new DefaultRetryPolicy());

        // Adding request to request queue
        VolleyController.getInstance().addToRequestQueue(stringRequest, "VodSource");
    }

    public void getChannelsList(ChannelsListListener channelsListListener) {
        this.channelsListListener = channelsListListener;
        ChannelsList channelsList = new ChannelsList();

        StringRequest stringRequest = new StringRequest(URL_SERVER + SERVICE_CHANNELS_LIST, channelsList, channelsList);

        // Time out set to 2000ms and retry number is set to 2
        stringRequest.setRetryPolicy(new DefaultRetryPolicy());

        // Adding request to request queue
        VolleyController.getInstance().addToRequestQueue(stringRequest, "VodSource");
    }

    public void getVideosMostPopular(VideosMostPopularListener mostPopularListener) {
        this.mostPopularListener = mostPopularListener;
        MostPopular mostPopular = new MostPopular();

        StringRequest stringRequest = new StringRequest(URL_SERVER + SERVICE_VIDEOS_MOST_POPULAR, mostPopular, mostPopular);

        // Time out set to 2000ms and retry number is set to 2
        stringRequest.setRetryPolicy(new DefaultRetryPolicy());

        // Adding request to request queue
        VolleyController.getInstance().addToRequestQueue(stringRequest, "VodSource");
    }

    public ImageLoader getImageLoader() {
        return volleyController.getImageLoader();
    }

    private class MostPopular implements Response.Listener<String>, Response.ErrorListener {
        @Override
        public void onResponse(String response) {
            ArrayList<Video> arrayList = new ArrayList<>();

            try {
                JSONArray jsonArray = new JSONArray(response);

                Log.d("VodSource", jsonArray.toString());

                for(int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    Log.d("VodSource", "for[" + i + "] = " + jsonObject.toString());

                    Video video = Video.getVideoFromJsonObject(jsonObject);

                    if(video != null)
                        arrayList.add(video);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mostPopularListener.onSucess(arrayList);
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            mostPopularListener.onError(error);
        }
    }

    private class ChannelsList implements Response.Listener<String>, Response.ErrorListener {
        @Override
        public void onResponse(String response) {
            ArrayList<Channel> arrayList = new ArrayList<>();

            try {
                JSONArray jsonArray = new JSONArray(response);

                Log.d("VodSource", jsonArray.toString());

                for(int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    Log.d("VodSource", "channel[" + i + "] = " + jsonObject.toString());

                    Channel channel = Channel.getChannelFromJsonObject(jsonObject);

                    if(channel != null)
                        arrayList.add(channel);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            channelsListListener.onSucess(arrayList);
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            channelsListListener.onError(error);
        }
    }

    private class ChannelResponseListener implements Response.Listener<String>, Response.ErrorListener {
        private Channel channel;

        public ChannelResponseListener(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void onResponse(String response) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("playlist_track");

                Log.d("VodSource", "channel = " + jsonObject.toString());

                ArrayList<Video> tracks = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonVideo = jsonArray.getJSONObject(i);
                    Video video = Video.getVideoFromJsonObject(jsonVideo);
                    if(video != null)
                        tracks.add(video);
                }
                channel.setTracks(tracks);
            } catch (JSONException e) {
                Log.e("ChannelRespListener", "Erro ao fazer parse de JSON para Vídeo");
            }

            channelListener.onSucess(channel);
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            channelListener.onError(error);
        }
    }

    private class VideoResponseListener implements Response.Listener<String>, Response.ErrorListener {
        @Override
        public void onResponse(String response) {
            Video video = null;
            try {
                JSONObject jsonObject = new JSONObject(response);

                Log.d("VodSource", "Video = " + jsonObject.toString());

                video = Video.getVideoFromJsonObject(jsonObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            videoListener.onSucess(video);
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            videoListener.onError(error);
        }
    }
}