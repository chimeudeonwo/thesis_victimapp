package com.example.loginregister;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.List;

import support.Display;
import support.ParamsRef;
import support.PermissionMgt;

public class UserSignUp extends AppCompatActivity {

    private TextView displayUserId;
    private Button registerBtn;
    private static final int PHONE_STATE_PERMISSION_REQUEST_CODE = 2;
    private static final int FLOCATION_PERMISSION_REQUEST_CODE = 3;
    private SharedPreferences sharedPreferences;
    // creating constant keys for shared preferences.
    public static final String SHARED_PREFS = "shared_prefs";
    // key for storing email.
    public static final String USER_ID = "USERID";
    private LocationManager locationManager;
    private double latitude;
    private double longitude;
    public static String[] PERMISSIONS;
    public static final int MULTIPLE_PERMISSION_REQ_CODE = 1;

    // display
    Display display = new Display(this);

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_up);

        displayUserId = (TextView) findViewById(R.id.viewUserId);
        registerBtn = (Button) findViewById(R.id.registerBtn);
        this.sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);    // for single XML SharedPreferences file

        // request permissions
        requestPhoneState_location_permissions();

        //onClick event
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)

            @Override
            public void onClick(View view) {

                // locationRequest = initLocationRequest();
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    display.displayDialogMsg("isProvider is null", "Provider");
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                } else computeUserLastLocation();
                computeUserLastLocation(); // compute user location if permission already granted

                try {
                    postUser();
                    displayUserId.setText(getUserId());

                    // go to homePage
                    Intent emSysHomeIntent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(emSysHomeIntent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    // ------------------- End of onCreate() --------------------------------------------

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // check which permissions the user granted
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // get IMEI
        }

        if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            // get get location
        }

        if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
            // get IMEI
        }
    }

    public void requestPhoneState_location_permissions() {
        PERMISSIONS = new String[]{
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        if (!hasPermission(UserSignUp.this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(UserSignUp.this, PERMISSIONS, MULTIPLE_PERMISSION_REQ_CODE);
        }
    }

    public boolean hasPermission(Activity context, String... PERMISSIONS) {
        if (context != null && PERMISSIONS != null) {
            for (String permission : PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    // ------------------------ Methods Starts here ---------------------------------------

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public JSONObject getRegisterFormData() throws JSONException {

        //for Geolocation and Victim
        String userIMEI = ""; // "userIMEI04"; // not null   TODO: ensure this is implemented before

        // ask for permission if not yet granted and if granted, proceed otherwise toast reason why it is needed
        if (PermissionMgt.checkReadPhoneStatePermission(this)) {
            // TODO: when tested with real phone, ensure that at this point, the user Imei (userIpAddress) is not null
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            userIMEI = telephonyManager.getImei(); //TODO uncomment when running on phone
        } else {
            Toast.makeText(this, "To use this service please allow access to phone state", Toast.LENGTH_SHORT).show();

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE}, PHONE_STATE_PERMISSION_REQUEST_CODE);
        }

        try {
            //json request payload
            JSONObject json = new JSONObject();

            JSONObject user = getUserFormData();
            JSONObject victim = getVictimFormData(userIMEI);
            JSONObject geolocation = getAddressAndGeolocationFormData(userIMEI).getJSONObject("geolocation");
            JSONObject address = getAddressAndGeolocationFormData(userIMEI).getJSONObject("address");

            //add all to json
            json.put("user", user);
            json.put("victim", victim);
            json.put("geolocation", geolocation);
            json.put("address", address);

            return json;
        } catch (JSONException e) {
            Log.v("RegisterFormDataError", e.getCause() + ", " + e.getMessage());
        }
        return null;
    }

    private JSONObject getUserFormData() throws JSONException {
        //User
        EditText editUsername = (EditText) findViewById(R.id.editUsername);
        String username = editUsername.getText().toString();

        String password = ((EditText) findViewById(R.id.editPassword)).getText().toString();

        String email = ((EditText) findViewById(R.id.editEmail)).getText().toString();
        String who = ((Spinner) findViewById(R.id.whoSpinner)).getSelectedItem().toString();
        boolean checkedUserData = ((CheckBox) findViewById(R.id.checkBox)).isChecked();

        //user
        JSONObject user = new JSONObject();
        user.put("username", username);
        user.put("password", password);
        user.put("email", email);

        if (who.equals("Select category")) {
            //Toast.makeText(this, " choose user or security agency", Toast.LENGTH_SHORT).show();
            //TODO: prompt user to choose user or security agency
            editUsername.setError("choose user or security agency");
        }

        if (who.equalsIgnoreCase("USER")) {
            user.put("WHO", "VICTIM");
        }

        user.put("agreedToUserDataTerms", checkedUserData);

        return user;
    }

    private JSONObject getVictimFormData(String userIMEI) throws JSONException {
        // victim
        String firstname = ((EditText) findViewById(R.id.editFirstname)).getText().toString();
        String lastname = ((EditText) findViewById(R.id.editLastname)).getText().toString();
        String phone_number = ((EditText) findViewById(R.id.editPhoneNumber)).getText().toString();
        String contact_person_email = ((EditText) findViewById(R.id.editContactPersonEmail)).getText().toString();

        // victim
        JSONObject victim = new JSONObject();
        victim.put("firstname", firstname);
        victim.put("lastname", lastname);
        victim.put("contactPersonEmail", contact_person_email);
        victim.put("userPhone", phone_number);
        victim.put("imei", userIMEI);   // TODO: remember to ensure this is working properly

        return victim;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private JSONObject getAddressAndGeolocationFormData(String userIMEI) throws JSONException {
        //Address
        String streetAndHouseNr = ((EditText) findViewById(R.id.editStreet)).getText().toString()
                + " " + ((EditText) findViewById(R.id.editStreetNr)).getText().toString();
        int postcode = Integer.parseInt(((EditText) findViewById(R.id.editPostCode)).getText().toString());
        String lga = ((Spinner) findViewById(R.id.lgaSpinner)).getSelectedItem().toString();
        String state = ((Spinner) findViewById(R.id.stateSpinner)).getSelectedItem().toString();
        String country = ((Spinner) findViewById(R.id.countrySpinner)).getSelectedItem().toString();

        // user location

        String latitude = String.valueOf(this.latitude); //TODO: get this automatically
        String longitude = String.valueOf(this.longitude); // getUserLocation().get("longitude").getAsString();

        //address
        JSONObject address = new JSONObject();
        address.put("streetAndHouseNr", streetAndHouseNr);
        address.put("postcode", postcode);
        address.put("lga", lga);
        address.put("state", state);

        // geolocation
        JSONObject geolocation = new JSONObject();
        geolocation.put("city", state);
        geolocation.put("country", country);
        geolocation.put("latitude", latitude);
        geolocation.put("longitude", longitude);
        geolocation.put(ParamsRef.USER_IMEI, userIMEI);

        JSONObject addrAndGeo = new JSONObject();
        addrAndGeo.put("address", address);
        addrAndGeo.put("geolocation", geolocation);

        return addrAndGeo;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void postUser() throws JSONException {

        System.out.println("simple request called");
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this.getApplicationContext());
        String url = "http://192.168.2.122:8080/api/v1/register/user";
        // String url = "http://10.0.2.2:8080/api/v1/register/user";
        // String url = "http://localhost:8080/api/v1/register/user";

        final String requestBody = String.valueOf(getRegisterFormData());

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        displayUserId.setText(response);
                        saveUserId(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext().getApplicationContext(), "That didn't work!" + error, Toast.LENGTH_SHORT).show();
                Log.d("RegisterRequestError", "did not send request", error);
            }
        }) {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public byte[] getBody() throws AuthFailureError {
                return requestBody.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void saveUserId(String msg) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // below lines will put values for
        // message in shared preferences.
        editor.putString(USER_ID, msg);
        // to save our data with key and value.
        editor.apply();
        // on below line we are displaying a toast message after adding data to shared prefs.
        Toast.makeText(this, "Message saved to Shared Preferences", Toast.LENGTH_SHORT).show();

    }

    // get value // userId
    public String getUserId() {
        return sharedPreferences.getString(USER_ID, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private boolean computeUserLastLocation() {
        Location locationGPS = getLastKnownLocation();
        if (locationGPS != null) {
            this.latitude = locationGPS.getLatitude();
            this.longitude = locationGPS.getLongitude();
            return true;
        }
         display.displayDialogMsg("Unable to find location, Please Turn On Your GPS to use this service", "Turn On GPS");
        return false;
    }

    private Location getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MULTIPLE_PERMISSION_REQ_CODE);
        }

        locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    @SuppressLint("MissingPermission")
    private LocationRequest initLocationRequest() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5);
        locationRequest.setFastestInterval(0);
        locationRequest.setNumUpdates(1);

        return locationRequest;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FLOCATION_PERMISSION_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // Toast.makeText(this, "GPS turn on ...", Toast.LENGTH_LONG).show();
                    display.displayDialogMsg("GPS turn on your GPS to enable emSys calculate your location.", "Turn On GPS");
                    break;

                case Activity.RESULT_CANCELED:
                    display.displayDialogMsg("GPS turn on your GPS to enable emSys calculate your location.", "Turn On GPS");
                    break;
            }
        }
    }
}