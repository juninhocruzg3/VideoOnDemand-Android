package com.promobile.vod.vodmobile.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.promobile.vod.vodmobile.R;
import com.promobile.vod.vodmobile.connection.VodSource;
import com.promobile.vod.vodmobile.model.Video;

import java.util.List;

/**
 * Created by CRUZ JR, A.C.V. on 13/08/15.
 * Esta classe faz a adaptação de uma List da classe Vídeo para um ListView
 */
public class VideoListAdapter extends ArrayAdapter<Video> {
    private Context context;
    private List<Video> videosList;
    private ImageLoader imageLoader;

    private int selectedItem;

    public VideoListAdapter(Context context, List<Video> videosList, ImageLoader imageLoader) {
        super(context, 0, videosList);
        this.context = context;
        this.videosList = videosList;
        this.imageLoader = imageLoader;
        selectedItem = -1;
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
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        Video video = videosList.get(position);
        String title = "" + video.getTitle();
        holder.title.setText(title);

        String duration = "" + video.getFormattedDuration();
        holder.duration.setText(duration);

        if(video.getThumbnails() == null) {
            imageLoader.get(VodSource.URL_SERVER + video.getThumb(), new VodImageListener(holder.imageView));
        }
        else {
            imageLoader.get(VodSource.URL_SERVER + video.getThumbnails().high, new VodImageListener(holder.imageView));
        }

        Log.d("VodListAdapter", "Item [" + position + "] => " + video.getTitle() + "\nViewParentCount = " + parent.getChildCount());

        Button btnDetails, btnWatch;

        btnDetails = (Button) view.findViewById(R.id.item_button_details);
        btnWatch = (Button) view.findViewById(R.id.item_button_watch);

        if(selectedItem == position) {
            btnDetails.setVisibility(View.VISIBLE);
            btnWatch.setVisibility(View.VISIBLE);
        }
        else {
            btnDetails.setVisibility(View.GONE);
            btnWatch.setVisibility(View.GONE);
        }

        return view;
    }

    public void showVideoOptions(int position) {
        this.selectedItem = position;

        notifyDataSetChanged();
    }

    public void showVideoOptions() {
        this.showVideoOptions(-1);
    }

    private class ViewHolder {
        public TextView title;
        public TextView duration;
        public ImageView imageView;
    }

    private class VodImageListener implements ImageLoader.ImageListener {
        private ImageView imageView;

        public VodImageListener(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
            if (response.getBitmap() != null) {
                imageView.setImageBitmap(response.getBitmap());
            }
            else {
                imageView.setImageResource(Video.DEFAULT_IMAGE);
            }

            notifyDataSetChanged();
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("VodImageListener", "Erro ao baixar ImageView");
            imageView.setImageResource(Video.ERROR_IMAGE);

            notifyDataSetChanged();
        }
    }
}
