package jrfeng.simplemusic.activity.main.listnav;

import android.support.v4.app.Fragment;
import android.widget.ImageView;

import java.util.List;

import jrfeng.player.mode.MusicStorage;
import jrfeng.player.player.MusicPlayerClient;
import jrfeng.simplemusic.base.BasePresenter;
import jrfeng.simplemusic.base.BaseView;

public interface MusicListNavContract {

    interface View extends BaseView<Presenter> {
        void refreshActionBarTitle();

        void refreshGroupList();

        void refreshPlayMode();

        void startFragment(Fragment fragment);
    }

    interface Presenter extends BasePresenter {
        void setPlayMode(MusicPlayerClient.PlayMode playMode);

        MusicPlayerClient.PlayMode getPlayMode();

        void createNewMusicList(String listName);

        int getMusicListCount();

        int getAlbumCount();

        int getArtistCount();

        List<String> getGroupNames();

        int getGroupSize(MusicStorage.GroupType groupType, String groupName);

        void setGroupIcon(String groupName, ImageView iconView);

        void openMusicList(MusicStorage.GroupType groupType, String groupName);

        void deleteMusicList(String listName);
    }
}
