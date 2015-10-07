package com.promobile.vod.vodmobile.activities.navigation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.promobile.vod.vodmobile.R;
import com.promobile.vod.vodmobile.activities.PlayListActivity;

import java.util.ArrayList;

/**
 * Created by CRUZ JR, A.C.V. on 06/10/15.
 *
 */
public class ChannelFragment extends MainActivity.PlaceholderFragment {
    private View rootView;
    private ListView listView;
    private ArrayAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_channel, container, false);

        init();

        return rootView;
    }

    private void init() {
        listView = (ListView) rootView.findViewById(R.id.listview_channel);
        adapter = new ArrayAdapter(rootView.getContext(), android.R.layout.simple_list_item_1);

        ArrayList<String> list = new ArrayList<>();
        list.add("Algorítmos e Estrutura de Dados");

        adapter.add(list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnChannelClickList());
    }

    private class OnChannelClickList implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(rootView.getContext(), PlayListActivity.class);
            startActivity(intent);
        }
    }
}
