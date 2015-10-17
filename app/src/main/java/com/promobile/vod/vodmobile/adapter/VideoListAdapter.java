package com.promobile.vod.vodmobile.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.promobile.vod.vodmobile.R;
import com.promobile.vod.vodmobile.connection.VodSource;
import com.promobile.vod.vodmobile.model.Video;

import java.util.List;

/**
 * Created by CRUZ JR, A.C.V. on 13/08/15.
 */
public class VideoListAdapter extends ArrayAdapter<Video> {
    private Context context;
    private List<Video> videosList;
    private ImageLoader imageLoader;

    public VideoListAdapter(Context context, List<Video> videosList, ImageLoader imageLoader) {
        super(context, 0, videosList);
        this.context = context;
        this.videosList = videosList;
        this.imageLoader = imageLoader;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;



        if(view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_list_video, null);
            holder = new ViewHolder();
            view.setTag(holder);

            holder.title = (TextView) view.findViewById(R.id.item_title_video);
            holder.duration = (TextView) view.findViewById(R.id.item_duration_video);
            holder.imageView = (ImageView) view.findViewById(R.id.item_image_video);
            holder.video = videosList.get(position);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        holder.title.setText("" + holder.video.getTitle());
        holder.duration.setText("" + holder.video.getDescription());

        Log.d("VodList.ImageLoader", "start debug");
        Log.d("VodList.ImageLoader", holder.video.getThumb());
        Log.d("VodList.ImageLoader", holder.video.toString());

        imageLoader.get(VodSource.URL_SERVER + holder.video.getThumb(), ImageLoader.getImageListener(holder.imageView, R.mipmap.vod_logo, R.mipmap.film_sintel));

        return view;
    }

    private static class ViewHolder {
        public Video video;
        public TextView title;
        public TextView duration;
        public ImageView imageView;
    }
}
