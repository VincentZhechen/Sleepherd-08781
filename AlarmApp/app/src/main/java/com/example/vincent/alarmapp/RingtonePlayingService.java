package com.example.vincent.alarmapp;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by vincent on 2/25/18.
 */

public class RingtonePlayingService extends Service{

    private MediaPlayer media_song;
    private int start_id;
    private boolean isRunning;
    private Context context;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id" + startId + ": " + intent);

        // notification
        // set up the notification service

            final NotificationManager notify_manager = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);

            // set up an intent that goes to the Main Activity
            Intent intent1 = new Intent(this.getApplicationContext(), MainActivity.class);

            // set up a pending intent
            PendingIntent pending_intent_main_activity = PendingIntent.getActivity(this,
                    0, intent1, 0);

            // make the notification parameters
            Notification notification_popup = new Notification.Builder(this)
                    .setContentTitle("An alarm is going off!")
                    .setContentText("Click me, wake up!")
                    .setContentIntent(pending_intent_main_activity)
                    .setAutoCancel(true)
                    .build();




        // fetch the extra string values
        String state = intent.getExtras().getString("extra");


        if (state != null) {
            switch (state) {
                case "alarm on":
                    start_id = 1;
                    break;
                case "alarm off":
                    start_id = 0;
                    break;
                default:
                    start_id = 0;
                    break;
            }
        } else {
            return START_NOT_STICKY;
        }


        // if there is no music playing, and the user pressed "alarm on"
        // music playing
        if (!this.isRunning && start_id == 1) {
            // create an instance of the media player
            media_song = MediaPlayer.create(this, R.raw.wake_up);
            media_song.start();
            Log.i("LocalService", "song played");

//            // set up the notification call command
//            notify_manager.notify(0, notification_popup);

            isRunning = true;
            start_id = 0;
        }

        // if there is music playing, and the user pressed "alarm off"
        // music stop
        else if (this.isRunning && start_id == 0) {
            // stop the media player
            Log.i("LocalService", "song Stopped");
            media_song.stop();
            media_song.reset();

            isRunning = false;
            start_id = 0;
        }

        // if there is no music playing, and the user pressed "alarm off"
        // do nothing
        else if (!this.isRunning && start_id == 0) {
            start_id = 0;
        }

        // if there is music playing, and the user pressed "alarm on"
        // do nothing
        else {
            start_id = 0;
        }



        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        isRunning = false;

        // tell the user stopped
        Toast.makeText(this, "On Destroy called", Toast.LENGTH_SHORT).show();
    }

}
