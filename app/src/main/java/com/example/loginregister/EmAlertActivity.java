package com.example.loginregister;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import support.Display;
import support.ParamsRef;
import support.PermissionMgt;
import support.SimpleRequest;

public class EmAlertActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    public double latitude;
    public double longitude;
    public String city;
    public String country;
    private SimpleRequest simpleRequest;
    private LocationManager locationManager;
    public static String[] PERMISSIONS;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 20;
    private static final int FINE_LOCATION_PERMISSION_REQUEST_CODE = 30;
    private static final int PHONE_STATE_PERMISSION_REQUEST_CODE = 40;
    private static final int MULTIPLE_PERMISSION_REQ_CODE = 70;
    private FusedLocationProviderClient fusedLocationClient;
    private Location userCurrentLocation;

    Display display = new Display(this);

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_em_alert);

        // init
        PERMISSIONS = new String[]{
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
        };

        PermissionMgt.requestMultiplePermissions(this, PERMISSIONS);

        sharedPreferences = getSharedPreferences();
        simpleRequest = new SimpleRequest(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Ensure that GPS is turned ON, ensure that the lat, lon and city is already available.
        boolean isGPSProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isNetworkProvider && !isGPSProvider) {
            display.displayDialogMsg("Network- and GPS- Providers are null", "Providers");
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            isNetworkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            isGPSProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        if (isNetworkProvider || isGPSProvider) {

           /*boolean hasCoarseLocationPermission = hasPermission(EmAlertActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
           boolean hasFineLocationPermission = hasPermission(EmAlertActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
           boolean hasPhoneStatePermission = hasPermission(EmAlertActivity.this, Manifest.permission.READ_PHONE_STATE);
           // boolean hasExternalStoragePermission = ContextCompat.checkSelfPermission(EmAlertActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);

           if(!hasPhoneStatePermission ||  !hasCoarseLocationPermission|| !hasFineLocationPermission){
               // this.requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_REQUEST_CODE);
               display.displayDialogMsg("permissions not granted: " + hasPhoneStatePermission
                        + hasCoarseLocationPermission + hasFineLocationPermission, "PERMISSIONS");
               requestPhoneState_location_permissions();
               // requestLocationPermission();
               (new Handler()).postDelayed(this::thisActivity, 10000);
               // return;
           } */

           /* Location location = getLocation();
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                setLatitude(lat);
                setLongitude(lon);
                JSONObject userCurrentCity = getUserLocationCity(lat, lon);
                try {
                    setCity(userCurrentCity.getString(ParamsRef.CITY));
                    setCountry(userCurrentCity.getString(ParamsRef.COUNTRY));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }*/

            if (ContextCompat.checkSelfPermission(EmAlertActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(EmAlertActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                PermissionMgt.requestMultiplePermissions(this, PERMISSIONS);
            }

            // on click of trigger btn, send emergency request
            Button emAlertBtn = findViewById(R.id.btnEmAlert);
            emAlertBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        display.displayDialogMsg("SDK lower than version.M", "SDK_VERSION");
                        return;
                    }

                    try {
                        String responseResult = simpleRequest.postRequest(getEmAlertRequestPayLoad(), SimpleRequest.HOST + "/victim/emAlert",
                                getUserAuthToken());
                        if (responseResult != null && !responseResult.isEmpty() && responseResult.contains("received")) {
                            emAlertBtn.setBackgroundColor(Color.parseColor("green"));
                        }
                        // wait a second and change it back to default
                        (new Handler()).postDelayed(this::setBackgroundColor, 2000);
                        // emAlertBtn.setBackgroundColor(Color.parseColor("red"));
                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }
                }

                private void setBackgroundColor() {
                    emAlertBtn.setBackgroundColor(Color.parseColor("red"));
                }
            });

            // go to home on click.
            View mainActivityHome = findViewById(R.id.firstFragment); // fragment
            mainActivityHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mainActivity();
                }
            });

            // go to history on click.
            View historyBtn = findViewById(R.id.emAlertHistoryId);
            historyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    emAlertHistoryActivity();
                }
            });
        } else {
            display.displayDialogMsg("Please turn on your GPS", "TurnOnGPS");
            (new Handler()).postDelayed(this::thisActivity, 10000);
        }
    }
    // ------------------------------ End of Executables -------------------------------------------

    public void thisActivity() {
        Intent homeIntent = new Intent(this, EmAlertActivity.class);
        startActivity(homeIntent);
    }

    public void mainActivity() {
        Intent homeIntent = new Intent(this, MainActivity.class);
        startActivity(homeIntent);
    }

    public void emAlertHistoryActivity() {
        Intent homeIntent = new Intent(this, EmAlertHistory.class);
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCity() {
        //return (city != null) ? city : "HH"; // "HH" <-- Default
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    // get city using lat and lon
    public JSONObject getUserLocationCity(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                JSONObject geo = new JSONObject();
                geo.put(ParamsRef.CITY, addresses.get(0).getLocality());
                geo.put(ParamsRef.STATE, addresses.get(0).getAdminArea());
                geo.put(ParamsRef.COUNTRY, addresses.get(0).getCountryName());
                return geo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Send Emergency request. The request body will have two JSONObjects: victimEmAlert & geolocation
    @RequiresApi(api = Build.VERSION_CODES.O)
    public JSONObject getEmAlertRequestPayLoad() throws JSONException, ParseException {
        JSONObject payload = new JSONObject();
        payload.put(ParamsRef.VICTIM_EM_ALERT, getVictimEmAlert());

        //add geolocation.put(ParamsRef.USER_IMEI, telephonyManager.getImei());
        JSONObject geolocation = getGeolocation();
        geolocation.put(ParamsRef.USER_IMEI, getUserIMEI().getString(ParamsRef.USER_IMEI));

        payload.put(ParamsRef.GEOLOCATION, geolocation);
        return payload;
    }

    public JSONObject getVictimEmAlert() throws JSONException, ParseException {
        JSONObject victimAlert = new JSONObject();
        victimAlert.put(ParamsRef.VICTIM_ID_USERID, getUserId());
        victimAlert.put(ParamsRef.FOUND_DEVICE, devicesFound());
        victimAlert.put(ParamsRef.TIME, System.currentTimeMillis());
        return victimAlert;
    }

    /**Gets the user's device IMEI. */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public JSONObject getUserIMEI() throws JSONException {
        // check for user permission to ensure that it is granted to access user imei
        if(!PermissionMgt.checkReadPhoneStatePermission(this)){
            PermissionMgt.requestPhoneStatePermission(this);
        }
        JSONObject userIMEI = new JSONObject();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        userIMEI.put(ParamsRef.USER_IMEI, telephonyManager.getImei());
        return userIMEI;
    }

    /**Gets Users location*/
    @RequiresApi(api = Build.VERSION_CODES.O)
    public JSONObject getGeolocation() throws JSONException {
       if(!PermissionMgt.checkCoarseLocationPermission(this)){
           PermissionMgt.requestCoarseLocationPermission(this);
       }
       if(!PermissionMgt.checkFineLocationPermission(this)){
           PermissionMgt.requestFineLocationPermission(this);
       }

        JSONObject geolocation = new JSONObject();
        Location location = getLocation();

        if (location != null) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            setLatitude(lat);
            setLongitude(lon);
            JSONObject userCurrentCity = getUserLocationCity(lat, lon);
            setCity(userCurrentCity.getString(ParamsRef.CITY));
            setCountry(userCurrentCity.getString(ParamsRef.COUNTRY));
            geolocation.put(ParamsRef.CITY, getCity());
            geolocation.put(ParamsRef.COUNTRY, getCountry());
            geolocation.put(ParamsRef.LATITUDE, getLatitude());
            geolocation.put(ParamsRef.LONGITUDE, getLongitude());
            return geolocation;
        }

        // Set dummy info only for the thesis demonstration
        geolocation.put(ParamsRef.CITY, "Hamburg");
        geolocation.put(ParamsRef.COUNTRY, "Germany_Dummy");
        geolocation.put(ParamsRef.LATITUDE, 53.265);
        geolocation.put(ParamsRef.LONGITUDE, 10.256);
        return geolocation;
    }

    private String devicesFound() {
        return "Default --> No device found";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FINE_LOCATION_PERMISSION_REQUEST_CODE) {
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

    // TODO make it return an integer (1,2,3) if the permission(s) are granted i.e. 1 if only one is granted, 2 if
    // TODO 2 permissions are granted, 3 if all are granted, -1 if none is granted.
    @RequiresApi(api = Build.VERSION_CODES.M)
    /**
     * Requests for the listed permissions from the user.
     */
    private void requestPhoneState_location_permissions() {
        if (!(hasPermission(this, PERMISSIONS))) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                display.displayDialogMsg("This is required to enable the system determine your location", "Permission");
            }
            ActivityCompat.requestPermissions(this, PERMISSIONS, MULTIPLE_PERMISSION_REQ_CODE);
        }
    }

    /**
     * Checks weather the current context has the required permission.
     *
     * @param context     - the current application context.
     * @param PERMISSIONS - array of required permissions.
     * @return true if the required permission is already granted to the context.
     */
    private boolean hasPermission(Activity context, String... PERMISSIONS) {
        if (context != null && PERMISSIONS != null) {
            int permissionIncrement = 1;
            for (String permission : PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    display.displayDialogMsg("This context has no " + permission + " permission", "Permission Not Granted, hence requested");
                    ActivityCompat.requestPermissions(this, new String[]{permission}, (MULTIPLE_PERMISSION_REQ_CODE + permissionIncrement++));
                }
            }
            return true;
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    /** returns the user last known location*/
    public Location getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_REQUEST_CODE);
        }

        if (!(hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION))) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_REQUEST_CODE);
            display.displayDialogMsg("permission requested", "PermitRequest");
            thisActivity();
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            userCurrentLocation = location;
                        }
                    }
                });

        if(userCurrentLocation != null){
            return userCurrentLocation;
        }

        if (locationManager != null) {
            Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastKnownLocationGPS != null) {
                return lastKnownLocationGPS;
            } else {
                return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // should explanation be shown
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality");
                // .setPositiveButton("OK", requestPhoneState_location_permissions())
            } else {
                // No explanation needed, the permission can be requested
                requestPhoneState_location_permissions();
                // requestLocationPermission();
            }
        } else {
            checkBackGroundLocation();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPhoneStatePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // should explanation be shown
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                new AlertDialog.Builder(this)
                        .setTitle("Phone State Permission Needed")
                        .setMessage("This app needs the telephony permission, please accept to use Telephony functionality");
                // .setPositiveButton("OK", requestPhoneState_location_permissions())
            } else {
                // No explanation needed, the permission can be requested
                requestPhoneState_location_permissions();
                // requestTelephonyPermission();
            }
        } else {
            checkBackGroundLocation();
        }
    }

    private void checkBackGroundLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestBackgroundLocationPermission();
        }
    }

    private void requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    /*

     private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void requestPhoneStatePermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.READ_PHONE_STATE}, PHONE_PERMISSION_REQ_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode){
            case FINE_LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    display.displayDialogMsg("grantResults[0]: "+ grantResults[0], "getLocation");
                }
            case MULTIPLE_PERMISSION_REQ_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    display.displayDialogMsg("sMultiple permission: "+ checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION), "getLocation");
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    } */
}