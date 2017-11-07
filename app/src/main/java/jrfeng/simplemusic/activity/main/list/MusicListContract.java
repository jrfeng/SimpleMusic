package jrfeng.simplemusic.activity.main.list;

import java.util.List;

import jrfeng.player.data.Music;
import jrfeng.player.player.MusicPlayerClient;
import jrfeng.simplemusic.base.BasePresenter;
import jrfeng.simplemusic.base.BaseView;

public interface MusicListContract {
    interface View extends BaseView<Presenter> {
        void refreshMusicList();

        void refreshPlayingMusicPosition(int position);

        void refreshPlayMode();

        void refreshTitle();

        void musicListScrollTo(int position);

        void close();
    }

    interface Presenter extends BasePresenter {
        int getMusicGroupSize();

        void setPlayMode(MusicPlayerClient.PlayMode playMode);

        MusicPlayerClient.PlayMode getPlayMode();

        int getPlayingMusicPosition();

        List<String> getTempListMusicNames();

        List<Music> getTempList();

        boolean tempListIsEmpty();

        void clearTempList();

        void clearRecentPlayRecord();

        boolean isPlayingCurrentMusicGroup();
    }
}
