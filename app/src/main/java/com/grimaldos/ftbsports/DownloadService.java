package com.grimaldos.ftbsports;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A service that manages the image downloading from a specified server
 * in a different thread.
 */
public class DownloadService extends IntentService {

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int count = 0;
        List<String> fNames = new ArrayList<String>();
        File f;

        // Check if external storage is available.
        String extStorState = Environment.getExternalStorageState();
        if (extStorState.equals(Environment.MEDIA_MOUNTED)) {
            // If it is available, use it.
            f = new File(Environment.getExternalStorageDirectory() + "/FromTheBench/Images");

            // Create the folder if it doesn't exist yet.
            if (!f.exists()) {
                if (f.mkdirs()) {
                    Log.d("mkdirs", "Nueva carpeta creada");
                } else {
                    Log.d("mkdirs", "Fallo al crear la carpeta");
                }
            } else {
                Log.d("f.exists", "La carpeta ya existía");
            }
            Log.d("File path", f.getAbsolutePath());
        } else {
            // If external storage is not available, use internal storage.
            f = new File(this.getFilesDir().getPath());
            Log.d("File path", f.getAbsolutePath());
        }

        // Get files in the folder and count them.
        File[] files = f.listFiles();
        if (files != null) {
            Log.d("Files length", Integer.toString(files.length));

            // Get the ".png" files and store their names.
            for (File file : files) {
                Log.d("File", file.getName());
                if (file.isFile() && file.getName().toLowerCase().endsWith(".png")) {
                    Log.d("Image?", "Me intelesa");
                    fNames.add(file.getName());
                }
            }
            // Number of images stored
            count = fNames.size();
        } else {
            Log.d("Files", "NULL");
        }

        Log.d("Count", Integer.toString(count));

        // Build JSON to send it to the server
        JSONArray jsonArrayAux = new JSONArray(fNames);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("count", count);
            jsonObject.put("list", jsonArrayAux);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("JSON", jsonObject.toString());

        try {
            // Get JSON object from String
            String json = serverPetition(jsonObject);
            JSONObject jObj = new JSONObject(json);
            JSONArray jArray = jObj.getJSONArray("list");

            // Get information from JSON object
            for (int i = 0; i < jArray.length(); ++i) {
                JSONObject c = jArray.getJSONObject(i);

                String urlString = c.getString("url");
                String name = c.getString("nombre");

                // Download image from URL
                downloadImage(urlString, name, f.getAbsolutePath());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Performs a petition to a server, sending a JSON with information of
     * the images stored in the device.
     * Also manages the answer from the server.
     *
     * @param jsonObject JSON to be sent to the server.
     * @return Answer from the server.
     */
    private String serverPetition(JSONObject jsonObject) {
        // Connect and send petition
        try {
            URL imgURL = new URL("http://ftbsports.com/android/api/get_images_cache.php");

            HttpURLConnection connection = (HttpURLConnection) imgURL.openConnection();
            connection.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write("data=" + jsonObject.toString());
            writer.flush();
            writer.close();

            // Receive and process answer
            BufferedReader reader = new BufferedReader(new InputStreamReader
                    (connection.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                Log.d("JSON RESPONSE", line);
                builder.append(line + "\n");
            }
            return builder.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Downloads and stores an image from an url.
     *
     * @param urlString Url where the image is downloaded from.
     * @param name      Name of the image to be downloaded.
     * @param path      Place to store the image in the device.
     */
    private void downloadImage(String urlString, String name, String path) {
        try {
            URL url = new URL(urlString);
            InputStream input = url.openStream();
            try {
                OutputStream output = new FileOutputStream(new File(path, name));
                try {
                    byte[] buffer = new byte[1024];
                    int bytesRead = 0;
                    while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
                        output.write(buffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                input.close();
            }
            scanFile(path + "/" + name);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Requests the media scanner to scan a file.
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