package com.sm.stepsassistant;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;
import android.app.ActivityManager;
import android.content.Context;
import android.app.ActivityManager.RunningServiceInfo;

import java.text.NumberFormat;
import java.util.Calendar;

public class MyWearActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int steps = StartListenerService.calculateSteps(this);
        final String time = StartListenerService.calculateTime(this);
        setPercentage(steps);

        setContentView(R.layout.activity_my_wear);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                TextView stepTextView = (TextView) stub.findViewById(R.id.stepTextView);
                TextView timeTextView = (TextView) stub.findViewById(R.id.timeTextView);
                stepTextView.setText(NumberFormat.getInstance().format(steps)+"");
                timeTextView.setText(time);
            }
        });

        setInitialAlarm();
        if (!isMyServiceRunning(StartListenerService.class)) {
            Intent listenerIntent = new Intent(this, StartListenerService.class);
            startService(listenerIntent);
        }
    }

    public void setInitialAlarm(){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR,0);
        c.add(Calendar.DATE,1);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        Intent resetIntent = new Intent(this, ResetReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, resetIntent,0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, c.getTimeInMillis(), 86400000, pi); //86400000 is ms per day
        Log.d("OUTPUT","Alarm set for midnight");
    }

    public void setPercentage(int steps){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final int stepGoal = prefs.getInt(StartListenerService.DAILY_GOAL,10000);
        PercentView.percent = (float)steps/(float)stepGoal;
        if (PercentView.percent > 1) PercentView.percent = 1;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
