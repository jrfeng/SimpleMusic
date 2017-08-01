package jrfeng.simplemusic.service.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.data.Music;

public class PlayerClient implements ServiceConnection, PlayerController {
    private PlayerService.Controller mController;
    private Context mContext;
    private boolean isConnect;
    private OnConnectedListener mConnectedListener;

    public PlayerClient(Context context) {
        mContext = context;
    }

    public void connect() {
        connect(null);
    }

    public void connect(OnConnectedListener listener) {
        mConnectedListener = listener;
        mContext.bindService(new Intent(mContext, PlayerService.class), this, Context.BIND_AUTO_CREATE);
    }

    public void disconnect() {
        isConnect = false;
        mContext.unbindService(this);
    }

    public boolean isConnect() {
        return isConnect;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        isConnect = true;
        mController = (PlayerService.Controller) iBinder;
        if (mConnectedListener != null) {
            mConnectedListener.onConnected();
            mConnectedListener = null;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    }

    @Override
    public void previous() {
        mController.previous();
    }

    @Override
    public void next() {
        mController.next();
    }

    @Override
    public void play() {
        mController.play();
    }

    @Override
    public void play(int position) {
        mController.play(position);
    }

    @Override
    public void play(String listName, int position) {
        mController.play(listName, position);
    }

    @Override
    public void pause() {
        mController.pause();
    }

    @Override
    public void play_pause() {
        mController.play_pause();
    }

    @Override
    public void stop() {
        mController.stop();
    }

    @Override
    public boolean isPlaying() {
        return mController.isPlaying();
    }

    @Override
    public boolean isLooping() {
        return mController.isLooping();
    }

    @Override
    public Music getPlayingMusic() {
        return mController.getPlayingMusic();
    }

    @Override
    public List<Music> getMusicList() {
        return mController.getMusicList();
    }

    @Override
    public String getCurrentListName() {
        return mController.getCurrentListName();
    }

    @Override
    public void clearRecentPlayList() {
        mController.clearRecentPlayList();
    }

    @Override
    public boolean setLooping(boolean looping) {
        return mController.setLooping(looping);
    }

    @Override
    public void seekTo(int msec) {
        mController.seekTo(msec);
    }

    @Override
    public int getDuration() {
        return mController.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mController.getCurrentPosition();
    }

    @Override
    public void reload() {
        mController.reload();
    }

    @Override
    public void shutdown() {
        disconnect();
        mController.shutdown();
    }

    //**********************listener********************

    public interface OnConnectedListener {
        void onConnected();
    }
}