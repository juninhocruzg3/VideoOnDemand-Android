package br.ufam.icomp.fingerprinting;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.WindowManager;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    // Used to get WiFi networks configured in the supplicant.
    private WifiManager wifiManager;
    private ArrayList<WifiConfiguration> wifiConfigurationArrayList;
    private String wifiList = "";

    // Used to get bluetooth paired devices.
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> bluetoothDevices;
    private String bluetoothDeviceString = "";

    // Used to store the data that will be sent to the server.
    private JSONObject data;

    // Server's url.
    private String urlSendTo;

    private Listener listener;


    public Fingerprinting (Context mContext){
        this.context = mContext;
        data = new JSONObject();
        this.urlSendTo = "http://192.168.3.103/vod/index.php?r=fpvod/verifydevice/";
    }

    public Fingerprinting (Context mContext, String url){
        this.context = mContext;
        this.data = new JSONObject();
        this.urlSendTo = url;
    }

    // Main function which is responsible to do the fingerprinting.
    public void doFingerprinting(){

        try {
            getNetworkInterface();
            getDeviceId();
            getScreenDimension();
            getWifiConfiguredNetworks();
            getBluetoothPairedDevices();
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
        interfaceType = networkInfo.getTypeName();

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

    // Getting WiFi networks configured in the supplicant.
    private void getWifiConfiguredNetworks() throws JSONException {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            wifiConfigurationArrayList = (ArrayList) wifiManager.getConfiguredNetworks();
            wifiManager.setWifiEnabled(false);
        } else {
            wifiConfigurationArrayList = (ArrayList) wifiManager.getConfiguredNetworks();
        }

        for(WifiConfiguration wm : wifiConfigurationArrayList) {
            wifiList += wm.SSID + ", ";
        }

        data.put("wifi", wifiList);
    }

    // Getting bluetooth paired device.
    private void getBluetoothPairedDevices() throws JSONException {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            while(bluetoothAdapter.getState() != bluetoothAdapter.STATE_ON) {}
            bluetoothDevices = bluetoothAdapter.getBondedDevices();
            bluetoothAdapter.disable();
        } else {
            bluetoothDevices = bluetoothAdapter.getBondedDevices();
        }

        for (BluetoothDevice bd : bluetoothDevices) {
            bluetoothDeviceString += bd.getAddress() + ", ";
        }

        data.put("bluetooth", bluetoothDeviceString);
    }

    public void getDevice() throws JSONException {
        data.put("device", "mobile");
    }

    private void makeKey() throws JSONException {
        String key = md5(interfaceType + deviceId + width + height + wifiList +
                bluetoothDeviceString);

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

    public void sendFingerprinting(Context context, String data) {

        HandleResponse hr = new HandleResponse();
        hr.sendAttributesToServer(context, data, urlSendTo);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onFinish(String s);
    }

    protected class HandleResponse {

        //private static HandleResponse mInstance = null;

        public HandleResponse() {

        }

        /*public static HandleResponse getInstance() {
            if (mInstance == null) {
                mInstance = new HandleResponse();
            }

            return mInstance;
        }*/

        public void sendAttributesToServer(final Context context, final String attributes, String url) {
            //String url = "http://192.168.3.103/vod/index.php?r=fpvod/verifydevice/";

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

    protected class CustomJsonObjectRequest extends Request<JSONObject> {
        private Response.Listener<JSONObject> response;
        private Map<String, String> params;


        public CustomJsonObjectRequest(int method, String url, Map<String, String> params, Response.Listener<JSONObject> response, Response.ErrorListener listener) {
            super(method, url, listener);
            this.params = params;
            this.response = response;
            // TODO Auto-generated constructor stub
        }
        public CustomJsonObjectRequest(String url, Map<String, String> params, Response.Listener<JSONObject> response, Response.ErrorListener listener) {
            super(Method.GET, url, listener);
            this.params = params;
            this.response = response;
            // TODO Auto-generated constructor stub
        }

        public Map<String, String> getParams() throws AuthFailureError {
            return params;
        }

	/*public Map<String, String> getHeaders() throws AuthFailureError{
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("apiKey", "Essa e minha API KEY: json object");

		return(header);
	}*/

        public Priority getPriority(){
            return(Priority.NORMAL);
        }


        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
            try {
                String js = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                return(Response.success(new JSONObject(js), HttpHeaderParser.parseCacheHeaders(response)));
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void deliverResponse(JSONObject response) {
            this.response.onResponse(response);
        }

    }

}
