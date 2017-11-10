package jrfeng.player.base;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import jrfeng.player.player.MusicPlayerClient;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null && !MusicPlayerClient.getInstance().isConnect()) {
            resetApp();
        }
    }

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
