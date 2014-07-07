package com.sm.stepsassistant;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class ResetReceiver extends BroadcastReceiver {
    public ResetReceiver() {
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("OUTPUT", "resetting everything");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        int counterSinceRestart = prefs.getInt(StartListenerService.COUNTER_SINCE_RESTART, 0);
        int counter = StartListenerService.calculateSteps(context);
        int msWalked = prefs.getInt(StartListenerService.TIME_WALKED, 0);

        editor.putInt(StartListenerService.DAILY_COUNTER,(0-counterSinceRestart));
        editor.putInt(StartListenerService.TIME_WALKED,0);

        String valuesToExport = prefs.getString(StartListenerService.DATA_TO_EXPORT,"");
        JSONArray jsonArray = new JSONArray();
        try {
            if (!valuesToExport.equals("")) {
                jsonArray = new JSONArray(valuesToExport);
            }

            Calendar c = Calendar.getInstance(); //should get the date that it is run, (which would be at midnight)
            c.add(Calendar.DATE,-1); //saving data for the previous day!

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("day",c.get(Calendar.DATE));
            jsonObject.put("month",c.get(Calendar.MONTH));
            jsonObject.put("year",c.get(Calendar.YEAR));
            jsonObject.put("steps",counter);
            jsonObject.put("msTime",msWalked);
            jsonArray.put(jsonObject);
            valuesToExport = jsonArray.toString();
        } catch (JSONException e){
            e.printStackTrace();
        }

        editor.putString(StartListenerService.DATA_TO_EXPORT,valuesToExport);
        editor.commit();

        Intent resetNotification = new Intent(context, NotificationReceiver.class);
        context.startActivity(resetNotification);
    }
}
