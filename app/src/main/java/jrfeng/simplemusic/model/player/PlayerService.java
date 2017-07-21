package jrfeng.simplemusic.model.player;

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
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.List;

import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.main.MainActivity;
import jrfeng.simplemusic.data.Music;
import jrfeng.simplemusic.model.MusicStorage;
import jrfeng.simplemusic.utils.L;

public class PlayerService extends Service {
    private RemoteViews mNotifyView;
    private MusicStorage mMusicStorage;
    private SharedPreferences mPreferences;
    private PendingIntent mWelcomeActivityPendingIntent;

    private static final int NOTIFY_ID = 1;
    private static final String PREFERENCES_NAME = "player_state.dat";

    private static final String KEY_LIST_NAME = "listName";
    private static final String KEY_MUSIC_POSITION = "musicPosition";
    private static final String KEY_PLAYING_POSITION = "playingPosition";
    private static final String KEY_LOOPING = "looping";

    @Override
    public void onCreate() {
        super.onCreate();
        //调试
        log("onCreate");
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

        //***************测试用*************
//        String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
//        List<Music> musicList = new LinkedList<>();
//        musicList.add(new Music(dir + "/想要去的地方.mp3", "想要去的地方", "关晓彤", "未知", "未知", "未知"));
//        musicList.add(new Music(dir + "/卑微的承诺.mp3", "卑微的承诺", "乔洋", "未知", "未知", "未知"));
//        controller.load(musicList);
        //**********************************

        String listName = mPreferences.getString(KEY_LIST_NAME, "所有音乐");
        int musicPosition = mPreferences.getInt(KEY_MUSIC_POSITION, 0);
        int playingPosition = mPreferences.getInt(KEY_PLAYING_POSITION, 0);
        boolean looping = mPreferences.getBoolean(KEY_LOOPING, false);

        controller.load(
                listName,
                musicPosition,
                playingPosition,
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
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(mWelcomeActivityPendingIntent)
                .setCustomContentView(mNotifyView)
                .build();

        startForeground(NOTIFY_ID, notification);
    }

    private void registerMediaButtonReceiver() {
        //将应用程序注册为MediaButton的唯一处理程序
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        ComponentName componentName = new ComponentName(getBaseContext(), MediaButtonControlReceiver.class);
        audioManager.registerMediaButtonEventReceiver(componentName);
    }

    //********************Controller*********************

    public class Controller extends Binder implements PlayerController,
            MediaPlayer.OnErrorListener,
            MediaPlayer.OnCompletionListener,
            MediaPlayer.OnSeekCompleteListener {
        private MediaPlayer mMediaPlayer;
        private Music mPlayingMusic;
        private int mPlayingPosition;
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
                private boolean transientLoss;

                @Override
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            //暂停播放
                            pause();
                            transientLoss = true;
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS:
                            //停止播放，并注销媒体按钮 Receiver
                            pause();
                            transientLoss = false;
                            mAudioManager.unregisterMediaButtonEventReceiver(
                                    new ComponentName(getBaseContext(), MediaButtonControlReceiver.class));
                            break;
                        case AudioManager.AUDIOFOCUS_GAIN:
                            if (transientLoss) {
                                play();
                            } else {
                                registerMediaButtonReceiver();
                            }
                            break;
                    }
                }
            };

            //请求音频焦点
            mAudioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }

        //**************************public***********************

        public void load(String listName, int musicPosition, int playingPosition, boolean looping) {
            //调试
            log("load");

            mListName = listName;
            mMusicList = mMusicStorage.getMusicList(mListName);
            mMusicPosition = musicPosition;
            mPlayingPosition = playingPosition;
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
            if (mPlayingMusic == null) {
                mPlayingMusic = mMusicList.get(mMusicPosition);
                mNotifyView.setTextViewText(R.id.tvTitle, mPlayingMusic.getSongName());
                mNotifyView.setTextViewText(R.id.tvArtist, mPlayingMusic.getArtist());
                updateNotifyView();
                prepare();
            }
        }

        public boolean isPlaying() {
            return mPlaying;
        }

        //********************private***********************

        private void updateNotifyView() {
            Notification notification = new NotificationCompat.Builder(getBaseContext())
                    .setSmallIcon(R.mipmap.ic_launcher)
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
                if (mPlayingPosition > 0) {
                    mMediaPlayer.seekTo(mPlayingPosition);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void saveState() {
            //调试
            log("saveState");

            if (mPlayingMusic == null) {
                mPreferences.edit()
                        .putString(KEY_LIST_NAME, mListName)
                        .putInt(KEY_MUSIC_POSITION, mMusicPosition)
                        .putInt(KEY_PLAYING_POSITION, 0)
                        .putBoolean(KEY_LOOPING, mLooping)
                        .apply();
            } else {
                mPreferences.edit()
                        .putString(KEY_LIST_NAME, mListName)
                        .putInt(KEY_MUSIC_POSITION, mMusicPosition)
                        .putInt(KEY_PLAYING_POSITION, mMediaPlayer.getCurrentPosition())
                        .putBoolean(KEY_LOOPING, mLooping)
                        .apply();
            }
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
            mPlayingPosition = 0;
            prepare();
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
            mPlayingPosition = 0;
            prepare();
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

            //更新View
            mNotifyView.setImageViewResource(R.id.ibPlayPause, R.drawable.btn_pause);
            mNotifyView.setTextViewText(R.id.tvTitle, mPlayingMusic.getSongName());
            mNotifyView.setTextViewText(R.id.tvArtist, mPlayingMusic.getArtist());
            updateNotifyView();

            if (!mPlaying) {
                mPlaying = true;
                mMediaPlayer.start();
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

            //更新View
            mNotifyView.setImageViewResource(R.id.ibPlayPause, R.drawable.btn_play);
            updateNotifyView();

            if (mPlaying) {
                mPlaying = false;
                mMediaPlayer.pause();
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


            mPlaying = false;
            prepare();
            mPlayingPosition = 0;
        }

        @Override
        public void seekTo(float percent) {
            //调试
            log("seekTo");

            if (mPlayingMusic == null) {
                //调试
                logE("PlayingMusic is Null");
                return;
            }

            //TODO seek逻辑
        }

        @Override
        public void shutdown() {
            //调试
            log("shutdown");

            //放弃音频焦点
            mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
            //注销媒体按钮
            mAudioManager.unregisterMediaButtonEventReceiver(
                    new ComponentName(getBaseContext(), MediaButtonControlReceiver.class));

            //同时结束应用程序
            MyApplication.shutdown();
            release();
            stopSelf();
        }

        //****************MediaPlayer Listener**************

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
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

            //更新View
            mNotifyView.setImageViewResource(R.id.ibPlayPause, R.drawable.btn_play);
            updateNotifyView();
            mPlaying = false;
            mPlayingPosition = 0;
            prepare();
            return true;
        }

        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {
            //TODO seek逻辑
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
