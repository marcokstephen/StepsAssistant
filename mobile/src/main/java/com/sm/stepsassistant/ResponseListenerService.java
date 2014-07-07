package com.sm.stepsassistant;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class ResponseListenerService extends WearableListenerService {
    private static final String START_ACTIVITY_PATH = "/start-activity";
    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        Log.d("OUTPUT", "Starting ResponseListenerService (PHONE)!");
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent){
        Log.d("OUTPUT", "Message received on PHONE!!");
        if (messageEvent.getPath().equals(START_ACTIVITY_PATH)){
            String message = new String(messageEvent.getData());
            Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
