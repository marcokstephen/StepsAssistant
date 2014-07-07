package com.sm.stepsassistant;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
            Toast toast = Toast.makeText(this, "Sync Complete!", Toast.LENGTH_SHORT);
            toast.show();
            String response = new String(messageEvent.getData());
            if (!response.equals("")) {
                try {
                    Log.d("OUTPUT", response);
                    updateList(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        stopSelf();
    }

    public void updateList(String response) throws JSONException {
        JSONArray jsonArray = new JSONArray(response);
        for (int i = 0; i < jsonArray.length(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            MyActivity.dayList.add(new Day(jsonObject));
        }
        //TODO: find a way to update the list !
    }
}
