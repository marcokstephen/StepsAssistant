package com.sm.stepsassistant;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class MyActivity extends Activity {

    GoogleApiClient mGoogleApiClient;
    static Context c;
    private static final String START_ACTIVITY_PATH = "/start-activity";
    public static List<Day> dayList = new ArrayList<Day>();
    public ListView historyListView;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        c = this;

        mGoogleApiClient = new GoogleApiClient.Builder(c)
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

        historyListView = (ListView)findViewById(R.id.historyListView);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String historyString = prefs.getString(ResponseListenerService.SAVED_HISTORY,"");
        if (!historyString.equals("")){
            try {
                JSONArray historyJsonArray = new JSONArray(historyString);
                for (int i = 0; i < historyJsonArray.length(); i++) {
                    JSONObject jsonObject = historyJsonArray.getJSONObject(i);
                    MyActivity.dayList.add(new Day(jsonObject));
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
        }

        updateListView();
    }

    public void updateListView(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HistoryListAdapter hla = new HistoryListAdapter(c,dayList);
                historyListView.setAdapter(hla);
            }
        });
    }

    public static void sortDayList(){
        Collections.sort(dayList, new Comparator<Day>(){
            public int compare(Day day1, Day day2){
                Time time1 = new Time();
                time1.set(day1.getDay(),day1.getMonth(),day1.getYear());
                Time time2 = new Time();
                time2.set(day2.getDay(),day2.getMonth(),day2.getYear());
                if (time1.toMillis(false) > time2.toMillis(false)) return -1;
                return 1;
            }
        });
    }

    public static String dayListToString(){
        JSONArray jsonArray = new JSONArray();
        for (Day currentDay : dayList) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("day", currentDay.getDay());
                jsonObject.put("month", currentDay.getMonth());
                jsonObject.put("year", currentDay.getYear());
                jsonObject.put("steps", currentDay.getStepCount());
                jsonObject.put("msTime", currentDay.getTime());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray.toString();
    }

    public void exportData(View view){
        String columnString = "Day,Month,Year,Steps,SecondsWalked";
        for (Day day : dayList) {
            String datastring = "\n" + day.getDay() + "," + day.getMonth() + "," + day.getYear() + "," + day.getStepCount() + "," + day.getTime();
            columnString = columnString + datastring;
        }
        File file = null;
        File root = Environment.getExternalStorageDirectory();
        if (root.canWrite()){
            File dir = new File(root.getAbsolutePath()+"/StepAssistantData");
            dir.mkdirs();
            String filename = "StepAssistantData.csv";
            file = new File(dir, filename);
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(file);
            } catch (FileNotFoundException e){
                e.printStackTrace();
            }
            try {
                out.write(columnString.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        Uri u1 = null;
        u1 = Uri.fromFile(file);

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Step History");
        sendIntent.putExtra(Intent.EXTRA_STREAM, u1);
        sendIntent.setType("text/html");
        startActivity(sendIntent);
    }

    public void onStartSyncDataClick(View view){
        Log.d("OUTPUT","Starting sync data!!");

        new StartWearableActivityTask().execute();
        Toast toast = Toast.makeText(this,"Syncing...",Toast.LENGTH_SHORT);
        toast.show();
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                updateListView();
            }
        };
        handler.postDelayed(runnable,1000);
    }

    private class StartWearableActivityTask extends AsyncTask<Void, Void, Void> {

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
                    mGoogleApiClient, node, START_ACTIVITY_PATH, "Syncing!".getBytes()).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Toast toast = Toast.makeText(c,"Could not connect!",Toast.LENGTH_SHORT);
                                toast.show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear_history) {
            Log.d("OUTPUT","Clear history pressed");
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Delete All History");
            alertDialogBuilder.setMessage("This will permanently delete all history. Are you sure?")
                    .setCancelable(true)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @SuppressLint("CommitPrefEdits")
                        public void onClick(DialogInterface dialog, int id) {
                            dayList.clear();
                            updateListView();
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(ResponseListenerService.SAVED_HISTORY, "");
                            editor.commit();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else if (id == R.id.view_settings) {
            Log.d("OUTPUT","Settings pressed");
            Intent intent = new Intent(this, Prefs.class);
            startActivity(intent);
        } else if (id == R.id.action_help) {
            Log.d("OUTPUT","Help pressed");
        }
        return super.onOptionsItemSelected(item);
    }
}