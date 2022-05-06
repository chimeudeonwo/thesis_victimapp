package com.example.loginregister;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;

import support.Display;
import support.ParamsRef;
import support.SimpleRequest;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private Display display;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init simpleRequest
        SimpleRequest simpleRequest = new SimpleRequest(MainActivity.this);
        display = new Display(this);

        // init sharedPreferences
        sharedPreferences = getSharedPreferences(ParamsRef.SHARED_PREFS, Context.MODE_PRIVATE);

        // bottom nav
        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        final NavController navController = Navigation.findNavController(findViewById(R.id.fragmentView));

        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // change the action bar title to show the current page
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).build();  // pass the set pf three top destination for each menu item

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // fragments
        View signUpFragment = findViewById(R.id.signUpFragment);
        View loginFragment = findViewById(R.id.loginFragment);
        View logoutFragment = findViewById(R.id.logoutFragment);

        // Remove logout view from users that are not logged in
        removeLogoutItem(logoutFragment);

        // check for logged in user
        final String userId = getUserId();
        try {
            simpleRequest.getRequest(SimpleRequest.HOST + "/user/getUserId/" + userId, getUserAuthToken(), new SimpleRequest.VolleyResponseListener() {
                @Override
                public void onSuccess(String response) {
                    if (Long.parseLong(userId) == Long.parseLong(response)) {
                        // replace the login fragment if user is logged
                        View emAlertFragment = removeLayouts(loginFragment, signUpFragment, logoutFragment);
                        // display.displayDialogMsg("replace returned value is: " + emAlertFragment, "ViewId");

                        if(emAlertFragment != null){
                            emAlertFragment.findViewById(R.id.emAlertHome).setOnClickListener(new View.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.Q)
                                @Override
                                public void onClick(View view) {
                                    startEmAlertActivity();
                                }
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    display.displayDialogMsg("Please login to continue..." , "Login");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // show if user is not logged, on clicked, start register activity
        signUpFragment.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick(View view) {
                signUpActivity();
            }
        });

        // show login when user is not logged, on click start login activity
        loginFragment.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick(View view) {
                loginActivity();
            }
        });

        // show logout btn when user is logged, on click clear user Id and Auth Token, then start home activity
        logoutFragment.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick(View view) {
                clearUSerDetails();
                startNamedActivity(MainActivity.class);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    /** Removes login and register layouts when a user is logged in*/
    public View removeLayouts(View loginView, View signUpView, View logoutFragment) {

        // The specified child already has a parent. You must call removeView() on the child's parent first.
        ViewGroup parent = (ViewGroup) loginView.getParent();
        if (parent != null) {
            parent.removeView(loginView);
            parent.removeView(signUpView);
        }

        // inflate the main activity layout and add the emAlert view
        View inflatedView = getLayoutInflater().inflate(R.layout.activity_em_alert, null);

        assert parent != null;
        parent.addView(inflatedView);
        parent.addView(logoutFragment);  // Add logout view from users that are logged in

        return inflatedView.findViewById(R.id.emAlertHome);
    }

    public void signUpActivity() {
        // start signUp activity
        Intent signUpIntent = new Intent(this, UserSignUp.class);
        startActivity(signUpIntent);
    }

    public void loginActivity() {
        Intent loginIntent = new Intent(this, Login.class);
        startActivity(loginIntent);
    }

    public String getUserId() {
        return sharedPreferences.getString(ParamsRef.USER_ID, null);
    }

    public String getUserAuthToken() {
        return sharedPreferences.getString(ParamsRef.USER_TOKEN, null);
    }

    public void startNamedActivity(Class<?> className) {
        Intent namedIntent = new Intent(this, className);
        startActivity(namedIntent);
    }

    public void startEmAlertActivity() {
        Intent namedIntent = new Intent(this, EmAlertActivity.class);
        startActivity(namedIntent);
    }

    private void removeLogoutItem(View logoutView){
        ViewGroup parent = (ViewGroup) logoutView.getParent();
        if (parent != null) {
            parent.removeView(logoutView);
        }
    }

    public void clearUSerDetails(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // below lines will put values for
        // message in shared preferences.
        editor.putString(ParamsRef.USER_TOKEN, "");
        editor.putString(ParamsRef.USER_ID, "");
        // to save our data with key and value.
        editor.apply();
    }
}