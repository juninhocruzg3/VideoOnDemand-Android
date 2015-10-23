package com.promobile.vod.vodmobile.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.promobile.vod.vodmobile.R;
import com.promobile.vod.vodmobile.activities.navigation.MainActivity;
import com.promobile.vod.vodmobile.connection.VodSource;
import com.promobile.vod.vodmobile.model.Channel;
import com.promobile.vod.vodmobile.util.Fingerprinting;
import com.promobile.vod.vodmobile.util.LocalStorage;
import com.promobile.vod.vodmobile.vodplayer.VodPlayer;

import java.util.ArrayList;

public class SplashActivity extends Activity {
    private ProgressDialog pDialog;
    private Fingerprinting fingerprinting;
    private VodSource vodSource;
    private LocalStorage localStorage;
    private boolean isFingerprintingComplete, isUpdateChannelListComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        localStorage = LocalStorage.getInstance(getApplicationContext());
        if(localStorage.getBooleanFromStorage(LocalStorage.IS_FORMAT_SELECTED)) {
            localStorage.getIntFromStorage(LocalStorage.FORMAT_SELECTED);
        }
        else {
            localStorage.addToStorage(LocalStorage.FORMAT_SELECTED, VodPlayer.ADAPTECH_EVALUATOR);
        }

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();

        fingerprinting = new Fingerprinting(getApplication());

        fingerprinting.setListener(new Fingerprinting.Listener() {
            @Override
            public void onFinish(String s) {
                onFingerPrintingFinish(s);
            }
        });

        fingerprinting.doFingerprinting();
        updateChannelList();
    }

    private void updateChannelList() {
        vodSource = VodSource.getInstance();
        vodSource.getChannelsList(new VodSource.ChannelsListListener() {
            @Override
            public void onSucess(ArrayList<Channel> arrayList) {
                localStorage.addToStorage(LocalStorage.CHANNEL_LIST, arrayList);
                isUpdateChannelListComplete = true;
                if(isFingerprintingComplete)
                    startMainActivity();
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(getApplicationContext(), getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                isUpdateChannelListComplete = true;
                if(isFingerprintingComplete)
                    startMainActivity();
            }
        });
    }

    private void onFingerPrintingFinish(String data) {
        fingerprinting.sendFingerprinting(data);
        pDialog.hide();

        Log.d("SplashAct.onFin...", data);

        isFingerprintingComplete = true;
        if(isUpdateChannelListComplete) {
            startMainActivity();
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
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

    @Override
    protected void onStop() {
        super.onStop();

//        if(VolleyManager.hasInstance()) {
//            VolleyManager.getInstance(getApplicationContext()).killRequests("fingerprint");
//        }
    }
}
