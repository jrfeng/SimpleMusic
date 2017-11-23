package jrfeng.simplemusic;

import android.app.Application;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

public class MyApplication extends Application {
    /**
     * 调试用，请在调试时将其设为 true。
     */
    public static final boolean DEBUG = true;

    public static final String TAG = "Application";

    private static MyApplication mMyApplication;

    @Override
    public void onCreate() {
        super.onCreate();

        if(LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);//初始化内存监测工具

        //调试
        log("【************Application : onCreate****************】");
        if (mMyApplication == null) {
            mMyApplication = this;
        }
    }

    public static MyApplication getInstance() {
        return mMyApplication;
    }

    //****************调试用***************

    private static void log(String msg) {
        if (MyApplication.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
