package com.promobile.vod.vodmobile.util;


import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.promobile.vod.vodmobile.connection.VolleyController;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;


public class Fingerprinting {

    private Context context;

    // Used to get network interface's info.
    private NetworkInfo networkInfo;
    private ConnectivityManager cm;
    private String interfaceType;

    // Used to get device's ID (IMEI for GSM and the MEID or ESN for CDMA phones).
    private TelephonyManager telMnger;
    private String deviceId;

    // Used to get device screen's dimensions.
    private WindowManager windowManager;
    private Display display;
    private Point size;
    private String width, height;

    // Used to store the data that will be sent to the server.
    private JSONObject data;

    // Server's url.
    private String urlSendTo;

    private Listener listener;


    public Fingerprinting (Context mContext){
        this.context = mContext;
        this.data = new JSONObject();
        this.urlSendTo = "http://10.208.200.92/vod/fpvod/verifydevice/";
        //this.urlSendTo = "http://10.208.200.92/vod_mobile/webservice/";
    }

    public Fingerprinting (Context mContext, String url) {
        this.context = mContext;
        this.data = new JSONObject();
        this.urlSendTo = url;
    }

    // Main function which is responsible to do the fingerprinting.
    public void doFingerprinting() {

        try {
            getNetworkInterface();
            getDeviceId();
            getScreenDimension();
            getDevice();
            makeKey();
        }
        catch(JSONException e){
            e.printStackTrace();
        }

        
        if(listener != null)
            listener.onFinish(data.toString());
    }

    // Getting network's interface name (WIFI, MOBILE).
    private void getNetworkInterface() throws JSONException {
        cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            interfaceType = networkInfo.getTypeName();
        } else {
            interfaceType = "Not connected";
        }
        data.put("interface", interfaceType);
    }

    // Getting device'd ID (IMEI for GSM and the MEID or ESN for CDMA phones).
    private void getDeviceId() throws JSONException {
        telMnger = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        deviceId = telMnger.getDeviceId();

        data.put("imei", deviceId);
    }

    // Getting device screen's dimensions.
    private void getScreenDimension() throws JSONException {
        windowManager = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        width = String.valueOf(size.x);
        height = String.valueOf(size.y);

        data.put("width", width);
        data.put("height", height);
    }


    public void getDevice() throws JSONException {
        data.put("device", "mobile");
    }

    private void makeKey() throws JSONException {
        String key = md5(deviceId + width + height);

        data.put("key", key);
    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    // Called by some activity which already has the collected attributes.
    public void sendFingerprinting(String data) {

        sendAttributesToServer(data, urlSendTo);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onFinish(String s);
    }

    public void sendAttributesToServer(final String attributes, String url) {

            StringRequest strReq = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d("Response: ", response);

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("FingerPrintingError", "Erro no envio do Fingerprinting ao Servidor.");
                }
            }){

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    //params.put("attributes",  attributes);
                    params.put("attributes",  attributes);

                    return params;
                }

            };

            // Time out set to 10000ms and retry number is set to 2
            DefaultRetryPolicy  retryPolicy = new DefaultRetryPolicy(2000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            strReq.setRetryPolicy(retryPolicy);

            // Adding request to request queue
            VolleyController.getInstance().addToRequestQueue(strReq, "strReq");

    }
}
