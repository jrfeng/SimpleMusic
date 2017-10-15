package jrfeng.simplemusic;

import android.app.Application;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import jrfeng.musicplayer.player.Configure;
import jrfeng.simplemusic.base.BaseActivity;
import jrfeng.musicplayer.player.MusicPlayerClient;
import jrfeng.simplemusic.utils.log.L;

public class MyApplication extends Application {
    public static final String TAG = "SimpleMusic";

    private static MyApplication mMyApplication;
    private static List<BaseActivity> mActivityStack;

    @Override
    public void onCreate() {
        super.onCreate();
        //调试
        L.d("App", "【************Application : onCreate****************】");
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
        L.d(TAG, "MyApplication : shutdown app");
        for (int i = 0; i < mActivityStack.size(); i++) {
            if (!mActivityStack.get(i).isFinishing()) {
                mActivityStack.get(i).finish();
            }
        }
        mActivityStack.clear();
    }
}
