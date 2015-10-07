package com.promobile.vod.vodmobile.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.promobile.vod.vodmobile.R;
import com.promobile.vod.vodmobile.activities.navigation.MainActivity;

import java.util.ArrayList;

public class VideoListActivity extends Activity {

    private ListView listView;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        init();
    }

    private void init() {
        listView = (ListView) findViewById(R.id.listview_videolist);

        ArrayList<String> list = new ArrayList<>();
        list.add("Rosmael");
        list.add("Maykon");
        list.add("Júnior");

        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, list);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new OnClickItemVideoList());
    }

    class OnClickItemVideoList implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(getApplicationContext(), "Você clicou em " + adapter.getItem(position), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(VideoListActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }
}
