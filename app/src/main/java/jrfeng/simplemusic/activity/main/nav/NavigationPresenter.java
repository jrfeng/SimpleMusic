package jrfeng.simplemusic.activity.main.nav;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import jrfeng.player.data.Music;
import jrfeng.player.mode.MusicStorage;
import jrfeng.player.player.MusicPlayerClient;
import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.activity.main.listnav.MusicListNavFragment;
import jrfeng.simplemusic.activity.main.list.MusicListFragment;
import jrfeng.simplemusic.activity.scan.ScanActivity;
import jrfeng.simplemusic.receiver.PlayerActionDisposerAdapter;
import jrfeng.simplemusic.receiver.PlayerActionReceiver;

public class NavigationPresenter extends PlayerActionDisposerAdapter implements NavigationContract.Presenter {
    private static final String TAG = "NavigationPresenter";
    private NavigationContract.View mView;
    private Context mContext;
    private MusicStorage mMusicStorage;
    private MusicPlayerClient mClient;

    private PlayerActionReceiver mPlayerActionReceiver;

    private MusicStorage.OnMusicGroupChangListener mMusicGroupChangeListener;

    public NavigationPresenter(Context context, NavigationContract.View view) {
        mContext = context;
        mView = view;
        mMusicStorage = MusicPlayerClient.getInstance().getMusicStorage();
        mClient = MusicPlayerClient.getInstance();
        mPlayerActionReceiver = new PlayerActionReceiver(context, this);

        mMusicGroupChangeListener = new MusicStorage.OnMusicGroupChangListener() {
            @Override
            public void onMusicGroupChanged(MusicStorage.GroupType groupType, String groupName, MusicStorage.GroupAction action) {
                mView.refreshAllView();
            }
        };
    }

    @Override
    public void begin() {
        mPlayerActionReceiver.register();
        mMusicStorage.addMusicGroupChangeListener(mMusicGroupChangeListener);
        mView.refreshAllView();
    }

    @Override
    public void end() {
        mPlayerActionReceiver.unregister();
        mMusicStorage.removeMusicGroupChangeListener(mMusicGroupChangeListener);
    }

    @Override
    public void onNavMenuItemSelected(int which) {
        switch (which) {
            case 0:
                openMusicList(MusicStorage.GroupType.MUSIC_LIST, MusicStorage.MUSIC_LIST_I_LOVE);
                break;
            case 1:
                openMusicGroup(MusicStorage.GroupType.MUSIC_LIST);
                break;
            case 2:
                openMusicGroup(MusicStorage.GroupType.ALBUM_LIST);
                break;
            case 3:
                openMusicGroup(MusicStorage.GroupType.ARTIST_LIST);
                break;
            case 4:
                openMusicList(MusicStorage.GroupType.MUSIC_LIST, MusicStorage.MUSIC_LIST_RECENT_PLAY);
                break;
            case 5:
                mContext.startActivity(new Intent(mContext, ScanActivity.class));
                break;
        }
    }

    @Override
    public void onMusicListTitleMenuSelected(View view, int which) {
        switch (which) {
            case 0:
                mView.showPlayModeMenu(view);
                break;
            case 1:
                int position = getPlayingMusicPosition();
                mView.musicListScrollTo(position);
                break;
            case 2:
                mView.showMore_Menu(view);
                break;
        }
    }

    @Override
    public int getILoveCount() {
        return mMusicStorage.getILoveSize();
    }

    @Override
    public int getMusicListCount() {
        return mMusicStorage.getMusicListSize();
    }

    @Override
    public int getAlbumCount() {
        return mMusicStorage.getAlbumSize();
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
        return mMusicStorage.size();
    }

    @Override
    public MusicPlayerClient.PlayMode getPlayMode() {
        return mClient.getPlayMode();
    }

    @Override
    public void setPlayMode(MusicPlayerClient.PlayMode mode) {
        mClient.setPlayMode(mode);
        mView.setViewPlayMode(mode);
    }

    @Override
    public List<String> getTempListMusicNames() {
        return mClient.getTempListMusicNames();
    }

    @Override
    public List<Music> getTempList() {
        return mClient.getTempList();
    }

    @Override
    public void clearTempList() {
        mClient.clearTempList();
        Toast.makeText(mContext, "临时列表 已清空", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean tempListIsEmpty() {
        return mClient.tempListIsEmpty();
    }

    @Override
    public void playTempMusic(int position) {
        mClient.playTempMusic(position, true);
    }

    @Override
    public void onPlay() {
        mView.refreshRecentPlayCount();
        mView.refreshMusicList();
        if (isCurrentMusicGroup()) {
            log("onPlay");
            mView.refreshPlayingMusicPosition(mClient.getPlayingMusicIndex());
            mView.musicListScrollTo(getPlayingMusicPosition());
        }
    }

    private boolean isCurrentMusicGroup() {
        return mClient.getPlayingMusicGroupType() == MusicStorage.GroupType.MUSIC_LIST
                && mClient.getPlayingMusicGroupName().equals(MusicStorage.MUSIC_LIST_ALL_MUSIC);
    }

    private int getPlayingMusicPosition() {
        return mMusicStorage.getAllMusic().indexOf(mClient.getPlayingMusic());
    }

    private void openMusicList(MusicStorage.GroupType groupType, String groupName) {
        Fragment fragment = new MusicListFragment();
        Bundle args = new Bundle();
        args.putString(MusicListFragment.KEY_GROUP_TYPE, groupType.name());
        args.putString(MusicListFragment.KEY_GROUP_NAME, groupName);
        fragment.setArguments(args);
        mView.startFragment(fragment);
    }

    private void openMusicGroup(MusicStorage.GroupType groupType) {
        Fragment fragment = new MusicListNavFragment();
        Bundle args = new Bundle();
        args.putString(MusicListNavFragment.KEY_GROUP_TYPE, groupType.name());
        fragment.setArguments(args);
        mView.startFragment(fragment);
    }

    //******************调试用********************

    private static void log(String msg) {
        if (MyApplication.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
