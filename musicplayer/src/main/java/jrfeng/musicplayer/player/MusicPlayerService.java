package jrfeng.musicplayer.player;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import jrfeng.musicplayer.data.Music;

public class MusicPlayerService extends Service {
    public static final String ACTION_PLAY = "jrfeng.simplemusic.action.PLAY";
    public static final String ACTION_PAUSE = "jrfeng.simplemusic.action.PAUSE";
    public static final String ACTION_NEXT = "jrfeng.simplemusic.action.NEXT";
    public static final String ACTION_PREVIOUS = "jrfeng.simplemusic.action.PREVIOUS";
    public static final String ACTION_STOP = "jrfeng.simplemusic.action.STOP";
    public static final String ACTION_ERROR = "jrfeng.simplemusic.action.ERROR";
    public static final String ACTION_SHUTDOWN = "jrfeng.simplemusic.action.SHUTDOWN";
    public static final String RECEIVER_PERMISSION = "jrfeng.simplemusic.permission.MUSIC_PLAYER_ACTION";

    public static final String KEY_PLAYING_MUSIC = "playing_music";

    private List<Music> mRecentPlayList;
    private boolean mRecordRecentPlay;

    private MusicProvider mMusicProvider;
    private SharedPreferences mPreferences;

    private static final int NOTIFY_ID = 1;
    private static final String PREFERENCES_NAME = "player_state.dat";

    private static final String KEY_LIST_NAME = "listName";
    private static final String KEY_MUSIC_POSITION = "musicPosition";
    private static final String KEY_LOOPING = "looping";

    private boolean mHasMediaButton;

    private NotifyControllerView mControllerView;

    @Override
    public void onCreate() {
        super.onCreate();
        //调试
        log("onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //调试
        log("onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //调试
        log("onBind");

        try {
            Class cl = decodeNotifyViewClass();
            mControllerView = (NotifyControllerView) cl.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mControllerView = new DefaultNotifyControllerView();
        }

        runAsForeground();              //作为前台Service运行
        registerMediaButtonReceiver();  //将应用程序注册为MediaButton的唯一处理程序
        Controller controller = new Controller();

        mPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        String listName = mPreferences.getString(KEY_LIST_NAME, MusicProvider.DEFAULT_MUSIC_LIST);
        int musicPosition = mPreferences.getInt(KEY_MUSIC_POSITION, 0);
        boolean looping = mPreferences.getBoolean(KEY_LOOPING, false);

        controller.init(
                listName,
                musicPosition,
                looping);

        return controller;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //调试
        log("onUnBind");

        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //调试
        log("onDestroy");
        stopForeground(true);
    }

    //*********************private************************

    private void runAsForeground() {
        startForeground(NOTIFY_ID, mControllerView.getNotification(this, NOTIFY_ID));
    }

    private Class decodeNotifyViewClass() {
        //从配置文件解析
        Context context = getApplicationContext();
        Class cl;
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
                    if (nodeName.equals("notify-view")) {
                        str = parser.nextText();
                    }
                }
                eventType = parser.next();
            }
            //调试
            Log.d("App", "解析的 NotifyView : " + str);
            if (!str.equals("")) {
                cl = Class.forName(str);
            } else {
                cl = DefaultNotifyControllerView.class;
            }
        } catch (Exception e) {
            e.printStackTrace();
            cl = DefaultNotifyControllerView.class;
        }
        return cl;
    }

    private void registerMediaButtonReceiver() {
        //将应用程序注册为MediaButton的唯一处理程序
        log("注册MediaButton");
        mHasMediaButton = true;
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        ComponentName componentName = new ComponentName(getBaseContext(), MediaButtonReceiver.class);
        audioManager.registerMediaButtonEventReceiver(componentName);
    }

    private void unregisterMediaButtonReceiver() {
        //注销媒体按钮监听器
        //将应用程序注册为MediaButton的唯一处理程序
        log("注销MediaButton");
        mHasMediaButton = false;
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.unregisterMediaButtonEventReceiver(
                new ComponentName(getBaseContext(), MediaButtonReceiver.class));
    }

    //********************Controller*********************

    public class Controller extends Binder implements MusicPlayerController,
            MediaPlayer.OnErrorListener,
            MediaPlayer.OnCompletionListener,
            MediaPlayer.OnSeekCompleteListener {
        private MediaPlayer mMediaPlayer;
        private Music mPlayingMusic;
        private boolean mLooping;
        private boolean mPlaying;

        private String mCurrentListName;
        private List<Music> mMusicList;
        private int mMusicPosition;

        private AudioManager mAudioManager;
        private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;

        private ValueAnimator volumeAnim;
        private Timer mTimer;

        private List<MusicProgressListener> mProgressListeners;
        private boolean mProgressGeneratorRunning;

        //***********************构造函数*********************

        private Controller() {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
            mMediaPlayer.setWakeMode(getBaseContext(), PowerManager.PARTIAL_WAKE_LOCK);

            mProgressListeners = new LinkedList<>();
            mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

            mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
                private boolean lossCanDuck;
                private boolean lossTransient;

                @Override
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            if (isPlaying()) {
                                lossCanDuck = true;
                                mMediaPlayer.setVolume(0.1F, 0.1F);
                            }
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            //暂停播放
                            if (isPlaying()) {
                                lossTransient = true;
                                pause();
                            }
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS:
                            //停止播放，并注销媒体按钮 Receiver
                            if (isPlaying()) {
                                pause();
                            }
                            unregisterMediaButtonReceiver();
                            //调试
                            log("失去音频焦点, 暂停播放");
                            break;
                        case AudioManager.AUDIOFOCUS_GAIN:
                            if (lossCanDuck) {
                                lossCanDuck = false;
                                volumeTransition(0.1F, 1.0F, false, null);
                            }

                            if (lossTransient) {
                                lossTransient = false;
                                play();
                            }
                            break;
                    }
                }
            };
        }

        //**************************public***********************

        void init(String listName, int musicPosition, boolean looping) {
            mCurrentListName = listName;
            mMusicPosition = musicPosition;
            mLooping = looping;
        }

        @Override
        public void setMusicProvider(MusicProvider provider) {
            mMusicProvider = provider;
        }

        public MusicProvider getMusicProvider() {
            return mMusicProvider;
        }

        public void load() {
            //调试
            log("load");
            mMusicList = mMusicProvider.getMusicList(mCurrentListName);
            mRecentPlayList = mMusicProvider.getMusicList("最近播放");
            mRecordRecentPlay = mRecentPlayList != null;//是否记录最近播放
            if (mMusicList.size() > 0) {
                mPlayingMusic = mMusicList.get(mMusicPosition);
                prepare();
            }
        }

        @Override
        public void reload() {
            if (mMusicList.size() > 0 && mPlayingMusic == null) {
                mPlayingMusic = mMusicList.get(mMusicPosition);
                prepare();
                if (mMusicList.size() == 1) {
                    mMediaPlayer.setLooping(true);
                }
            }
        }

        @Override
        public void addMusicProgressListener(MusicProgressListener listener) {
            mProgressListeners.add(listener);
            if (!mProgressGeneratorRunning) {
                startProgressGenerator();
            }
        }

        @Override
        public void removeMusicProgressListener(MusicProgressListener listener) {
            mProgressListeners.remove(listener);
            if (mProgressListeners.size() == 0) {
                //调试
                log("Stop progress generator : 观察者集为空");
                stopProgressGenerator();
            }
        }

        @Override
        public boolean isPlaying() {
            return mPlaying;
        }

        @Override
        public boolean isLooping() {
            return mLooping;
        }

        @Override
        public Music getPlayingMusic() {
            return mPlayingMusic;
        }

        @Override
        public int getPlayingMusicIndex() {
            return mMusicPosition;
        }

        @Override
        public List<Music> getMusicList() {
            return mMusicList;
        }

        @Override
        public String getCurrentListName() {
            return mCurrentListName;
        }

        @Override
        public boolean setLooping(boolean looping) {
            if (mPlayingMusic == null) {
                return false;
            }

            mLooping = looping;
            mMediaPlayer.setLooping(mLooping);
            return true;
        }

        @Override
        public int getMusicLength() {
            if (mPlayingMusic == null) {
                return 0;
            }
            return mMediaPlayer.getDuration();
        }

        @Override
        public int getMusicProgress() {
            if (mPlayingMusic == null) {
                return 0;
            }
            return mMediaPlayer.getCurrentPosition();
        }

        //********************private***********************

        private void release() {
            //调试
            log("release");

            saveState();

            mPlaying = false;
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        private void prepare() {
            //调试
            log("prepare");

            if (mPlayingMusic == null) {
                //调试
                logE("PlayingMusic is Null");
                return;
            }

            try {
                stopProgressGenerator();
                mMediaPlayer.reset();
                mPlaying = false;
                mPlayingMusic = mMusicList.get(mMusicPosition);
                mMediaPlayer.setDataSource(mPlayingMusic.getPath());
                mMediaPlayer.prepare();
                mMediaPlayer.setLooping(mLooping);
                mControllerView.updateText(mPlayingMusic.getSongName(), mPlayingMusic.getArtist());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendActionBroadcast(String action) {
            log("发送广播 : " + action);
            Intent intent = new Intent(action);
            intent.putExtra(KEY_PLAYING_MUSIC, mPlayingMusic);
            sendBroadcast(intent, RECEIVER_PERMISSION);
        }

        private void requestAudioFocus() {
            //请求音频焦点
            mAudioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }

        private void abandonAudioFocus() {
            //放弃音频焦点
            mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
        }

        private void saveState() {
            //调试
            log("saveState");

            mPreferences.edit()
                    .putString(KEY_LIST_NAME, mCurrentListName)
                    .putInt(KEY_MUSIC_POSITION, mMusicPosition)
                    .putBoolean(KEY_LOOPING, mLooping)
                    .apply();
        }

        //关键的方法，负责渐隐播放/渐隐暂停
        private void volumeTransition(float start, float end, boolean act, final String action) {
            if (volumeAnim != null && volumeAnim.isRunning()) {
                volumeAnim.cancel();
            }
            volumeAnim = ValueAnimator.ofFloat(start, end);
            volumeAnim.setDuration(600);
            if (act) {
                volumeAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        if (action.equals(ACTION_PLAY) && mPlaying) {
                            mMediaPlayer.start();
                            mMediaPlayer.setVolume(0, 0);
                            startProgressGenerator();
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (action.equals(ACTION_PAUSE) && !mPlaying) {
                            stopProgressGenerator();
                            mMediaPlayer.pause();
                        }
                    }
                });
            }
            volumeAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float currentValue = (float) valueAnimator.getAnimatedValue();
                    mMediaPlayer.setVolume(currentValue, currentValue);
                }
            });
            volumeAnim.start();
        }

        private void startProgressGenerator() {
            stopProgressGenerator();
            //调试
            log("Start progress generator.");

            if (mProgressListeners.size() == 0) {
                //调试
                log("Progress generator : 观察者集为空，不启动");
                return;
            }

            mTimer = new Timer(true);
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    int progress = getMusicProgress();
                    for (int i = 0; i < mProgressListeners.size(); i++) {
                        mProgressListeners.get(i).onProgressUpdated(progress);
                    }
                }
            }, new Date(), 1000);
            mProgressGeneratorRunning = true;
        }

        private void stopProgressGenerator() {
            if (mTimer != null) {
                //调试
                log("Stop progress generator.");
                mTimer.cancel();
                mTimer = null;
                mProgressGeneratorRunning = false;
            }
        }

        //***********************MusicPlayerController*****************

        @Override
        public void previous() {
            //调试
            log("previous");

            if (mPlayingMusic == null) {
                //调试
                logE("PlayingMusic is Null");
                return;
            }

            mMusicPosition--;
            if (mMusicPosition < 0) {
                mMusicPosition = mMusicList.size() - 1;
            }
            prepare();
            sendActionBroadcast(ACTION_PREVIOUS);
            play();
        }

        @Override
        public void next() {
            //调试
            log("next");

            if (mPlayingMusic == null) {
                //调试
                logE("PlayingMusic is Null");
                return;
            }

            mMusicPosition++;
            if (mMusicPosition >= mMusicList.size()) {
                mMusicPosition = 0;
            }
            prepare();
            sendActionBroadcast(ACTION_NEXT);
            play();
        }

        @Override
        public void play() {
            //调试
            log("play");

            if (mPlayingMusic == null) {
                //调试
                logE("PlayingMusic is Null");
                return;
            }

            File file = new File(mPlayingMusic.getPath());
            if (!file.exists()) {
                Toast.makeText(getBaseContext(), "文件不存在", Toast.LENGTH_SHORT).show();
            }

            if (!mPlaying) {
                mPlaying = true;

                if (!mHasMediaButton) {
                    registerMediaButtonReceiver();
                }

                requestAudioFocus();

                //更新View
                mControllerView.play();

                if (mRecordRecentPlay && !mRecentPlayList.contains(mPlayingMusic)) {
                    mRecentPlayList.add(0, mPlayingMusic);
                }

                //渐隐播放
                volumeTransition(0.0F, 1.0F, true, ACTION_PLAY);
                sendActionBroadcast(ACTION_PLAY);
            }
        }

        @Override
        public void play(int position) {
            mPlayingMusic = mMusicList.get(position);
            mMusicPosition = position;
            prepare();
            play();
        }

        @Override
        public void play(String listName, int position) {
            if (!listName.equals(mCurrentListName)) {
                init(listName, position, mLooping);
                load();
                mMusicPosition = position;
                prepare();
                play();
            } else {
                play(position);
            }
        }

        @Override
        public void pause() {
            //调试
            log("pause");

            if (mPlayingMusic == null) {
                //调试
                logE("PlayingMusic is Null");
                return;
            }


            if (mPlaying) {
                mPlaying = false;

                abandonAudioFocus();

                //更新View
                mControllerView.pause();
                sendActionBroadcast(ACTION_PAUSE);
                //渐隐暂停
                volumeTransition(1.0F, 0.0F, true, ACTION_PAUSE);

                //同时保存状态
                saveState();
            }
        }

        @Override
        public void play_pause() {
            //调试
            log("play_pause");

            if (mPlaying) {
                pause();
            } else {
                play();
            }
        }

        @Override
        public void stop() {
            //调试
            log("stop");

            if (mPlayingMusic == null) {
                //调试
                logE("PlayingMusic is Null");
                return;
            }

            //更新View
            mControllerView.pause();
            sendActionBroadcast(ACTION_STOP);
            abandonAudioFocus();

            pause();    //暂停播放
            prepare();  //重置播放器
        }

        @Override
        public void seekTo(int msec) {
            if (mPlayingMusic == null) {
                return;
            }

            //调试
            log("seekTo");
            stopProgressGenerator();
            mMediaPlayer.seekTo(msec);
        }

        @Override
        public void shutdown(Context context) {
            //调试
            log("shutdown");

            stopProgressGenerator();
            abandonAudioFocus();
            //保存数据集
            new Thread() {
                @Override
                public void run() {
                    mMusicProvider.saveDataSet();
                }
            }.start();

            //发送结束应用程序的广播
            sendActionBroadcast(ACTION_SHUTDOWN);
            release();
            stopSelf();
        }

        //****************MediaPlayer Listener**************

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            next();
        }

        @Override
        public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
            //调试
            logE("onError : 出错, 停止播放 : what : " + what + "/extra : " + extra);

            sendActionBroadcast(ACTION_ERROR);

            //更新View
            mControllerView.pause();
            mPlaying = false;
            prepare();
            return true;
        }

        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {
            if (isPlaying()) {
                sendActionBroadcast(ACTION_PLAY);
                startProgressGenerator();
            }
        }
    }

    //*********************调试用**********************

    private void log(String... msg) {
        String s = "";
        for (int i = 0; i < msg.length - 1; i++) {
            s += msg[i] + " : ";
        }
        s += msg[msg.length > 1 ? msg.length - 1 : 0];
        Log.d("MusicPlayerService", s);
    }

    private void logE(String... msg) {
        String s = "";
        for (int i = 0; i < msg.length - 1; i++) {
            s += msg[i] + " : ";
        }
        s += msg[msg.length > 1 ? msg.length - 1 : 0];
        Log.e("MusicPlayerService", s);
    }

    //*********************interface*******************

    public interface NotifyControllerView {
        Notification getNotification(Context context, int notifyId);

        void play();

        void pause();

        void updateText(String songName, String artist);
    }

    public interface MusicProgressListener {
        void onProgressUpdated(int progress);
    }
}