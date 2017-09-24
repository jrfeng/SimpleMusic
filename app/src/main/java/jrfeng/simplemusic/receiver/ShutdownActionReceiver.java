package jrfeng.simplemusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import jrfeng.simplemusic.MyApplication;

public class ShutdownActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(MyApplication.TAG, "收到 SHUTDOWN");
        MyApplication.shutdown();
    }
}
