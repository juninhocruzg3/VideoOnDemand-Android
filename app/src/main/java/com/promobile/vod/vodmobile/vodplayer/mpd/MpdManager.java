package com.promobile.vod.vodmobile.vodplayer.mpd;

import com.google.android.exoplayer.dash.mpd.AdaptationSet;
import com.google.android.exoplayer.dash.mpd.MediaPresentationDescription;
import com.google.android.exoplayer.dash.mpd.Period;
import com.google.android.exoplayer.dash.mpd.Representation;

import java.util.List;

/**
 * Created by CRUZ JR, A.C.V. on 19/05/15.
 * Essa classe gerencia as informações do MPD
 */
public class MpdManager {
    private MediaPresentationDescription mpd;
    private String contentId;

    public MpdManager(String contentId, MediaPresentationDescription mpd) {
        this.mpd = mpd;
        this.contentId = contentId;
    }

    public List<Representation> getVideoRepresentationList() {
        List<Period> periodList = mpd.periods;
        if(periodList.size() > 0) {
            List<AdaptationSet> adaptationSetList = periodList.get(0).adaptationSets;
            if(adaptationSetList.size() > 0) {
                List<Representation> representationList = null;

                for(int i = 0; i < adaptationSetList.size(); i++) {
                    representationList = adaptationSetList.get(i).representations;

                    if(representationList.size() > 0) {
                        String sub = representationList.get(0).format.mimeType.substring(0, 5);
                        int quest = sub.compareToIgnoreCase("video");
                        if(quest == 0) {
                            return representationList;
                        }
                    }
                }
            }
        }

        return null;
    }

    public List<Representation> getAudioRepresentationList() {
        List<Period> periodList = mpd.periods;
        if(periodList.size() > 0) {
            List<AdaptationSet> adaptationSetList = periodList.get(0).adaptationSets;
            if(adaptationSetList.size() > 0) {
                List<Representation> representationList = null;

                for(int i = 0; i < adaptationSetList.size(); i++) {
                    representationList = adaptationSetList.get(i).representations;

                    if(representationList.size() > 0) {
                        String sub = representationList.get(0).format.mimeType.substring(0, 5);
                        int quest = sub.compareToIgnoreCase("audio");
                        if(quest == 0) {
                            return representationList;
                        }
                    }
                }
            }
        }

        return null;
    }

    public MediaPresentationDescription getMpd() {
        return mpd;
    }
}
