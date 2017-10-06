package jrfeng.musicplayer.utils.scanner;

import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import jrfeng.musicplayer.data.Music;
import jrfeng.musicplayer.utils.mp3.MP3Info;
import jrfeng.musicplayer.utils.mp3.MP3Util;

public class MusicScanner {
    private static final String TAG = "MusicScanner";
    private static final int DIR_DEEP = 3; //目录的深度默认为3。如果需要更深的目录，将该值增大即可。
    private List<File> mDirs;
    private List<Music> mMusicTemp;

    private FileFilter mFileFilter;
    private FileFilter mDirFilter;

    private Thread mScanThread;

    private OnScanListener mListener;

    private int mScanPercent;

    public MusicScanner() {
        mDirs = new LinkedList<>();
        mMusicTemp = new LinkedList<>();
        mFileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                String f = file.getName();
                return f.endsWith(".mp3");
            }
        };

        mDirFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() && !file.getName().startsWith(".");
            }
        };
    }

//    该方法目前用不上
//    public void scan(File targetDir, OnScanListener listener) {
//        scan(new File[]{targetDir}, listener);
//    }

    public void scan(final File[] dirs, OnScanListener listener) {
        mScanPercent = 0;
        mMusicTemp.clear();
        mListener = listener;
        mListener.onStart();
        mScanThread = new Thread() {
            @Override
            public void run() {
                //调试
                Log.d(TAG, "开始扫描");

                for (File dir : dirs) {
                    getDirs(dir, 0);
                }
                scan();
                if (!Thread.currentThread().isInterrupted() && mListener != null) {
                    mListener.onFinished(mMusicTemp);
                }
                mDirs.clear();
                mListener = null;

                //调试
                Log.d(TAG, "结束扫描");
            }
        };
        mScanThread.start();
    }

    public void stopScan() {
        //调试
        Log.d(TAG, "终止扫描");
        mScanThread.interrupt();
    }

    //********************private******************

    private void scan() {
        for (int i = 0; i < mDirs.size(); i++) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            mScanPercent = (int) Math.round(((double) i / mDirs.size()) * 100);
            if (mListener != null) {
                mListener.onScan(mScanPercent,
                        mDirs.get(i).getAbsolutePath(),
                        mMusicTemp.size());
            }
            File[] musicFiles = mDirs.get(i).listFiles(mFileFilter);
            addToTemp(musicFiles);
        }
    }

    private void getDirs(File dir, int currentDeep) {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }

        //调试
        Log.d(TAG, "获取目录 : " + dir);

        mDirs.add(dir);
        if (mListener != null) {
            mListener.onScan(mScanPercent,
                    dir.getAbsolutePath(),
                    mMusicTemp.size());
        }
        File[] subDirs = dir.listFiles(mDirFilter);
        if (subDirs != null && currentDeep < DIR_DEEP) {
            for (File d : subDirs) {
                getDirs(d, currentDeep + 1);
            }
        }
    }

    private void addToTemp(File[] musicFiles) {
        if (musicFiles == null || Thread.currentThread().isInterrupted()) {
            return;
        }

        String name;
        for (File f : musicFiles) {
            if (mListener != null) {
                mListener.onScan(mScanPercent,
                        f.getAbsolutePath(),
                        mMusicTemp.size());
            }
            name = f.getName();
            String songName = name.substring(0, name.lastIndexOf("."));
            if (name.endsWith(".mp3")) {
                try {
                    MP3Info mp3Info = MP3Util.load(f);
                    //过滤小于60秒的文件
                    if (mp3Info.getLengthMesc() < 60) {
                        continue;
                    }
                    addMusic(new Music(f.getAbsolutePath(),
                            songName,
                            mp3Info.getArtist(),
                            mp3Info.getAlbum(),
                            mp3Info.getYear(),
                            mp3Info.getComment()));
                } catch (IOException e) {
                    System.err.println(e.toString());
                }
            }
        }
    }

    private void addMusic(Music music) {
        //调试
        Log.d(TAG, "添加音乐 : " + music.getSongName());

        mMusicTemp.add(music);
    }

    //******************interface*******************

    public interface OnScanListener {
        void onStart();

        void onScan(int percent, String path, int count);

        void onFinished(List<Music> musics);
    }
}
