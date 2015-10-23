package com.promobile.vod.vodmobile.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer.VideoSurfaceView;
import com.promobile.vod.vodmobile.R;
import com.promobile.vod.vodmobile.connection.VodSource;
import com.promobile.vod.vodmobile.model.Video;
import com.promobile.vod.vodmobile.util.LocalStorage;
import com.promobile.vod.vodmobile.vodplayer.VodPlayer;
import com.promobile.vod.vodmobile.vodplayer.util.TimeFormat;

import java.io.IOException;


public class VodPlayerActivity extends Activity {
    private VideoSurfaceView videoSurfaceView;
    private VodPlayer vodPlayer;

    private static int NUM_RENDERER = 2;

    private TextView tvCurrentPosition, tvDuration;
    private SeekBar progressBar;
    private LinearLayout textLinearLayout, progressLinearLayout;

    private long currentPosition;
    private boolean thereIsThis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_vod_player);

        init();

        buildDashVodPlayer();
    }

    /**
     * Inicializa as variáveis
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void init() {
        thereIsThis = true;
        videoSurfaceView = (VideoSurfaceView) findViewById(R.id.video_surface_view);

        tvCurrentPosition = (TextView) findViewById(R.id.tv_current_position);
        tvDuration = (TextView) findViewById(R.id.tv_duration);

        progressBar = (SeekBar) findViewById(R.id.progress_bar);
        progressBar.setProgress(0);

        textLinearLayout = (LinearLayout) findViewById(R.id.layout_texts);
        progressLinearLayout = (LinearLayout) findViewById(R.id.layout_progress);
    }

    private void buildBasicVodPlayer() {
        vodPlayer = new VodPlayer(getApplicationContext(), videoSurfaceView, NUM_RENDERER);
        vodPlayer.builderBasicPlayer(getString(R.string.url_video));
        vodPlayer.start();
    }

    private void buildDashVodPlayer() {
        LocalStorage localStorage = LocalStorage.getInstance(getApplicationContext());

        Video video = localStorage.getObjectFromStorage(LocalStorage.OBJ_VIDEO, Video.class);

        int dash_evaluator_mode = localStorage.getIntFromStorage(LocalStorage.FORMAT_SELECTED);
        String url = VodSource.URL_SERVER + video.getPath();

        vodPlayer = new VodPlayer(getApplicationContext(), videoSurfaceView, NUM_RENDERER);
        try {
            vodPlayer.builderDashPlayer(url, dash_evaluator_mode);
        } catch (IOException e) {
            Log.e("VodPlayer", "Erro em vodPlayer.builderDashPlayer: " + e.getMessage());
        }
        vodPlayer.setVodPlayerListener(new VodPlayer.VodPlayerListener() {
            @Override
            public void onPrepared() {
                gerarLogs();
                vodPlayer.start();
            }

            @Override
            public void onLoadingError() {
                Toast.makeText(getApplicationContext(), getString(R.string.server_fail), Toast.LENGTH_LONG).show();

                finish();
            }
        });

        vodPlayer.setSeekBar(progressBar);
    }

    private void gerarLogs() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("VodPlayerAct", "BufferedPercentage: " + vodPlayer.getExoPlayer().getBufferedPercentage() +
                        "\nCurrentPosition:" + ((double) vodPlayer.getExoPlayer().getCurrentPosition() / 1000.0) + "s" +
                        "\nBufferedPosition: " + ((double) vodPlayer.getExoPlayer().getBufferedPosition() / 1000.0) + "s" +
                        "\nDuration: " + ((double) vodPlayer.getExoPlayer().getDuration() / 1000.0) + "s" +
                        "\nBufferTime: " + ((double) (vodPlayer.getExoPlayer().getBufferedPosition() - vodPlayer.getExoPlayer().getCurrentPosition())) / 1000.0 + "s");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvCurrentPosition.setText(TimeFormat.miliToHHmmss(vodPlayer.getExoPlayer().getCurrentPosition()));
                        tvDuration.setText(TimeFormat.miliToHHmmss(vodPlayer.getExoPlayer().getDuration()));
                        progressBar.setProgress((int) (100 * ((float) vodPlayer.getExoPlayer().getCurrentPosition() / (float) vodPlayer.getExoPlayer().getDuration())));
                        progressBar.setSecondaryProgress((int) (100 * ((float) vodPlayer.getExoPlayer().getBufferedPosition() / (float) vodPlayer.getExoPlayer().getDuration())));
                    }
                });
                if(thereIsThis)
                    gerarLogs();
            }
        }, 1000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_vod_player, menu);
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

    public void onClickVideoSurfaceView(View view) {
        if(vodPlayer.getPlayerStatus() == VodPlayer.PLAYING) {
            vodPlayer.pause();
        }
        else {
            vodPlayer.start();
        }
    }

    private void setViewsVisibility() {
        if(progressBar.getVisibility() == View.VISIBLE) {
            progressLinearLayout.setVisibility(View.INVISIBLE);
            textLinearLayout.setVisibility(View.INVISIBLE);
        }
        else {
            progressLinearLayout.setVisibility(View.VISIBLE);
            textLinearLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        vodPlayer.release();
        thereIsThis = false;

        super.onStop();
    }
}
