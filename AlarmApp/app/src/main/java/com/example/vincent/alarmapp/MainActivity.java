package com.example.vincent.alarmapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    // to make our alarm manager
    AlarmManager alarm_manager;
    TimePicker alarm_timepicker;
    TextView update_text;
    Context context;

    PendingIntent pending_intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.context = this;

        // initialize our alarm manager
        alarm_manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // initialize our timepicker
        alarm_timepicker = (TimePicker)findViewById(R.id.Time_Picker);

        // initialize our text update box
        update_text = (TextView)findViewById(R.id.Update_Text);

        // create an instance of a calendar
        final Calendar calendar = Calendar.getInstance();

        // create an intent to the Alarm Receiver class
        final Intent my_intent = new Intent(this.context, Alarm_Receiver.class);


        // initialize buttons
        Button start_alarm = (Button) findViewById(R.id.Start_Alarm);


        // create onclick listener to start alarm
        start_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // set calendar instance to with the hour and minutes we picked on the time picker
                calendar.set(Calendar.HOUR_OF_DAY, alarm_timepicker.getHour());
                calendar.set(Calendar.MINUTE, alarm_timepicker.getMinute());

                int hour = alarm_timepicker.getHour();
                int minute = alarm_timepicker.getMinute();

                String hour_string;
                String minute_string;


                hour_string = String.valueOf(hour);


                if (minute < 10) {
                    minute_string = "0" + String.valueOf(minute);
                } else {
                    minute_string = String.valueOf(minute);
                }

                set_alarm_text("Alarm set to: " + hour_string + ":" + minute_string);

                // put in extra string into my_intent
                // tell the clock that you pressed the alarm on button
                my_intent.putExtra("extra", "alarm on");

                //create a pending intent that delays the intent until the specified calendar time.
                pending_intent = PendingIntent.getBroadcast(MainActivity.this, 0,
                        my_intent, PendingIntent.FLAG_UPDATE_CURRENT);

                // set the alarm manager
                alarm_manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        pending_intent);
            }
        });


        Button end_alarm = (Button) findViewById(R.id.End_Alarm);

        // create onclick listener to end alarm
        end_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                set_alarm_text("Alarm off!");

                // cancel the pending intent
                alarm_manager.cancel(pending_intent);

                // put extra into my_intent, that you pressed the alarm off button
                my_intent.putExtra("extra", "alarm off");

                // stop the ringtone
                sendBroadcast(my_intent);
            }
        });
    }

    private void set_alarm_text(String s) {
        update_text.setText(s);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
