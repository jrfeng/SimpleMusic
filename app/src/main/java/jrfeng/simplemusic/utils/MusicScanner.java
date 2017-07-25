package jrfeng.simplemusic.utils;

import java.io.File;
import java.io.FileFilter;

import jrfeng.simplemusic.data.Music;
import jrfeng.simplemusic.model.MusicStorage;
import jrfeng.simplemusic.utils.mp3info.BaseInfo;
import jrfeng.simplemusic.utils.mp3info.Mp3Info;

public class MusicScanner {
    private MusicStorage mMusicStorage;
    private Mp3Info mMp3Info;

    private FileFilter musicFileFilter;
    private FileFilter dirFilter;

    private int addCount;

    /**
     * 创建音乐扫描器
     *
     * @param musicStorage 音乐存储器
     */
    public MusicScanner(MusicStorage musicStorage) {
        mMusicStorage = musicStorage;

        musicFileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                String f = file.getName();
                return f.endsWith(".mp3") || f.endsWith(".flac") || f.endsWith(".wav");
            }
        };

        dirFilter = new FileFilter() {
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
        addCount = 0;
        if (listener != null) {
            listener.onStart();
        }
        scan(targetDir);
        if (listener != null) {
            listener.onFinished(addCount);
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

        File[] musicFiles = targetDir.listFiles(musicFileFilter);

        addToMusicStorage(musicFiles);

        File[] dirs = targetDir.listFiles(dirFilter);

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
            if (name.endsWith(".mp3")) {
                mMp3Info.load(f);
                //过滤小于60秒的文件
                if (mMp3Info.getLengthSeconds() < 60) {
                    continue;
                }

                BaseInfo baseInfo;
                String songName = name.substring(0, name.lastIndexOf("."));
                if (mMp3Info.hasId3v2()) {
                    baseInfo = mMp3Info.getId3v2Info();
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
                String songName = name.substring(0, name.lastIndexOf("."));
                addMusic(f.getAbsolutePath(),
                        songName,
                        "未知",
                        "未知",
                        "未知",
                        "未知");
            }
        }
    }

    private void addMusic(String path, String songName, String artist, String album, String year, String comment) {
        if (mMusicStorage.addMusic(new Music(path,
                songName,
                artist,
                album,
                year,
                comment))) {
            addCount++;
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
