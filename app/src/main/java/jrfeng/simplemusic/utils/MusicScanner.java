package jrfeng.simplemusic.utils;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileFilter;

import jrfeng.simplemusic.data.Music;
import jrfeng.simplemusic.model.MusicDBHelper;
import jrfeng.simplemusic.model.MusicStorage;
import jrfeng.simplemusic.utils.mp3info.BaseInfo;
import jrfeng.simplemusic.utils.mp3info.Id3v2Info;
import jrfeng.simplemusic.utils.mp3info.Mp3Info;

public class MusicScanner {
    private MusicStorage mMusicStorage;
    private Mp3Info mMp3Info;

    private FileFilter mMusicFileFilter;
    private FileFilter mDirFilter;

    private SQLiteDatabase mMusicDB;
    private ContentValues mValues;

    private int mCount;

    /**
     * 创建音乐扫描器
     *
     * @param musicStorage 音乐存储器
     */
    public MusicScanner(MusicStorage musicStorage, SQLiteDatabase database) {
        mMusicStorage = musicStorage;
        mMusicDB = database;
        mValues = new ContentValues();

        mMusicFileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                String f = file.getName();
                return f.endsWith(".mp3") || f.endsWith(".flac") || f.endsWith(".wav");
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
     * 扫描指定目录。
     * 有阻塞调用线程的风险。
     *
     * @param targetDir 要扫描的目标目录。
     * @param listener  扫描监听器（一次性的）。在扫描开始时，和扫描完成后会收到通知。
     */
    public void scan(File targetDir, OnScanListener listener) {
        mMp3Info = new Mp3Info();
        mCount = 0;
        if (listener != null) {
            listener.onStart();
        }
        scan(targetDir);
        if (listener != null) {
            listener.onFinished(mCount);
        }
        mMp3Info.release();
        mMusicStorage.saveAsync();
    }

    /**
     * 扫描指定目录。
     *
     * @param targetDir 要扫描的目标目录。
     */
    private void scan(File targetDir) {
        if (!targetDir.exists()) {
            System.err.println("target directory not exist.");
            return;
        }

        if (!targetDir.isDirectory()) {
            System.err.println("not a directory.");
            return;
        }

        File[] musicFiles = targetDir.listFiles(mMusicFileFilter);

        addToMusicStorage(musicFiles);

        File[] dirs = targetDir.listFiles(mDirFilter);

        if (dirs != null) {
            for (File dir : dirs) {
                scan(dir);
            }
        }
    }

    /**
     * 将音乐添加到音乐存储器
     *
     * @param musicFiles 要添加的音乐文件。
     */
    private void addToMusicStorage(File[] musicFiles) {
        if (musicFiles == null) {
            return;
        }

        String name;
        for (File f : musicFiles) {
            name = f.getName();
            String songName = name.substring(0, name.lastIndexOf("."));
            if (name.endsWith(".mp3")) {
                mMp3Info.load(f);
                //过滤小于60秒的文件
                if (mMp3Info.getLengthSeconds() < 60) {
                    continue;
                }

                BaseInfo baseInfo;
                if (mMp3Info.hasId3v2()) {
                    baseInfo = mMp3Info.getId3v2Info();
                    Id3v2Info id3v2Info = (Id3v2Info) baseInfo;
                    if (id3v2Info.hasImage()) {
                        byte[] image = id3v2Info.getImage();
                        mValues.put(MusicDBHelper.COLUMN_IMAGE, image);
                    }
                } else if (mMp3Info.hasId3v1()) {
                    baseInfo = mMp3Info.getId3v1Info();
                } else {
                    addMusic(f.getAbsolutePath(),
                            songName,
                            "未知",
                            "未知",
                            "未知",
                            "未知");
                    continue;
                }

                addMusic(f.getAbsolutePath(),
                        songName,
                        baseInfo.getArtist(),
                        baseInfo.getAlbum(),
                        baseInfo.getYear(),
                        baseInfo.getComment());
            } else {
                addMusic(f.getAbsolutePath(),
                        songName,
                        "未知",
                        "未知",
                        "未知",
                        "未知");
            }
            mMusicDB.insert(MusicDBHelper.TABLE_MUSIC_LIST, null, mValues);
            mValues.clear();
        }
    }

    private void addMusic(String path, String songName, String artist, String album, String year, String comment) {
        if (mMusicStorage.addMusic(new Music(path,
                songName,
                artist,
                album,
                year,
                comment))) {
            mValues.put(MusicDBHelper.COLUMN_PATH, path);
            mValues.put(MusicDBHelper.COLUMN_NAME, songName);
            mValues.put(MusicDBHelper.COLUMN_ARTIST, artist);
            mValues.put(MusicDBHelper.COLUMN_ALBUM, album);
            mValues.put(MusicDBHelper.COLUMN_YEAR, year);
            mValues.put(MusicDBHelper.COLUMN_COMMENT, comment);
            mCount++;
        }
    }

    //*****************************interface**********************

    /**
     * 扫描监听器接口
     */
    public interface OnScanListener {
        void onStart();

        void onFinished(int count);
    }
}
