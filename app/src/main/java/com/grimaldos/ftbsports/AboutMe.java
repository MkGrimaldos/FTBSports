package com.grimaldos.ftbsports;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class AboutMe extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.about_me, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void dialPhone(View v) {
        TextView phoneView = (TextView) findViewById(R.id.phoneView);
        String number = "tel:" + phoneView.getText();
        Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse(number));
        startActivity(dial);
    }

    public void sendEmail(View v) {
        TextView emailView = (TextView) findViewById(R.id.emailView);
        String email = emailView.getText().toString();
        Intent send = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null));
        startActivity(Intent.createChooser(send, "Send email..."));
    }

    public void connectLinkedIn(View v) {
        String linkedin = "https://www.linkedin.com/pub/miguel-%C3%A1ngel-grimaldos-moreno/5b/6a7/5b4";
        Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(linkedin));
        startActivity(browser);
    }

    public void connectGitHub(View v) {
        String github = "https://github.com/MkGrimaldos";
        Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(github));
        startActivity(browser);
    }
}
