package jrfeng.simplemusic.activity.main.nav;

import android.support.v4.app.Fragment;

import java.util.List;

import jrfeng.player.data.Music;
import jrfeng.player.player.MusicPlayerClient;
import jrfeng.simplemusic.base.BasePresenter;
import jrfeng.simplemusic.base.BaseView;

public interface NavigationContract {
    interface View extends BaseView<Presenter> {

        void refreshMenusDescribe();

        void refreshRecentPlayCount();

        void refreshMusicListTitle();

        void refreshMusicList();

        void refreshAllView();

        void refreshPlayingMusicPosition(int position);

        void setViewPlayMode(MusicPlayerClient.PlayMode mode);

        void musicListScrollTo(int position);

        void showPlayModeMenu(android.view.View anchorView);

        void showMore_Menu(android.view.View anchorView);

        void startFragment(Fragment fragment);
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

        List<String> getTempListMusicNames();

        List<Music> getTempList();

        void clearTempList();

        boolean tempListIsEmpty();

        void playTempMusic(int position);
    }
}
