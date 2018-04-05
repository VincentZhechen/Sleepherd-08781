package com.github.ppartisan.simplealarms.ui;

import android.os.Bundle;

import com.github.ppartisan.simplealarms.R;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

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
import java.util.Collections;
import java.util.List;

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

                            try {
                                Integer.parseInt(key);
                                lights.add(key);
                            } catch (NumberFormatException e){
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
                        myConnection2.setRequestProperty("on","true");
                        myConnection2.setRequestProperty("ct","2700");
                        myConnection2.setRequestProperty("bri","254");

                        myConnection2.disconnect();
                    }
                } catch(MalformedURLException e){
                    Log.d("my log", "malformed url exception");
                } catch(IOException e) {
                    Log.d("my_log", "io connection exception");
                }

            }
        });

        //end function

        //start blue light filtering function

        UiModeManager uiManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
        uiManager.setNightMode(UiModeManager.MODE_NIGHT_YES);


        //end blue light filtering function
        UiModeManager uiManager2 = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
        uiManager2.setNightMode(UiModeManager.MODE_NIGHT_NO);

        //list all apps on the device
        PackageManager packageManager = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        ArrayList<String> appslist = new ArrayList<>();
        List<ResolveInfo> appList = packageManager.queryIntentActivities(mainIntent, 0);
        Collections.sort(appList, new ResolveInfo.DisplayNameComparator(packageManager));
        List<PackageInfo> packs = packageManager.getInstalledPackages(0);
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            ApplicationInfo a = p.applicationInfo;
            // skip system apps if they shall not be included
            if ((a.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                continue;
            }
            appslist.add(p.packageName);
        }


        //getting the current foreground app
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> RunningTask = mActivityManager.getRunningTasks(1);
        ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
        String activityOnTop = ar.topActivity.getClassName();

        //https://stackoverflow.com/questions/19852069/blocking-android-apps-programmatically
    }
}
