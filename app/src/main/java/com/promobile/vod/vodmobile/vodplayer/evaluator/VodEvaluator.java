package com.promobile.vod.vodmobile.vodplayer.evaluator;

import android.util.Log;

import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.chunk.FormatEvaluator;
import com.google.android.exoplayer.chunk.MediaChunk;
import com.google.android.exoplayer.upstream.BandwidthMeter;

import java.util.List;

/**
 * Created by CRUZ JR, A.C.V. on 19/06/15.
 * Esta classe implementa um Avaliador de Formatos para o VodPlayer, que utiliza o ExoPlayer.
 */
public class VodEvaluator implements FormatEvaluator {
    private static final String TAG = "VodEvaluator";

    private static final int L_BUFFER_1 = 10000; //Limiar 1 de avaliação do buffer = 10s (em milissegundos)
    private static final int L_BUFFER_2 = 20000; //Limiar 2 de avaliação do buffer = 20s (em milissegundos)
    private static final int L_BUFFER_MAX = 30000; //Limiar Máximo de avaliação do buffer = 30s (em milissegundos)

    private static final double CONST_WEIGHT = 80 /100; //Constante de peso percentual 80%

    private BandwidthMeter bandwidthMeter;
    private double bitrateMedia;

    public VodEvaluator(BandwidthMeter bandwidthMeter) {
        this.bandwidthMeter = bandwidthMeter;
        bitrateMedia = bandwidthMeter.getBitrateEstimate();
    }

    @Override
    public void enable() {
        //Nada a fazer.
    }

    @Override
    public void disable() {
        //Nada a fazer.
    }

    @Override
    public void evaluate(List<? extends MediaChunk> queue, long playbackPositionUs, Format[] formats, Evaluation evaluation) {
        Log.d(TAG, "Iniciando avaliação.");
        //Obtendo tempo de vídeo em buffer
        long bufferTime = queue.isEmpty() ? 0 : queue.get(queue.size() - 1).endTimeUs - playbackPositionUs;
        //Obtendo formato atual de qualidade de vídeo
        Format current = evaluation.format;

        //À princípio, considera-se a qualidade atual como a ideal.
        Format ideal = current;

        double bitrate = bandwidthMeter.getBitrateEstimate();
        bitrateMedia = CONST_WEIGHT * bitrateMedia + (1 - CONST_WEIGHT) * bitrate;

        Format phi1 = calculatePhi(formats, CONST_WEIGHT * bitrate);

        Format phi2 = calculatePhi(formats, bitrateMedia);

        if(bufferTime <= L_BUFFER_1) {
            //Estado de pânico: Reduz a reprodução para a pior qualidade, a fim de não travar o vídeo.
            ideal = formats[formats.length - 1];

            Log.d(TAG, "estado: PÂNICO");
        }
        else if(bufferTime <= L_BUFFER_2) {
            //Neste caso, avalia-se a taxa de transmissão de dados.
            if(phi1.bitrate < current.bitrate) {
                ideal = phi1;
                Log.d(TAG, "estado: REDUÇÃO");
            }
            else if(phi1.bitrate > current.bitrate) {
                ideal = phi1;

                Log.d(TAG, "estado: INCREMENTO");
            }
        }
        else if(bufferTime <= L_BUFFER_MAX) {
            //Neste caso, as condições de buffer estão boas. Será avaliado a taxa de transmissão de dados.
            if(phi2.bitrate > current.bitrate) {
                ideal = phi2;

                Log.d(TAG, "estado: OK");
            }
        }

        evaluation.format = ideal;

        Log.d(TAG, "Avaliação encerrada:\n Bitrate: " + ideal.bitrate + "\nwidth: " + ideal.width + "\nHeight: " + ideal.height);
    }

    private Format calculatePhi(Format[] formats, double bitrate) {
        for (int i = 0; i < formats.length; i++) {
            if (formats[i].bitrate <= bitrate * CONST_WEIGHT) {
                return formats[i];
            }
        }
        return formats[formats.length-1];
    }
}
