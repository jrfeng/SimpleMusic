package jrfeng.simplemusic;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import java.util.LinkedList;
import java.util.List;

import jrfeng.simplemusic.base.BaseActivity;
import jrfeng.simplemusic.model.MusicDBHelper;
import jrfeng.musicplayer.player.MusicPlayerClient;
import jrfeng.simplemusic.utils.log.L;

public class MyApplication extends Application {
    public static final String TAG = "SimpleMusic";

    private static MyApplication mMyApplication;
    private static List<BaseActivity> mActivityStack;

    private MusicPlayerClient mPlayerClient;

    private SQLiteDatabase mMusicDB;

    @Override
    public void onCreate() {
        super.onCreate();
        //调试
        L.d("App", "【************Application : onCreate****************】");
        if (mMyApplication == null) {
            mMyApplication = this;
            mActivityStack = new LinkedList<>();

            mPlayerClient = MusicPlayerClient.getInstance();

            MusicDBHelper dBHelper = new MusicDBHelper(this, MusicDBHelper.DB_NAME, null, MusicDBHelper.DB_VERSION);
            mMusicDB = dBHelper.getWritableDatabase();
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

    public MusicPlayerClient getPlayerClient() {
        return mPlayerClient;
    }

    public SQLiteDatabase getMusicDB() {
        return mMusicDB;
    }
}
