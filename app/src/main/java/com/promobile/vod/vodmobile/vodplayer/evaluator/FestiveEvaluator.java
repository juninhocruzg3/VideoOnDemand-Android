package com.promobile.vod.vodmobile.vodplayer.evaluator;

import android.util.Log;

import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.chunk.FormatEvaluator;
import com.google.android.exoplayer.chunk.MediaChunk;
import com.google.android.exoplayer.upstream.BandwidthMeter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by CRUZ JR, A.C.V. on 25/08/15.
 * Esta classe implementa um avaliador FESTIVE
 */
public class FestiveEvaluator implements FormatEvaluator {
    public static final String TAG = "FESTIVE Evaluator";

    public static final double CONST_P = 85.0 / 100.0;
    public static final double CONST_ALPHA = 12.0;

    public static final int STATE_AGGRESSIVE = 0;
    public static final int STATE_STEADY = 1;
    private static final int DOWNLOADS_BASE = 20;

    private int state;

    private BandwidthMeter bandwidthMeter;
    private ArrayList<HistoricChunk> historicChunkList;

    private Format[] formats;
    private double bitrateReference;
    private double bitrateCompare;

    private boolean canIncrease;
    private int numberToChangeCanIncrease;

    public FestiveEvaluator(BandwidthMeter bandwidthMeter) {
        super();

        state = STATE_AGGRESSIVE;
        this.bandwidthMeter = bandwidthMeter;
        historicChunkList = new ArrayList<>();
    }

    @Override
    public void enable() {
        //Nada a fazer
    }

    @Override
    public void disable() {
        //Nada a fazer
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
            this.formats = formats;
        }

        Log.d(TAG, "Iniciando avaliação.");

        //Obtendo tempo de vídeo em buffer
        long bufferTime = queue.isEmpty() ? 0 : queue.get(queue.size() - 1).endTimeUs - playbackPositionUs;

        //Obtendo formato atual de qualidade de vídeo => Bitrate Current
        Format current = evaluation.format;

        if(current == null) {
            current = formats[formats.length-1];
        }

        //À princípio, considera-se a qualidade atual como a ideal.
        Format ideal = current;

        double bitrateEstimate = bandwidthMeter.getBitrateEstimate(); //Dado do último chunk baixado: Chunk (Em bytes) / Tempo (Em segundos) => b/s

        if(bitrateEstimate != -1) {
            if (isVideo) {
                if(!queue.isEmpty()) {

                    /**
                     * Adicionando dados ao histórico de Chunks
                     */
                    HistoricChunk historicChunk = new HistoricChunk();
                    MediaChunk lastChunkDownloaded = queue.get(queue.size() - 1);
                    historicChunk.startTimeUs = lastChunkDownloaded.startTimeUs;
                    historicChunk.endTimeUs = lastChunkDownloaded.endTimeUs;
                    historicChunk.nextChunkIndex = lastChunkDownloaded.nextChunkIndex;
                    historicChunk.bitrateEstimate = bitrateEstimate;
                    historicChunk.time = Calendar.getInstance().getTimeInMillis();
                    historicChunk.bitrateDownloaded = current.id;  //Qualidade em que o Chunk foi baixado.

                    historicChunkList.add(historicChunk);

                    /**
                     * Gerando Log para debug... Está comentado por causa do atraso na reprodução.
                     */
                    String log = "Quantidade de chunks baixados"+historicChunkList.size();
//
//                    for (int i = 0; i < historicChunkList.size(); i++) {
//                        String chunkLog = "[" + i + "]\n" + historicChunkList.get(i).toString() + "\n\n";
//                        log += chunkLog;
//                    }
//
                    Log.i(TAG, log);


                    /**
                     * Bitrate Selection => Estado normal
                     */
                    if (state == STATE_STEADY){
                        Log.d(TAG, "STEADY STATE");
                        /**
                         * Este é o bitrate apenas para a comparação
                         */
                        bitrateCompare = getBitrateHarmonicEstimated(historicChunkList) * CONST_P;

                        Log.d(TAG, "Média harmônica calculada!");

                        double bitrateCurrent = current.bitrate;

                        if(bitrateCompare < bitrateCurrent) {
                            Log.d(TAG, "decrementando...");
                            if(canDecrease(current, formats)) {
                                ideal = decrease(current, formats);
                                numberToChangeCanIncrease = formats.length - identifyFormat(ideal, formats);
                                canIncrease = false;
                                Log.i(TAG, "Esperar para incremento: " + numberToChangeCanIncrease);
                            }
                        }
                        else if(canIncrease) {
                            Log.d(TAG, "Incrementando...");
                            bitrateReference = getNextHigher(bitrateCurrent);

                            Log.d(TAG, "Bitrate referencia encontrado: " + bitrateReference);

                            double scoreReference = getTotalScore(bitrateReference) + 1;
                            double scoreCurrent = getTotalScore(bitrateCurrent);

                            Log.d(TAG, "Scores obtidos");

                            Log.i(TAG, "Score:\nReference = " + scoreReference + "\nCurrent = " + scoreCurrent);

                            if (scoreReference < scoreCurrent) {
                                /**
                                 * Formato alterado para Bitrate referencia.
                                 */
                                ideal = increase(current, formats);
                                int formatPosition = identifyFormat(ideal, formats);

                                numberToChangeCanIncrease = (formatPosition == 0) ? 0 : formats.length - formatPosition;

                                canIncrease = false;
                            } else {
                                ideal = current;
                            }
                        }
                        else {
                            if(numberToChangeCanIncrease > 1) {
                                numberToChangeCanIncrease--;
                                canIncrease = false;
                            }
                            else if(numberToChangeCanIncrease == 1) {
                                numberToChangeCanIncrease--;
                                canIncrease = true;
                            }
                        }
                    }
                    else if(state == STATE_AGGRESSIVE) {
                        Log.d(TAG, "AGRESSIVE STATE");
                        //Estado de download agressivo: Neste estado, serão baixados os 20 primeiros chunks na pior qualidade.
                        ideal = formats[formats.length -1];

                        if(historicChunkList.size() < DOWNLOADS_BASE) {
                            state = STATE_AGGRESSIVE;
                        }
                        else {
                            state = STATE_STEADY;
                            canIncrease = true;
                            numberToChangeCanIncrease = 0;
                        }
                    }
                }
            }
        }

        evaluation.format = ideal;

        Log.i(TAG, "Avaliação encerrada: idealFormat: " + identifyFormat(ideal, formats));
    }

    /**
     * Retorna o bitrate imediatamente maior do que o bitrate corrente. Segundo a lista de formatos "Format[]".
     * @param current bitrate corrente
     * @return bitrate imediatamente maior do que o bitrate corrente. Segundo a lista de formatos "Format[]".
     */
    private double getNextHigher(double current) {
        for(int i = formats.length; i > 0; i--) {
            if(formats[i-1].bitrate > current) {
                return formats[i-1].bitrate;
            }
        }

        //Este caso não deve acontecer.
        return current;
    }

    private double getTotalScore(double bitrate) {
        return getEfficiencyScore(bitrate) * CONST_ALPHA + getStabilityScore(bitrate);
    }

    private double getEfficiencyScore(double bitrate) {
        double score = 0.0;

        Log.d(TAG, "calculando Ineficiencia");

        if(bitrateReference < bitrateCompare) {
            score = ( bitrate / bitrateReference ) - 1;
        }
        else {
            score = ( bitrate / bitrateCompare ) - 1;
        }

        if(score < 0)
            score = score * (-1);

        Log.d(TAG, "Ineficiência calculada: " + score);

        return score;
    }

    private double getStabilityScore(double bitrate) {
        int changesCount = 0;

        Log.d(TAG, "calculando Instabilidade");

        long currentTime = Calendar.getInstance().getTimeInMillis();

        Log.d(TAG, "DATA... OK");

        long limitTime = currentTime - 20000;   //20s antes = 20000ms

        Log.d(TAG, "Time limit... OK");

        for(int i = historicChunkList.size(); i > 0 && historicChunkList.get(i-1).time > limitTime; i--) {
            changesCount++;
        }

        Log.d(TAG, "instabilidade calculada");

        /**
         * Resolvendo Potência
         */
        int score = changesCount == 0? 0 : 2;

        for(int i = 1; i < changesCount; i++) {
            score = score * 2;
        }

        Log.d(TAG, "Potência resolvida");

        return score;
    }

    private double getBitrateHarmonicEstimated(List<HistoricChunk> historicChunkList) {
        if(historicChunkList.size() < DOWNLOADS_BASE) {
            Log.e(TAG, "Erro ao calcular média harmônica. Histórico tem menos de 20 objetos.");

            return 0;
        }

        /**
         * Somatório de 1/x para o cálculo da média. SUM_INV() = (1/x0 + 1/x1 + 1/x2 + ... + 1/x19)
         */
        double inverseSum = 0.0;

        for(int i = historicChunkList.size() - DOWNLOADS_BASE; i < historicChunkList.size(); i++) {
            inverseSum += 1/historicChunkList.get(i).bitrateEstimate;
        }

        double harmonicMean = DOWNLOADS_BASE / inverseSum;

        Log.i(TAG, "HarmonicMean = " + harmonicMean);

        return harmonicMean;
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

    private int identifyFormat(Format current, Format[] formats) {
        for (int i = 0; i < formats.length; i++) {
            if(formats[i].bitrate == current.bitrate) {
                return i;
            }
        }
        //Algo errado ocorreu
        Log.e(TAG, "Erro 01 - Algo errado ocorreu");
        return 0;
    }

    private Format increase(Format current, Format[] formats) {
        int i = identifyFormat(current, formats);

        if(i > 0) {
            return formats[i - 1];
        }

        //Impossível incrementar. Já está na qualidade máxima
        Log.d(TAG, "Impossível incrementar. Já está na qualidade máxima.");
        return formats[i];
    }

    private Format decrease(Format current, Format[] formats) {
        int i = identifyFormat(current, formats);

        if(i < formats.length - 1) {
            return formats[i + 1];
        }

        //Impossível decrementar. Já está na qualidade mínima
        Log.d(TAG, "Impossível decrementar. Já está na qualidade mínima.");
        return formats[i];
    }

    class HistoricChunk {

        public long startTimeUs;
        public long endTimeUs;
        public int nextChunkIndex;
        public double bitrateEstimate;
        public long time;
        public String bitrateDownloaded;

        @Override
        public String toString() {
            return "Historic Chunk\nstartTimeUs = " + startTimeUs + "\nendTimeUs = " + endTimeUs + "\nNextID = " + nextChunkIndex +
                    "\nBitrateEstimated: " + bitrateEstimate;
        }
    }
}
