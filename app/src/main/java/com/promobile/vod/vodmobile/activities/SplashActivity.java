package com.promobile.vod.vodmobile.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.promobile.vod.vodmobile.R;
import com.promobile.vod.vodmobile.activities.navigation.MainActivity;
import com.promobile.vod.vodmobile.connection.VolleyManager;
import com.promobile.vod.vodmobile.util.LocalStorage;
import com.promobile.vod.vodmobile.vodplayer.VodPlayer;

import br.ufam.icomp.fingerprinting.Fingerprinting;

public class SplashActivity extends Activity {
    private ProgressDialog pDialog;
    private Fingerprinting fingerprinting;

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

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();

        fingerprinting = new Fingerprinting(this);

        fingerprinting.setListener(new Fingerprinting.Listener() {
            @Override
            public void onFinish(String s) {
                onFingerPrintingFinish(s);
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    fingerprinting.doFingerprinting();
                } catch(Exception e) {
                    String error = "Erro no fingerprint:\n" + e.getLocalizedMessage() + "\n" + e.getMessage();
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                    Log.e("FingerPrinting", error);

                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        }, 3000);
    }

    private void onFingerPrintingFinish(String data) {
        fingerprinting.sendFingerprinting(getApplicationContext(), data);
        pDialog.hide();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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

        if(VolleyManager.hasInstance()) {
            VolleyManager.getInstance(getApplicationContext()).killRequests("fingerprint");
        }
    }
}
