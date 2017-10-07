package jrfeng.musicplayer.player;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

import jrfeng.musicplayer.data.Music;
import jrfeng.musicplayer.mode.MusicStorage;

public class MusicPlayerClient implements ServiceConnection, MusicPlayerController {
    private static final String TAG = "MusicPlayerClient";

    private static MusicPlayerClient mInstance;
    private MusicPlayerService.Controller mController;
    private boolean isConnect;
    private OnConnectedListener mConnectedListener;
    private MusicStorage mMusicStorage;

    public void connect(final Context context, OnConnectedListener listener) {
        //避免重复连接
        if (isConnect()) {
            return;
        }

        //解析配置文件
        try {
            Configure.decode(context.getApplicationContext());
        } catch (XmlPullParserException e) {
            Log.e(TAG, "XmlPullParserException : " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "IOException : " + e.toString());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException : " + e.toString());
        }
        mConnectedListener = listener;

        new Thread() {
            @Override
            public void run() {
                try {
                    initMusicStorage(context);
                    context.bindService(new Intent(context, MusicPlayerService.class),
                            MusicPlayerClient.this,
                            Context.BIND_AUTO_CREATE);
                } catch (IllegalAccessException e) {
                    System.err.println(e.toString());
                } catch (InstantiationException e) {
                    System.err.println(e.toString());
                }
            }
        }.start();
    }

    public static synchronized MusicPlayerClient getInstance() {
        if (mInstance == null) {
            mInstance = new MusicPlayerClient();
        }
        return mInstance;
    }

    public boolean isConnect() {
        return isConnect;
    }

    public MusicStorage getMusicStorage() {
        return mMusicStorage;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        isConnect = true;
        mController = (MusicPlayerService.Controller) iBinder;
        mController.init(mMusicStorage);
        if (mConnectedListener != null) {
            mConnectedListener.onConnected();
            mConnectedListener = null;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        isConnect = false;
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
    public void playMusicGroup(MusicStorage.GroupType groupType, String groupName, int position) {
        mController.playMusicGroup(groupType, groupName, position);
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
    public boolean isPrepared() {
        return mController.isPrepared();
    }

    @Override
    public Music getPlayingMusic() {
        return mController.getPlayingMusic();
    }

    @Override
    public int getPlayingMusicIndex() {
        return mController.getPlayingMusicIndex();
    }

    @Override
    public List<Music> getMusicList() {
        return mController.getMusicList();
    }

    @Override
    public String getMusicGroupName() {
        return mController.getMusicGroupName();
    }

    @Override
    public List<Music> getRecentPlayList() {
        return mController.getRecentPlayList();
    }

    @Override
    public int getRecentPlayCount() {
        return mController.getRecentPlayCount();
    }

    @Override
    public boolean setLooping(boolean looping) {
        return mController.setLooping(looping);
    }

    @Override
    public void setRandomPlay(boolean randomPlay) {
        mController.setRandomPlay(randomPlay);
    }

    @Override
    public void addTempMusic(Music music) {
        mController.addTempMusic(music);
    }

    @Override
    public void seekTo(int msec) {
        mController.seekTo(msec);
    }

    @Override
    public int getMusicLength() {
        return mController.getMusicLength();
    }

    @Override
    public int getMusicProgress() {
        return mController.getMusicProgress();
    }

    @Override
    public void addMusicProgressListener(MusicProgressListener listener) {
        mController.addMusicProgressListener(listener);
    }

    @Override
    public void removeMusicProgressListener(MusicProgressListener listener) {
        mController.removeMusicProgressListener(listener);
    }

    @Override
    public void shutdown(Context context) {
        disconnect(context);
        mController.shutdown(context);
    }

    //***********************private********************

    private void initMusicStorage(final Context context) throws IllegalAccessException, InstantiationException {
        //从配置文件解析
        Class cl = Configure.getMusicStorageClass();
        if (cl != null) {
            mMusicStorage = (MusicStorage) cl.newInstance();
            mMusicStorage.restore(context);
        } else {
            throw new NullPointerException("Class object is null. please check your \"music_player.xml\" file.");
        }
    }

    private void disconnect(Context context) {
        isConnect = false;
        context.unbindService(this);
    }

    //**********************listener********************

    public interface OnConnectedListener {
        void onConnected();
    }

    public interface MusicProgressListener {
        void onProgressUpdated(int progress);
    }

    public interface NotifyControllerView {
        Notification getNotification(Context context, int notifyId);

        void play();

        void pause();

        void updateText(String songName, String artist);
    }

    //**********************静态成员类****************

    public static class Action {
        public static final String ACTION_PLAY = "jrfeng.simplemusic.action.PLAY";
        public static final String ACTION_PAUSE = "jrfeng.simplemusic.action.PAUSE";
        public static final String ACTION_NEXT = "jrfeng.simplemusic.action.NEXT";
        public static final String ACTION_PREVIOUS = "jrfeng.simplemusic.action.PREVIOUS";
        public static final String ACTION_STOP = "jrfeng.simplemusic.action.STOP";
        public static final String ACTION_PREPARED = "jrfeng.simplemusic.action.PREPARED";
        public static final String ACTION_ERROR = "jrfeng.simplemusic.action.ERROR";
        public static final String ACTION_SHUTDOWN = "jrfeng.simplemusic.action.SHUTDOWN";
        public static final String ACTION_MUSIC_NOT_EXIST = "jrfeng.simplemusic.action.MUSIC_NOT_EXIST";
    }
}