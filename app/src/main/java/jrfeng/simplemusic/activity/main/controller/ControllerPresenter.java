package jrfeng.simplemusic.activity.main.controller;

import android.content.Context;

import java.io.File;

import jrfeng.player.data.Music;
import jrfeng.player.mode.MusicStorage;
import jrfeng.player.player.MusicPlayerClient;
import jrfeng.player.utils.mp3.MP3Util;
import jrfeng.simplemusic.dialog.PlayingMusicGroupDialog;
import jrfeng.simplemusic.dialog.TempPlayDialog;
import jrfeng.simplemusic.receiver.PlayerActionDisposer;
import jrfeng.simplemusic.receiver.PlayerActionReceiver;

public class ControllerPresenter implements ControllerContract.Presenter, PlayerActionDisposer {
    private Context mContext;
    private ControllerContract.View mView;
    private MusicPlayerClient mClient;
    private PlayerActionReceiver mPlayerActionReceiver;

    private MusicPlayerClient.PlayerProgressListener mProgressListener;

    public ControllerPresenter(Context context, ControllerContract.View view) {
        mContext = context;
        mView = view;
        mClient = MusicPlayerClient.getInstance();
        mProgressListener = new MusicPlayerClient.PlayerProgressListener() {
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
                    MusicStorage.MUSIC_LIST_ALL_MUSIC,
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
    public void openPlayingMusicGroup() {
        if (mClient.isPlayingTempMusic()) {
            TempPlayDialog.show(mContext);
        } else {
            PlayingMusicGroupDialog.show(mContext, mClient.getPlayingMusicGroupType(),
                    mClient.getPlayingMusicGroupName(), mClient.getPlayingMusicIndex());
        }
    }

    @Override
    public void onPlay() {
        mView.viewPlay();
    }

    @Override
    public void onPause() {
        mView.viewPause();
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
        mView.viewPause();
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

    @Override
    public void onReset() {
        mView.reset();
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
            songName = music.getName();
            artist = music.getArtist();
            songProgress = mClient.getMusicProgress();
            songLength = mClient.getMusicLength();
            image = MP3Util.getMp3Image(new File(music.getPath()));
            isPLaying = mClient.isPlaying();
        }
        mView.refreshViews(songName, artist, songProgress, songLength, image, isPLaying);
        if (mClient.isPlayingTempMusic()) {
            mView.showTempPlayMark();
        } else {
            mView.hideTempPlayMark();
        }
    }

    private boolean isMusicFileExist() {
        Music music = mClient.getPlayingMusic();
        return music != null && (new File(music.getPath())).exists();
    }
}
