package com.promobile.vod.vodmobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.promobile.vod.vodmobile.R;
import com.promobile.vod.vodmobile.model.Video;

import java.util.List;

/**
 * Created by CRUZ JR, A.C.V. on 13/08/15.
 */
public class VideoListAdapter extends ArrayAdapter<Video> {
    private Context context;
    private List<Video> videosList;

    public VideoListAdapter(Context context, List<Video> videosList) {
        super(context, 0, videosList);
        this.context = context;
        this.videosList = videosList;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Video video = videosList.get(position);

        if(view == null)
            view = LayoutInflater.from(context).inflate(R.layout.item_list_video, null);

        TextView title = (TextView) view.findViewById(R.id.title_video);
        TextView duration = (TextView) view.findViewById(R.id.duration_video);
        ImageView image = (ImageView) view.findViewById(R.id.image_video);

        title.setText(video.getName());
        duration.setText(video.getFormattedDuration());
        if(video.getImage() != 0)
            image.setImageResource(video.getImage());
        else
            image.setImageResource(R.mipmap.vod_logo);

        return view;
    }
}
