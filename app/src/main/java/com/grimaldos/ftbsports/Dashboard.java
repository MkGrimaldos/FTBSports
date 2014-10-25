package com.grimaldos.ftbsports;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


public class Dashboard extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);

        // Animar el logo para que vaya de arriba a abajo
        ImageView logo = (ImageView) findViewById(R.id.logoImageView);
        Animation upToDown = AnimationUtils.loadAnimation(this, R.anim.up_to_down);
        logo.setAnimation(upToDown);

        // Animar los focos de la derecha para que entren de derecha a izquierda
        ImageView latDcha = (ImageView) findViewById(R.id.derImageView);
        Animation rightToLeft = AnimationUtils.loadAnimation(this, R.anim.right_to_left);
        latDcha.setAnimation(rightToLeft);

        // Animar los focos de la izquierda para que entren de izquierda a derecha
        ImageView latIzq = (ImageView) findViewById(R.id.izqImageView);
        Animation leftToRight = AnimationUtils.loadAnimation(this, R.anim.left_to_right);
        latIzq.setAnimation(leftToRight);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
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

    public void aboutMe(View v) {
        Intent about = new Intent(Dashboard.this, AboutMe.class);
        startActivity(about);
    }
}
