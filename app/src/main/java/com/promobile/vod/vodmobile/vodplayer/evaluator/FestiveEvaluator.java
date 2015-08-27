package com.promobile.vod.vodmobile.vodplayer.evaluator;

import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.chunk.FormatEvaluator;
import com.google.android.exoplayer.chunk.MediaChunk;

import java.util.List;

/**
 * Created by CRUZ JR, A.C.V. on 25/08/15.
 * Esta classe implementa um avaliador FESTIVE
 */
public class FestiveEvaluator implements FormatEvaluator {
    public static final String TAG = "FESTIVE Evaluator";

    public static final double CONST_P = 85.0 / 100.0;

    public static final int STATE_AGGRESSIVE = 0;
    public static final int STATE_STEADY = 1;

    private int state;

    public FestiveEvaluator() {
        super();

        state = STATE_AGGRESSIVE;
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }

    @Override
    public void evaluate(List<? extends MediaChunk> queue, long playbackPositionUs, Format[] formats, Evaluation evaluation) {
    }
}
