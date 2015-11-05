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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer.VideoSurfaceView;
import com.promobile.vod.vodmobile.R;
import com.promobile.vod.vodmobile.connection.VodSource;
import com.promobile.vod.vodmobile.model.Video;
import com.promobile.vod.vodmobile.util.LocalStorage;
import com.promobile.vod.vodmobile.vodplayer.VodPlayer;
import com.promobile.vod.vodmobile.vodplayer.logs.LogOnDemand;
import com.promobile.vod.vodmobile.vodplayer.util.TimeFormat;

import java.io.IOException;


public class VodPlayerActivity extends Activity {
    private VideoSurfaceView videoSurfaceView;
    private VodPlayer vodPlayer;

    private static int NUM_RENDERER = 2;

    private TextView tvCurrentPosition, tvDuration, tvTitle;
    private SeekBar progressBar;
    private RelativeLayout playerBottonBar, playerTopBar;

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
        tvTitle = (TextView) findViewById(R.id.player_tv_video_title);

        progressBar = (SeekBar) findViewById(R.id.progress_bar);
        progressBar.setProgress(0);

        playerBottonBar = (RelativeLayout) findViewById(R.id.player_bottom_bar);
        playerTopBar = (RelativeLayout) findViewById(R.id.player_top_bar);
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
        tvTitle.setText(video.getTitle());

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
                int bufferedPercentage = vodPlayer.getExoPlayer().getBufferedPercentage();
                long currentPosition = vodPlayer.getExoPlayer().getCurrentPosition();
                long bufferedPosition = vodPlayer.getExoPlayer().getBufferedPosition();
                long duration = vodPlayer.getExoPlayer().getDuration();
                long bufferStock = vodPlayer.getExoPlayer().getBufferedPosition() - vodPlayer.getExoPlayer().getCurrentPosition();

                if (LogOnDemand.haveBufferLog) {
                    LogOnDemand.addBufferLog(bufferedPercentage, currentPosition, bufferedPosition, duration, bufferStock);
                }

                Log.i("VodPlayerAct", "BufferedPercentage: " + bufferedPercentage +
                        "\nCurrentPosition:" + ((double) currentPosition / 1000.0) + "s" +
                        "\nBufferedPosition: " + ((double) bufferedPosition / 1000.0) + "s" +
                        "\nDuration: " + ((double) duration / 1000.0) + "s" +
                        "\nBufferTime: " + ((double) (bufferStock)) / 1000.0 + "s");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvCurrentPosition.setText(TimeFormat.miliToHHmmss(vodPlayer.getExoPlayer().getCurrentPosition()));
                        tvDuration.setText(TimeFormat.miliToHHmmss(vodPlayer.getExoPlayer().getDuration()));
                        progressBar.setProgress((int) (100 * ((float) vodPlayer.getExoPlayer().getCurrentPosition() / (float) vodPlayer.getExoPlayer().getDuration())));
                        progressBar.setSecondaryProgress((int) (100 * ((float) vodPlayer.getExoPlayer().getBufferedPosition() / (float) vodPlayer.getExoPlayer().getDuration())));
                    }
                });
                if (thereIsThis)
                    gerarLogs();
            }
        }, 2000);
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
        Animation bottonAnimation = null;
        Animation topAnimation = null;
        if(playerBottonBar.getVisibility() == View.VISIBLE) {
            playerBottonBar.setVisibility(View.GONE);
            playerTopBar.setVisibility(View.GONE);
            bottonAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_bottom_button);
            topAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_top_button);
        }
        else {
            playerBottonBar.setVisibility(View.VISIBLE);
            playerTopBar.setVisibility(View.VISIBLE);
            bottonAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_bottom_button);
            topAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_top_button);
        }
        LayoutAnimationController bottonAnimationControler = new LayoutAnimationController(bottonAnimation);
        LayoutAnimationController topAnimationControler = new LayoutAnimationController(topAnimation);
        //playerBottonBar.setLayoutAnimation(bottonAnimationControler);
        //playerTopBar.setLayoutAnimation(topAnimationControler);
        playerBottonBar.startAnimation(bottonAnimation);
        playerTopBar.startAnimation(topAnimation);

    }

    private void setViewsVisibility() {
        if(progressBar.getVisibility() == View.VISIBLE) {
            playerTopBar.setVisibility(View.INVISIBLE);
            playerBottonBar.setVisibility(View.INVISIBLE);
        }
        else {
            playerTopBar.setVisibility(View.VISIBLE);
            playerBottonBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        vodPlayer.release();
        thereIsThis = false;

        super.onStop();
    }

    public void onClickPlayButton(View view) {
        if(vodPlayer.getPlayerStatus() == VodPlayer.PLAYING) {
            vodPlayer.pause();
        }
        else {
            vodPlayer.start();
        }
    }
}
