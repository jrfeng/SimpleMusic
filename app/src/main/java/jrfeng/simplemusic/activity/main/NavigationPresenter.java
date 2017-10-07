package jrfeng.simplemusic.activity.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.Collections;
import java.util.List;

import jrfeng.musicplayer.data.Music;
import jrfeng.musicplayer.mode.MusicStorage;
import jrfeng.musicplayer.player.MusicPlayerClient;
import jrfeng.simplemusic.utils.sort.MusicComparator;


public class NavigationPresenter extends BroadcastReceiver implements NavigationContract.Presenter {
    private Context mContext;
    private NavigationContract.View mView;
    private MusicPlayerClient mClient;
    private MusicStorage mMusicStorage;
    private MusicPlayerClient.MusicProgressListener mProgressListener;

    public NavigationPresenter(Context context, NavigationContract.View view) {
        mContext = context;
        mView = view;
        mClient = MusicPlayerClient.getInstance();
        mMusicStorage = mClient.getMusicStorage();
        mProgressListener = new MusicPlayerClient.MusicProgressListener() {
            @Override
            public void onProgressUpdated(int progress) {
                //由 Timer 调用，不需要 Handler
                mView.refreshPlayingProgress(progress);
            }
        };
    }

    //*********************private*******************

    private void registerActionReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_PLAY);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_PAUSE);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_NEXT);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_PREVIOUS);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_STOP);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_PREPARED);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_ERROR);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_MUSIC_NOT_EXIST);
        mContext.registerReceiver(this, intentFilter);
    }

    //*********************Override******************

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!mClient.isPrepared()) {
            return;
        }
        //根据 Action 对 View 进行控制
        switch (intent.getAction()) {
            case MusicPlayerClient.Action.ACTION_PLAY:
                mView.viewPlay();
                break;
            case MusicPlayerClient.Action.ACTION_PAUSE:
                mView.viewPause();
                break;
            case MusicPlayerClient.Action.ACTION_NEXT:
                mView.refreshRecentPlay(mClient.getRecentPlayCount());
                break;
            case MusicPlayerClient.Action.ACTION_PREVIOUS:
                mView.refreshRecentPlay(mMusicStorage.getRecentPlayCount());
                break;
            case MusicPlayerClient.Action.ACTION_STOP:
                mView.viewPause();
                break;
            case MusicPlayerClient.Action.ACTION_PREPARED:
                mView.refreshPlayerView(mClient.getPlayingMusic());
                break;
            case MusicPlayerClient.Action.ACTION_ERROR:
                mView.viewPause();
                break;
            case MusicPlayerClient.Action.ACTION_MUSIC_NOT_EXIST:
                mView.viewPause();
                mView.disableListItem(mClient.getPlayingMusicIndex());
                break;
        }
    }

    @Override
    public void begin() {
        registerActionReceiver();
        mClient.addMusicProgressListener(mProgressListener);
    }

    @Override
    public void end() {
        mContext.unregisterReceiver(this);
        mClient.removeMusicProgressListener(mProgressListener);
    }

    @Override
    public void play() {
        mClient.play();
    }

    @Override
    public void pause() {
        mClient.pause();
    }

    @Override
    public void next() {
        mClient.next();
    }

    @Override
    public void previous() {
        mClient.previous();
    }

    @Override
    public void playMusicGroup(MusicStorage.GroupType groupType, String groupName, int position) {
        mClient.playMusicGroup(groupType, groupName, position);
    }

    @Override
    public List<Music> getGroupAllMusic() {
        return mClient.getMusicList();
    }

    @Override
    public String getGroupName() {
        return mClient.getMusicGroupName();
    }

    @Override
    public int getILoveCount() {
        return mMusicStorage.getILoveCount();
    }

    @Override
    public int getMusicListCount() {
        return mMusicStorage.getMusicListCount();
    }

    @Override
    public int getAlbumCount() {
        return mMusicStorage.getAlbumCount();
    }

    @Override
    public int getArtistCount() {
        return mMusicStorage.getArtistCount();
    }

    @Override
    public int getRecentPlayCount() {
        return mMusicStorage.getRecentPlayCount();
    }

    @Override
    public int getAllMusicCount() {
        return mMusicStorage.getAllMusicCount();
    }

    @Override
    public List<Music> getAllMusic() {
        return mMusicStorage.getAllMusic();
    }

    @Override
    public void sortByName() {
        Collections.sort(mMusicStorage.getAllMusic(), MusicComparator.BY_NAME);
        mView.refreshMusicListView();
    }

    @Override
    public void sortByNameReverse() {
        Collections.sort(mMusicStorage.getAllMusic(), MusicComparator.BY_NAME_REVERSE);
        mView.refreshMusicListView();
    }
}
