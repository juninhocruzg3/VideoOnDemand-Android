package com.promobile.vod.vodmobile.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.promobile.vod.vodmobile.R;
import com.promobile.vod.vodmobile.util.LocalStorage;
import com.promobile.vod.vodmobile.vodplayer.VodPlayer;

public class SettingsActivity extends AppCompatActivity {
    RadioGroup radioGroup;
    LocalStorage localStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        init();
    }

    private void init() {
        radioGroup = (RadioGroup) findViewById(R.id.radio_group_evaluator);
        localStorage = LocalStorage.getInstance(getApplicationContext());
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioBtn_adaptech) {
                    localStorage.addToStorage(LocalStorage.IS_FORMAT_SELECTED, true);
                    localStorage.addToStorage(LocalStorage.FORMAT_SELECTED, VodPlayer.ADAPTECH_EVALUATOR);
                    Toast.makeText(getApplicationContext(), "AdapTech Selecionado!", Toast.LENGTH_SHORT).show();
                } else if (checkedId == R.id.radioBtn_festive) {
                    localStorage.addToStorage(LocalStorage.IS_FORMAT_SELECTED, true);
                    localStorage.addToStorage(LocalStorage.FORMAT_SELECTED, VodPlayer.FESTIVE_EVALUATOR);
                    Toast.makeText(getApplicationContext(), "FESTIVE Selecionado!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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
