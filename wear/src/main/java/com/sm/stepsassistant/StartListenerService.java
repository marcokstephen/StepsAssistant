package com.sm.stepsassistant;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.content.Context;

public class StartListenerService extends Service implements SensorEventListener {

    public static final String COUNTER_SINCE_RESTART = "com.sm.stepsassistant.COUNTER_SINCE_RESTART";
    public static final String DAILY_COUNTER = "com.sm.stepsassistant.DAILY_COUNTER";
    public static final String TIME_WALKED = "com.sm.stepsassistant.TIME_WALKED";
    public static final String DATA_TO_EXPORT = "com.sm.stepsassistant.DATA_TO_EXPORT";
    public static final String DAILY_GOAL = "com.sm.stepsassistant.DAILY_GOAL";
    public static final String SHOW_STEP_CARD = "com.sm.stepsassistant.SHOW_STEP_CARD";
    private long currentStep = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("OUTPUT", "Starting ListenerService");
        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor mStepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(this,mStepSensor,SensorManager.SENSOR_DELAY_NORMAL);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(DATA_TO_EXPORT,"Hello world!");
        editor.commit();

        Intent dataListenerIntent = new Intent(this, DataLayerListenerService.class);
        startService(dataListenerIntent);
        return 0;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){}

    @SuppressLint("CommitPrefEdits")
    public void onSensorChanged(SensorEvent sensorEvent){
        int msWalked;
        long lastStep;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        int stepsSinceRestart = (int) sensorEvent.values[0];
        if (stepsSinceRestart < prefs.getInt(COUNTER_SINCE_RESTART,0)){
            int oldSum = prefs.getInt(DAILY_COUNTER,0);
            int amountToAdd = prefs.getInt(COUNTER_SINCE_RESTART,0);
            editor.putInt(DAILY_COUNTER,oldSum+amountToAdd);
        }
        editor.putInt(COUNTER_SINCE_RESTART,stepsSinceRestart);

        //Counting the amount of time that the person is walking for
        msWalked = prefs.getInt(TIME_WALKED, 0);
        lastStep = currentStep;
        currentStep = sensorEvent.timestamp/1000000000;
        if (currentStep - lastStep < 5){
            msWalked += (int)(currentStep - lastStep);
        } else {
            msWalked += 5; //rough estimation to make up for missed time
        }

        editor.putInt(TIME_WALKED, msWalked);
        editor.commit();
        Log.d("OUTPUT","Time walked: "+msWalked + ", Steps: "+calculateSteps(this));
    }

    public static int calculateSteps(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(COUNTER_SINCE_RESTART,0) + prefs.getInt(DAILY_COUNTER,0);
    }
    public static String calculateTime(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int hour=0,minute=0,second=0;
        int time = prefs.getInt(TIME_WALKED,0);
        hour = time/3600;
        time %= 3600;
        minute = time/60;
        time %= 60;
        second = time;

        return String.format("%02d",hour)+":"+String.format("%02d",minute)+":"+String.format("%02d",second);
    }
}
