package com.promobile.vod.vodmobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.promobile.vod.vodmobile.R;
import com.promobile.vod.vodmobile.connection.VodSource;
import com.promobile.vod.vodmobile.model.Video;
import com.promobile.vod.vodmobile.util.LocalStorage;

public class DescriptionActivity extends AppCompatActivity {
    private LocalStorage localStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        init();
    }

    private void init() {
        /**
         * LocalStorage para dados persistidos
         */
        localStorage = LocalStorage.getInstance(getApplicationContext());
        Video video = localStorage.getObjectFromStorage(LocalStorage.OBJ_VIDEO, Video.class);

        /**
         * Views
         */
        ImageView image = (ImageView) findViewById(R.id.image_description);
        TextView title = (TextView) findViewById(R.id.title_description);
        TextView duration = (TextView) findViewById(R.id.duration_description);
        TextView date = (TextView) findViewById(R.id.date_description);
        TextView description = (TextView) findViewById(R.id.text_description);

        /**
         * Preenchimento das Views
         */
        title.setText(video.getTitle());
        duration.setText(video.getFormattedDuration());
        description.setText(video.getDescription());

        String uploaded = getString(R.string.uploaded)+ " " + video.getFormattedDate();
        date.setText(uploaded);

        ImageLoader imageLoader = VodSource.getInstance().getImageLoader();
        imageLoader.get(VodSource.URL_SERVER + video.getThumb(), new DescriptionImageListener(image));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_description, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickToPlay(View view) {
        Intent intent = new Intent(DescriptionActivity.this, VodPlayerActivity.class);
        startActivity(intent);
    }

    private class DescriptionImageListener implements ImageLoader.ImageListener {
        private ImageView imageView;

        public DescriptionImageListener(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
            if(response.getBitmap() != null) {
                imageView.setImageBitmap(response.getBitmap());
            }
            else {
                imageView.setImageResource(Video.DEFAULT_IMAGE);
            }
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("DescriptionImgListener", "Erro ao baixar imagem do vídeo");
            imageView.setImageResource(Video.ERROR_IMAGE);
        }
    }
}
