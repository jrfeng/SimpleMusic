package jrfeng.simplemusic.activity.main.list;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import jrfeng.player.data.Music;
import jrfeng.player.mode.MusicStorage;
import jrfeng.player.player.MusicPlayerClient;
import jrfeng.simplemusic.receiver.PlayerActionDisposerAdapter;
import jrfeng.simplemusic.receiver.PlayerActionReceiver;

public class MusicListPresenter extends PlayerActionDisposerAdapter implements MusicListContract.Presenter {
    private static final String TAG = "MusicListPresenter";
    private MusicListContract.View mView;
    private Context mContext;

    private MusicStorage.GroupType mGroupType;
    private String mGroupName;

    private MusicPlayerClient mClient;
    private PlayerActionReceiver mActionReceiver;
    private MusicStorage mMusicStorage;
    private List<Music> mMusicGroup;
    private MusicStorage.OnMusicGroupChangListener mMusicGroupChangeListener;

    public MusicListPresenter(Context context,
                              MusicListContract.View view,
                              final MusicStorage.GroupType groupType,
                              final String groupName) {
        mView = view;
        mContext = context;
        mGroupType = groupType;
        mGroupName = groupName;
        mActionReceiver = new PlayerActionReceiver(context, this);
        mClient = MusicPlayerClient.getInstance();
        mMusicStorage = mClient.getMusicStorage();
        mMusicGroup = mMusicStorage.getMusicGroup(groupType, groupName);
        mMusicGroupChangeListener = new MusicStorage.OnMusicGroupChangListener() {
            @Override
            public void onMusicGroupChanged(MusicStorage.GroupType groupType, String groupName, MusicStorage.GroupAction action) {
                if (groupType != mGroupType || !groupName.equals(mGroupName)) {
                    return;
                }

                refreshViews();

                if (mMusicGroup.size() < 1) {
                    mMusicStorage.removeMusicGroupChangeListener(mMusicGroupChangeListener);
                    mView.close();
                }
            }
        };
    }

    @Override
    public void begin() {
        refreshViews();
        mActionReceiver.register();
        mMusicStorage.addMusicGroupChangeListener(mMusicGroupChangeListener);
    }

    @Override
    public void end() {
        mActionReceiver.unregister();
        mMusicStorage.removeMusicGroupChangeListener(mMusicGroupChangeListener);
    }

    @Override
    public void onPlay() {
        mView.refreshMusicList();
        if (isPlayingCurrentMusicGroup()) {
            mView.refreshPlayingMusicPosition(mClient.getPlayingMusicIndex());
        }

//        int i = mMusicGroup.indexOf(mClient.getPlayingMusic());
//        if (i != -1) {
//            mView.musicListScrollTo(i);
//        }
    }

    private void refreshViews() {
        //测试
        log("刷新 View : " + mGroupName);

        mView.refreshTitle();
        mView.refreshPlayMode();
        mView.refreshMusicList();
        if (isPlayingCurrentMusicGroup()) {
            mView.refreshPlayingMusicPosition(mClient.getPlayingMusicIndex());
        }
    }

    //************调试用**************

    private static void log(String msg) {
        Log.d(TAG, msg);
    }

    //*************************Presenter*********************

    @Override
    public int getMusicGroupSize() {
        return mMusicGroup.size();
    }

    @Override
    public void setPlayMode(MusicPlayerClient.PlayMode playMode) {
        mClient.setPlayMode(playMode);
        mView.refreshPlayMode();
    }

    @Override
    public MusicPlayerClient.PlayMode getPlayMode() {
        return mClient.getPlayMode();
    }

    @Override
    public int getPlayingMusicPosition() {
        return mMusicGroup.indexOf(mClient.getPlayingMusic());
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
    public boolean tempListIsEmpty() {
        return mClient.tempListIsEmpty();
    }

    @Override
    public void clearTempList() {
        mClient.clearTempList();
        Toast.makeText(mContext, "临时列表 已清空", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void clearRecentPlayRecord() {
        mMusicStorage.clearRecentPlay();
        Toast.makeText(mContext, "已清空", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean isPlayingCurrentMusicGroup() {
        return mClient.getMusicGroupType() == mGroupType
                && mClient.getMusicGroupName().equals(mGroupName);
    }
}
