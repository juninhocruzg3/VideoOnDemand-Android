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
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.promobile.vod.vodmobile.R;
import com.promobile.vod.vodmobile.activities.DescriptionActivity;
import com.promobile.vod.vodmobile.adapter.VideoListAdapter;
import com.promobile.vod.vodmobile.connection.LruBitmapCache;
import com.promobile.vod.vodmobile.connection.VodSource;
import com.promobile.vod.vodmobile.model.Video;
import com.promobile.vod.vodmobile.util.LocalStorage;

import java.util.ArrayList;

/**
 * Created by CRUZ JR, A.C.V. on 06/10/15.
 */
public class HomeFragment extends MainActivity.PlaceholderFragment {
    private View rootView;

    private ListView listView;
    private ProgressDialog progressDialog;
    private VodSource vodSource;
    private VideoListAdapter adapter;

    int i;
    private TextView tvError;
    private int thumbDownloadPosition;
    private ImageLoader imageLoader;
    private LruBitmapCache lruBitmapCache;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        init();

        return rootView;
    }

    private void init() {
        initializeProgressDialog();
        initializeVariables();
        sendRequestVideoList();
    }

    private void sendRequestVideoList() {
        vodSource.getVideosMostPopular(new VodSource.VideosMostPopularListener() {
            @Override
            public void onSucess(ArrayList<Video> arrayList) {
                if (arrayList != null) {
                    if(i == 0)
                        i++;
                    else
                        adapter = new VideoListAdapter(rootView.getContext(), arrayList, imageLoader);
                    listView.setAdapter(adapter);

                    finalizeProgressDialog();

                    if (adapter.isEmpty()) {
                        onErrorDownloadList();
                    }
                    else {
                        thumbDownloadPosition = 0;
                        startThumbDownload();
                    }
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.e("HomeFragment", "Erro no download da lista de Vídeos Mais Populares.");
                onErrorDownloadList();
            }
        });
    }

    private void onErrorDownloadList() {
        tvError = (TextView) rootView.findViewById(R.id.tv_erro_most_popular_list);
        tvError.setText(getString(R.string.erro_most_popular_list));
        tvError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeProgressDialog();
                sendRequestVideoList();
                tvError.setText("");
            }
        });
    }

    private void startThumbDownload() {

    }


    private void initializeVariables() {
        if(vodSource == null)
            vodSource = VodSource.getInstance();

        lruBitmapCache = new LruBitmapCache();

        imageLoader = new ImageLoader(vodSource.getQueue(), lruBitmapCache);

        listView = (ListView) rootView.findViewById(R.id.listview_home);

        adapter = new VideoListAdapter(rootView.getContext(), new ArrayList<Video>(), imageLoader);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new OnVideoItemClickListener());
    }

    private void initializeProgressDialog() {
        progressDialog = ProgressDialog.show(rootView.getContext(), null, getString(R.string.loading));
        progressDialog.setCancelable(false);
    }

    private void finalizeProgressDialog() {
        progressDialog.dismiss();
    }

    private class OnVideoItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            LocalStorage.getInstance(rootView.getContext()).addToStorage(LocalStorage.VIDEO_URL, adapter.getItem(position).getPath());
            Log.d("PlayListAct", "position: " + position);
            Intent intent = new Intent(rootView.getContext(), DescriptionActivity.class);
            startActivity(intent);
        }
    }
}
