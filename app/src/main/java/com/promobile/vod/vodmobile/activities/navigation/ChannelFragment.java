package com.promobile.vod.vodmobile.activities.navigation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.promobile.vod.vodmobile.R;
import com.promobile.vod.vodmobile.activities.ChannelActivity;
import com.promobile.vod.vodmobile.connection.VodSource;
import com.promobile.vod.vodmobile.model.Channel;
import com.promobile.vod.vodmobile.util.LocalStorage;

import java.util.ArrayList;

/**
 * Created by CRUZ JR, A.C.V. on 06/10/15.
 *
 */
public class ChannelFragment extends MainActivity.PlaceholderFragment {
    private View rootView;
    private ListView listView;
    private ArrayAdapter adapter;
    private LocalStorage localStrorage;
    private VodSource vodSource;
    private TextView tvError;
    private static ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_channel, container, false);

        init();

        return rootView;
    }

    private void init() {
        initializeProgressDialog();
        listView = (ListView) rootView.findViewById(R.id.listview_channel);
        localStrorage = LocalStorage.getInstance(rootView.getContext());

        if(vodSource == null) {
            vodSource = VodSource.getInstance();
        }

        tvError = (TextView) rootView.findViewById(R.id.tv_erro_channel_list);
        tvError.setClickable(false);
        tvError.setText("");

        listView.setOnItemClickListener(new OnChannelClickList());

        sendChannelListRequest();
    }

    private void sendChannelListRequest() {
        vodSource.getChannelsList(new VodSource.ChannelsListListener() {
            @Override
            public void onSucess(ArrayList<Channel> arrayList) {
                if (arrayList != null) {
                    try {
                        adapter = new ArrayAdapter(rootView.getContext(), android.R.layout.simple_list_item_1, arrayList);
                        listView.setAdapter(adapter);


                    } catch (Exception e) {
                        Log.e("onSucessChannelList", "Adapter não suportou ArrayList<Canal>.");
                    }

                    finalizeProgressDialog();
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.e("onErrorChannelList", "Erro ao baixar lista de canais.");
                finalizeProgressDialog();
                onErrorDownloadList();
            }
        });
    }

    private void onErrorDownloadList() {
        tvError.setText(getString(R.string.error_channel_list));
        tvError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeProgressDialog();
                sendChannelListRequest();
                tvError.setText("");
                tvError.setClickable(false);
            }
        });
        tvError.setClickable(true);
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

    private class OnChannelClickList implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Channel channel = (Channel) adapter.getItem(position);
            localStrorage.addToStorage(LocalStorage.OBJ_CHANNEL, channel);
            Intent intent = new Intent(rootView.getContext(), ChannelActivity.class);
            startActivity(intent);
        }
    }
}
