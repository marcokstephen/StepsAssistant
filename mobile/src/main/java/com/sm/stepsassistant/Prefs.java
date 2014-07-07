package com.sm.stepsassistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashSet;

public class Prefs extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    GoogleApiClient mGoogleApiClient;
    private static final String CHANGE_PREFERENCE_PATH = "/preference-change";
    private String dataToTransfer;
    private Context c;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        c = this;
        addPreferencesFromResource(R.xml.prefs);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {}
                    @Override
                    public void onConnectionSuspended(int i) {}
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {}
                })
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        boolean showNotifications = sharedPreferences.getBoolean("showNotification",true);
        int stepGoal = Integer.parseInt(sharedPreferences.getString("dailyStepGoal","10000"));
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("showNotifications",showNotifications);
            jsonObject.put("stepGoal",stepGoal);
            dataToTransfer = jsonObject.toString();
        } catch (JSONException e){
            e.printStackTrace();
        }
        //TODO: Some sort of "failed sync" toast
        new SyncPreferenceTask().execute();
    }


    private class SyncPreferenceTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                syncPreferenceMessage(node);
            }
            return null;
        }

        private void syncPreferenceMessage(String node) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node, CHANGE_PREFERENCE_PATH, dataToTransfer.getBytes()).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Toast toast = Toast.makeText(c, "Could not sync settings with wearable",Toast.LENGTH_SHORT);
                                toast.show();
                            } else {
                                Log.d("OUTPUT", "Successfully sent!");
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