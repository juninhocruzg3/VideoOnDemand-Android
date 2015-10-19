package com.promobile.vod.vodmobile.activities.navigation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.promobile.vod.vodmobile.R;
import com.promobile.vod.vodmobile.activities.DescriptionActivity;
import com.promobile.vod.vodmobile.activities.VodPlayerActivity;
import com.promobile.vod.vodmobile.adapter.VideoListAdapter;
import com.promobile.vod.vodmobile.connection.VodSource;
import com.promobile.vod.vodmobile.model.Video;
import com.promobile.vod.vodmobile.util.LocalStorage;

import java.util.ArrayList;

/**
 * Created by CRUZ JR, A.C.V. on 06/10/15.
 */
public class HomeFragment extends MainActivity.PlaceholderFragment {
    private View rootView;

    private LocalStorage localStorage;
    private ListView listView;
    private static ProgressDialog progressDialog;
    private VodSource vodSource;
    private VideoListAdapter adapter;
    private TextView tvError;
    private ImageLoader imageLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null)
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
                    adapter = new VideoListAdapter(rootView.getContext(), arrayList, imageLoader);
                    listView.setAdapter(adapter);

                    finalizeProgressDialog();

                    if (adapter.isEmpty()) {
                        onErrorDownloadList();
                    }
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.e("HomeFragment", "Erro no download da lista de Vídeos Mais Populares.");
                finalizeProgressDialog();
                onErrorDownloadList();
            }
        });
    }

    private void onErrorDownloadList() {
        tvError.setText(getString(R.string.error_video_list));
        tvError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeProgressDialog();
                sendRequestVideoList();
                tvError.setText("");
                tvError.setClickable(false);
            }
        });
        tvError.setClickable(true);
    }

    private void initializeVariables() {
        localStorage = LocalStorage.getInstance(rootView.getContext());

        if(vodSource == null)
            vodSource = VodSource.getInstance();

        imageLoader = vodSource.getImageLoader();

        tvError = (TextView) rootView.findViewById(R.id.tv_erro_most_popular_list);
        tvError.setClickable(false);
        tvError.setText("");

        listView = (ListView) rootView.findViewById(R.id.listview_home);

        listView.setOnItemClickListener(new OnVideoItemClickListener());
    }

    private void initializeProgressDialog() {
        finalizeProgressDialog();
        progressDialog = ProgressDialog.show(rootView.getContext(), null, getString(R.string.loading));
        progressDialog.setCancelable(false);
    }

    private void finalizeProgressDialog() {
        if(progressDialog != null)
            progressDialog.dismiss();
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
                        localStorage.addToStorage(LocalStorage.VIDEO_URL, adapter.getItem(pposition).getPath());
                        localStorage.addToStorage(LocalStorage.OBJ_VIDEO, adapter.getItem(pposition));
                        Intent intent = new Intent(rootView.getContext(), DescriptionActivity.class);
                        startActivity(intent);
                    }
                });
                btnWatch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        localStorage.addToStorage(LocalStorage.VIDEO_URL, adapter.getItem(pposition).getPath());
                        localStorage.addToStorage(LocalStorage.OBJ_VIDEO, adapter.getItem(pposition));
                        Intent intent = new Intent(rootView.getContext(), VodPlayerActivity.class);
                        startActivity(intent);
                    }
                });
            }

            adapter.showVideoOptions(position);
        }
    }
}
