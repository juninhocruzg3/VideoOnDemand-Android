package com.promobile.vod.vodmobile.connection;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by CRUZ JR, A.C.V. on 30/09/15.
 */
public class VolleyManager {
    private static VolleyManager volleyManager;
    private Context context;
    private RequestQueue queue;

    public static VolleyManager getInstance(Context context) {
        if(volleyManager == null) {
            volleyManager = new VolleyManager();
            volleyManager.context = context;
            volleyManager.queue = Volley.newRequestQueue(context);
        }

        return volleyManager;
    }

    public void request(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        StringRequest request = new StringRequest(Request.Method.POST, url, listener, errorListener);

        queue.add(request);
    }

    public static boolean hasInstance() {
        if(volleyManager != null) {
            return true;
        }
        return false;
    }

    public void killRequests(String tag) {
        if(volleyManager.queue != null) {
            volleyManager.queue.cancelAll(tag);
        }
    }
}
