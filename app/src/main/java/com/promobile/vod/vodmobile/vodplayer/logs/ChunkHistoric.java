package com.promobile.vod.vodmobile.vodplayer.logs;

import java.util.ArrayList;

/**
 * Created by CRUZ JR, A.C.V. on 22/07/15.
 * Classe criada para gerenciar os valores de Logs para análises e cálculos
 */
public class ChunkHistoric {
    private static ArrayList<ChunkHistoric> chunkHistorics;

    private double bandWidthMeter;
    private long bytes;
    private long length;

    public static ArrayList<ChunkHistoric> getInstance() {
        if(chunkHistorics == null)
            chunkHistorics = new ArrayList<>();

        return chunkHistorics;
    }

    public static ChunkHistoric addChunkLog(double bandWidthMeter, long bytes) {
        getInstance();

        ChunkHistoric chunkHistoric = new ChunkHistoric(bandWidthMeter, bytes);

        chunkHistorics.add(chunkHistoric);

        return chunkHistoric;
    }

    private ChunkHistoric(double bandWidthMeter, long bytes) {
        this.bandWidthMeter = bandWidthMeter;
        this.bytes = bytes;
    }

    public static ArrayList<ChunkHistoric> getChunkHistorics() {
        return chunkHistorics;
    }

    public static void setChunkHistorics(ArrayList<ChunkHistoric> chunkHistorics) {
        ChunkHistoric.chunkHistorics = chunkHistorics;
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
