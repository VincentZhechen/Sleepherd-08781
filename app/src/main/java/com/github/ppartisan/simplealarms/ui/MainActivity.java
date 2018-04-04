package com.github.ppartisan.simplealarms.ui;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;

import com.github.ppartisan.simplealarms.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //start function
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<String> lights = new ArrayList<String>();
                    URL hueURL = new URL("http://<bridge ip address>/api/<user name>/lights/");
                    HttpsURLConnection myConnection = (HttpsURLConnection) hueURL.openConnection();
                    myConnection.setRequestMethod("GET");

                    if (myConnection.getResponseCode() == 200) {
                        // Success
                        // Further processing here
                        InputStream responseBody = myConnection.getInputStream();
                        InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                        JsonReader jsonReader = new JsonReader(responseBodyReader);

                        jsonReader.beginObject(); // Start processing the JSON object
                        while (jsonReader.hasNext()) { // Loop through all keys
                            String key = jsonReader.nextName(); // Fetch the next key
                            try (Integer.parseInt(key)) {
                                lights.add(key);
                            } catch (NumberFormatException){
                                jsonReader.skipValue();
                            }
                        }

                        jsonReader.close();

                        myConnection.disconnect();


                    } else {
                        // Error handling code goes here
                    }
                    for (int x=0; x<lights.size(); x++) {
                        URL huelightURL = new URL("http://<bridge ip address>/api/<user name>/lights/" + lights.get(x)+"/state/");
                        HttpsURLConnection myConnection2 = (HttpsURLConnection) huelightURL.openConnection();
                        myConnection2.setRequestMethod("PUT");
                        myConnection2.setRequestProperty("on",true);
                        myConnection2.setRequestProperty("ct",2700);
                        myConnection2.setRequestProperty("bri",254);

                        myConnection2.disconnect();
                    }
                } catch(MalformedURLException){
                    Log.d("my log", "malformed url exception");
                } catch(IOException) {
                    Log.d("my_log", "io connection exception");
                }

            }
        });

        //end function
    }

}
