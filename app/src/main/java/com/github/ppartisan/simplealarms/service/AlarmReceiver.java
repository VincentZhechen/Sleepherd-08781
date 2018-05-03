package com.github.ppartisan.simplealarms.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.SparseBooleanArray;
import android.app.UiModeManager;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ppartisan.simplealarms.R;
import com.github.ppartisan.simplealarms.model.Alarm;
import com.github.ppartisan.simplealarms.ui.AlarmLandingPageActivity;
import com.github.ppartisan.simplealarms.ui.MainActivity;
import com.github.ppartisan.simplealarms.util.AlarmUtils;

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

import java.util.Calendar;

public final class AlarmReceiver extends BroadcastReceiver {

    private static final String ALARM_EXTRA = "alarm_extra";

    @Override
    public void onReceive(Context context, Intent intent) {

        final Alarm alarm = intent.getParcelableExtra(ALARM_EXTRA);
        final int id = AlarmUtils.getNotificationId(alarm);

        final NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final Intent notifIntent = new Intent(context, AlarmLandingPageActivity.class);
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final PendingIntent pIntent = PendingIntent.getActivity(
                context, id, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_alarm_white_24dp);
        builder.setColor(ContextCompat.getColor(context, R.color.accent));
        builder.setContentTitle(context.getString(R.string.app_name));
//        builder.setContentText(alarm.getLabel());
//        builder.setTicker(alarm.getLabel());
        builder.setVibrate(new long[] {1000,500,1000,500,1000,500});
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        builder.setContentIntent(pIntent);
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_HIGH);

        manager.notify(id, builder.build());


//        if (alarm.isBlueLightEnabled()) {
//            nightModeOff();
//        }
//
//        if (alarm.isSmartLightEnabled()) {
//            smartLightTurnOn();
//        }


        //Reset Alarm manually
        setReminderAlarm(context, alarm);
    }

    //Convenience method for setting a notification
    public static void setReminderAlarm(Context context, Alarm alarm) {

        //Check whether the alarm is set to run on any days
        if(!AlarmUtils.isAlarmActive(alarm)) {
            //If alarm not set to run on any days, cancel any existing notifications for this alarm
            cancelReminderAlarm(context, alarm);
            return;
        }

        final Calendar nextAlarmTime = getTimeForNextAlarm(alarm);
        alarm.setTime(nextAlarmTime.getTimeInMillis());

        final Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(ALARM_EXTRA, alarm);
        final PendingIntent pIntent = PendingIntent.getBroadcast(
                context,
                AlarmUtils.getNotificationId(alarm),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            am.set(AlarmManager.RTC_WAKEUP, alarm.getTime(), pIntent);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, alarm.getTime(), pIntent);
        }

    }

    /**
     * Calculates the actual time of the next alarm/notification based on the user-set time the
     * alarm should sound each day, the days the alarm is set to run, and the current time.
     * @param alarm Alarm containing the daily time the alarm is set to run and days the alarm
     *              should run
     * @return A Calendar with the actual time of the next alarm.
     */
    private static Calendar getTimeForNextAlarm(Alarm alarm) {

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(alarm.getTime());

        final long currentTime = System.currentTimeMillis();
        final int startIndex = getStartIndexFromTime(calendar);

        int count = 0;
        boolean isAlarmSetForDay;

        final SparseBooleanArray daysArray = alarm.getDays();

        do {
            final int index = (startIndex + count) % 7;
            isAlarmSetForDay =
                    daysArray.valueAt(index) && (calendar.getTimeInMillis() > currentTime);
            if(!isAlarmSetForDay) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                count++;
            }
        } while(!isAlarmSetForDay && count < 7);

        return calendar;

    }

    public static void cancelReminderAlarm(Context context, Alarm alarm) {

        final Intent intent = new Intent(context, AlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(
                context,
                AlarmUtils.getNotificationId(alarm),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        final AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pIntent);
    }

    private static int getStartIndexFromTime(Calendar c) {

        final int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        int startIndex = 0;
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                startIndex = 0;
                break;
            case Calendar.TUESDAY:
                startIndex = 1;
                break;
            case Calendar.WEDNESDAY:
                startIndex = 2;
                break;
            case Calendar.THURSDAY:
                startIndex = 3;
                break;
            case Calendar.FRIDAY:
                startIndex = 4;
                break;
            case Calendar.SATURDAY:
                startIndex = 5;
                break;
            case Calendar.SUNDAY:
                startIndex = 6;
                break;
        }

        return startIndex;

    }

    private void nightModeOn(Context context) {
        UiModeManager uiManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        uiManager.setNightMode(UiModeManager.MODE_NIGHT_YES);
        String mode = Integer.toString(uiManager.getNightMode());
        Toast.makeText(context.getApplicationContext(),mode,Toast.LENGTH_SHORT).show();
    }

    private void nightModeOff(Context context) {
        UiModeManager uiManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        uiManager.setNightMode(UiModeManager.MODE_NIGHT_NO);
    }

    private void smartLightTurnOn() {
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
                        myConnection2.setRequestProperty("on", "true");
                        myConnection2.setRequestProperty("ct", "2700");
                        myConnection2.setRequestProperty("bri", "254");

                        myConnection2.disconnect();
                    }
                } catch(MalformedURLException e){
                    Log.d("my log", "malformed url exception");
                } catch(IOException e) {
                    Log.d("my_log", "io connection exception");
                } catch (Exception e) {

                }

            }
        });
    }

}
