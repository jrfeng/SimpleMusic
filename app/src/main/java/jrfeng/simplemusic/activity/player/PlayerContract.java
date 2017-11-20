package jrfeng.simplemusic.activity.player;

import android.graphics.Bitmap;

import jrfeng.player.player.MusicPlayerClient;
import jrfeng.simplemusic.base.BasePresenter;
import jrfeng.simplemusic.base.BaseView;

public interface PlayerContract {

    interface View extends BaseView<Presenter> {
        void setSongName(String name);

        void setSongArtist(String artist);

        void setSongImage(byte[] bitmap);

        void setSongProgress(int progress);

        void setSongProgressLength(int length);

        void viewStart();

        void viewPause();

        void viewStop();

        void love(boolean love);
    }

    interface Presenter extends BasePresenter {
        int getAudioSessionId();

        void musicSeekTo(int progress);

        void playerNext();

        void playerPrevious();

        void playerPlayPause();

        void loveOrNotLovePlayingMusic();

        void setPlayMode(MusicPlayerClient.PlayMode mode);

        MusicPlayerClient.PlayMode getPlayMode();
    }
}
