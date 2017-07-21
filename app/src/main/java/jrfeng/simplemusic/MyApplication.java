package jrfeng.simplemusic;

import android.app.Application;

import java.util.LinkedList;
import java.util.List;

import jrfeng.simplemusic.base.BaseActivity;
import jrfeng.simplemusic.model.MusicStorage;
import jrfeng.simplemusic.model.player.PlayerClient;
import jrfeng.simplemusic.utils.L;
import jrfeng.simplemusic.utils.MusicScanner;

public class MyApplication extends Application {
    public static final String TAG = "SimpleMusic";

    private static MyApplication mMyApplication;
    private static List<BaseActivity> mActivityStack;

    private PlayerClient mPlayerClient;
    private MusicStorage mMusicStorage;
    private MusicScanner mMusicScanner;

    @Override
    public void onCreate() {
        super.onCreate();
        if (mMyApplication == null) {
            mMyApplication = this;
            mActivityStack = new LinkedList<>();
            mPlayerClient = new PlayerClient(this);
            mMusicStorage = new MusicStorage(this);
            mMusicScanner = new MusicScanner(mMusicStorage);
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

    public PlayerClient getPlayerClient() {
        return mPlayerClient;
    }

    public MusicStorage getMusicStorage() {
        return mMusicStorage;
    }

    public MusicScanner getMusicScanner() {
        return mMusicScanner;
    }
}
