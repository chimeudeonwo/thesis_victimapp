package support;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Arrays;

public class PermissionMgt {

    /**Determines if the Read Phone state permission is already granted or not.*/
    public static boolean checkReadPhoneStatePermission(Activity activity){
        if (ContextCompat.checkSelfPermission(
                activity, Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED) {
                return true;
        }
        return false;
    }

    public static boolean checkFineLocationPermission(Activity activity){
        if (ContextCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
                return true;
        }
        return false;
    }

    public static boolean checkCoarseLocationPermission(Activity activity){
        if (ContextCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
                return true;
        }
        return false;
    }

    public static boolean checkStoragePermission(Activity activity){
        if (ContextCompat.checkSelfPermission(
                activity, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
                return true;
        }
        return false;
    }

    // method to check for location permissions granted or not
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static boolean checkLocationPermissions(Context context) {
        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void requestMultiplePermissions(Activity context, String[] PERMISSIONS) {
        int MULTIPLE_PERMISSION_REQ_CODE = 500;
        int permissionId = 1;
        if(context.shouldShowRequestPermissionRationale(
                "should show reason for asking this permission: ")){
            //display "This permission is needed for the app to work"
        }
        ActivityCompat.requestPermissions(context, PERMISSIONS,
                MULTIPLE_PERMISSION_REQ_CODE + permissionId);
    }

    public static boolean hasPermission(Activity context, String... PERMISSIONS){
        if(context != null && PERMISSIONS != null) {
            for(String permission: PERMISSIONS){
                if(ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static void requestPhoneStatePermission(Activity context){
        int PHONE_STATE_PERMISSION = 200;
        ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_PHONE_STATE},
                PHONE_STATE_PERMISSION);
    }

    public static void requestFineLocationPermission(Activity context){
        int PHONE_STATE_PERMISSION = 300;
        ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PHONE_STATE_PERMISSION);
    }

    public static void requestCoarseLocationPermission(Activity context){
        int PHONE_STATE_PERMISSION = 400;
        ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                PHONE_STATE_PERMISSION);
    }

    public static void requestExternalStoragePermission(Activity context){
        int PHONE_STATE_PERMISSION = 500;
        ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PHONE_STATE_PERMISSION);
    }

}
