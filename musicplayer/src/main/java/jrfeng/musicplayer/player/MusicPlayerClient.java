package jrfeng.musicplayer.player;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Scanner;

import jrfeng.musicplayer.data.Music;

public class MusicPlayerClient implements ServiceConnection, MusicPlayerController {
    private static MusicPlayerClient mInstance;
    private MusicPlayerService.Controller mController;
    private boolean isConnect;
    private OnConnectedListener mConnectedListener;
    private MusicProvider mMusicProvider;

    public void connect(Context context) {
        connect(context, null);
    }

    public void connect(final Context context, OnConnectedListener listener) {
        mConnectedListener = listener;
        mMusicProvider = initMusicListProvider(context);
        Intent intent = new Intent(context, MusicPlayerService.class);
        context.bindService(intent, MusicPlayerClient.this, Context.BIND_AUTO_CREATE);
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

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        isConnect = true;
        mController = (MusicPlayerService.Controller) iBinder;
        mController.load(mMusicProvider);
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
    public String getCurrentListName() {
        return mController.getCurrentListName();
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
    public int getMusicLength() {
        return mController.getMusicLength();
    }

    @Override
    public int getMusicProgress() {
        return mController.getMusicProgress();
    }

    @Override
    public MusicProvider getMusicProvider() {
        return mController.getMusicProvider();
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

    private MusicProvider initMusicListProvider(Context context) {
        //从配置文件解析
        Class cl = decodeMusicProviderClass(context.getApplicationContext());
        MusicProvider provider = null;
        try {
            if (cl != null) {
                provider = (MusicProvider) cl.newInstance();
                provider.initDataSet(context);
            } else {
                throw new NullPointerException("Class object is null. please check your \"music_player.xml\" file.");
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return provider;
    }

    private Class decodeMusicProviderClass(Context context) {
        //从配置文件解析
        Class cl = null;
        try {
            InputStream inputStream = context.getAssets().open("music_player.xml");
            StringBuilder builder = new StringBuilder(128);
            Scanner scanner = new Scanner(inputStream);
            while (scanner.hasNext()) {
                builder.append(scanner.nextLine());
            }
            scanner.close();
            String content = builder.toString();
            //解析XML
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(content));
            String str = "";
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = parser.getName();
                if (eventType == XmlPullParser.START_TAG) {
                    if (nodeName.equals("music-provider")) {
                        str = parser.nextText();
                    }
                }
                eventType = parser.next();
            }
            //调试
            Log.d("App", "解析的 MusicProvider : " + str);
            cl = Class.forName(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cl;
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

    public static class Key {
        public static final String KEY_PLAYING_MUSIC = "playing_music";
    }
}