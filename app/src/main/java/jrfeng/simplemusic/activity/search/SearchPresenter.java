package jrfeng.simplemusic.activity.search;

import java.util.LinkedList;
import java.util.List;

import jrfeng.player.data.Music;
import jrfeng.player.mode.MusicStorage;
import jrfeng.player.player.MusicPlayerClient;

public class SearchPresenter implements SearchContract.Presenter {
    private SearchContract.View mView;
    private MusicStorage.GroupType mGroupType;
    private String mGroupName;

    private MusicPlayerClient mClient;
    private List<Music> mMusicGroup;

    private List<Music> mSearchResult;

    public SearchPresenter(SearchContract.View view,
                           MusicStorage.GroupType groupType,
                           String groupName) {
        mView = view;
        mGroupType = groupType;
        mGroupName = groupName;

        mClient = MusicPlayerClient.getInstance();
        mMusicGroup = mClient.getMusicStorage().getMusicGroup(mGroupType, mGroupName);

        mSearchResult = new LinkedList<>();
    }

    @Override
    public void begin() {
        //什么也不做
    }

    @Override
    public void end() {
        //什么也不做
    }

    @Override
    public void search(String key) {
        mSearchResult.clear();
        if (!key.equals("")) {
            for (Music music : mMusicGroup) {
                if (music.getName().contains(key)
                        || music.getArtist().contains(key)) {
                    mSearchResult.add(music);
                }
            }
        }
        mView.updateSearchResult(mSearchResult);
    }

    @Override
    public void play(Music music) {
        mClient.loadMusicGroup(mGroupType, mGroupName, mMusicGroup.indexOf(music), true);
        mView.close();
    }
}
