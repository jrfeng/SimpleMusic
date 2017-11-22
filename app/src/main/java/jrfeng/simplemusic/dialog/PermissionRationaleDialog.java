package jrfeng.simplemusic.dialog;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;

import jrfeng.simplemusic.widget.CustomAlertDialog;

public class PermissionRationaleDialog {
    public static void show(String message,
                            final Activity activity,
                            final String permission,
                            final int requestCode) {
        CustomAlertDialog dialog = new CustomAlertDialog(activity);
        dialog.setStyle(CustomAlertDialog.Style.JUST_MESSAGE);
        dialog.setTitle("权限说明");
        dialog.setMessage(message);
        dialog.setPositiveButtonListener(new CustomAlertDialog.OnButtonClickListener() {
            @Override
            public void onButtonClicked(String input, boolean optionChecked, int arg) {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
            }
        });
        dialog.show();
    }
}
