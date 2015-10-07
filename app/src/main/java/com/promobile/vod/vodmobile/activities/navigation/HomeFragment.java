package com.promobile.vod.vodmobile.activities.navigation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.promobile.vod.vodmobile.R;
import com.promobile.vod.vodmobile.activities.DescriptionActivity;
import com.promobile.vod.vodmobile.adapter.VideoListAdapter;
import com.promobile.vod.vodmobile.model.Video;
import com.promobile.vod.vodmobile.util.LocalStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CRUZ JR, A.C.V. on 06/10/15.
 */
public class HomeFragment extends MainActivity.PlaceholderFragment {
    private View rootView;

    private ListView listView;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        init();

        return rootView;
    }

    private void init() {
        initializeProgressDialog();
        initializeListView();
        finalizeProgressDialog();
    }

    private void initializeListView() {
        listView = (ListView) rootView.findViewById(R.id.listview_home);

        Video video1 = new Video(getString(R.string.url_dash_youtube), "Glass - Advertising", 135, R.mipmap.film_google_glass);
        Video video2 = new Video(getString(R.string.url_dash_2s_vodserver_dataset8), "Sintel - Open Movie", 888, R.mipmap.film_sintel);

        ArrayList<Video> list = new ArrayList<>();
        list.add(video1);
        list.add(video2);

        listView.setAdapter(new VideoListAdapter(rootView.getContext(), list));

        listView.setOnItemClickListener(new OnVideoItemClickListener(list));
    }

    private void initializeProgressDialog() {

    }

    private void finalizeProgressDialog() {

    }

    private class OnVideoItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        List<Video> videoList;

        public OnVideoItemClickListener(List<Video> list) {
            this.videoList = list;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            LocalStorage.getInstance(rootView.getContext()).addToStorage(LocalStorage.VIDEO_URL, videoList.get(position).getUrl());
            Log.d("PlayListAct", "position: " + position);
            Intent intent = new Intent(rootView.getContext(), DescriptionActivity.class);
            startActivity(intent);
        }
    }
}
