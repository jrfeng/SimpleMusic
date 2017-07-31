package jrfeng.simplemusic.activity.navigation;

import android.content.Intent;
import android.graphics.Bitmap;

import jrfeng.simplemusic.base.BasePresenter;
import jrfeng.simplemusic.base.BaseView;

public interface NavigationContract {
    interface View extends BaseView<Presenter> {
        void toggleToPlay();

        void toggleToPause();

        void setProgressMax(int max);

        void setProgress(int progress);

        void setCtlSongName(String songName);

        void setCtlArtist(String artist);

        void setCtlImage(byte[] imageData);

        void refreshRecentPlayList();

        void setAllMusicMenuDesc(String desc);

        void setILoveMenuDesc(String desc);

        void setMusicListMenuDesc(String desc);

        void setAlbumMenuDesc(String desc);

        void setArtistMenuDesc(String desc);
    }

    interface Presenter extends BasePresenter {
        void onMenuClicked(Intent intent);

        void onPlayPauseClicked();

        void onNextClicked();

        void onCtlMenuClicked();

        void onClearRecentPlayClicked();

        void onSeekBarStartSeeking();

        void onSeekBarStopSeeking(int progress);

        void onMenuItemCreated(int which);
    }
}
