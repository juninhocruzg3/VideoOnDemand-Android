package com.promobile.vod.vodmobile.vodplayer.util;

/**
 * Created by CRUZ JR, A.C.V. on 08/06/15.
 * Classe criada para manipular formatos de tempo
 */
public class TimeFormat {
    public static String miliToHHmmss(long time) {
        long seconds = (time/1000);
        long minutes = (seconds/60);
        long hour = minutes/60;
        minutes = minutes % 60;
        seconds = seconds % 60;

        if(hour > 0) {
            return String.format("%03d:%02d:%02ds", hour, minutes, seconds);
        } else {
            return String.format("%02d:%02ds", minutes, seconds);
        }
    }
}
