package com.example.loginregister;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import support.Display;
import support.ParamsRef;
import support.SimpleRequest;

public class EmAlertHistory extends AppCompatActivity {

    private ListView histories;
    private SimpleRequest simpleRequest;
    private SharedPreferences sharedPreferences;

    Display display = new Display(this);

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_alert_history);

        // get
        histories = findViewById(R.id.emAlertHistoryView);
        simpleRequest = new SimpleRequest(this);
        sharedPreferences = getSharedPreferences();

        //data
        final ArrayList<String> data_histories = new ArrayList<>();
        data_histories.add("Lagos"); // TODO: replace lagos with date of the alert i.e. localDate
        data_histories.add("Hamburg");
        data_histories.add("Berlin");

        // real data
        final ArrayList<String> historiesView = new ArrayList<>(); // holds the list view (what is displayed)
        final ArrayList<JSONObject> jsonListOfHistories = new ArrayList<>(); // hold the data list
        String userId = getUserId();
        try {
            simpleRequest.getRequest(SimpleRequest.HOST + "/user/emAlertHistory/" + userId, getUserAuthToken(), new SimpleRequest.VolleyResponseListener() {
                @Override
                public void onSuccess(String response) throws JSONException {
                    if (response != null && !response.isEmpty()) {
                        JSONArray responseArr = new JSONArray(response);
                        for(int index = 0; index < responseArr.length(); index++){
                            historiesView.add("EmAlert Sent On " + new JSONObject(String.valueOf(responseArr.get(index))).get("localDate").toString()); // display alert list with dates of the alert
                            jsonListOfHistories.add(new JSONObject(String.valueOf(responseArr.get(index))));
                            ArrayAdapter<String> citiesAdapter = new ArrayAdapter<String>(EmAlertHistory.this, android.R.layout.simple_list_item_1, historiesView);
                            histories.setAdapter(citiesAdapter);
                        }
                        // display.displayDialogMsg("response0.getTime: "+new JSONObject(String.valueOf(responseArr.get(0))).get("time").toString(), "HISTORY");
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    display.displayDialogMsg("History fetch failed!", "HISTORY");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

        histories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // TODO: when clicked on one, expand the list or use a display to show the details
                Toast.makeText(EmAlertHistory.this, jsonListOfHistories.get(position) + " Selected", Toast.LENGTH_SHORT).show();
            }
        });

        // go to home on click.
        View mainActivityHome = findViewById(R.id.firstFragment); // emAlertHistoryId
        mainActivityHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity();
            }
        });

        // go to emAlert tab on click.
        View emAlertActivityHome = findViewById(R.id.emAlertHome); // emAlertHistoryId
        emAlertActivityHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emAlertActivity();
            }
        });
    }
    // -------------------- end of executables -----------------------

    private void mainActivity() {
        Intent homeIntent = new Intent(this, MainActivity.class);
        startActivity(homeIntent);
    }

    private void emAlertActivity() {
        Intent homeIntent = new Intent(this, EmAlertActivity.class);
        startActivity(homeIntent);
    }

    public SharedPreferences getSharedPreferences() {
        return getSharedPreferences(ParamsRef.SHARED_PREFS, Context.MODE_PRIVATE);
    }

    private String getUserId() {
        return this.sharedPreferences.getString(ParamsRef.USER_ID, null);
    }


    private String getUserAuthToken() {
        return this.sharedPreferences.getString(ParamsRef.USER_TOKEN, null);
    }
}
