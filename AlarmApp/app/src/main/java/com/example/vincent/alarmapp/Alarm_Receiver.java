package com.example.vincent.alarmapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by vincent on 2/25/18.
 */

public class Alarm_Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

//        Log.e("We are in the receiver.", "Yay!");

        // fetch extra strings from the intent
        String get_extra = intent.getExtras().getString("extra");

        Log.e("Extra key? ", get_extra);


        // create an intent to the ringtone service
        Intent service_intent = new Intent(context, RingtonePlayingService.class);

        // pass the extra string from the Main Activity to the Ringtone Playing Service
        service_intent.putExtra("extra", get_extra);

        // start the ringtone service
        context.startService(service_intent);

    }
}
