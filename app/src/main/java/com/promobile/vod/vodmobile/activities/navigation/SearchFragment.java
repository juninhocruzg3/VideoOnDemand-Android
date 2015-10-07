package com.promobile.vod.vodmobile.activities.navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.promobile.vod.vodmobile.R;

/**
 * Created by CRUZ JR, A.C.V. on 06/10/15.
 */
public class SearchFragment extends MainActivity.PlaceholderFragment {
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search, container, false);

        init();

        return rootView;
    }

    private void init() {

    }
}
