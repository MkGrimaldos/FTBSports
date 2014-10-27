package com.grimaldos.ftbsports;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;


public class ProCon extends ActionBarActivity {

    Producer asyncProducer = null;
    Consumer asyncConsumer = null;
    static LinkedBlockingQueue<Integer> producedList = new LinkedBlockingQueue<Integer>(10);
    static ListView prodListView, consListView;
    SimpleAdapter producerAdapter, consumerAdapter;
    ArrayList<HashMap<String, Integer>> producedArray, consumedArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro_con);

        prodListView = (ListView) findViewById(R.id.prodList);
        producedArray = new ArrayList<HashMap<String, Integer>>();
        producerAdapter = new SimpleAdapter(ProCon.this, producedArray, R.layout.item_producer,
                new String[]{"num"}, new int[]{R.id.prodItemTitle});
        prodListView.setAdapter(producerAdapter);
        asyncProducer = new Producer();
        asyncProducer.execute();

        consListView = (ListView) findViewById(R.id.consList);
        consumedArray = new ArrayList<HashMap<String, Integer>>();
        consumerAdapter = new SimpleAdapter(ProCon.this, consumedArray, R.layout.item_consumer,
                new String[]{"num"}, new int[]{R.id.consItemTitle});
        consListView.setAdapter(consumerAdapter);
        asyncConsumer = new Consumer();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            asyncConsumer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        else
            asyncConsumer.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (asyncProducer != null) {
            asyncProducer.cancel(true);
        }
        producedList.clear();

        if (asyncConsumer != null) {
            asyncConsumer.cancel(true);
        }
        consumedArray.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pro_con, menu);
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

    public class Producer extends AsyncTask<Void, Void, Void> {

        int minTime = 750, maxTime = 2000;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                while (!isCancelled()) {
                    waitFor(minTime, maxTime);

                    if (producedList.size() < 10 && isConnected()) {
                        int number = getIntFromServer();

                        if (number != -1) {
                            producedList.put(number);

                            Integer aux[] = producedList.toArray(new Integer[producedList.size()]);
                            producedArray.clear();

                            for (Integer anAux : aux) {
                                HashMap<String, Integer> map = new HashMap<String, Integer>();
                                map.put("num", anAux);

                                producedArray.add(map);
                            }

                            publishProgress();
                        }
                        //throw new Exception("No se pudo conectar con el servidor." +
                        //        "Por favor, compruebe su conexión a internet e inténtelo de nuevo.");
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            //Log.v("PRODUCER", "I'm updating my progress!");
            producerAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    public class Consumer extends AsyncTask<Void, Void, Void> {

        int minTime = 650, maxTime = 1800;

        @Override
        protected Void doInBackground(Void... params) {
            while (!isCancelled()) {
                waitFor(minTime, maxTime);

                if (producedList.size() > 0) {
                    int number = producedList.poll();
                    HashMap<String, Integer> consMap = new HashMap<String, Integer>();
                    consMap.put("num", number);
                    consumedArray.add(consMap);

                    Integer prodAux[] = producedList.toArray(new Integer[producedList.size()]);
                    producedArray.clear();

                    for (Integer numAux : prodAux) {
                        HashMap<String, Integer> prodMap = new HashMap<String, Integer>();
                        prodMap.put("num", numAux);

                        producedArray.add(prodMap);
                    }

                    publishProgress();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            consumerAdapter.notifyDataSetChanged();
            producerAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    private void waitFor(int bottom, int top) {
        Random rnd = new Random();
        long time = rnd.nextInt(top - bottom + 1) + bottom;
        SystemClock.sleep(time);
    }

    /**
     * Checks whether the device is connected to the Internet or not.
     *
     * @return True if it is connected, false otherwise.
     */
    private boolean isConnected() {
        // Check Internet connection.
        ConnectivityManager conMgr = (ConnectivityManager) ProCon.this.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        return CheckConnection.checkConnection(conMgr);
    }

    private int getIntFromServer() {
        int number = -1;
        try {
            URL authURL = new URL("http://ftbsports.com/android/api/get_rand.php");

            HttpURLConnection connection = (HttpURLConnection) authURL.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader
                    (connection.getInputStream()));

            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                //Log.d("RESPONSE", line);
                buffer.append(line + "\n");
            }

            String json = buffer.toString();

            JSONObject jObj = new JSONObject(json);

            number = jObj.getInt("num");
            Log.d("NUMBER", Integer.toString(number));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return number;
    }

    /**
     * Shows a customized dialog with the message passed by parameter.
     *
     * @param message String to show in the dialog.
     */
    private void showCustomDialog(String message) {
        final Dialog dialog = new Dialog(ProCon.this, R.style.Theme_AppCompat_DialogWhenLarge);
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
