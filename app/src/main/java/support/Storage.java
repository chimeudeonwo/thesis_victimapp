package support;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class Storage {
    public static final String SHARED_PREFS = "shared_prefs";
    private SharedPreferences sharedPreferences;
    private Activity activity;

    public Storage(Activity activity){
        this.activity = activity;
        // this.sharedPreferences = sharedPreferences;
        this.sharedPreferences = this.getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
    }

    public Storage(){
    }

    public Activity getActivity() {
        return activity;
    }

    public void saveUserId(String userId) {
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        // below lines will put values for
        // message in shared preferences.
        editor.putString(ParamsRef.USER_ID, userId);
        // to save our data with key and value.
        editor.apply();
        // on below line we are displaying a toast message after adding data to shared prefs.
        Toast.makeText(activity, "Message saved to Shared Preferences", Toast.LENGTH_SHORT).show();

    }

    public void saveUserAuthToken(String token) {
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        // below lines will put values for
        // message in shared preferences.
        editor.putString(ParamsRef.USER_TOKEN, token);
        // to save our data with key and value.
        editor.apply();
        // on below line we are displaying a toast message after adding data to shared prefs.
        Toast.makeText(activity, "Message saved to Shared Preferences", Toast.LENGTH_SHORT).show();

    }

    // get value // userId
    public String getUserId(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(ParamsRef.USER_ID, null);
    }

    public String getUserAuthToken(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(ParamsRef.USER_TOKEN, null);
    }
}
