package com.promobile.vod.vodmobile.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
    private VideoListAdapter adapter;
    private LocalStorage localStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);

        init();
    }

    private void init() {
        initializeProgressDialog();
        initializeViews();
        sendRequestPlayList();
        finalizeProgressDialog();
    }

    private void sendRequestPlayList() {

    }

    private void initializeViews() {
        listView = (ListView) findViewById(R.id.listView);

        ArrayList<Video> list = new ArrayList<>();

        //adapter = new VideoListAdapter(getApplicationContext(), list, );

        //listView.setAdapter(adapter);

        listView.setOnItemClickListener(new OnVideoItemClickListener(list));

        localStorage = LocalStorage.getInstance(getApplicationContext());
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
            Button btnDetails, btnWatch;
            final int pposition = position;

            btnDetails = (Button) view.findViewById(R.id.item_button_details);
            btnWatch = (Button) view.findViewById(R.id.item_button_watch);

            if(btnDetails.getVisibility() == View.GONE && btnWatch.getVisibility() == View.GONE) {
                btnDetails.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        localStorage.addToStorage(LocalStorage.VIDEO_URL, videoList.get(pposition).getPath());
                        localStorage.addToStorage(LocalStorage.OBJ_VIDEO, videoList.get(pposition));
                        Intent intent = new Intent(getApplicationContext(), DescriptionActivity.class);
                        startActivity(intent);
                    }
                });
                btnWatch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        localStorage.addToStorage(LocalStorage.VIDEO_URL, videoList.get(pposition).getPath());
                        localStorage.addToStorage(LocalStorage.OBJ_VIDEO, videoList.get(pposition));
                        Intent intent = new Intent(getApplicationContext(), VodPlayerActivity.class);
                        startActivity(intent);
                    }
                });

                btnDetails.setVisibility(View.VISIBLE);
                btnWatch.setVisibility(View.VISIBLE);
            }
            else {
                btnDetails.setVisibility(View.GONE);
                btnWatch.setVisibility(View.GONE);
            }
        }
    }
}
