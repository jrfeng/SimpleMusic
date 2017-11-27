package jrfeng.player.player;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

import jrfeng.player.data.Music;
import jrfeng.player.mode.MusicStorage;
import jrfeng.player.mode.MusicStorageImp;

public class MusicPlayerClient implements ServiceConnection, MusicPlayerController {
    private static final String TAG = "MusicPlayerClient";

    private static MusicPlayerClient mInstance;
    private MusicPlayerController mController;
    private boolean isConnect;
    private OnConnectedListener mConnectedListener;
    private MusicStorage mMusicStorage;

    private MusicPlayerClient() {
    }

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
                mMusicStorage = new MusicStorageImp(context);
                mMusicStorage.restore();
                context.bindService(new Intent(context, MusicPlayerService.class),
                        MusicPlayerClient.this,
                        Context.BIND_AUTO_CREATE);
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
        ((MustInitialize) mController).init(mMusicStorage);
        if (mConnectedListener != null) {
            mConnectedListener.onConnected();
            mConnectedListener = null;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        isConnect = false;
        Log.e(TAG, "Client : **************Warning***************");
        Log.e(TAG, "Client : onServiceDisconnected: 非正常断开连接");
        Log.e(TAG, "Client : ************************************");
    }

    @Override
    public int getAudioSessionId() {
        return mController.getAudioSessionId();
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
    public void nextButNotPlay() {
        mController.nextButNotPlay();
    }

    @Override
    public void loadMusicGroup(MusicStorage.GroupType groupType, String groupName, int position, boolean play) {
        mController.loadMusicGroup(groupType, groupName, position, play);
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
    public void playPause() {
        mController.playPause();
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
    public MusicStorage.GroupType getMusicGroupType() {
        return mController.getMusicGroupType();
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
    public boolean setPlayMode(PlayMode mode) {
        return mController.setPlayMode(mode);
    }

    @Override
    public PlayMode getPlayMode() {
        return mController.getPlayMode();
    }

    @Override
    public void addTempPlayMusic(Music music) {
        mController.addTempPlayMusic(music);
    }

    @Override
    public void addTempPlayMusics(List<Music> musics) {
        mController.addTempPlayMusics(musics);
    }

    @Override
    public boolean isPlayingTempMusic() {
        return mController.isPlayingTempMusic();
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
    public void addMusicProgressListener(PlayerProgressListener listener) {
        mController.addMusicProgressListener(listener);
    }

    @Override
    public void removeMusicProgressListener(PlayerProgressListener listener) {
        mController.removeMusicProgressListener(listener);
    }

    @Override
    public List<Music> getTempList() {
        return mController.getTempList();
    }

    @Override
    public List<String> getTempListMusicNames() {
        return mController.getTempListMusicNames();
    }

    @Override
    public void clearTempList() {
        mController.clearTempList();
    }

    @Override
    public boolean tempListIsEmpty() {
        return mController.tempListIsEmpty();
    }

    @Override
    public void playTempMusic(int position, boolean autoPlay) {
        mController.playTempMusic(position, autoPlay);
    }

    @Override
    public void shutdown(Context context) {
        disconnect(context);
        mController.shutdown(context);
        mInstance = null;
    }

    //***********************private********************

    private void disconnect(Context context) {
        isConnect = false;
        context.unbindService(this);
    }

    //**********************listener********************

    /**
     * MusicPlayClient 连接状态监听器。
     */
    public interface OnConnectedListener {
        /**
         * 如果连接成功，该方法会被调用。
         */
        void onConnected();
    }

    /**
     * 播放器播放进度监听器。
     */
    public interface PlayerProgressListener {
        /**
         * 当正在播放音乐时，该方法会被调用。
         *
         * @param progress 播放器播放进度。
         */
        void onProgressUpdated(int progress);
    }

    /**
     * 通知栏 View 接口。
     * 已经提供了一个默认的实现 {@link DefaultNotifyControllerView}。
     */
    public interface NotifyControllerView {
        Notification getNotification(Context context, int notifyId);

        void play();

        void pause();

        void updateText(String songName, String artist);

        void setNotifyIcon(Bitmap bitmap);

        void setNotifyIcon(int resId);

        void showTempPlayMark();

        void hideTempPlayMark();
    }

    //**********************静态成员类****************

    /**
     * 播放器动作。
     */
    public static class Action {
        public static final String ACTION_PLAY = "jrfeng.simplemusic.action.PLAY";
        public static final String ACTION_PAUSE = "jrfeng.simplemusic.action.PAUSE";
        public static final String ACTION_NEXT = "jrfeng.simplemusic.action.NEXT";
        public static final String ACTION_PREVIOUS = "jrfeng.simplemusic.action.PREVIOUS";
        public static final String ACTION_STOP = "jrfeng.simplemusic.action.STOP";
        public static final String ACTION_PREPARED = "jrfeng.simplemusic.action.PREPARED";
        public static final String ACTION_ERROR = "jrfeng.simplemusic.action.ERROR";
        public static final String ACTION_MUSIC_NOT_EXIST = "jrfeng.simplemusic.action.MUSIC_NOT_EXIST";
        public static final String ACTION_RESET = "jrfeng.simplemusic.action.RESET_PLAYER";
    }

    /**
     * 播放器播放模式。
     */
    public enum PlayMode {
        /**
         * 顺序模式。
         */
        MODE_ORDER,
        /**
         * 循环模式。
         */
        MODE_LOOP,
        /**
         * 随机模式。
         */
        MODE_RANDOM
    }
}