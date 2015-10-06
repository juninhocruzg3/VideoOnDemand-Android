package br.ufam.icomp.fingerprinting;

import java.util.HashMap;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;


public class HandleResponse {

    private static HandleResponse mInstance = null;

    public HandleResponse() {

    }

    public static HandleResponse getInstance() {
        if (mInstance == null) {
            mInstance = new HandleResponse();
        }

        return mInstance;
    }

    public void sendAttributesToServer(final Context context, final String attributes) {
        String url = "http://192.168.3.103/vod/index.php?r=fpvod/verifydevice/";

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("attributes",attributes);

        CustomJsonObjectRequest request = new CustomJsonObjectRequest(Request.Method.POST,
                url,
                params,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.i("Script", "SUCCESS: "+response);
                        //Toast.makeText(context, "Enviou =)", Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.d("Error", "Error: " + error.getMessage());
                        //Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });


        // Adding request to request queue
        VolleyController.getInstance().addToRequestQueue(request, "jsonObjReq");

    }
}