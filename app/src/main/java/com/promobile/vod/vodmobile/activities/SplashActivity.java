package com.promobile.vod.vodmobile.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

import com.promobile.vod.vodmobile.R;
import com.promobile.vod.vodmobile.util.LocalStorage;
import com.promobile.vod.vodmobile.vodplayer.VodPlayer;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LocalStorage localStorage = LocalStorage.getInstance(getApplicationContext());
        if(localStorage.getBooleanFromStorage(LocalStorage.IS_FORMAT_SELECTED)) {
            localStorage.getIntFromStorage(LocalStorage.FORMAT_SELECTED);
        }
        else {
            localStorage.addToStorage(LocalStorage.FORMAT_SELECTED, VodPlayer.ADAPTECH_EVALUATOR);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, PlayListActivity.class);
                startActivity(intent);
            }
        }, 3000);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
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
