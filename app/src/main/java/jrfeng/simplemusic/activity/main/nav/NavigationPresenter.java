package jrfeng.simplemusic.activity.main.nav;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jrfeng.musicplayer.data.Music;
import jrfeng.musicplayer.mode.MusicStorage;
import jrfeng.musicplayer.player.MusicPlayerClient;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.scan.ScanActivity;
import jrfeng.simplemusic.receiver.PlayerActionDisposerAdapter;
import jrfeng.simplemusic.receiver.PlayerActionReceiver;
import jrfeng.simplemusic.widget.DropDownMenu;

public class NavigationPresenter extends PlayerActionDisposerAdapter implements NavigationContract.Presenter {
    private NavigationContract.View mView;
    private Context mContext;
    private MusicStorage mMusicStorage;
    private MusicPlayerClient mClient;

    private PlayerActionReceiver mPlayerActionReceiver;

    public NavigationPresenter(Context context, NavigationContract.View view) {
        mContext = context;
        mView = view;
        mMusicStorage = MusicPlayerClient.getInstance().getMusicStorage();
        mClient = MusicPlayerClient.getInstance();
        mPlayerActionReceiver = new PlayerActionReceiver(context, this);
    }

    @Override
    public void begin() {
        mPlayerActionReceiver.register();
        refreshViews();
    }

    @Override
    public void end() {
        mPlayerActionReceiver.unregister();
    }

    @Override
    public void onNavMenuItemSelected(int which) {
        switch (which) {
            case 0:
                //TODO
                Toast.makeText(mContext, "NavMenu : 0", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                //TODO
                Toast.makeText(mContext, "NavMenu : 1", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                //TODO
                Toast.makeText(mContext, "NavMenu : 2", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                //TODO
                Toast.makeText(mContext, "NavMenu : 3", Toast.LENGTH_SHORT).show();
                break;
            case 4:
                //TODO
                Toast.makeText(mContext, "NavMenu : 4", Toast.LENGTH_SHORT).show();
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
                mView.musicListScrollTo(getPlayingMusicPosition());
                break;
            case 2:
                //TODO 显示底部菜单
                Toast.makeText(mContext, "TitleMenu : 2", Toast.LENGTH_SHORT).show();
                break;
        }
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
    public MusicPlayerClient.PlayMode getPlayMode() {
        return mClient.getPlayMode();
    }

    @Override
    public void setPlayMode(MusicPlayerClient.PlayMode mode) {
        mClient.setPlayMode(mode);
        mView.setViewPlayMode(mode);
    }

    @Override
    public List<Music> getAllMusic() {
        return mMusicStorage.getAllMusic();
    }

    @Override
    public void onMusicListItemClicked(int position) {
        if (position == mClient.getPlayingMusicIndex()) {
            mClient.playPause();
        } else {
            mClient.playMusicGroup(MusicStorage.GroupType.MUSIC_LIST,
                    MusicStorage.MUSIC_LIST_ALL,
                    position);
        }
    }

    @Override
    public int getPlayingMusicPosition() {
        if (!isPlayingAllMusicGroup()) {
            return -1;
        }
        return mClient.getPlayingMusicIndex();
    }

    @Override
    public void addTempPlayMusic(Music music) {
        mClient.addTempPlayMusic(music);
    }

    @Override
    public boolean isTempPlay() {
        return isPlayingAllMusicGroup() && mClient.isTempPlay();
    }

    @Override
    public void onPlay() {
        mView.refreshRecentPlayCount();
        if (isPlayingAllMusicGroup()) {
            Log.d("Indicator", "onPlay");
            mView.refreshPlayingMusicPosition(mClient.getPlayingMusicIndex());
            mView.musicListScrollTo(mClient.getPlayingMusicIndex());
        }
    }

    private void refreshViews() {
        mView.refreshMenusDescribe();
        mView.refreshRecentPlayCount();
        mView.refreshMusicListTitle();
        mView.refreshMusicList();
    }

    private boolean isPlayingAllMusicGroup() {
        return mClient.getMusicGroupType() == MusicStorage.GroupType.MUSIC_LIST
                && mClient.getMusicGroupName().equals(MusicStorage.MUSIC_LIST_ALL);
    }
}
