package com.grimaldos.ftbsports;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

/**
 * Main menu of the app.
 */
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

    /**
     * Start the AboutMe activity.
     *
     * @param v
     */
    public void aboutMe(View v) {
        Intent about = new Intent(Dashboard.this, AboutMe.class);
        startActivity(about);
    }

    /**
     * Start the Producer/Consumer activity.
     *
     * @param v
     */
    public void proCon(View v) {
        Intent prodCons = new Intent(Dashboard.this, ProCon.class);
        startActivity(prodCons);
    }

    /**
     * Destroy all the png files contained in the default folder.
     *
     * @param v
     */
    public void vaciarCache(View v) {
        int count = 0;
        File f;
        String message;

        // Check if external storage is available.
        String extStorState = Environment.getExternalStorageState();
        if (extStorState.equals(Environment.MEDIA_MOUNTED)) {
            // If it is available, use it.
            f = new File(Environment.getExternalStorageDirectory() + "/FromTheBench/Images");
        } else {
            // If external storage is not available, use internal storage.
            f = new File(this.getFilesDir().getPath());
        }

        // Get files in the folder and count them.
        File[] files = f.listFiles();
        if (files != null) {
            // Get the ".png" files and delete them.
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".png")) {
                    Log.d("Destroying Image", file.getName());
                    if (new File(file.getAbsolutePath()).delete()) {
                        scanFile(file.getAbsolutePath());
                        ++count;
                        Log.d("Image", "DESTROYED");
                    } else {
                        Log.d("Image", "MISSED");
                    }
                }
            }
            // Show feedback to the user depending on the result.
            if (count > 0) {
                message = "Se eliminaron " + count + " imágenes correctamente.";
            } else {
                message = "No se encontró ninguna imagen en la carpeta.";
            }

        } else {
            message = "No se encontró la carpeta contenedora.";
            Log.d("Files", "NULL");
        }
        showCustomDialog(message);
    }

    /**
     * Clear user information, start Login activity and finish this activity.
     *
     * @param v
     */
    public void logout(View v) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.remove("username");
        editor.remove("password");
        editor.apply();
        Log.d("Logout", "Username and password cleared");

        Intent login = new Intent(Dashboard.this, Login.class);
        startActivity(login);

        finish();
    }

    /**
     * Show a customized dialog with the message passed by parameter.
     *
     * @param message String to show in the dialog.
     */
    private void showCustomDialog(String message) {
        final Dialog dialog = new Dialog(Dashboard.this, R.style.Theme_AppCompat_DialogWhenLarge);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.error_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x66000000));
        TextView errorText = (TextView) dialog.findViewById(R.id.errorText);
        errorText.setText(message);

        Button okButton = (Button) dialog.findViewById(R.id.errorButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * Request the media scanner to scan a file.
     *
     * @param path Location of the file to be scanned.
     */
    private void scanFile(String path) {
        Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri fileContentUri = Uri.fromFile(f);
        mediaScannerIntent.setData(fileContentUri);
        this.sendBroadcast(mediaScannerIntent);
    }
}