package jrfeng.simplemusic.activity.main;

import android.content.Intent;

import java.util.List;

import jrfeng.simplemusic.base.BasePresenter;
import jrfeng.simplemusic.base.BaseView;
import jrfeng.musicplayer.data.Music;

public interface NavigationContract {
    interface View extends BaseView<Presenter> {
        void toggleToPlay();

        void toggleToPause();

        void setProgressMax(int max);

        void setProgress(int progress);

        void setCtlSongName(String songName);

        void setCtlArtist(String artist);

        void setCtlImage(byte[] imageData);

        void setILoveMenuDesc(String desc);

        void setMusicListMenuDesc(String desc);

        void setAlbumMenuDesc(String desc);

        void setArtistMenuDesc(String desc);

        void setRecentPlayMenuDesc(String desc);

        void updateAllMusicList();

        void updateAllMusicListTitle();

        void showSortMenu();

        void showTitleMenu();

        void showListItemMenu(Music music);

        void setChoice(int itemPosition);

        void scrollTo(int position);
    }

    interface Presenter extends BasePresenter {
        void onMenuClicked(Intent intent);

        void onPlayPauseClicked();

        void onNextClicked();

        void onCtlMenuClicked();

        void onSeekBarStartSeeking();

        void onSeekBarStopSeeking(int progress);

        void onMenuItemCreated(int which);

        void onListItemClicked(String listName, int position);

        void onListItemMenuClicked(int position);

        void onTitleSortButtonClicked();

        void onTitleMenuButtonClicked();

        void onTitleLocateButtonClicked();

        List<Music> getAllMusicList();

        int getAllMusicListSize();
    }
}
