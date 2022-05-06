package support;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class ExitActions {
    public static void exitThisActivity(Activity activity, Class<?> clazz){
        Intent intent = new Intent(activity, clazz);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        activity.startActivity(intent);
        //activity.stopService(intent);
        activity.finishAffinity();
    }

    public static void exitApp(Activity activity, String errMsg){
        activity.moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        Log.e("PermissionGrantError: ", errMsg);
        System.exit(1);
    }
}
