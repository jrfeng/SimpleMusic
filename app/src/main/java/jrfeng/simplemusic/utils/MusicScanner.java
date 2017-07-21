package jrfeng.simplemusic.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.security.InvalidParameterException;

import jrfeng.simplemusic.data.Music;
import jrfeng.simplemusic.model.MusicStorage;

public class MusicScanner {
    private OnScanListener mScanListener;
    private MusicStorage mMusicStorage;
    private Mp3BaseInfo mMp3Info;

    /**
     * 创建音乐扫描器
     *
     * @param musicStorage 音乐存储器
     */
    public MusicScanner(MusicStorage musicStorage) {
        mMp3Info = new Mp3BaseInfo();
        mMusicStorage = musicStorage;
    }

    /**
     * 扫描指定目录。
     * 有阻塞调用线程的风险。
     *
     * @param targetDir 要扫描的目标目录。
     * @param listener  扫描监听器。在扫描时，和扫描完成后是会收到通知。
     */
    public void scan(File targetDir, OnScanListener listener) {
        mScanListener = listener;
        if (mScanListener != null) {
            mScanListener.onStart();
        }
        scan(targetDir);
        if (mScanListener != null) {
            mScanListener.onFinished();
        }
        mMusicStorage.saveAsync();
    }

    /**
     * 扫描指定目录。
     *
     * @param targetDir 要扫描的目标目录。
     */
    private void scan(File targetDir) {
        if (!targetDir.exists()) {
            throw new InvalidParameterException("directory not exists.");
        }

        if (!targetDir.isDirectory()) {
            throw new InvalidParameterException("need a directory.");
        }

        if (mScanListener != null) {
            mScanListener.onScan(targetDir.getAbsolutePath());
        }

        File[] musicFiles = targetDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String f = file.getName();
                return f.endsWith(".mp3") || f.endsWith(".ogg") || f.endsWith(".flac") || f.endsWith(".wav");
            }
        });

        addToMusicStorage(musicFiles);

        File[] dirs = targetDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() && !file.getName().startsWith(".");
            }
        });

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
            if (mScanListener != null) {
                mScanListener.onScan(f.getAbsolutePath());
            }
            name = f.getName();
            if (name.endsWith(".mp3")) {
                try {
                    mMp3Info.load(f);
                    mMusicStorage.addMusic(new Music(f.getAbsolutePath(),
                            mMp3Info.getSongName(),
                            mMp3Info.getArtist(),
                            mMp3Info.getAlbum(),
                            mMp3Info.getYear(),
                            mMp3Info.getComment()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                String songName = name.substring(0, name.lastIndexOf("."));
                mMusicStorage.addMusic(new Music(f.getAbsolutePath(),
                        songName,
                        "未知歌手",
                        "未知专辑",
                        "未知年份",
                        "未知"));
            }
        }
    }

    //*****************************interface**********************

    /**
     * 扫描监听器接口
     */
    public interface OnScanListener {
        void onScan(String fileName);

        void onStart();

        void onFinished();
    }
}
