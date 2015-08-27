package com.promobile.vod.vodmobile.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.promobile.vod.vodmobile.R;
import com.promobile.vod.vodmobile.adapter.VideoListAdapter;
import com.promobile.vod.vodmobile.model.Video;
import com.promobile.vod.vodmobile.util.LocalStorage;

import java.util.ArrayList;
import java.util.List;


public class PlayListActivity extends ActionBarActivity {
    private ListView listView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);

        init();
    }

    private void init() {
        initializeProgressDialog();
        initializeListView();
        finalizeProgressDialog();
    }

    private void initializeListView() {
        listView = (ListView) findViewById(R.id.listView);

        Video video1 = new Video(getString(R.string.url_dash_youtube), "Glass - Advertising", 135, R.mipmap.film_google_glass);
        Video video2 = new Video(getString(R.string.url_dash_2s_vodserver_dataset8), "Sintel - Open Movie", 888, R.mipmap.film_sintel);

        ArrayList<Video> list = new ArrayList<>();
        list.add(video1);
        list.add(video2);

        listView.setAdapter(new VideoListAdapter(getApplicationContext(), list));

        listView.setOnItemClickListener(new OnVideoItemClickListener(list));
    }

    private void initializeProgressDialog() {
        progressDialog = ProgressDialog.show(this, "", getString(R.string.loading));
    }

    private void finalizeProgressDialog() {
        progressDialog.dismiss();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class OnVideoItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        List<Video> videoList;

        public OnVideoItemClickListener(List<Video> list) {
            this.videoList = list;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            LocalStorage.getInstance(getApplicationContext()).addToStorage(LocalStorage.VIDEO_URL, videoList.get(position).getUrl());
            Log.d("PlayListAct", "position: " + position);
            Intent intent = new Intent(PlayListActivity.this, DescriptionActivity.class);
            startActivity(intent);
        }
    }
}
