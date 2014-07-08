package com.sm.stepsassistant;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
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
        int msWalked = prefs.getInt(StartListenerService.TIME_WALKED, 0)/1000;

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
            jsonObject.put("steps", counter);
            jsonObject.put("msTime",msWalked);
            jsonArray.put(jsonObject);
            valuesToExport = jsonArray.toString();
        } catch (JSONException e){
            e.printStackTrace();
        }

        editor.putString(StartListenerService.DATA_TO_EXPORT,valuesToExport);
        editor.commit();


        int notificationId = 1;
        Intent openIntent = new Intent(context, MyWearActivity.class);
        PendingIntent openToday = PendingIntent.getActivity(context,0,openIntent,0);
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setBackground(BitmapFactory.decodeResource(context.getResources(), R.drawable.notify_background))
                .setCustomSizePreset(Notification.WearableExtender.SIZE_LARGE);

        int numberOfSteps = StartListenerService.calculateSteps(context);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder (context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(NumberFormat.getInstance().format(numberOfSteps)+" steps")
                .setContentText(StartListenerService.calculateTime(context))
                .addAction(R.drawable.ic_action_full_screen, "Open", openToday)
                .extend(wearableExtender);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(notificationId,notificationBuilder.build());
        Log.d("OUTPUT", "Notification Updated!");
    }
}
