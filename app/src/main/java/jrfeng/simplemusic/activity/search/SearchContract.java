package jrfeng.simplemusic.activity.search;

import java.util.List;

import jrfeng.player.data.Music;
import jrfeng.simplemusic.base.BasePresenter;
import jrfeng.simplemusic.base.BaseView;

public interface SearchContract {

    interface View extends BaseView<Presenter> {
        void updateSearchResult(List<Music> result);

        void close();
    }

    interface Presenter extends BasePresenter {
        void search(String key);

        void play(Music music);
    }
}
