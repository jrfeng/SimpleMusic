package jrfeng.simplemusic;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import jrfeng.simplemusic.base.BaseActivity;
import jrfeng.simplemusic.model.MusicDBHelper;
import jrfeng.simplemusic.model.MusicStorage;
import jrfeng.simplemusic.service.player.PlayerClient;
import jrfeng.simplemusic.utils.durable.Durable;
import jrfeng.simplemusic.utils.log.L;

public class MyApplication extends Application {
    public static final String TAG = "SimpleMusic";

    private static MyApplication mMyApplication;
    private static List<BaseActivity> mActivityStack;

    private PlayerClient mPlayerClient;

    private SQLiteDatabase mMusicDB;
    private MusicStorage mMusicStorage;

    @Override
    public void onCreate() {
        super.onCreate();
        if (mMyApplication == null) {
            mMyApplication = this;
            mActivityStack = new LinkedList<>();
            mPlayerClient = new PlayerClient(this);
            mMusicStorage = new MusicStorage(this);

            MusicDBHelper dBHelper = new MusicDBHelper(this, MusicDBHelper.DB_NAME, null, MusicDBHelper.DB_VERSION);
            mMusicDB = dBHelper.getWritableDatabase();
        }

        Log.d("SimpleMusic", "Application onCreate");
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

    public SQLiteDatabase getMusicDB() {
        return mMusicDB;
    }
}
