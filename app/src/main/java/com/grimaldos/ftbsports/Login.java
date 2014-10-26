package com.grimaldos.ftbsports;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * A login screen that offers login via username/password.
 */
public class Login extends Activity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    //static Dialog spinner;

    private BroadcastReceiver netBroadcastReceiver;

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // When network state changes, if it just connected to the Internet download images
        netBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (isConnected()) {
                    Intent downloadImages = new Intent(Login.this, DownloadService.class);
                    startService(downloadImages);
                }
            }
        };
        registerReceiver(netBroadcastReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mUsernameSignInButton = (Button) findViewById(R.id.username_sign_in_button);
        mUsernameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String mUsername = settings.getString("username", null);
        String mPassword = settings.getString("password", null);
        if (mUsername != null && mPassword != null) {
            mUsernameView.setText(mUsername);
            mPasswordView.setText(mPassword);
            attemptLogin();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(netBroadcastReceiver);
        super.onDestroy();
    }

    /**
     * Attempts to sign in with the account specified by the login form.
     * If there are form errors (missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check if the user entered a password.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check if the user entered a username.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        // Check Internet connection.
        if (!isConnected()) {
            // If Internet connection is not working cancel login
            showCustomDialog("No se pudo conectar con el servidor. Compruebe su conexión a " +
                    "internet e inténtelo de nuevo.");
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error if exists.
            if (focusView != null)
                focusView.requestFocus();
        } else {
            // Kick off a background task to perform the user login attempt.
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;
        private String errorMessage = "";
        final Dialog spinner = new Dialog(Login.this, R.style.Theme_AppCompat_DialogWhenLarge);

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            // Set custom progress dialog.
            spinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
            spinner.setCancelable(false);
            spinner.setContentView(R.layout.loading_dialog);
            spinner.getWindow().setBackgroundDrawable(new ColorDrawable(0x66000000));

            spinner.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL authURL = new URL("http://ftbsports.com/android/api/login.php?user="
                        + mUsername + "&password=" + mPassword);

                HttpURLConnection connection = (HttpURLConnection) authURL.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                BufferedReader reader = new BufferedReader(new InputStreamReader
                        (connection.getInputStream()));

                StringBuilder buffer = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                String json = buffer.toString();

                JSONObject jObj = new JSONObject(json);

                String status = jObj.getString("status");

                if (status.equals("0")) {
                    return true;
                } else if (status.equals("-1")) {
                    errorMessage = jObj.getString("message");
                    Log.d("Status:" + status, errorMessage);
                    return false;
                } else if (status.equals("-2")) {
                    errorMessage = "Error desconocido, por favor inténtelo de nuevo.";
                    Log.d("Status:" + status, errorMessage);
                    return false;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            spinner.dismiss();

            if (success) {
                Log.d("AUTH SUCCESS", "Preparing to store info");
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = settings.edit();
                Log.d("STORE USERNAME", mUsername);
                editor.putString("username", mUsername);
                Log.d("STORE PASSWORD", mPassword);
                editor.putString("password", mPassword);
                editor.apply();
                Intent dashboard = new Intent(Login.this, Dashboard.class);
                startActivity(dashboard);
                finish();
            } else {
                showCustomDialog(errorMessage);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            spinner.dismiss();
        }
    }

    /**
     * Shows a customized dialog with the message passed by parameter.
     *
     * @param message String to show in the dialog.
     */
    private void showCustomDialog(String message) {
        final Dialog dialog = new Dialog(Login.this, R.style.Theme_AppCompat_DialogWhenLarge);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.error_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x66000000));
        TextView errorText = (TextView) dialog.findViewById(R.id.errorText);
        errorText.setText(message);

        Button okButton = (Button) dialog.findViewById(R.id.errorButton);
        okButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * Checks whether the device is connected to the Internet or not.
     *
     * @return True if it is connected, false otherwise.
     */
    private boolean isConnected() {
        // Check Internet connection.
        ConnectivityManager conMgr = (ConnectivityManager) Login.this.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        return CheckConnection.checkConnection(conMgr);
    }
}