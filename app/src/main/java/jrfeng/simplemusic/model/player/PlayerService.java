package jrfeng.simplemusic.model.player;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.main.MainActivity;
import jrfeng.simplemusic.data.Music;
import jrfeng.simplemusic.model.MusicStorage;
import jrfeng.simplemusic.utils.log.L;

public class PlayerService extends Service {
    public static final String ACTION_PLAY = "jrfeng.simplemusic.action.PLAY";
    public static final String ACTION_PAUSE = "jrfeng.simplemusic.action.PAUSE";
    public static final String ACTION_NEXT = "jrfeng.simplemusic.action.NEXT";
    public static final String ACTION_PREVIOUS = "jrfeng.simplemusic.action.PREVIOUS";

    public static final String KEY_PLAYING_MUSIC = "playing_music";

    private LocalBroadcastManager mLocalBroadcastManager;

    private RemoteViews mNotifyView;
    private MusicStorage mMusicStorage;
    private SharedPreferences mPreferences;
    private PendingIntent mWelcomeActivityPendingIntent;

    private static final int NOTIFY_ID = 1;
    private static final String PREFERENCES_NAME = "player_state.dat";

    private static final String KEY_LIST_NAME = "listName";
    private static final String KEY_MUSIC_POSITION = "musicPosition";
    private static final String KEY_LOOPING = "looping";

    private boolean mHasMediaButton;

    @Override
    public void onCreate() {
        super.onCreate();
        //调试
        log("onCreate");
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getBaseContext());
        mMusicStorage = MyApplication.getInstance().getMusicStorage();
        mPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        mWelcomeActivityPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        runAsForeground();              //作为前台Service运行
        registerMediaButtonReceiver();  //将应用程序注册为MediaButton的唯一处理程序
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

        Controller controller = new Controller();

        String listName = mPreferences.getString(KEY_LIST_NAME, "所有音乐");
        int musicPosition = mPreferences.getInt(KEY_MUSIC_POSITION, 0);
        boolean looping = mPreferences.getBoolean(KEY_LOOPING, false);

        controller.load(
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
        mNotifyView = new RemoteViews(getPackageName(), R.layout.notify);

        mNotifyView.setOnClickPendingIntent(R.id.ibPrevious,
                PendingIntent.getBroadcast(this, 0, new Intent(MediaButtonControlReceiver.PLAYER_PREVIOUS), 0));

        mNotifyView.setOnClickPendingIntent(R.id.ibPlayPause,
                PendingIntent.getBroadcast(this, 0, new Intent(MediaButtonControlReceiver.PLAYER_PLAY_PAUSE), 0));

        mNotifyView.setOnClickPendingIntent(R.id.ibNext,
                PendingIntent.getBroadcast(this, 0, new Intent(MediaButtonControlReceiver.PLAYER_NEXT), 0));

        mNotifyView.setOnClickPendingIntent(R.id.ibCancel,
                PendingIntent.getBroadcast(this, 0, new Intent(MediaButtonControlReceiver.PLAYER_SHUTDOWN), 0));

        Notification notification = new NotificationCompat.Builder(getBaseContext())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(mWelcomeActivityPendingIntent)
                .setCustomContentView(mNotifyView)
                .build();

        startForeground(NOTIFY_ID, notification);
    }

    private void registerMediaButtonReceiver() {
        //将应用程序注册为MediaButton的唯一处理程序
        mHasMediaButton = true;
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        ComponentName componentName = new ComponentName(getBaseContext(), MediaButtonControlReceiver.class);
        audioManager.registerMediaButtonEventReceiver(componentName);
    }

    private void unregisterMediaButtonReceiver() {
        //注销媒体按钮监听器
        //将应用程序注册为MediaButton的唯一处理程序
        mHasMediaButton = false;
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.unregisterMediaButtonEventReceiver(
                new ComponentName(getBaseContext(), MediaButtonControlReceiver.class));
    }

    //********************Controller*********************

    public class Controller extends Binder implements PlayerController,
            MediaPlayer.OnErrorListener,
            MediaPlayer.OnCompletionListener,
            MediaPlayer.OnSeekCompleteListener {
        private MediaPlayer mMediaPlayer;
        private Music mPlayingMusic;
        private boolean mLooping;
        private boolean mPlaying;

        private String mListName;
        private List<Music> mMusicList;
        private int mMusicPosition;

        private AudioManager mAudioManager;
        private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;

        //***********************Constructor*********************

        public Controller() {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
            mMediaPlayer.setWakeMode(getBaseContext(), PowerManager.PARTIAL_WAKE_LOCK);

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

        public void load(String listName, int musicPosition, boolean looping) {
            //调试
            log("load");

            mListName = listName;
            mMusicList = mMusicStorage.getMusicList(mListName);
            mMusicPosition = musicPosition;
            mLooping = looping;

            if (mMusicList.size() > 0) {
                mPlayingMusic = mMusicList.get(mMusicPosition);
                mNotifyView.setTextViewText(R.id.tvTitle, mPlayingMusic.getSongName());
                mNotifyView.setTextViewText(R.id.tvArtist, mPlayingMusic.getArtist());
                updateNotifyView();
                prepare();
            }
        }

        @Override
        public void reload() {
            if (mMusicList.size() > 0 && mPlayingMusic == null) {
                mPlayingMusic = mMusicList.get(mMusicPosition);
                mNotifyView.setTextViewText(R.id.tvTitle, mPlayingMusic.getSongName());
                mNotifyView.setTextViewText(R.id.tvArtist, mPlayingMusic.getArtist());
                updateNotifyView();
                prepare();
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
        public boolean setLooping(boolean looping) {
            if (mPlayingMusic == null) {
                return false;
            }

            mLooping = looping;
            mMediaPlayer.setLooping(mLooping);
            return true;
        }

        @Override
        public int getDuration() {
            if (mPlayingMusic == null) {
                return 0;
            }
            return mMediaPlayer.getDuration();
        }

        @Override
        public int getCurrentPosition() {
            if (mPlayingMusic == null) {
                return 0;
            }
            return mMediaPlayer.getCurrentPosition();
        }

        //********************private***********************

        private void updateNotifyView() {
            Notification notification = new NotificationCompat.Builder(getBaseContext())
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setCustomContentView(mNotifyView)
                    .setContentIntent(mWelcomeActivityPendingIntent)
                    .build();

            startForeground(NOTIFY_ID, notification);
        }

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
                mMediaPlayer.reset();
                mPlaying = false;
                mMediaPlayer.setDataSource(mPlayingMusic.getPath());
                mMediaPlayer.prepare();
                mMediaPlayer.setLooping(mLooping);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendActionBroadcast(String action) {
            Intent intent = new Intent(action);
            intent.putExtra(KEY_PLAYING_MUSIC, mPlayingMusic);
            mLocalBroadcastManager.sendBroadcast(intent);
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
                    .putString(KEY_LIST_NAME, mListName)
                    .putInt(KEY_MUSIC_POSITION, mMusicPosition)
                    .putBoolean(KEY_LOOPING, mLooping)
                    .apply();
        }

        private void volumeTransition(float start, float end, boolean act, final String action) {
            ValueAnimator animator = ValueAnimator.ofFloat(start, end);
            animator.setDuration(1000);
            if (act) {
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        if (action.equals(ACTION_PLAY)) {
                            mMediaPlayer.setVolume(0, 0);
                            mMediaPlayer.start();
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (action.equals(ACTION_PAUSE)) {
                            mMediaPlayer.pause();
                        }
                    }
                });
            }
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float currentValue = (float) valueAnimator.getAnimatedValue();
                    mMediaPlayer.setVolume(currentValue, currentValue);
                }
            });
            animator.start();
        }

        //***********************PlayerController*****************

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
            mPlayingMusic = mMusicList.get(mMusicPosition);
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
            mPlayingMusic = mMusicList.get(mMusicPosition);
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
//                Toast.makeText(getBaseContext(), "播放下一首", Toast.LENGTH_SHORT).show();
//                next();
            }

            if (!mPlaying) {
                mPlaying = true;

                if (!mHasMediaButton) {
                    registerMediaButtonReceiver();
                }

                requestAudioFocus();

                //更新View
                mNotifyView.setImageViewResource(R.id.ibPlayPause, R.drawable.btn_pause);
                mNotifyView.setTextViewText(R.id.tvTitle, mPlayingMusic.getSongName());
                mNotifyView.setTextViewText(R.id.tvArtist, mPlayingMusic.getArtist());
                updateNotifyView();

                volumeTransition(0.0F, 1.0F, true, ACTION_PLAY);
                sendActionBroadcast(ACTION_PLAY);
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
                mNotifyView.setImageViewResource(R.id.ibPlayPause, R.drawable.btn_play);
                updateNotifyView();
//                mMediaPlayer.pause();
                volumeTransition(1.0F, 0.0F, true, ACTION_PAUSE);
                sendActionBroadcast(ACTION_PAUSE);
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
            mNotifyView.setImageViewResource(R.id.ibPlayPause, R.drawable.btn_play);
            updateNotifyView();

            abandonAudioFocus();

            mPlaying = false;
            prepare();
        }

        @Override
        public void seekTo(int msec) {
            if (mPlayingMusic == null) {
                return;
            }

            //调试
            log("seekTo");

            mMediaPlayer.seekTo(msec);
        }

        @Override
        public void shutdown() {
            //调试
            log("shutdown");

            abandonAudioFocus();

            //同时结束应用程序
            MyApplication.shutdown();
            release();
            stopSelf();
        }

        //****************MediaPlayer Listener**************

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            abandonAudioFocus();
            if (mMusicList.size() == 1) {
                mNotifyView.setImageViewResource(R.id.ibPlayPause, R.drawable.btn_play);
                updateNotifyView();
            } else {
                next();
            }
        }

        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            //调试
            log("onError");

            abandonAudioFocus();

            //更新View
            mNotifyView.setImageViewResource(R.id.ibPlayPause, R.drawable.btn_play);
            updateNotifyView();
            mPlaying = false;
            prepare();
            return true;
        }

        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {
            play();
        }
    }

    //*********************调试用**********************

    private void log(String... msg) {
        String s = "PlayerService : ";
        for (int i = 0; i < msg.length - 1; i++) {
            s += msg[i] + " : ";
        }
        s += msg[msg.length > 1 ? msg.length - 1 : 0];
        L.d(MyApplication.TAG, s);
    }

    private void logE(String... msg) {
        String s = "";
        for (int i = 0; i < msg.length - 1; i++) {
            s += msg[i] + " : ";
        }
        s += msg[msg.length > 1 ? msg.length - 1 : 0];
        L.e(MyApplication.TAG, s);
    }
}
