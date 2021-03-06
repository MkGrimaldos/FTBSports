package com.grimaldos.ftbsports;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
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

    /**
     * Start the Dial Phone activity passing the phone number.
     *
     * @param v
     */
    public void dialPhone(View v) {
        TextView phoneView = (TextView) findViewById(R.id.phoneView);
        String number = "tel:" + phoneView.getText();

        Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse(number));
        startActivity(dial);
    }

    /**
     * Start an Email activity passing the email address.
     *
     * @param v
     */
    public void sendEmail(View v) {
        TextView emailView = (TextView) findViewById(R.id.emailView);
        String email = emailView.getText().toString();

        Intent send = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null));
        startActivity(Intent.createChooser(send, "Send email..."));
    }

    /**
     * Open the browser, showing the Linkedin Profile from the developer.
     *
     * @param v
     */
    public void connectLinkedIn(View v) {
        if (isConnected()) {
            String linkedin = "https://www.linkedin.com/pub/miguel-%C3%A1ngel-grimaldos-moreno/5b/6a7/5b4";
            Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(linkedin));
            startActivity(browser);
        } else {
            showCustomDialog("No se pudo conectar con el servidor. " +
                    "Por favor, compruebe su conexión a internet e inténtelo de nuevo.");
        }
    }

    /**
     * Open the browser, showing the GitHub Profile from the developer.
     *
     * @param v
     */
    public void connectGitHub(View v) {
        if (isConnected()) {
            String github = "https://github.com/MkGrimaldos";
            Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(github));
            startActivity(browser);
        } else {
            showCustomDialog("No se pudo conectar con el servidor. " +
                    "Por favor, compruebe su conexión a internet e inténtelo de nuevo.");
        }
    }

    /**
     * Check whether the device is connected to the Internet or not.
     *
     * @return True if it is connected, false otherwise.
     */
    private boolean isConnected() {
        ConnectivityManager conMgr = (ConnectivityManager) AboutMe.this.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        return CheckConnection.checkConnection(conMgr);
    }

    /**
     * Show a customized dialog with the message passed by parameter.
     *
     * @param message String to show in the dialog.
     */
    private void showCustomDialog(String message) {
        final Dialog dialog = new Dialog(AboutMe.this, R.style.Theme_AppCompat_DialogWhenLarge);
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
}