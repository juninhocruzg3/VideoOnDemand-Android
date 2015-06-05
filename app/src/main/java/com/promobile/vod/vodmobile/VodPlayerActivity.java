package com.promobile.vod.vodmobile;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.exoplayer.VideoSurfaceView;
import com.promobile.vod.vodmobile.vodplayer.VodPlayer;

import java.io.IOException;


public class VodPlayerActivity extends Activity {
    private VideoSurfaceView videoSurfaceView;
    private VodPlayer vodPlayer;

    private static int NUM_RENDERER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod_player);

        init();

        buildDashVodPlayer();
    }

    /**
     * Inicializa as variáveis
     */
    private void init() {
        videoSurfaceView = (VideoSurfaceView) findViewById(R.id.video_surface_view);
    }

    private void buildBasicVodPlayer() {
        vodPlayer = new VodPlayer(getApplicationContext(), videoSurfaceView, NUM_RENDERER);
        vodPlayer.builderBasicPlayer(getString(R.string.url_video));
        vodPlayer.start();
    }

    private void buildDashVodPlayer() {
        vodPlayer = new VodPlayer(getApplicationContext(), videoSurfaceView, NUM_RENDERER);
        try {
            vodPlayer.builderDashPlayer(getString(R.string.url_dash_youtube));
        } catch (IOException e) {
            Log.e("VodPlayer", "Erro em vodPlayer.builderDashPlayer: " + e.getMessage());
        }
        vodPlayer.setVodPlayerListener(new VodPlayer.VodPlayerListener() {
            @Override
            public void onPrepared() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        vodPlayer.start();
                    }
                }, 20000);

                gerarLogs();
            }
        });
    }

    private void gerarLogs() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("VodPlayerAct", "BufferedPercentage: " + vodPlayer.getExoPlayer().getBufferedPercentage() +
                                        "\nBufferedPosition: " + vodPlayer.getExoPlayer().getBufferedPosition() +
                                        "\nDuration: " + vodPlayer.getExoPlayer().getDuration());
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
}
