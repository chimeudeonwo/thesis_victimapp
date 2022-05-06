package support;

import android.app.Activity;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class Display {

    private Activity activity;

    public Display(Activity activity){
        this.activity = activity;
    }

    public void displayDialogMsg(String msg, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msg).setTitle(title);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void toastMsg(String msg){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
