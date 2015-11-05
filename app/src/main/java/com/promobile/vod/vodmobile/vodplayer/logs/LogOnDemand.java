package com.promobile.vod.vodmobile.vodplayer.logs;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.promobile.vod.vodmobile.connection.VodSource;
import com.promobile.vod.vodmobile.connection.VolleyController;
import com.promobile.vod.vodmobile.util.Fingerprinting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by CRUZ JR, A.C.V. on 23/07/15.
 * esta classe foi criada para gerenciar todos os logs da aplicação
 */
public class LogOnDemand {
    private static LogOnDemand logOnDemand;

    private static final String LOG_WEBSERVICE = "/api/logchunks";

    /**
     * Logs sobre chunks
     */
    private ArrayList<ChunkLog> chunkLogsList;
    public static boolean haveChunkLog;

    /**
     * Logs sobre buffer
     */
    private ArrayList<BufferLog> bufferLogsList;
    public static boolean haveBufferLog;

    /**
     * Logs sobre atrasos na reprodução
     */
    private ArrayList<StallLog> stallLogList;
    public static boolean haveStallLog;

    private static void reset() {
        logOnDemand = new LogOnDemand();
    }

    public static void enable() {
        getInstance();
    }

    public static ArrayList<ChunkLog> getChunkLogsList() {
        getInstance();

        return logOnDemand.chunkLogsList;
    }

    public static ArrayList<BufferLog> getBufferLogsList() {
        getInstance();

        return logOnDemand.bufferLogsList;
    }

    public static ArrayList<StallLog> getStallLogsList() {
        getInstance();

        return logOnDemand.stallLogList;
    }

    private static void getInstance() {
        if(logOnDemand == null)
            logOnDemand = new LogOnDemand();
    }

    private LogOnDemand() {
        chunkLogsList = new ArrayList<>();
        bufferLogsList = new ArrayList<>();
        stallLogList = new ArrayList<>();
    }

    private static boolean isLast;
    public static void addFinishChunkLog(long length, int nextChunkIndex, double bitrateEstimate) {
        long finishTime = new Date().getTime();
        if(nextChunkIndex != -1) {
            isLast = false;
        }

        if (isLast) {
            //Nothing to do
        }
        else {
            Log.d("ChunkLog", "Finish ChunkLog\nFinish Time: " + finishTime + "\nLength: " + length + "\nNextChunkIndex: " + nextChunkIndex + "\nBitrate: " + bitrateEstimate);
            ChunkLog chunkLog = getChunkLogsList().get(getChunkLogsList().size() - 1);
            chunkLog.atEndDownload = finishTime;
            chunkLog.chunkSize = length;
            chunkLog.nextChunk = nextChunkIndex;
            chunkLog.bandwidthMeter = bitrateEstimate;
        }

        if(!isLast && nextChunkIndex == -1) {
            isLast = true;

            printChunkLogs();
        }
    }

    private static void printChunkLogs() {
        ArrayList<ChunkLog> chunkLogs = getChunkLogsList();
        String log = "";

        for(int i = 0; i < chunkLogs.size(); i++) {
            log += "\n\nchunk[" + (i+1) + "]\nsegmentN: " + chunkLogs.get(i).segmentNum;
        }

        Log.d("ChunkLog", log);
    }

    public static void addStartChunkLog(int segmentNum, String representationId, int width) {
        long startTime = new Date().getTime();
        if(segmentNum != -1) {
            Log.d("ChunkLog", "Start ChunkLog\nStart Time: " + startTime + "\nSegment Num: " + segmentNum + "\nrepresentation id = " + representationId + "\nWhidth = " + width);

            ChunkLog chunkLog = new ChunkLog();
            chunkLog.atRequest = startTime;
            chunkLog.representationId = representationId;
            chunkLog.segmentNum = segmentNum;
            chunkLog.width = width;

            getChunkLogsList().add(chunkLog);
        }
    }

    public static void addBufferLog(int bufferedPercentage, long currentPosition, long bufferedPosition, long duration, long bufferStock) {
        BufferLog bufferLog = new BufferLog();
        bufferLog.bufferPosition = bufferedPosition;
        bufferLog.bufferStock = bufferStock;
        bufferLog.currentPosition = currentPosition;
        bufferLog.duration = duration;
        bufferLog.bufferPercentage = bufferedPercentage;

        getBufferLogsList().add(bufferLog);
    }

    public static void addStartStallLog() {
        long startTime = new Date().getTime();
        StallLog stallLog = new StallLog();
        stallLog.timeStart = startTime;
        getStallLogsList().add(stallLog);
    }

    public static void addFinishStallLog() {
        long finishTime = new Date().getTime();
        ArrayList<StallLog> stallLogsList = getStallLogsList();
        StallLog stallLog = stallLogsList.get(stallLogsList.size()-1);
        stallLog.timeEnd = finishTime;
        stallLog.duration = stallLog.timeEnd - stallLog.timeStart;
    }

    public static void sendLogs() {
        StringRequest stringRequest = new StringRequest(StringRequest.Method.POST, VodSource.URL_SERVER + LOG_WEBSERVICE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("LogOnDemand", "Log Enviado: " + response);
                LogOnDemand.reset();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("LogOnDemand", "Erro ao enviar log: " + error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();

                Gson gson = new Gson();

                JSONArray bufferJsonArray = new JSONArray();
                if(haveBufferLog) {
                    ArrayList<BufferLog> bufferLogsList = getBufferLogsList();
                    for (int i = 0; i < bufferLogsList.size(); i++) {
                        try {
                            bufferJsonArray.put(new JSONObject(gson.toJson(bufferLogsList.get(i))));
                        } catch (JSONException e) {
                            Log.e("LogOnDemand", "[bufferLog] Erro ao passar Gson para JSONObject: " + e.getMessage());
                        }
                    }
                }

                JSONArray chunkJsonArray = new JSONArray();
                if(haveChunkLog) {
                    ArrayList<ChunkLog> chunkLogsList = getChunkLogsList();
                    for (int i = 0; i < chunkLogsList.size(); i++) {
                        try {
                            chunkJsonArray.put(new JSONObject(gson.toJson(chunkLogsList.get(i))));
                        } catch (JSONException e) {
                            Log.e("LogOnDemand", "[chunkLog] Erro ao passar Gson para JSONObject: " + e.getMessage());
                        }
                    }
                }

                JSONArray stallJsonArray = new JSONArray();
                if(haveStallLog) {
                    ArrayList<StallLog> stallLogsList = getStallLogsList();
                    for (int i = 0; i < stallLogsList.size(); i++) {
                        try {
                            stallJsonArray.put(new JSONObject(gson.toJson(stallLogsList.get(i))));
                        } catch (JSONException e) {
                            Log.e("LogOnDemand", "[stallLog] Erro ao passar Gson para JSONObject: " + e.getMessage());
                        }
                    }
                }

                JSONObject logJsonObject = new JSONObject();
                try {
                    if(haveBufferLog)
                        logJsonObject.put("bufferLog", bufferJsonArray);
                    if(haveChunkLog)
                        logJsonObject.put("chunkLog", chunkJsonArray);
                    if(haveStallLog)
                        logJsonObject.put("stallLog", stallJsonArray);
                } catch (JSONException e) {
                    Log.e("LogOnDemand", "Erro : " + e.getMessage());
                }


                Log.d("LogOnDemand", "id_FP = " + Fingerprinting.getFpId());
                Log.d("LogOnDemand", "register = " + logJsonObject.toString());
                params.put("register", logJsonObject.toString());
                params.put("id_FP", Fingerprinting.getFpId());

                return params;
            }
        };

        // Time out set to 2000ms and retry number is set to 2
        stringRequest.setRetryPolicy(new DefaultRetryPolicy());

        // Adding request to request queue
        VolleyController.getInstance().addToRequestQueue(stringRequest, "LogOnDemand");
    }

    public static class ChunkLog {
        public long atRequest;
        public long atResponse;
        public long atEndDownload;
        public int segmentNum;
        public String representationId;
        public long chunkSize;
        public int chunkDuration;
        public int width;
        public double bandwidthMeter;
        public long length;
        public int nextChunk;
    }

    public static class BufferLog {
        public long duration;
        public long currentPosition;
        public long bufferPosition;
        public long bufferTime;
        public long bufferStock;
        public int bufferPercentage;
    }

    private static class StallLog {
        public long timeStart;
        public long timeEnd;
        public long duration;
    }
}
