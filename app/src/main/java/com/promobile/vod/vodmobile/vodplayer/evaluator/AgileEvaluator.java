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
public class AgileEvaluator implements FormatEvaluator {
    private static final String TAG = "AgileEvaluator";

    private static int LIM_MIN_FOR_INCREASE; //Quantidade mínima de avaliações que devem ser feitas antes de incrementar

    public static final int L_BUFFER_REF = 20000000; //Limiar de REFERÊNCIA de conteúdo do buffer = 20s (em microssegundos)
    public static final int L_BUFFER_ALERT = L_BUFFER_REF/2; //Limiar de ALERTA de conteúdo do buffer = 10s (em microssegundos)
    public static final int L_BUFFER_MAX = 30000000; //Limiar Máximo de conteúdo do buffer = 30s (em microssegundos)

    private static final int MICROSECOND_TO_SECOND = 1/1000000; //Constante de transformação de microssegundos para segundos

    public static final int AGILE_MODE_BUFFER_OSCILLATIONS = 0;
    public static final int AGILE_MODE_VIDEO_RATE_FLUTUATIONS = 1;

    private static final double CONST_Kp_WEIGHT = 0.1; //Constante de peso percentual 10%
    private static final double CONST_Ki_WEIGHT = 0.01; //Constante de peso percentual 10%

    private boolean all_enable;
    private long chunksDuration; //Duração padão dos chunks (Em microsegundos)

    private int AGILE_MODE;

    private BandwidthMeter bandwidthMeter;

    private double sum_relative_buffer;
    private long currentBufferTime;

    private int evaluationCounter;

    public AgileEvaluator(BandwidthMeter bandwidthMeter, int AGILE_MODE) {
        this.bandwidthMeter = bandwidthMeter;
        this.AGILE_MODE = AGILE_MODE;
        sum_relative_buffer = 0;
        all_enable = false;
        evaluationCounter = 0;
    }

    @Override
    public void enable() {
        Log.d("AgileEvaluator", "Avaliador 'AGILE' ativado!");
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
        long lastBufferTime = currentBufferTime;
        currentBufferTime = queue.isEmpty() ? 0 : queue.get(queue.size() - 1).endTimeUs - playbackPositionUs;

        setDynamicCounter(currentBufferTime - lastBufferTime);

        //Obtendo formato atual de qualidade de vídeo
        Format current = evaluation.format;

        if(current == null) {
            current = formats[formats.length-1];
        }

        //À princípio, considera-se a qualidade atual como a ideal.
        Format ideal = current;

        //Obtendo largura de banda
        double currentBitrate = bandwidthMeter.getBitrateEstimate();

        if(all_enable) {
            double idealBitrate = 0;

            /**
             * Calculos do bitrate ideal
             */

            //Primeiro, para o caso do controle de oscilação no buffer.
            if (AGILE_MODE == AGILE_MODE_BUFFER_OSCILLATIONS) {
                double weight = CONST_Kp_WEIGHT * (currentBufferTime - L_BUFFER_REF) + CONST_Ki_WEIGHT * sum_relative_buffer;

                sum_relative_buffer += (currentBufferTime - L_BUFFER_REF);

                idealBitrate = ((weight * MICROSECOND_TO_SECOND) + 1) * currentBitrate;
            }
            //Segundo, para o caso de controle de oscilações da qualidade de vídeo.
            if(AGILE_MODE == AGILE_MODE_VIDEO_RATE_FLUTUATIONS) {
                double fluctuation = getFluctuationQ() * getFluctuationT(lastBufferTime) * getFluctuationV(formats[0].bitrate);
                idealBitrate = fluctuation * currentBitrate;
            }

            /**
             * Identificação do formato ideal
             */

            if (currentBufferTime <= L_BUFFER_ALERT) {
                //Estado de pânico: Decrementa a qualidade, a fim de não travar o vídeo.
                if(canDecrease(current, formats)) {
                    ideal = decrease(current, formats);
                }

                evaluationCounter = 0;
                Log.d(TAG, "estado: PÂNICO");
            } else if (idealBitrate > current.bitrate) {
                //Neste caso, verifica-se a contagem de avaliações para o incremento.
                evaluationCounter++;

                if(evaluationCounter > LIM_MIN_FOR_INCREASE) {
                    ideal = identifyIdealFormat(formats, idealBitrate);
                    evaluationCounter = 0;
                }

                Log.d(TAG, "estado: ANÁLISE DE INCREMENTO.");
            } else if(idealBitrate < current.bitrate) {
                //Neste caso, reinicia-se a contagem de avaliações para incremento.
                evaluationCounter = 0;

                Log.d(TAG, "estado: SEM INCREMENTO.");
            }
        }
        else {
            //Verifica se a classe de Download de chunks já baixou o primeiro chunk
            if (!queue.isEmpty()) {
                all_enable = true;
                chunksDuration = queue.get(queue.size()-1).startTimeUs - queue.get(queue.size()-1).endTimeUs;
            }
        }


        evaluation.format = ideal;

        Log.d(TAG, "Avaliação encerrada:\nBufferTime: " + currentBufferTime + "\nBitrate: " + ideal.bitrate + "\nwidth: " + ideal.width + "\nHeight: " + ideal.height
                                                    + "\nFormato selecionado: " + (identifyFormat(ideal, formats) + 1) + "/" + formats.length);

        if(!queue.isEmpty()) Log.d(TAG,  "\nQueue size = " + queue.size());

    }

    private void setDynamicCounter(long bufferDif) {
        if(bufferDif >= 0.4*chunksDuration && bufferDif < chunksDuration) {
            LIM_MIN_FOR_INCREASE = 1;
        }
        else if(bufferDif >= 0.2*chunksDuration && bufferDif < 0.4*chunksDuration) {
            LIM_MIN_FOR_INCREASE = 5;
        }
        else if(bufferDif >= 0 && bufferDif < 0.2*chunksDuration) {
            LIM_MIN_FOR_INCREASE = 15;
        }
        else {
            LIM_MIN_FOR_INCREASE = 20;
        }
    }

    private double getFluctuationV(int maxBitrate) {
        return 0;
    }

    private double getFluctuationT(long lastBufferTime) {
        return 0;
    }

    private double getFluctuationQ() {
        return 0;
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

    private Format identifyIdealFormat(Format[] formats, double idealBitrate) {
        for (int i = 0; i < formats.length; i++) {
            if (formats[i].bitrate < idealBitrate) {
                return formats[i];
            }
        }
        return formats[formats.length-1];
    }
}
