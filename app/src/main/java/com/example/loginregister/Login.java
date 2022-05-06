package com.example.loginregister;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import support.Display;
import support.ParamsRef;
import support.SimpleRequest;

public class Login extends AppCompatActivity {
    private final SimpleRequest simpleRequest;
    Display display = new Display(this);
    private SharedPreferences sharedPreferences;

    public Login() {
        this.simpleRequest = new SimpleRequest(this);
    }

    public SimpleRequest getSimpleRequest() {
        return simpleRequest;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.sharedPreferences = getSharedPreferences(ParamsRef.SHARED_PREFS, Context.MODE_PRIVATE);

        Button registerBtn = (Button) findViewById(R.id.loginButton);
        //onClick event
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)

            @Override
            public void onClick(View view) {
                // String loginUrl = "http://192.168.2.122:8080/api/v1/authenticate/user";
                String loginUrl = SimpleRequest.HOST + "/authenticate/user";
                // ensure all fields are not null
                String username = ((EditText) Login.this.findViewById(R.id.loginUsername)).getText().toString();
                String password = ((EditText) Login.this.findViewById(R.id.loginPassword)).getText().toString();
                if (allFieldsFilled(username, password)) {
                    // append it to the request body
                    JSONObject loginData = new JSONObject();
                    try {
                        loginData.put("username", username);
                        loginData.put("password", password);

                        // send the request and get the response from the getter method
                        simpleRequest.postJsonRequest(loginData, loginUrl);
                        JSONObject serverResponse = simpleRequest.getServerResponse();

                        long userId = serverResponse.getLong(ParamsRef.USER_ID);
                        String userAuthToken = serverResponse.getString(ParamsRef.USER_TOKEN);

                        if (userId > 0) {
                            saveUserId(String.valueOf(userId));
                            saveUserAuthToken(userAuthToken);
                        }
                        // if user is successfully logged in, start EmAlert Activity
                        beginEmAlertActivity();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else
                    display.displayDialogMsg("Username and/or Password must not be empty", "NotNull");
            }
        });
    }

    /**
     * To validate that the user info given and the details from the DB are same
     */
    private void validateUserDetails(String userDetails) {

    }

    private void beginEmAlertActivity() {
        Intent emAlertIntent = new Intent(this, EmAlertActivity.class);
        startActivity(emAlertIntent);
    }

    private boolean allFieldsFilled(String username, String password) {
        /*return ((!TextUtils.isEmpty(((EditText) Login.this.findViewById(R.id.loginUsername)).getText().toString()))
                && (!TextUtils.isEmpty(((EditText) Login.this.findViewById(R.id.loginPassword)).getText().toString()))); */
        return ((!TextUtils.isEmpty(username)) && (!TextUtils.isEmpty(password)));
    }

    private void saveUserAuthToken(String token) {
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        // below lines will put values for
        // message in shared preferences.
        editor.putString(ParamsRef.USER_TOKEN, token);
        // to save our data with key and value.
        editor.apply();
        // on below line we are displaying a toast message after adding data to shared prefs.
        // Toast.makeText(this, "Auth Token saved to Shared Preferences", Toast.LENGTH_SHORT).show();
    }

    private void saveUserId(String userId) {
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        // below lines will put values for
        // message in shared preferences.
        editor.putString(ParamsRef.USER_ID, userId);
        // to save our data with key and value.
        editor.apply();
        // on below line we are displaying a toast message after adding data to shared prefs.
        // Toast.makeText(this, "User Id saved to Shared Preferences", Toast.LENGTH_SHORT).show();
    }

    // get value // userId
    public String getUserAuthToken() {
        return this.sharedPreferences.getString(ParamsRef.USER_TOKEN, null);
    }
}