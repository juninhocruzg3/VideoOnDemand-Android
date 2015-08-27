package com.promobile.vod.vodmobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.promobile.vod.vodmobile.R;

public class DescriptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        init();
    }

    private void init() {
        ImageView image = (ImageView) findViewById(R.id.image_description);
        TextView title = (TextView) findViewById(R.id.title_description);
        TextView duration = (TextView) findViewById(R.id.duration_description);
        TextView date = (TextView) findViewById(R.id.date_description);
        TextView description = (TextView) findViewById(R.id.text_description);

        image.setImageResource(R.mipmap.film_sintel);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DescriptionActivity.this, VodPlayerActivity.class);
                startActivity(intent);
            }
        });
        title.setText("Sintel - Open Movie");
        duration.setText("14:48s");
        description.setText(getString(R.string.sintel_description));


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
}
