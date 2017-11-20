package jrfeng.simplemusic.dialog;

import android.content.Context;
import android.content.Intent;

import jrfeng.simplemusic.widget.CustomAlertDialog;

public class RequestPermissionDialog {

    public static void show(Context context, String message) {
        CustomAlertDialog alertDialog = new CustomAlertDialog(context);
        alertDialog.setStyle(CustomAlertDialog.Style.JUST_MESSAGE);
        alertDialog.setTitle("权限申请");
        alertDialog.setMessage(message);
        alertDialog.setPositiveButtonListener(new CustomAlertDialog.OnButtonClickListener() {
            @Override
            public void onButtonClicked(String input, boolean optionChecked, int arg) {
                Intent intent = new Intent();
            }
        });
    }
}
