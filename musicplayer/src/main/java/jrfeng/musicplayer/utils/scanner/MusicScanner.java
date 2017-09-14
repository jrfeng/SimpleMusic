package jrfeng.musicplayer.utils.scanner;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import jrfeng.musicplayer.data.Music;
import jrfeng.musicplayer.utils.mp3info.BaseInfo;
import jrfeng.musicplayer.utils.mp3info.Id3v2Info;
import jrfeng.musicplayer.utils.mp3info.Mp3Info;

public class MusicScanner {
    private List<Music> mMusicTemp;
    private List<Music> mHasMusic;
    private Mp3Info mMp3Info;

    private FileFilter mFileFilter;
    private FileFilter mDirFilter;

    private OnScanListener mListener;

    public MusicScanner() {
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

    /**
     * 扫描单个目录下的 mp3 音乐。
     * @param targetDir  要扫描的目录。
     * @param has        已经有的音乐，用来过滤已有的音乐（为 null 时则不过滤任何音乐）。
     * @param listener   扫描监听器，它会在扫描开始时、扫描时、扫描接收时接收到通知。
     * @throws FileNotFoundException 指定目录不存在时会抛出该异常。
     */
    public void scan(File targetDir, List<Music> has, @NonNull OnScanListener listener) throws FileNotFoundException {
        scan(new File[]{targetDir}, has, listener);
    }

    /**
     * 扫描一系列指定目录下的 mp3 音乐。
     * @param dirs      要扫描的目录。
     * @param has       已经有的音乐，用来过滤已有的音乐（为 null 时则不过滤任何音乐）。
     * @param listener  扫描监听器，它会在扫描开始时、扫描时、扫描接收时接收到通知。
     */
    public void scan(final File[] dirs, List<Music> has, @NonNull OnScanListener listener) {
        mHasMusic = has;
        mMp3Info = new Mp3Info();
        mListener = listener;
        mListener.onStart();
        new Thread() {
            @Override
            public void run() {
                try {
                    for (File f : dirs) {
                        scan(f);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                mMp3Info.release();
                mListener.onFinished(mMusicTemp);
                mListener = null;
                if (mHasMusic != null) {
                    mHasMusic = null;
                }
            }
        }.start();
    }

    //********************private******************

    private void scan(File targetDir) throws FileNotFoundException {
        if (!targetDir.exists()) {
            throw new FileNotFoundException(targetDir.getAbsolutePath() + " not exist.");
        }

        if (!targetDir.isDirectory()) {
            throw new IllegalArgumentException(targetDir.getAbsolutePath() + " not a directory");
        }

        File[] musicFiles = targetDir.listFiles(mFileFilter);

        addToTemp(musicFiles);

        File[] dirs = targetDir.listFiles(mDirFilter);

        if (dirs != null) {
            for (File dir : dirs) {
                scan(dir);
            }
        }
    }

    private void addToTemp(File[] musicFiles) {
        if (musicFiles == null) {
            return;
        }

        String name;
        for (File f : musicFiles) {
            name = f.getName();
            String songName = name.substring(0, name.lastIndexOf("."));
            mListener.onScan(name);
            if (name.endsWith(".mp3")) {
                mMp3Info.load(f);
                //过滤小于60秒的文件
                if (mMp3Info.getLengthSeconds() < 60) {
                    continue;
                }

                BaseInfo baseInfo;
                if (mMp3Info.hasId3v2()) {
                    baseInfo = mMp3Info.getId3v2Info();
                } else if (mMp3Info.hasId3v1()) {
                    baseInfo = mMp3Info.getId3v1Info();
                } else {
                    addMusic(new Music(f.getAbsolutePath(),
                            songName,
                            "未知",
                            "未知",
                            "未知",
                            "未知"));
                    continue;
                }

                addMusic(new Music(f.getAbsolutePath(),
                        songName,
                        baseInfo.getArtist(),
                        baseInfo.getAlbum(),
                        baseInfo.getYear(),
                        baseInfo.getComment()));
            }
        }
    }

    private void addMusic(Music music) {
        if (mHasMusic != null) {
            if (!mHasMusic.contains(music)) {
                mMusicTemp.add(music);
            }
        } else {
            mMusicTemp.add(music);
        }
    }

    //******************interface*******************

    public interface OnScanListener {
        void onStart();

        void onScan(String file);

        void onFinished(List<Music> musics);
    }
}
