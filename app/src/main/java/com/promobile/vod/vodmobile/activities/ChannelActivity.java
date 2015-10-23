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
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.promobile.vod.vodmobile.R;
import com.promobile.vod.vodmobile.adapter.VideoListAdapter;
import com.promobile.vod.vodmobile.connection.VodSource;
import com.promobile.vod.vodmobile.model.Channel;
import com.promobile.vod.vodmobile.model.Video;
import com.promobile.vod.vodmobile.util.LocalStorage;

import java.util.ArrayList;


public class ChannelActivity extends ActionBarActivity {
    private ListView listView;
    private ProgressDialog progressDialog;
    private VideoListAdapter adapter;
    private LocalStorage localStorage;
    private TextView tvError;
    private VodSource vodSource;
    private ImageLoader imageLoader;
    private Channel channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);

        init();
    }

    private void init() {
        initializeProgressDialog();
        initializeVariables();
        sendRequestPlayList();
    }

    private void sendRequestPlayList() {
        vodSource.getChannel(channel, new VodSource.ChannelListener() {
            @Override
            public void onSucess(Channel channel) {
                if (channel != null && channel.getTracks() != null) {
                    ArrayList<Video> videoList = channel.getTracks();
                    adapter = new VideoListAdapter(getApplicationContext(), videoList, imageLoader);
                    listView.setAdapter(adapter);
                }

                if (adapter.isEmpty()) {
                    onErrorDownloadList();
                }

                finalizeProgressDialog();
            }

            @Override
            public void onError(VolleyError error) {
                onErrorDownloadList();
                finalizeProgressDialog();
            }
        });
    }

    private void onErrorDownloadList() {
        tvError.setText(getString(R.string.error_video_list));
        tvError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeProgressDialog();
                sendRequestPlayList();
                tvError.setText("");
                tvError.setClickable(false);
            }
        });
        tvError.setClickable(true);
    }

    private void initializeVariables() {
        localStorage = LocalStorage.getInstance(getApplicationContext());
        channel = localStorage.getObjectFromStorage(LocalStorage.OBJ_CHANNEL, Channel.class);

        setTitle(channel.getName());

        if(vodSource == null)
            vodSource = VodSource.getInstance();

        imageLoader = vodSource.getImageLoader();

        tvError = (TextView) findViewById(R.id.tv_erro_play_list);
        tvError.setClickable(false);
        tvError.setText("");

        listView = (ListView) findViewById(R.id.listview_playlist);

        adapter = null;

        listView.setOnItemClickListener(new OnVideoItemClickListener());
    }

    private void initializeProgressDialog() {
        finalizeProgressDialog();
        progressDialog = ProgressDialog.show(this, null, getString(R.string.loading));
        progressDialog.setCancelable(false);
    }

    private void finalizeProgressDialog() {
        if(progressDialog != null)
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
                        localStorage.addToStorage(LocalStorage.OBJ_VIDEO, adapter.getItem(pposition));
                        Intent intent = new Intent(getApplicationContext(), DescriptionActivity.class);
                        startActivity(intent);
                    }
                });
                btnWatch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        localStorage.addToStorage(LocalStorage.OBJ_VIDEO, adapter.getItem(pposition));
                        Intent intent = new Intent(getApplicationContext(), VodPlayerActivity.class);
                        startActivity(intent);
                    }
                });
            }

            adapter.showVideoOptions(position);
        }
    }
}
