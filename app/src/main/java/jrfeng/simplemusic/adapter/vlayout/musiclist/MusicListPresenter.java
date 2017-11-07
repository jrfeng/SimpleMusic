package jrfeng.simplemusic.adapter.vlayout.musiclist;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jrfeng.player.data.Music;
import jrfeng.player.mode.MusicStorage;
import jrfeng.player.player.MusicPlayerClient;
import jrfeng.simplemusic.base.BasePresenter;

class MusicListPresenter implements BasePresenter {
    private Context mContext;
    private MusicPlayerClient mClient;
    private MusicStorage mMusicStorage;

    private MusicStorage.GroupType mGroupType;
    private String mGroupName;
    private List<Music> mMusicGroup;
    private List<Music> mILove;

    MusicListPresenter(Context context,
                       MusicStorage.GroupType groupType,
                       String groupName) {
        mContext = context;
        mGroupType = groupType;
        mGroupName = groupName;
        mClient = MusicPlayerClient.getInstance();
        mMusicStorage = mClient.getMusicStorage();
        mMusicGroup = mMusicStorage.getMusicGroup(groupType, groupName);
        mILove = mMusicStorage.getILove();
    }

    @Override
    public void begin() {
        //什么也不做
    }

    @Override
    public void end() {
        //什么也不做
    }

    List<Music> getMusicGroup() {
        return mMusicGroup;
    }

    void playPause(int position) {
        if (playingCurrentMusicGroup() && mClient.getPlayingMusicIndex() == position) {
            mClient.playPause();
        } else {
            mClient.playMusicGroup(mGroupType, mGroupName, position);
        }
    }

    int getPlayingMusicPosition() {
        if (playingCurrentMusicGroup()) {
            return mClient.getPlayingMusicIndex();
        } else {
            return -1;
        }
    }

    void addMusicToTempPlay(Music music) {
        mClient.addTempPlayMusic(music);
    }

    boolean isPlayingTempMusic() {
        return mClient.isPlayingTempMusic();
    }

    void addMusicToILove(Music music) {
        mMusicStorage.addMusicToILove(music);
        Toast.makeText(mContext, "我喜欢 已添加", Toast.LENGTH_SHORT).show();
    }

    void removeMusicFromILove(Music music) {
        boolean result = mMusicStorage.removeMusicFromILove(music);
        if (result) {
            Toast.makeText(mContext, "我喜欢 已移除", Toast.LENGTH_SHORT).show();
        }
    }

    void removeMusicFromCurrentList(Music music) {
        if (mMusicStorage.removeMusicFromMusicList(music, mGroupName)) {
            Toast.makeText(mContext, "移除成功", Toast.LENGTH_SHORT).show();
        }
    }

    void removeMusicFromAllMusic(Music musics) {
        mMusicStorage.removeMusicFromAllMusic(musics);
        Toast.makeText(mContext, "删除成功", Toast.LENGTH_SHORT).show();
    }

    void deleteMusicFile(Music music) {
        if (mMusicStorage.deleteMusicFile(music)) {
            Toast.makeText(mContext, "删除成功", Toast.LENGTH_SHORT).show();
        }
    }

    List<String> getMusicListNames() {
        return mMusicStorage.getMusicListNames();
    }

    List<Integer> getMusicListsSize() {
        List<String> listNames = getMusicListNames();
        List<Integer> listsSize = new ArrayList<>(listNames.size());
        for (String name : listNames) {
            listsSize.add(mMusicStorage.getMusicGroup(MusicStorage.GroupType.MUSIC_LIST, name).size());
        }
        return listsSize;
    }

    void createNewMusicList(String listName) {
        mMusicStorage.createNewMusicList(listName);
    }

    void addMusicToMusicList(Music music, String listName) {
        List<Music> musicList = mMusicStorage.getMusicList(listName);
        if (musicList.contains(music)) {
            Toast.makeText(mContext, "已存在", Toast.LENGTH_SHORT).show();
        } else {
            mMusicStorage.addMusicToMusicList(music, listName);
            Toast.makeText(mContext, "添加到 " + listName + " 成功", Toast.LENGTH_SHORT).show();
        }
    }

    Music getPlayingMusic() {
        return mClient.getPlayingMusic();
    }

    boolean playingCurrentMusicGroup() {
        return mClient.getMusicGroupType() == mGroupType
                && mClient.getMusicGroupName().equals(mGroupName);
    }

    boolean isILove(Music music) {
        return mILove.contains(music);
    }
}
