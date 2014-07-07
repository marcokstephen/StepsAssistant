package com.sm.stepsassistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Collection;
import java.util.HashSet;

public class DataLayerListenerService extends WearableListenerService {
    private static final String START_ACTIVITY_PATH = "/start-activity";
    GoogleApiClient mGoogleApiClient;
    SharedPreferences prefs;
    private String data = "";
    Context context;

    @Override
    public void onCreate() {
        Log.d("OUTPUT","Starting DataLayerListenerService!");
        super.onCreate();
        context = this;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent){
        Log.d("OUTPUT", "Message received!!");
        if (messageEvent.getPath().equals(START_ACTIVITY_PATH)){
            String message = new String(messageEvent.getData());
            Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
            toast.show();

            prefs = PreferenceManager.getDefaultSharedPreferences(this);
            data = prefs.getString(StartListenerService.DATA_TO_EXPORT,"");
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(StartListenerService.DATA_TO_EXPORT,"");
            editor.commit();

            new SendDataToPhone().execute();
        }
    }

    public class SendDataToPhone extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                sendStartActivityMessage(node);
            }
            return null;
        }

        private void sendStartActivityMessage(String node) {

            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node, START_ACTIVITY_PATH, data.getBytes()).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.d("OUTPUT", "Failed to send message with status code: "
                                        + sendMessageResult.getStatus().getStatusCode());
                            } else {
                                Log.d("OUTPUT","Successfully sent!");
                            }
                        }
                    }
            );
        }

        private Collection<String> getNodes() {
            HashSet<String> results = new HashSet<String>();
            NodeApi.GetConnectedNodesResult nodes =
                    Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

            for (Node node : nodes.getNodes()) {
                results.add(node.getId());
            }

            return results;
        }
    }
}
