package jrfeng.simplemusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.utils.log.L;

public class ShutdownActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        L.d(MyApplication.TAG, "收到 SHUTDOWN");
        MyApplication.shutdown();
    }
}
