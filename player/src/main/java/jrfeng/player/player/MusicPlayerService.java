package jrfeng.player.player;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import jrfeng.player.R;
import jrfeng.player.data.Music;
import jrfeng.player.mode.MusicStorage;
import jrfeng.player.player.MusicPlayerClient.NotifyControllerView;
import jrfeng.player.utils.activity.ActivityStack;
import jrfeng.player.utils.mp3.MP3Util;

/**
 * 音乐播放器 Service。请不要调用该类任何方法，
 * 你应该通过 MusicPlayerClient 来使用音乐播放器 Service。
 */
public class MusicPlayerService extends Service {
    private static final int NOTIFY_ID = 1;
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
        log("Client : onDestroy");
        stopForeground(true);

        //调试
        log("Client : 销毁 Service");
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
            MusicPlayerController.MustInitialize,
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

        private PlayerCommandReceiver mCommandReceiver;

        private MediaPlayer mMediaPlayer;
        private Music mPlayingMusic;
        private boolean mPlaying;
        private List<Music> mMusicGroup;

        private MusicStorage mMusicStorage;
        private MusicStorage.OnMusicGroupChangListener mMusicGroupChangeListener;

        private MusicStorage.GroupType mMusicGroupType;
        private String mMusicGroupName;
        private int mMusicPosition;
        private int mMusicPlayingPosition;
        private MusicPlayerClient.PlayMode mPlayMode;

        private List<Music> mTempList;      //临时列表
        private boolean mPlayingTempMusic;

        private boolean mPrepared;
        private boolean mPlayAfterPrepared;

        private List<Music> mRecentPlayList;
        private boolean mRecordRecentPlay;

        private AudioManager mAudioManager;
        private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;

        private static final String RECEIVER_PERMISSION = "jrfeng.simplemusic.permission.RECEIVE_PLAY_ACTION";

        private ValueAnimator volumeAnim;
        private Timer mTimer;

        private List<MusicPlayerClient.PlayerProgressListener> mProgressListeners;
        private boolean mProgressGeneratorRunning;

        private Random mRandom;

        //***********************构造函数*********************

        private Controller() {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
            mMediaPlayer.setWakeMode(getBaseContext(), PowerManager.PARTIAL_WAKE_LOCK);

            mPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);

            mCommandReceiver = new PlayerCommandReceiver(getApplicationContext());

            mTempList = new LinkedList<>();

            mProgressListeners = new LinkedList<>();
            mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            initListeners();
        }

        //**************************public***********************

        @Override
        public void init(MusicStorage storage) {
            //调试
            log("init");

            mMusicStorage = storage;
            mMusicStorage.addMusicGroupChangeListener(mMusicGroupChangeListener);

            mMusicGroupType = MusicStorage.GroupType.valueOf(mPreferences.getString(KEY_MUSIC_GROUP_TYPE, MusicStorage.GroupType.MUSIC_LIST.name()));
            mMusicGroupName = mPreferences.getString(KEY_MUSIC_GROUP_NAME, MusicStorage.MUSIC_LIST_ALL_MUSIC);
            mMusicPosition = mPreferences.getInt(KEY_MUSIC_POSITION, 0);
            mMusicPlayingPosition = mPreferences.getInt(KEY_MUSIC_PLAYING_POSITION, 0);
            mPlayMode = MusicPlayerClient.PlayMode.valueOf(mPreferences.getString(KEY_PLAY_MODE, "MODE_ORDER"));

            mMusicGroup = mMusicStorage.getMusicGroup(
                    mMusicGroupType,
                    mMusicGroupName);
            mRecentPlayList = mMusicStorage.getRecentPlay();
            mRecordRecentPlay = mRecentPlayList != null;    //是否记录最近播放

            if (mMusicGroup == null) {
                mMusicGroupType = MusicStorage.GroupType.MUSIC_LIST;
                mMusicGroupName = MusicStorage.MUSIC_LIST_ALL_MUSIC;
                mMusicPosition = 0;
                mMusicPlayingPosition = 0;
                mMusicGroup = mMusicStorage.getMusicGroup(mMusicGroupType, mMusicGroupName);
            }

            if (mMusicGroup.size() > 0) {
                mPlayingMusic = mMusicGroup.get(Math.max(mMusicPosition, 0));
                prepare(false);
            }

            mCommandReceiver.register();
        }

        @Override
        public void addMusicProgressListener(MusicPlayerClient.PlayerProgressListener listener) {
            mProgressListeners.add(listener);
            if (!mProgressGeneratorRunning) {
                startProgressGenerator();
            }
        }

        @Override
        public void removeMusicProgressListener(MusicPlayerClient.PlayerProgressListener listener) {
            mProgressListeners.remove(listener);
            if (mProgressListeners.size() == 0) {
                //调试
                log("Stop progress generator : 观察者集为空");
                stopProgressGenerator();
            }
        }

        @Override
        public List<Music> getTempList() {
            return mTempList;
        }

        @Override
        public List<String> getTempListMusicNames() {
            List<String> names = new LinkedList<>();
            for (Music music : mTempList) {
                names.add(music.getName());
            }
            return names;
        }

        @Override
        public void clearTempList() {
            //注意，该方法不会清除临时播放标志位！也不应该清除临时播放标志位！
            mTempList.clear();
        }

        @Override
        public boolean tempListIsEmpty() {
            return mTempList.size() <= 0;
        }

        @Override
        public void playTempMusic(int position, boolean autoPlay) {
            Music tempMusic = mTempList.remove(position);
            if (!checkFile(tempMusic)) {
                return;
            }
            if (mMusicStorage.contains(tempMusic)) {
                mPlayingMusic = tempMusic;
                mPlayingTempMusic = true;
            } else {
                next();
            }

            prepare(autoPlay);
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
            return mMusicGroup.indexOf(mPlayingMusic);
        }

        @Override
        public List<Music> getMusicList() {
            return mMusicGroup;
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
            if (!mTempList.contains(music)) {
                mTempList.add(music);
            }
        }

        @Override
        public void addTempPlayMusics(List<Music> musics) {
            for (Music music : musics) {
                addTempPlayMusic(music);
            }
        }

        @Override
        public boolean isPlayingTempMusic() {
            return mPlayingTempMusic;
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

        //***********************MusicPlayerController*****************

        @Override
        public void previous() {
            //调试
            log("previous");

            if (mMusicGroup.size() < 1) {
                //调试
                logW("No previous, Music list is empty.");
                return;
            }

            mTempList.clear();//清空临时播放队列
            int position;
            if (mPlayMode == MusicPlayerClient.PlayMode.MODE_RANDOM) {
                position = randomPosition();
            } else {
                position = previousPosition();
            }
            if (!checkFile(mMusicGroup.get(position))) {
                return;
            }
            mMusicPosition = position;
            mPlayingMusic = mMusicGroup.get(mMusicPosition);
            sendActionBroadcast(MusicPlayerClient.Action.ACTION_PREVIOUS);
            prepare(true);
        }

        @Override
        public void next() {
            //调试
            log("next");

            //调试
            log("位置 : " + mMusicPosition);

            if (mMusicGroup.size() < 1) {
                //调试
                logW("No next, Music list is empty.");
                return;
            }

            //检查临时播放列表
            if (mTempList.size() > 0) {
                playTempMusic(0, true);
                return;
            } else {
                int position;

                if (mPlayMode == MusicPlayerClient.PlayMode.MODE_RANDOM) {
                    position = randomPosition();
                } else {
                    position = nextPosition();
                }
                if (!checkFile(mMusicGroup.get(position))) {
                    return;
                }
                mMusicPosition = position;
                mPlayingMusic = mMusicGroup.get(mMusicPosition);
            }

            sendActionBroadcast(MusicPlayerClient.Action.ACTION_NEXT);
            prepare(true);
        }

        @Override
        public void play() {
            //调试
            log("play");

            if (mPlayingMusic == null) {
                logW("Player is playing OR player is not prepared.");
                logW("Try play default music list.");
                loadMusicGroup(MusicStorage.GroupType.MUSIC_LIST,
                        MusicStorage.MUSIC_LIST_ALL_MUSIC,
                        0, false);
                mMusicPosition = 0;
                return;
            }

            if (!checkFile(mPlayingMusic)) {
                return;
            }

            if (!mPrepared) {
                prepare(true);
                return;
            }

            mPlaying = true;
            if (!mHasMediaButton) {
                registerMediaButtonReceiver();
            }

            //保存状态
            saveState();

            requestAudioFocus();

            //更新View
            mControllerView.play();

            if (mRecordRecentPlay) {
                mMusicStorage.addMusicToRecentPlay(mPlayingMusic);
            }

            //渐隐播放
            volumeTransition(0.0F, 1.0F, true, MusicPlayerClient.Action.ACTION_PLAY);
            sendActionBroadcast(MusicPlayerClient.Action.ACTION_PLAY);
        }

        @Override
        public void play(int position) {
            if (position > mMusicGroup.size()) {
                //调试
                logW("Position is too big, Not play(int position).");
                return;
            }

            if (!checkFile(mMusicGroup.get(position))) {
                return;
            }

            if (position == mMusicPosition) {
                playPause();
                return;
            }

            mTempList.clear();//清空临时播放队列
            mPlayingTempMusic = false;

            mMusicPosition = position;
            mPlayingMusic = mMusicGroup.get(mMusicPosition);
            prepare(true);
        }

        @Override
        public void nextButNotPlay() {
            //调试
            log("next");

            if (mMusicGroup.size() < 1) {
                //调试
                logW("No next, Music list is empty.");
                return;
            }

            //检查临时播放列表
            if (mTempList.size() > 0) {
                playTempMusic(0, false);
                return;
            } else {
                int position;

                if (mPlayMode == MusicPlayerClient.PlayMode.MODE_RANDOM) {
                    position = randomPosition();
                } else {
                    position = nextPosition();
                }
                if (!checkFile(mMusicGroup.get(position))) {
                    return;
                }
                mMusicPosition = position;
                mPlayingMusic = mMusicGroup.get(mMusicPosition);
            }

            sendActionBroadcast(MusicPlayerClient.Action.ACTION_NEXT);
            prepare(false);
        }

        @Override
        public void loadMusicGroup(MusicStorage.GroupType groupType, String groupName, int position, boolean play) {
            //调试
            logW("加载音乐组");
            logW("组类型 : " + groupType.name());
            logW("组名称 : " + groupName);

            List<Music> musicGroup = mMusicStorage.getMusicGroup(groupType, groupName);
            if (musicGroup == null || musicGroup.size() < 1) {
                //调试
                logW("Music list is null OR empty, No Load.");
                log("Load Group is null : " + (musicGroup == null));
                if (musicGroup != null) {//调试用
                    log("Load Group size    : " + musicGroup.size());
                }

                stop();
                return;
            }

            if (!checkFile(musicGroup.get(position))) {
                return;
            }

            mTempList.clear();//清空临时播放队列
            mPlayingTempMusic = false;
            mMusicGroupType = groupType;
            mMusicGroupName = groupName;
            mMusicGroup = musicGroup;

            if (position < 0) {
                position = 0;
            }

            if (position > mMusicGroup.size()) {
                position = mMusicGroup.size() - 1;
            }

            mMusicPosition = position;
            mMusicPlayingPosition = 0;
            mPlayingMusic = mMusicGroup.get(mMusicPosition);
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

            if (mMusicGroup.size() < 1) {
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
            stopProgressGenerator();
            //更新 View
            mControllerView.pause();
            //清空临时播放列表
            mTempList.clear();
            mPlaying = false;
            mPrepared = false;
            mMusicPlayingPosition = 0;
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();       //重置播放器
            }
            sendActionBroadcast(MusicPlayerClient.Action.ACTION_STOP);
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
            log("【退出应用程序】");

            stopProgressGenerator();
            //放弃音频焦点
            abandonAudioFocus();
            //注销媒体按钮接收器
            unregisterMediaButtonReceiver();

            //注销控制命令接收器
            mCommandReceiver.unregister();

            mMusicStorage.removeMusicGroupChangeListener(mMusicGroupChangeListener);

            releaseAndSaveState();//释放MediaPlayer，同时保存播放器状态。
            stopSelf();

            //退出应用
            ActivityStack.finishAll();
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
            log("GroupType : " + mMusicGroupType.name());
            log("GroupName : " + mMusicGroupName);
            log("GroupSize : " + mMusicGroup.size());
            mPrepared = true;
            sendActionBroadcast(MusicPlayerClient.Action.ACTION_PREPARED);
            if (mMusicPlayingPosition > 0) {
                mMediaPlayer.seekTo(mMusicPlayingPosition);
            }

            saveState();
            refreshNotifyIcon();

            mMusicPlayingPosition = 0;
            if (mPlayAfterPrepared) {
                mPlayAfterPrepared = false;
                play();
            }
        }

        //************************private***********************

        private void initListeners() {
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

            mMusicGroupChangeListener = new MusicStorage.OnMusicGroupChangListener() {
                @Override
                public void onMusicGroupChanged(MusicStorage.GroupType groupType, String groupName, MusicStorage.GroupAction action) {
                    //用于临时列表
                    if (groupType == MusicStorage.GroupType.MUSIC_LIST
                            && groupName.equals(MusicStorage.MUSIC_LIST_ALL_MUSIC)
                            && action == MusicStorage.GroupAction.REMOVE_MUSIC) {
                        if (mPlayingTempMusic && !mMusicStorage.contains(mPlayingMusic)) {
                            next();
                        }

                        //检查临时列表，清除已经彻底移除的音乐。因为会在迭代时移除列表中的元素，因此不能使用 foreach 循环。
                        Iterator<Music> iterator = mTempList.iterator();
                        while (iterator.hasNext()) {
                            Music music = iterator.next();
                            if (!mMusicStorage.contains(music)) {
                                iterator.remove();
                            }
                        }
                    }

                    //用于当前列表
                    if (groupType != mMusicGroupType || !groupName.equals(mMusicGroupName)) {
                        //调试
                        log("不是当前组");
                        return;
                    }

                    //调试
                    log("是当前组");
                    log("GroupType   : " + groupType.name());
                    log("GroupName   : " + groupName);
                    log("GroupAction : " + action.name());

                    switch (action) {
                        case DELETE_GROUP:
                            loadDefault();

                            //调试
                            log("当前歌单被删除");
                            log("加载默认歌单");
                            break;
                        case REMOVE_MUSIC:
                            if (!mMusicGroup.contains(mPlayingMusic)) {
                                //调试
                                log("正在播放歌曲被删除");

                                if (mMusicGroup.size() > 0) {
                                    //调试
                                    log("下一曲");
                                    mMusicPosition = mMusicPosition - 1;
                                    if (mPlaying) {
                                        next();
                                    } else {
                                        nextButNotPlay();
                                    }
                                } else {
                                    //调试
                                    log("加载默认歌单");

                                    loadDefault();
                                }
                            } else {
                                //调试
                                log("没有移除正在播放歌曲");
                            }
                            break;
                    }

                    //调试
                    log("保存播放位置");
                    log("播放位置更新前 : " + mMusicPosition);

                    if (mPlayingMusic != null) {
                        mMusicPosition = mMusicGroup.indexOf(mPlayingMusic);
                    } else {
                        mMusicPosition = 0;
                    }
                    saveState();//保存状态

                    //调试
                    log("播放位置更新后 : " + mMusicPosition);
                }
            };
        }

        private void loadDefault() {
            boolean play = mPlaying;
            stop();

            if (isDefaultMusicGroup()) {
                reset();
            } else {
                loadMusicGroup(MusicStorage.GroupType.MUSIC_LIST,
                        MusicStorage.MUSIC_LIST_ALL_MUSIC, 0, play);
            }
        }

        private void reset() {
            mMusicGroup = null;
            mPlayingMusic = null;
            mPrepared = false;
            mPlaying = false;
            mMediaPlayer.reset();
            mMusicPosition = 0;
            mMusicPlayingPosition = 0;
            sendActionBroadcast(MusicPlayerClient.Action.ACTION_RESET);
        }

        private boolean isDefaultMusicGroup() {
            return mMusicGroupType == MusicStorage.GroupType.MUSIC_LIST
                    && mMusicGroupName.equals(MusicStorage.MUSIC_LIST_ALL_MUSIC);
        }

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

            try {
                mPlayAfterPrepared = play;
                mMediaPlayer.reset();
                mControllerView.updateText(mPlayingMusic.getName(), mPlayingMusic.getArtist());
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
            boolean result = file.exists();
            if (!result) {
                if (mPlaying) {
                    pause();
                }
                sendActionBroadcast(MusicPlayerClient.Action.ACTION_MUSIC_NOT_EXIST);
            }
            return result;
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

        private int randomPosition() {
            mPlayingTempMusic = false;
            if (mRandom == null) {
                mRandom = new Random();
            }
            return mRandom.nextInt(mMusicGroup.size());
        }

        private int nextPosition() {
            mPlayingTempMusic = false;

            int position;
            position = mMusicPosition + 1;
            if (position >= mMusicGroup.size()) {
                position = 0;
            }
            return position;
        }

        private int previousPosition() {
            mPlayingTempMusic = false;

            int position;
            position = mMusicPosition - 1;
            if (position < 0) {
                position = mMusicGroup.size() - 1;
            }
            return position;
        }

        private void refreshNotifyIcon() {
            byte[] imageData = MP3Util.getMp3Image(new File(mPlayingMusic.getPath()));
            Bitmap bitmap;
            if (imageData != null) {
                int widthAndHeight = getResources().getDimensionPixelSize(R.dimen.iconWidthAndHeight);
                bitmap = loadBitmap(imageData, widthAndHeight, widthAndHeight);
                if (bitmap != null) {
                    mControllerView.setNotifyIcon(bitmap);
                } else {
                    mControllerView.setNotifyIcon(R.mipmap.ic_launcher);
                }
            } else {
                mControllerView.setNotifyIcon(R.mipmap.ic_launcher);
            }

            if (mPlayingTempMusic) {
                mControllerView.showTempPlayMark();
            } else {
                mControllerView.hideTempPlayMark();
            }
        }
    }

    private Bitmap loadBitmap(byte[] image, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(image, 0, image.length, options);

        int rawWidth = options.outWidth;
        int rawHeight = options.outHeight;

        int widthSample = rawWidth / width;
        int heightSample = rawHeight / height;

        int sample = Math.max(widthSample, heightSample);

        if (sample % 2 != 0) {
            sample = sample - 1;
            sample = Math.max(1, sample);
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = sample;

        return BitmapFactory.decodeByteArray(image, 0, image.length, options);
    }

    //*********************调试用**********************

    private static void log(String... msg) {
        String s = "";
        for (int i = 0; i < msg.length - 1; i++) {
            s += msg[i] + " : ";
        }
        s += msg[msg.length > 1 ? msg.length - 1 : 0];
        Log.d("MusicPlayerService", s);
    }

    private static void logE(String... msg) {
        String s = "";
        for (int i = 0; i < msg.length - 1; i++) {
            s += msg[i] + " : ";
        }
        s += msg[msg.length > 1 ? msg.length - 1 : 0];
        Log.e("MusicPlayerService", s);
    }

    private static void logW(String... msg) {
        String s = "";
        for (int i = 0; i < msg.length - 1; i++) {
            s += msg[i] + " : ";
        }
        s += msg[msg.length > 1 ? msg.length - 1 : 0];
        Log.w("MusicPlayerService", s);
    }
}
