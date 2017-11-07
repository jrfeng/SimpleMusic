package jrfeng.simplemusic.activity.scan;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;

import jrfeng.player.data.Music;
import jrfeng.player.utils.scanner.MusicScanner;
import jrfeng.simplemusic.MyApplication;

class ScanPresenter implements ScanContract.Presenter {
    private static final String TAG = "ScanPresenter";
    private ScanContract.View mView;
    private MusicScanner mMusicScanner;

    private int mScanPercent;
    private String mScanHint;
    private int mScanCount;
    private List<Music> mScannedMusics;
    private boolean mNeedRefreshView;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.what == 1 && mNeedRefreshView) {
                mView.refreshView(mScanPercent, mScanHint, mScanCount);
            } else if (message.what == 2) {
                mView.resetView();
                mView.showScannedMusic(mScannedMusics);//显示已扫描到的音乐的列表
            }
            return true;
        }
    });

    ScanPresenter(ScanContract.View view) {
        mView = view;
    }

    @Override
    public void begin() {

    }

    @Override
    public void end() {

    }

    @Override
    public void startScan() {
        //调试
        log("开始扫描");
        if (mMusicScanner == null) {
            mMusicScanner = new MusicScanner();
        }

        File[] dirs = new File[2];
        dirs[0] = new File(getStoragePath((Context) mView, false));
        dirs[1] = new File(getStoragePath((Context) mView, true));
        mMusicScanner.scan(dirs, new MusicScanner.OnScanListener() {
            @Override
            public void onStart() {
                mNeedRefreshView = true;
            }

            @Override
            public void onScan(int percent, String path, int count) {
                mScanPercent = percent;
                mScanHint = path;
                mScanCount = count;
                mHandler.sendEmptyMessage(1);
            }

            @Override
            public void onFinished(List<Music> musics) {
                mNeedRefreshView = false;
                mHandler.sendEmptyMessage(2);
                mScannedMusics = musics;
                //调试
                for (Music i : musics) {
                    Log.d("App", i.getName());
                }
                Log.d("App", "【扫描完成】");
                Log.d("App", "歌曲数 : " + musics.size());
            }
        });
    }

    @Override
    public void stopScan() {
        if (mMusicScanner != null) {
            mNeedRefreshView = false;
            mMusicScanner.stopScan();
        }
    }

    private String getStoragePath(Context mContext, boolean is_removable) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removable == removable) {
                    return path;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    private void log(String msg) {
        if (MyApplication.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
