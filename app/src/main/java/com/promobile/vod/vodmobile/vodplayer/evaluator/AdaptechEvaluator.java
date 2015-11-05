package com.promobile.vod.vodmobile.vodplayer.evaluator;

import android.util.Log;

import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.chunk.FormatEvaluator;
import com.google.android.exoplayer.chunk.MediaChunk;
import com.google.android.exoplayer.upstream.BandwidthMeter;
import com.promobile.vod.vodmobile.vodplayer.logs.LogOnDemand;
import com.promobile.vod.vodmobile.vodplayer.util.ChunkHistoric;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CRUZ JR, A.C.V. on 19/06/15.
 * Esta classe implementa um Avaliador de Formatos para o VodPlayer, que utiliza o ExoPlayer.
 */
public class AdaptechEvaluator implements FormatEvaluator {
    private static final String TAG = "AdapTechEvaluator";

    private static final int L_BUFFER_1 = 10000000; //Limiar 1 de avaliação do buffer = 10s (em microssegundos)
    private static final int L_BUFFER_2 = 20000000; //Limiar 2 de avaliação do buffer = 20s (em microssegundos)
    private static final int L_BUFFER_MAX = 30000000; //Limiar Máximo de avaliação do buffer = 30s (em microssegundos)

    private static final double CONST_WEIGHT = 80.0 /100.0; //Constante de peso percentual 80%

    private static final int TIME_LIMIT_TO_CALC_AVERAGE_BANDWIDTH = 15; //Tempo limite para calcular bandwidth médio = 15s (em segundos)

    private ArrayList<Double> historic;

    private BandwidthMeter bandwidthMeter;
    private double bitrateMedia;

    private boolean canDownload;

    public AdaptechEvaluator(BandwidthMeter bandwidthMeter) {
        this.bandwidthMeter = bandwidthMeter;
        bitrateMedia = 0;
        historic = new ArrayList<>();
    }

    @Override
    public void enable() {
        Log.d("AdaptechEvaluator", "Avaliador 'Adaptech' ativado!");
    }

    @Override
    public void disable() {
        //Nada a fazer.
    }

    @Override
    public void evaluate(List<? extends MediaChunk> queue, long playbackPositionUs, Format[] formats, Evaluation evaluation) {
        boolean isVideo = false;
        if (formats[0].mimeType.substring(0, 5).equalsIgnoreCase("audio")) {
            Log.d(TAG, "Formato de Áudio");
        }
        else {
            Log.d(TAG, "Formato de Vídeo");
            isVideo = true;
        }

        Log.d(TAG, "Iniciando avaliação.");
        //Obtendo tempo de vídeo em buffer
        long bufferTime = queue.isEmpty() ? 0 : queue.get(queue.size() - 1).endTimeUs - playbackPositionUs;
        //Obtendo formato atual de qualidade de vídeo
        Format current = evaluation.format;

        if(current == null) {
            current = formats[formats.length-1];
        }

        //À princípio, considera-se a qualidade atual como a ideal.
        Format ideal = current;

        double bitrate = bandwidthMeter.getBitrateEstimate();

        if(isVideo && !queue.isEmpty() && LogOnDemand.haveChunkLog) {
            MediaChunk logChunk = queue.get(queue.size() - 1);
            LogOnDemand.addFinishChunkLog(logChunk.getLength(), logChunk.nextChunkIndex, bitrate);
        }

        Log.d(TAG, "historico.size= " + historic.size());

        double bitrateMedia2 = 0;
        if(bitrate != -1) {
            if (isVideo) {
                if(!queue.isEmpty()) {
                    ChunkHistoric chunckLog = ChunkHistoric.addChunkLog(bitrate, queue.get(queue.size() - 1).getDataSpec().length);
                    bitrateMedia2 = calculateBitrateMedia(ChunkHistoric.getInstance());
                    Log.d(TAG, "Bitrate Média [2] = " + bitrateMedia2);
                }
            }
        }

        bitrateMedia = (bitrateMedia <= 0)? bitrate: CONST_WEIGHT * bitrateMedia + (1 - CONST_WEIGHT) * bitrate;

        Log.d(TAG, "Bitrate = " + bitrate);

        Format phi1 = calculatePhi(formats, CONST_WEIGHT * bitrate);

        Format phi2 = calculatePhi(formats, CONST_WEIGHT * bitrateMedia2);

        if (bufferTime <= L_BUFFER_1) {
            //Estado de pânico: Reduz a reprodução para a pior qualidade, a fim de não travar o vídeo.
            ideal = formats[formats.length - 1];

            canDownload = true;

            Log.d(TAG, "estado: PÂNICO");
        } else if (bufferTime <= L_BUFFER_2) {
            //Neste caso, avalia-se a taxa de transmissão de dados.
            if (phi1.bitrate < current.bitrate && canDecrease(current, formats)) {
                //Decrementar qualidade (um perfil)
                ideal = decrease(current, formats);
                Log.d(TAG, "estado: REDUÇÃO");
            } else if (phi1.bitrate > current.bitrate && canIncrease(current, formats)) {
                //Incrementar qualidade (um perfil)
                ideal = increase(current, formats);

                Log.d(TAG, "estado: INCREMENTO");
            }

            canDownload = true;
        } else if (bufferTime < L_BUFFER_MAX) {
            //Neste caso, as condições de buffer estão boas. Será avaliado a taxa de transmissão de dados.
            if (phi2.bitrate > current.bitrate && phi1.bitrate > current.bitrate && canIncrease(current, formats)) {
                ideal = increase(current, formats);

                Log.d(TAG, "estado: OK");
            }

            canDownload = true;
        } else if (bufferTime >= L_BUFFER_MAX) {
            canDownload = false;
        }

        evaluation.format = ideal;

        Log.d(TAG, "Avaliação encerrada:\nBufferTime: " + bufferTime + "\nBitrate: " + ideal.bitrate + "\nwidth: " + ideal.width + "\nHeight: " + ideal.height
                                                    + "\nFormato selecionado: " + (identifyFormat(ideal, formats) + 1) + "/" + formats.length);

        if(!queue.isEmpty()) Log.d(TAG,  "\nQueue size = " + queue.size());

    }

    private double calculateBitrateMedia(ArrayList<ChunkHistoric> chunkLogsList) {
        double media = 0;

        if(chunkLogsList.isEmpty()) {
            media = 0;
        }
        else {
            int firshIndexInTime = calculateFirshIndexInTime(chunkLogsList);

            media = chunkLogsList.get(firshIndexInTime).getBandWidthMeter();
            for (int i = firshIndexInTime+1; i < chunkLogsList.size(); i++) {
                media = (CONST_WEIGHT * media) + ((1 - CONST_WEIGHT) * chunkLogsList.get(i).getBandWidthMeter());
            }
        }

        return media;
    }

    private int calculateFirshIndexInTime(ArrayList<ChunkHistoric> chunkLogsList) {
        int firshIndexInTime = chunkLogsList.size();
        double totalTime = 0;
        do {
            if(firshIndexInTime == 0)
                return firshIndexInTime;
            else {
                firshIndexInTime--;
            }

            if(chunkLogsList.get(firshIndexInTime) != null)
                totalTime += ((double) chunkLogsList.get(firshIndexInTime).getBytes()) / chunkLogsList.get(firshIndexInTime).getBandWidthMeter();
        } while (totalTime < TIME_LIMIT_TO_CALC_AVERAGE_BANDWIDTH);

        return  firshIndexInTime;
    }

    private boolean canDecrease(Format current, Format[] formats) {
        int i = identifyFormat(current, formats);

        if (i < formats.length) {
            return true;
        }

        return false;
    }

    private boolean canIncrease(Format current, Format[] formats) {
        int i = identifyFormat(current, formats);

        if (i > 0) {
            return true;
        }
        return false;
    }

    private Format increase(Format current, Format[] formats) {
        int i = identifyFormat(current, formats);

        if(i > 0) {
            return formats[i - 1];
        }

        //Impossível incrementar. Já está na qualidade máxima
        Log.d("VodEvaluator", "Impossível incrementar. Já está na qualidade máxima.");
        return formats[i];
    }

    private Format decrease(Format current, Format[] formats) {
        int i = identifyFormat(current, formats);

        if(i < formats.length - 1) {
            return formats[i + 1];
        }

        //Impossível decrementar. Já está na qualidade mínima
        Log.d("VodEvaluator", "Impossível decrementar. Já está na qualidade mínima.");
        return formats[i];
    }

    private int identifyFormat(Format current, Format[] formats) {
        for (int i = 0; i < formats.length; i++) {
            if(formats[i].bitrate == current.bitrate) {
                return i;
            }
        }
        //Algo errado ocorreu
        Log.e("VodEvaluator", "Erro 01 - Algo errado ocorreu");
        return 0;
    }

    private Format calculatePhi(Format[] formats, double bitrate) {
        for (int i = 0; i < formats.length; i++) {
            double bit = bitrate * CONST_WEIGHT;
            if (formats[i].bitrate <= bit) {
                return formats[i];
            }
        }
        return formats[formats.length-1];
    }

    public boolean isCanDownload() {
        return canDownload;
    }
}
