package jrfeng.simplemusic.activity.main.nav;

import java.util.List;

import jrfeng.musicplayer.data.Music;
import jrfeng.musicplayer.player.MusicPlayerClient;
import jrfeng.simplemusic.base.BasePresenter;
import jrfeng.simplemusic.base.BaseView;

public interface NavigationContract {
    interface View extends BaseView<Presenter> {
        void refreshMenusDescribe();

        void refreshRecentPlayCount();

        void refreshMusicListTitle();

        void refreshMusicList();

        void refreshPlayingMusicPosition(int position);

        void setViewPlayMode(MusicPlayerClient.PlayMode mode);

        void musicListScrollTo(int position);

        void backTop();

        void showPlayModeMenu(android.view.View anchorView);
    }

    interface Presenter extends BasePresenter {
        void onNavMenuItemSelected(int which);

        void onMusicListTitleMenuSelected(android.view.View view, int which);

        int getILoveCount();

        int getMusicListCount();

        int getAlbumCount();

        int getArtistCount();

        int getRecentPlayCount();

        int getAllMusicCount();

        MusicPlayerClient.PlayMode getPlayMode();

        void setPlayMode(MusicPlayerClient.PlayMode mode);

        List<Music> getAllMusic();

        void onMusicListItemClicked(int position);

        int getPlayingMusicPosition();

        void addTempPlayMusic(Music music);

        boolean isTempPlay();
    }
}
