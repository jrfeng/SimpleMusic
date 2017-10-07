package jrfeng.simplemusic.activity.main;

import java.util.List;

import jrfeng.musicplayer.mode.MusicStorage;
import jrfeng.simplemusic.base.BasePresenter;
import jrfeng.simplemusic.base.BaseView;
import jrfeng.musicplayer.data.Music;

public interface NavigationContract {
    interface View extends BaseView<Presenter> {
        void viewPause();

        void viewPlay();

        void refreshRecentPlay(int count);

        void disableListItem(int position);

        void refreshPlayerView(Music music);

        void refreshPlayingProgress(int progress);

        void refreshMusicListView();
    }

    interface Presenter extends BasePresenter {
        void play();

        void pause();

        void next();

        void previous();

        void playMusicGroup(MusicStorage.GroupType groupType, String groupName, int position);

        List<Music> getGroupAllMusic();

        String getGroupName();

        int getILoveCount();

        int getMusicListCount();

        int getAlbumCount();

        int getArtistCount();

        int getRecentPlayCount();

        int getAllMusicCount();

        List<Music> getAllMusic();

        void sortByName();

        void sortByNameReverse();
    }
}
