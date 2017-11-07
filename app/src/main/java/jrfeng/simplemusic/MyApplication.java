package jrfeng.simplemusic;

import android.app.Application;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import jrfeng.simplemusic.base.BaseActivity;

public class MyApplication extends Application {
    /**
     * 调试用，请在调试时将其设为 true。
     */
    public static final boolean DEBUG = true;

    public static final String TAG = "Application";

    private static MyApplication mMyApplication;
    private static List<BaseActivity> mActivityStack;

    @Override
    public void onCreate() {
        super.onCreate();
        //调试
        log("【************Application : onCreate****************】");
        if (mMyApplication == null) {
            mMyApplication = this;
            mActivityStack = new LinkedList<>();
        }
    }

    public static MyApplication getInstance() {
        return mMyApplication;
    }

    public static void addActivity(BaseActivity activity) {
        mActivityStack.add(activity);
    }

    public static void removeActivity(BaseActivity activity) {
        mActivityStack.remove(activity);
    }

    public static void shutdown() {
        //调试
        log("MyApplication : shutdown app");
        for (int i = 0; i < mActivityStack.size(); i++) {
            if (!mActivityStack.get(i).isFinishing()) {
                mActivityStack.get(i).finish();
            }
        }
        mActivityStack.clear();
    }

    //****************调试用***************

    private static void log(String msg) {
        if (MyApplication.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
