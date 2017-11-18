package jrfeng.player.base;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import jrfeng.player.player.MusicPlayerClient;
import jrfeng.player.utils.activity.ActivityStack;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityStack.push(this);
        if (savedInstanceState != null && !MusicPlayerClient.getInstance().isConnect()) {
            resetApp();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityStack.pop(this);
    }

    //****************private**************

    private void resetApp() {
        //调试
        Log.d(TAG, "【重启应用】");

        PackageManager pm = getBaseContext().getPackageManager();
        String pkgName = getBaseContext().getPackageName();
        Intent intent = pm.getLaunchIntentForPackage(pkgName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
