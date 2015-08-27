package com.promobile.vod.vodmobile.vodplayer.logs;

import java.util.ArrayList;

/**
 * Created by CRUZ JR, A.C.V. on 22/07/15.
 * Classe criada para gerenciar os valores de Logs para análises e cálculos
 */
public class ChunkLog {
    private static ArrayList<ChunkLog> chunkLogs;

    private double bandWidthMeter;
    private long bytes;
    private long length;

    public static ArrayList<ChunkLog> getInstance() {
        if(chunkLogs == null)
            chunkLogs = new ArrayList<>();

        return chunkLogs;
    }

    public static ChunkLog addChunkLog(double bandWidthMeter, long bytes) {
        if(chunkLogs == null)
            getInstance();

        ChunkLog chunkLog = new ChunkLog(bandWidthMeter, bytes);

        chunkLogs.add(chunkLog);

        return chunkLog;
    }

    private ChunkLog(double bandWidthMeter, long bytes) {
        this.bandWidthMeter = bandWidthMeter;
        this.bytes = bytes;
    }

    public static ArrayList<ChunkLog> getChunkLogs() {
        return chunkLogs;
    }

    public static void setChunkLogs(ArrayList<ChunkLog> chunkLogs) {
        ChunkLog.chunkLogs = chunkLogs;
    }

    public double getBandWidthMeter() {
        return bandWidthMeter;
    }

    public void setBandWidthMeter(double bandWidthMeter) {
        this.bandWidthMeter = bandWidthMeter;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }
}
