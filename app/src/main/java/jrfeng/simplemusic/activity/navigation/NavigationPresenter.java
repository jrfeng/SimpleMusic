package jrfeng.simplemusic.activity.navigation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.data.Music;
import jrfeng.simplemusic.model.MusicDBHelper;
import jrfeng.simplemusic.model.MusicStorage;
import jrfeng.simplemusic.service.player.PlayerClient;

public class NavigationPresenter extends BroadcastReceiver implements NavigationContract.Presenter {
    private Context mContext;
    private NavigationContract.View mView;
    private PlayerClient mClient;
    private Music mPlayingMusic;

    private Timer mTimer;

    //用于更新主页面的“专辑”菜单项的描述和“歌手”菜单项的描述
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 1://更新“专辑”菜单的描述
                    if (message.arg1 > 0) {
                        mView.setAlbumMenuDesc(message.arg1 + "张专辑");
                    } else {
                        mView.setAlbumMenuDesc("暂无专辑");
                    }
                    break;
                case 2://更新“歌手”菜单的描述
                    if (message.arg1 > 0) {
                        mView.setArtistMenuDesc(message.arg1 + "位歌手");
                    } else {
                        mView.setArtistMenuDesc("暂无歌手");
                    }
                    break;
            }
            return true;
        }
    });

    public NavigationPresenter(Context context, NavigationContract.View view) {
        mContext = context;
        mView = view;
        mClient = MyApplication.getInstance().getPlayerClient();
    }

    @Override
    public void start() {
        refreshControllerView();
    }

    @Override
    public void stop() {
        stopProgressGenerator();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        refreshControllerView();
    }

    @Override
    public void onMenuClicked(Intent intent) {
        mContext.startActivity(intent);
    }

    @Override
    public void onPlayPauseClicked() {
        if (mClient.isPlaying()) {
            mClient.pause();
        } else {
            mClient.play();
        }
    }

    @Override
    public void onNextClicked() {
        mClient.next();
    }

    @Override
    public void onCtlMenuClicked() {
        //TODO 响应“控制器菜单”点击事件
    }

    @Override
    public void onClearRecentPlayClicked() {
        mView.refreshRecentPlayList();
    }

    @Override
    public void onSeekBarStartSeeking() {
        stopProgressGenerator();
    }

    @Override
    public void onSeekBarStopSeeking(int progress) {
        mClient.seekTo(progress);
    }

    @Override
    public void onMenuItemCreated(final int which) {
        MusicStorage musicStorage = MyApplication.getInstance().getMusicStorage();
        final SQLiteDatabase musicDB = MyApplication.getInstance().getMusicDB();
        switch (which) {
            case 0:
                int count_AllMusic = musicStorage.getListCount("所有音乐");
                if (count_AllMusic > 0) {
                    mView.setAllMusicMenuDesc(count_AllMusic + "首音乐");
                } else {
                    mView.setAllMusicMenuDesc("暂无音乐");
                }
                break;
            case 1:
                int count_ILove = musicStorage.getListCount("我喜欢");
                if (count_ILove > 0) {
                    mView.setILoveMenuDesc(count_ILove + "首音乐");
                } else {
                    mView.setILoveMenuDesc("暂无音乐");
                }
                break;
            case 2:
                int count_MusicList = musicStorage.getCustomMusicListCount();
                if (count_MusicList > 0) {
                    mView.setMusicListMenuDesc(count_MusicList + "首音乐");
                } else {
                    mView.setMusicListMenuDesc("暂无歌单");
                }
                break;
            case 3:
                new Thread() {
                    @Override
                    public void run() {
                        Cursor cursor = musicDB.query(MusicDBHelper.TABLE_MUSIC_LIST,
                                new String[]{MusicDBHelper.COLUMN_ALBUM},
                                null, null, MusicDBHelper.COLUMN_ALBUM, null, null);
                        int albumCount = cursor.getCount();
                        cursor.close();

                        Message message1 = handler.obtainMessage();
                        message1.what = 1;
                        message1.arg1 = albumCount;
                        handler.sendMessage(message1);
                    }
                }.start();
                break;
            case 4:
                new Thread() {
                    @Override
                    public void run() {
                        Cursor cursor = musicDB.query(MusicDBHelper.TABLE_MUSIC_LIST,
                                new String[]{MusicDBHelper.COLUMN_ARTIST},
                                null, null, MusicDBHelper.COLUMN_ARTIST, null, null);
                        int artistCount = cursor.getCount();
                        cursor.close();

                        Message message2 = handler.obtainMessage();
                        message2.what = 2;
                        message2.arg1 = artistCount;
                        handler.sendMessage(message2);
                    }
                }.start();
                break;
        }
    }

    //****************private***************

    private void refreshControllerView() {
        Music music = mClient.getPlayingMusic();
        if (music == null) {
            return;
        }

        if (mClient.isPlaying()) {
            mView.toggleToPlay();
            startProgressGenerator();
        } else {
            mView.toggleToPause();
            stopProgressGenerator();
        }

        mView.setProgressMax(mClient.getDuration());
        mView.setProgress(mClient.getCurrentPosition());

        if (!music.equals(mPlayingMusic)) {
            mPlayingMusic = music;
            mView.setCtlSongName(mPlayingMusic.getSongName());
            mView.setCtlArtist(mPlayingMusic.getArtist());
            mView.refreshRecentPlayList();
            //提取歌曲的封面图片
            new Thread() {
                @Override
                public void run() {
                    SQLiteDatabase musicDB = MyApplication.getInstance().getMusicDB();
                    Cursor cursor = musicDB.query(MusicDBHelper.TABLE_MUSIC_LIST,
                            new String[]{MusicDBHelper.COLUMN_IMAGE},
                            MusicDBHelper.COLUMN_PATH + " = '" + mPlayingMusic.getPath() + "'",
                            null, null, null, null);

                    byte[] image = null;
                    if (cursor.moveToFirst()) {
                        image = cursor.getBlob(cursor.getColumnIndex(MusicDBHelper.COLUMN_IMAGE));
                    }
                    cursor.close();
                    mView.setCtlImage(image);
                }
            }.start();
        }
    }

    private void startProgressGenerator() {
        stopProgressGenerator();
        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mView.setProgress(mClient.getCurrentPosition());
            }
        }, new Date(), 500);
    }

    private void stopProgressGenerator() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }
}
