package com.sm.stepsassistant;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.content.Context;

public class StartListenerService extends Service implements SensorEventListener {

    public static final String COUNTER_SINCE_RESTART = "com.sm.stepsassistant.COUNTER_SINCE_RESTART";
    public static final String DAILY_COUNTER = "com.sm.stepsassistant.DAILY_COUNTER";
    public static final String TIME_WALKED = "com.sm.stepsassistant.TIME_WALKED";
    public static final String DATA_TO_EXPORT = "com.sm.stepsassistant.DATA_TO_EXPORT";
    public static final String DAILY_GOAL = "com.sm.stepsassistant.DAILY_GOAL";
    public static final String SHOW_STEP_CARD = "com.sm.stepsassistant.SHOW_STEP_CARD";
    public static final String FIRST_TIME_LAUNCHING = "com.sm.stepsassistant.FIRST_TIME_LAUNCHING";
    private long currentStep = 0;
    private static Context c;
    private SharedPreferences prefs;

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
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        c = this;

        setInitialAlarm();

        Intent dataListenerIntent = new Intent(this, DataLayerListenerService.class);
        startService(dataListenerIntent);
        return 0;
    }

    public void setInitialAlarm(){
        Time time = new Time();
        time.setToNow();
        time.set(0,0,0,time.monthDay,time.month,time.year);
        time.set(time.toMillis(false)+86400000);
        Intent resetIntent = new Intent(c, ResetReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(c, 1, resetIntent,0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, time.toMillis(false), 86400000, pi); //86400000 is ms per day
        Log.d("OUTPUT","Alarm set for "+time.toMillis(false)+", Current: "+System.currentTimeMillis());
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){}

    @SuppressLint("CommitPrefEdits")
    public void onSensorChanged(SensorEvent sensorEvent){
        int msWalked;
        long lastStep;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        int stepsSinceRestart = (int) sensorEvent.values[0];
        boolean firstTimeLaunching = prefs.getBoolean(FIRST_TIME_LAUNCHING,true);

        if (firstTimeLaunching && stepsSinceRestart > 0){
            Log.d("OUTPUT","First time running!");
            editor.putInt(DAILY_COUNTER,(0-stepsSinceRestart));
            editor.putBoolean(FIRST_TIME_LAUNCHING,false);
        } else if (stepsSinceRestart < prefs.getInt(COUNTER_SINCE_RESTART,0)){
            int oldSum = prefs.getInt(DAILY_COUNTER,0);
            int amountToAdd = prefs.getInt(COUNTER_SINCE_RESTART,0);
            editor.putInt(DAILY_COUNTER,oldSum+amountToAdd);
        }
        editor.putInt(COUNTER_SINCE_RESTART,stepsSinceRestart);

        //Counting the amount of time that the person is walking for
        //msWalked is actually measured in seconds.....
        msWalked = prefs.getInt(TIME_WALKED, 0);
        lastStep = currentStep;
        currentStep = sensorEvent.timestamp/1000000;
        Log.d("OUTPUT","Difference: "+(currentStep - lastStep));
        if (currentStep - lastStep < 90000){ //if difference is less than 2 minutes (required since the sensor does not immediately update)
            Log.d("OUTPUT","Adding!");
            msWalked += (int)(currentStep - lastStep);
        } else if (lastStep != currentStep) {
            Log.d("OUTPUT","ADDING 5!");
            msWalked += 5000; //rough estimation to make up for missed time
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
        int hour,minute,second;
        int time = prefs.getInt(TIME_WALKED,0)/1000;
        hour = time/3600;
        time %= 3600;
        minute = time/60;
        time %= 60;
        second = time;

        return String.format("%02d",hour)+":"+String.format("%02d",minute)+":"+String.format("%02d",second);
    }
}
