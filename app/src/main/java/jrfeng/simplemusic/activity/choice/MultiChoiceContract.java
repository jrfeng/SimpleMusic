package jrfeng.simplemusic.activity.choice;

import java.util.List;

import jrfeng.player.data.Music;
import jrfeng.simplemusic.base.BasePresenter;
import jrfeng.simplemusic.base.BaseView;

public interface MultiChoiceContract {
    interface View extends BaseView<Presenter> {
        void close();
    }

    interface Presenter extends BasePresenter {
        List<Music> getMusicGroup();

        void addTempPlayMusics(List<Music> musics);

        void addMusicsToILove(List<Music> musics);

        void addMusicsToMusicList(List<Music> musics, String name);

        void removeMusics(List<Music> musics);

        void removeMusicsFromAllMusic(List<Music> musics);

        void deleteMusics(List<Music> musics);

        List<String> getMusicListNames();

        List<Integer> getMusicListsSize();

        void createNewMusicList(String listName, List<Music> addMusics);
    }
}
