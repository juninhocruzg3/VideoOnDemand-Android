package com.promobile.vod.vodmobile.connection;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.promobile.vod.vodmobile.R;
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
    private final String URL_VIDEOS_MOST_POPULAR = "http://vod.icomp.ufam.edu.br/api/videos_most_popular";
    public static final String URL_SERVER = "http://vod.icomp.ufam.edu.br";

    private static VodSource vodSource;

    private VideosMostPopularListener mostPopularListener;

    private VolleyController volleyController;

    private VodSource() {
        volleyController = VolleyController.getInstance();
    }

    public static VodSource getInstance() {
        if(vodSource == null)
            vodSource = new VodSource();

        return vodSource;
    }

    public void getVideosMostPopular(VideosMostPopularListener mostPopularListener) {
        MostPopular mostPopular = new MostPopular();
        this.mostPopularListener = mostPopularListener;
        StringRequest stringRequest = new StringRequest(URL_VIDEOS_MOST_POPULAR, mostPopular, mostPopular);

        // Time out set to 2000ms and retry number is set to 2
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(2000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        VolleyController.getInstance().addToRequestQueue(stringRequest, "VodSource");
    }

    public RequestQueue getQueue() {
        return volleyController.getRequestQueue();
    }

    public interface VideosMostPopularListener {
        void onSucess(ArrayList<Video> arrayList);
        void onError(VolleyError error);
    }

    public interface GetThumbListener {
        void onSucess(ArrayList<Video> arrayList);
        void onError(VolleyError error);
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

                    Video video = Video.getVideoFromJsonObject(jsonObject, R.mipmap.vod_logo);

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
}