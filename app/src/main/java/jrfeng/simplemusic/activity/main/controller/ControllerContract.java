package jrfeng.simplemusic.activity.main.controller;

import android.support.v4.app.Fragment;

import jrfeng.simplemusic.base.BasePresenter;
import jrfeng.simplemusic.base.BaseView;

public interface ControllerContract {
    interface View extends BaseView<Presenter> {
        void viewPlay();

        void viewPause();

        void viewSeekTo(int position);

        void refreshViews(String songName, String artist, int songProgress, int songLength, byte[] image, boolean isPlaying);

        void notifyPlayError();

        void notifyMusicNotExist();

        void showTempPlayMark();

        void hideTempPlayMark();
    }

    interface Presenter extends BasePresenter {
        void playPause();

        void next();

        void previous();

        void seekTo(int progress);

        void setSeekingState(boolean seeking);

        void openPlayingMusicGroup();
    }
}
