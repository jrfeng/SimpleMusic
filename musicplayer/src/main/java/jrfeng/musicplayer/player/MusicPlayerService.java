package jrfeng.musicplayer.player;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingDeque;

import jrfeng.musicplayer.data.Music;
import jrfeng.musicplayer.mode.MusicStorage;
import jrfeng.musicplayer.player.MusicPlayerClient.NotifyControllerView;

public class MusicPlayerService extends Service {
    private static final int NOTIFY_ID = 1;

    private MusicStorage mMusicStorage;
    private NotifyControllerView mControllerView;

    private boolean mHasMediaButton;

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
            Class cl = Configure.getNotificationViewClass();
            mControllerView = (NotifyControllerView) cl.newInstance();
        } catch (Exception e) {
            logE(e.toString());
        } finally {
            mControllerView = new DefaultNotifyControllerView();
        }

        runAsForeground();              //作为前台Service运行
        registerMediaButtonReceiver();  //将应用程序注册为MediaButton的唯一处理程序
        return new Controller();
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

    class Controller extends Binder implements MusicPlayerController,
            MediaPlayer.OnErrorListener,
            MediaPlayer.OnCompletionListener,
            MediaPlayer.OnSeekCompleteListener,
            MediaPlayer.OnPreparedListener {
        private static final String KEY_MUSIC_GROUP_TYPE = "groupType";
        private static final String KEY_MUSIC_GROUP_NAME = "listName";
        private static final String KEY_MUSIC_POSITION = "musicPosition";
        private static final String KEY_MUSIC_PLAYING_POSITION = "playingPosition";
        private static final String KEY_PLAY_MODE = "playMode";

        private static final String PREFERENCES_NAME = "player_state.dat";
        private SharedPreferences mPreferences;

        private MediaPlayer mMediaPlayer;
        private Music mPlayingMusic;
        private boolean mPlaying;
        private List<Music> mMusicList;

        private MusicStorage.GroupType mMusicGroupType;
        private String mMusicGroupName;
        private int mMusicPosition;
        private int mMusicPlayingPosition;
        private MusicPlayerClient.PlayMode mPlayMode;

        private Queue<Music> mTempQueue;    //临时列表
        private boolean mPlayedTempMusic;

        private boolean mPrepared;
        private boolean mPlayAfterPrepared;

        private List<Music> mRecentPlayList;
        private boolean mRecordRecentPlay;

        private AudioManager mAudioManager;
        private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;

        private static final String RECEIVER_PERMISSION = "jrfeng.simplemusic.permission.RECEIVE_PLAY_ACTION";

        private ValueAnimator volumeAnim;
        private Timer mTimer;

        private List<MusicPlayerClient.MusicProgressListener> mProgressListeners;
        private boolean mProgressGeneratorRunning;

        //***********************构造函数*********************

        private Controller() {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
            mMediaPlayer.setWakeMode(getBaseContext(), PowerManager.PARTIAL_WAKE_LOCK);

            mPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);

            mTempQueue = new LinkedBlockingDeque<>();

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

        void init(MusicStorage storage) {
            //调试
            log("init");

            mMusicStorage = storage;

            mMusicGroupType = MusicStorage.GroupType.valueOf(mPreferences.getString(KEY_MUSIC_GROUP_TYPE, MusicStorage.GroupType.MUSIC_LIST.name()));
            mMusicGroupName = mPreferences.getString(KEY_MUSIC_GROUP_NAME, MusicStorage.MUSIC_LIST_ALL);
            mMusicPosition = mPreferences.getInt(KEY_MUSIC_POSITION, 0);
            mMusicPlayingPosition = mPreferences.getInt(KEY_MUSIC_PLAYING_POSITION, 0);
            mPlayMode = MusicPlayerClient.PlayMode.valueOf(mPreferences.getString(KEY_PLAY_MODE, "MODE_ORDER"));

            mMusicList = mMusicStorage.getMusicGroup(
                    mMusicGroupType,
                    mMusicGroupName);
            mRecentPlayList = mMusicStorage.getRecentPlayList();
            mRecordRecentPlay = mRecentPlayList != null;//是否记录最近播放
            if (mMusicList == null) {
                mMusicGroupType = MusicStorage.GroupType.MUSIC_LIST;
                mMusicGroupName = MusicStorage.MUSIC_LIST_ALL;
                mMusicPosition = 0;
                mMusicPlayingPosition = 0;
                mMusicList = mMusicStorage.getMusicGroup(mMusicGroupType, mMusicGroupName);
            }
            if (mMusicList.size() > 0) {
                mPlayingMusic = mMusicList.get(mMusicPosition);
                prepare(false);
            }
        }

        @Override
        public void addMusicProgressListener(MusicPlayerClient.MusicProgressListener listener) {
            mProgressListeners.add(listener);
            if (!mProgressGeneratorRunning) {
                startProgressGenerator();
            }
        }

        @Override
        public void removeMusicProgressListener(MusicPlayerClient.MusicProgressListener listener) {
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
            return mPlayMode == MusicPlayerClient.PlayMode.MODE_LOOP;
        }

        @Override
        public boolean isPrepared() {
            return mPrepared;
        }

        @Override
        public Music getPlayingMusic() {
            return mPlayingMusic;
        }

        @Override
        public int getPlayingMusicIndex() {
            if (mPlayingMusic == null) {
                return -1;
            }
            return mMusicList.indexOf(mPlayingMusic);
        }

        @Override
        public List<Music> getMusicList() {
            return mMusicList;
        }

        @Override
        public MusicStorage.GroupType getMusicGroupType() {
            return mMusicGroupType;
        }

        @Override
        public String getMusicGroupName() {
            return mMusicGroupName;
        }

        @Override
        public List<Music> getRecentPlayList() {
            return mRecentPlayList;
        }

        @Override
        public int getRecentPlayCount() {
            return mRecentPlayList.size();
        }

        @Override
        public boolean setPlayMode(MusicPlayerClient.PlayMode mode) {
            if (mPlayingMusic == null || !mPrepared) {
                return false;
            }
            switch (mode) {
                case MODE_ORDER:
                    mMediaPlayer.setLooping(false);
                    break;
                case MODE_LOOP:
                    mMediaPlayer.setLooping(true);
                    break;
                case MODE_RANDOM:
                    mMediaPlayer.setLooping(false);
                    break;
            }
            mPlayMode = mode;
            mPreferences.edit().putString(KEY_PLAY_MODE, mPlayMode.name()).apply();
            return true;
        }

        @Override
        public MusicPlayerClient.PlayMode getPlayMode() {
            return mPlayMode;
        }

        @Override
        public void addTempPlayMusic(Music music) {
            mTempQueue.add(music);
        }

        @Override
        public boolean isTempPlay() {
            return mPlayedTempMusic;
        }

        @Override
        public int getMusicLength() {
            if (mPlayingMusic == null || !mPrepared) {
                return 100;
            }
            return mMediaPlayer.getDuration();
        }

        @Override
        public int getMusicProgress() {
            if (mPlayingMusic == null && !mPrepared) {
                return 0;
            }
            return mMediaPlayer.getCurrentPosition();
        }

        //********************private***********************

        private void releaseAndSaveState() {
            //调试
            log("release");

            if (mPrepared) {
                mMusicPlayingPosition = mMediaPlayer.getCurrentPosition();
            } else {
                mMusicPlayingPosition = 0;
            }

            saveState();

            mPlaying = false;
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        private void saveState() {
            mPreferences.edit()
                    .putString(KEY_MUSIC_GROUP_TYPE, mMusicGroupType.name())
                    .putString(KEY_MUSIC_GROUP_NAME, mMusicGroupName)
                    .putInt(KEY_MUSIC_POSITION, mMusicPosition)
                    .putInt(KEY_MUSIC_PLAYING_POSITION, mMusicPlayingPosition)
                    .putString(KEY_PLAY_MODE, mPlayMode.name())
                    .apply();
        }

        private void prepare(boolean play) {
            //调试
            log("prepare");

            mPrepared = false;
            mPlaying = false;
            mControllerView.pause();
            stopProgressGenerator();

            if (mPlayingMusic == null) {
                //调试
                logE("PlayingMusic is Null, Not prepare");
                return;
            }

            if (!checkFile(mPlayingMusic)) {
                mControllerView.updateText(mPlayingMusic.getSongName(), mPlayingMusic.getArtist());
                sendActionBroadcast(MusicPlayerClient.Action.ACTION_MUSIC_NOT_EXIST);
                return;
            }

            try {
                mPlayAfterPrepared = play;
                mMediaPlayer.reset();
                mControllerView.updateText(mPlayingMusic.getSongName(), mPlayingMusic.getArtist());
                mMediaPlayer.setDataSource(mPlayingMusic.getPath());
                mMediaPlayer.setLooping(isLooping());
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendActionBroadcast(String action) {
            log("发送广播 : " + action);
            Intent intent = new Intent(action);
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

        private boolean checkFile(Music music) {
            File file = new File(music.getPath());
            return file.exists();
        }

        //关键的方法，负责渐隐播放/渐隐暂停
        private void volumeTransition(float start, float end, boolean act, final String action) {
            if (volumeAnim != null && volumeAnim.isRunning()) {
                volumeAnim.cancel();
            }
            volumeAnim = ValueAnimator.ofFloat(start, end);
            volumeAnim.setDuration(800);
            if (act) {
                volumeAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        if (action.equals(MusicPlayerClient.Action.ACTION_PLAY) && mPlaying) {
                            mMediaPlayer.start();
                            mMediaPlayer.setVolume(0, 0);
                            startProgressGenerator();
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (action.equals(MusicPlayerClient.Action.ACTION_PAUSE) && !mPlaying) {
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

            if (mProgressListeners.size() == 0 || !mPrepared) {
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

            if (mMusicList.size() < 1) {
                //调试
                logW("No previous, Music list is empty.");
                return;
            }

            mTempQueue.clear();//清空临时播放队列
            if (mPlayMode == MusicPlayerClient.PlayMode.MODE_RANDOM) {
                Random random = new Random();
                mMusicPosition = random.nextInt(mMusicList.size());
            } else {
                if (!mPlayedTempMusic) {
                    mMusicPosition = mMusicList.indexOf(mPlayingMusic);
                }
                mPlayedTempMusic = false;
                mMusicPosition--;
                if (mMusicPosition < 0) {
                    mMusicPosition = mMusicList.size() - 1;
                }
            }
            mPlayingMusic = mMusicList.get(mMusicPosition);
            sendActionBroadcast(MusicPlayerClient.Action.ACTION_PREVIOUS);
            prepare(true);
        }

        @Override
        public void next() {
            //调试
            log("next");

            if (mMusicList.size() < 1) {
                //调试
                logW("No next, Music list is empty.");
                return;
            }

            //检查临时播放列表
            if (mTempQueue.size() > 0) {
                mPlayingMusic = mTempQueue.remove();
                mPlayedTempMusic = true;
            } else {
                if (mPlayMode == MusicPlayerClient.PlayMode.MODE_RANDOM) {
                    Random random = new Random();
                    mMusicPosition = random.nextInt(mMusicList.size());
                } else {
                    if (!mPlayedTempMusic) {
                        mMusicPosition = mMusicList.indexOf(mPlayingMusic);
                    }
                    mPlayedTempMusic = false;
                    mMusicPosition++;
                    if (mMusicPosition >= mMusicList.size()) {
                        mMusicPosition = 0;
                    }
                }
                mPlayingMusic = mMusicList.get(mMusicPosition);
            }
            sendActionBroadcast(MusicPlayerClient.Action.ACTION_NEXT);
            prepare(true);
        }

        @Override
        public void play() {
            //调试
            log("play");

            if ((mPlaying || !mPrepared) && mPlayingMusic == null) {
                logW("Player is playing OR player is not prepared.");
                logW("Try play default music list.");
                loadMusicGroup(MusicStorage.GroupType.MUSIC_LIST,
                        MusicStorage.MUSIC_LIST_ALL,
                        0, true);
                return;
            }

            mPlaying = true;
            if (!mHasMediaButton) {
                registerMediaButtonReceiver();
            }

            requestAudioFocus();

            //更新View
            mControllerView.play();

            if (mRecordRecentPlay) {
                mMusicStorage.recordRecentPlay(mPlayingMusic);
            }

            //渐隐播放
            volumeTransition(0.0F, 1.0F, true, MusicPlayerClient.Action.ACTION_PLAY);
            sendActionBroadcast(MusicPlayerClient.Action.ACTION_PLAY);
        }

        @Override
        public void play(int position) {
            if (position > mMusicList.size()) {
                //调试
                logW("Position is too big, Not play(int position).");
                return;
            }

            mTempQueue.clear();     //清空临时播放列表

            mMusicPosition = position;
            mPlayingMusic = mMusicList.get(mMusicPosition);
            prepare(true);
        }

        @Override
        public void loadMusicGroup(MusicStorage.GroupType groupType, String groupName, int position, boolean play) {
            mTempQueue.clear();//清空临时播放队列
            mPlayedTempMusic = false;
            if (mPlayingMusic == null || groupType != mMusicGroupType || !groupName.equals(mMusicGroupName)) {
                List<Music> musicGroup = mMusicStorage.getMusicGroup(mMusicGroupType, mMusicGroupName);
                if (musicGroup == null || mMusicList.size() < 1) {
                    //调试
                    logW("Music list is null OR empty, No play.");
                    return;
                }
                mTempQueue.clear();     //清空临时播放列表
                mMusicGroupType = groupType;
                mMusicGroupName = groupName;
                mMusicList = musicGroup;
            }
            mMusicPosition = position;
            mMusicPlayingPosition = 0;
            mPlayingMusic = mMusicList.get(mMusicPosition);
            prepare(play);
        }

        @Override
        public void playMusicGroup(MusicStorage.GroupType groupType, String groupName, int position) {
            loadMusicGroup(groupType, groupName, position, true);
        }

        @Override
        public void pause() {
            //调试
            log("pause");

            if (mMusicList.size() < 1) {
                //调试
                logW("No pause, Music list is empty.");
                return;
            }

            if (mPlaying) {
                mPlaying = false;
                //放弃音频焦点
                abandonAudioFocus();
                //更新View
                mControllerView.pause();
                sendActionBroadcast(MusicPlayerClient.Action.ACTION_PAUSE);
                //渐隐暂停
                volumeTransition(1.0F, 0.0F, true, MusicPlayerClient.Action.ACTION_PAUSE);
            }
        }

        @Override
        public void playPause() {
            //调试
            log("playPause");
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
                logW("PlayingMusic is Null, Not stop");
                return;
            }
            mControllerView.pause();    //更新View
            mTempQueue.clear();         //清空临时播放列表
            sendActionBroadcast(MusicPlayerClient.Action.ACTION_STOP);
            abandonAudioFocus();
            pause();    //暂停播放
            prepare(false);  //重置播放器
        }

        @Override
        public void seekTo(int msec) {
            if (mPlayingMusic == null || !mPrepared) {
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
                    mMusicStorage.saveChanges();
                }
            }.start();

            //发送结束应用程序的广播
            sendActionBroadcast(MusicPlayerClient.Action.ACTION_SHUTDOWN);
            Log.d("App", "【发送广播】 : " + MusicPlayerClient.Action.ACTION_SHUTDOWN);
            Intent intent = new Intent(MusicPlayerClient.Action.ACTION_SHUTDOWN);
            sendBroadcast(intent);
            releaseAndSaveState();//释放MediaPlayer，同时保存播放器状态。
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
            stopProgressGenerator();
            //发送“出错”广播
            sendActionBroadcast(MusicPlayerClient.Action.ACTION_ERROR);
            //更新View
            mControllerView.pause();
            prepare(false);
            return true;
        }

        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {
            if (isPlaying()) {
                sendActionBroadcast(MusicPlayerClient.Action.ACTION_PLAY);
                startProgressGenerator();
            }
        }

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            log("Music prepared.");
            mPrepared = true;
            sendActionBroadcast(MusicPlayerClient.Action.ACTION_PREPARED);
            if (mMusicPlayingPosition > 0) {
                mMediaPlayer.seekTo(mMusicPlayingPosition);
            }
            saveState();

            mMusicPlayingPosition = 0;
            if (mPlayAfterPrepared) {
                play();
                mPlayAfterPrepared = false;
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

    private void logW(String... msg) {
        String s = "";
        for (int i = 0; i < msg.length - 1; i++) {
            s += msg[i] + " : ";
        }
        s += msg[msg.length > 1 ? msg.length - 1 : 0];
        Log.w("MusicPlayerService", s);
    }
}
