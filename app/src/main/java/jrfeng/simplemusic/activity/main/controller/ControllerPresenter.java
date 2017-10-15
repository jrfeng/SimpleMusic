package jrfeng.simplemusic.activity.main.controller;

import android.content.Context;
import android.widget.Toast;

import java.io.File;

import jrfeng.musicplayer.data.Music;
import jrfeng.musicplayer.mode.MusicStorage;
import jrfeng.musicplayer.player.MusicPlayerClient;
import jrfeng.musicplayer.utils.mp3.MP3Util;
import jrfeng.simplemusic.receiver.PlayerActionDisposer;
import jrfeng.simplemusic.receiver.PlayerActionReceiver;

public class ControllerPresenter implements ControllerContract.Presenter, PlayerActionDisposer {
    private ControllerContract.View mView;
    private MusicPlayerClient mClient;
    private PlayerActionReceiver mPlayerActionReceiver;

    private MusicPlayerClient.MusicProgressListener mProgressListener;

    public ControllerPresenter(Context context, ControllerContract.View view) {
        mView = view;
        mClient = MusicPlayerClient.getInstance();
        mProgressListener = new MusicPlayerClient.MusicProgressListener() {
            @Override
            public void onProgressUpdated(int progress) {
                mView.viewSeekTo(progress);
            }
        };
        mPlayerActionReceiver = new PlayerActionReceiver(context, this);
    }

    @Override
    public void begin() {
        mClient.addMusicProgressListener(mProgressListener);
        mPlayerActionReceiver.register();
        if (mClient.getPlayingMusic() == null) {
            mClient.loadMusicGroup(MusicStorage.GroupType.MUSIC_LIST,
                    MusicStorage.MUSIC_LIST_ALL,
                    0, false);
        }
        notifyViewRefresh();
    }

    @Override
    public void end() {
        mClient.removeMusicProgressListener(mProgressListener);
        mPlayerActionReceiver.unregister();
    }

    @Override
    public void playPause() {
        mClient.playPause();
    }

    @Override
    public void next() {
        mClient.next();
    }

    @Override
    public void previous() {
        mClient.previous();
    }

    @Override
    public void seekTo(int progress) {
        if (!isMusicFileExist()) {
            mView.viewSeekTo(0);
        } else {
            mClient.seekTo(progress);
        }
    }

    @Override
    public void setSeekingState(boolean seeking) {
        if (seeking) {
            mClient.removeMusicProgressListener(mProgressListener);
        } else {
            mClient.addMusicProgressListener(mProgressListener);
        }
    }

    @Override
    public void onPlay() {
        mView.viewPause();
    }

    @Override
    public void onPause() {
        mView.viewPlay();
    }

    @Override
    public void onNext() {
        //什么也不做
    }

    @Override
    public void onPrevious() {
        //什么也不做
    }

    @Override
    public void onStop() {
        mView.viewPause();
    }

    @Override
    public void onPrepared() {
        notifyViewRefresh();
    }

    @Override
    public void onError() {
        mView.notifyPlayError();
    }

    @Override
    public void onMusicNotExist() {
        mView.notifyMusicNotExist();
    }

    //********************private********************

    private void notifyViewRefresh() {
        String songName;
        String artist;
        int songProgress;
        int songLength;
        byte[] image;
        boolean isPLaying;

        Music music = mClient.getPlayingMusic();
        if (music == null) {
            songName = null;
            artist = null;
            songProgress = 0;
            songLength = 100;
            image = null;
            isPLaying = false;
        } else {
            songName = music.getSongName();
            artist = music.getArtist();
            songProgress = mClient.getMusicProgress();
            songLength = mClient.getMusicLength();
            image = MP3Util.getMp3Image(new File(music.getPath()));
            isPLaying = mClient.isPlaying();
        }
        mView.refreshViews(songName, artist, songProgress, songLength, image, isPLaying);
    }

    private boolean isMusicFileExist() {
        Music music = mClient.getPlayingMusic();
        return music != null && (new File(music.getPath())).exists();
    }
}
