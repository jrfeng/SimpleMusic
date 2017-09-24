package jrfeng.simplemusic.activity.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jrfeng.simplemusic.MyApplication;
import jrfeng.musicplayer.data.Music;
import jrfeng.simplemusic.model.MusicDBHelper;
import jrfeng.simplemusic.model.MusicStorage;
import jrfeng.musicplayer.player.MusicPlayerClient;
//import jrfeng.musicplayer.player.MusicPlayerService;
import jrfeng.simplemusic.utils.durable.DurableList;

public class NavigationPresenter extends BroadcastReceiver implements NavigationContract.Presenter {
    private Context mContext;
    private NavigationContract.View mView;
    private MusicPlayerClient mClient;
    private Music mPlayingMusic;

    private int mAlbumCount;
    private int mArtistCount;

    private List<Music> mAllMusicList;

    private MusicPlayerClient.MusicProgressListener mProgressListener = new MusicPlayerClient.MusicProgressListener() {
        @Override
        public void onProgressUpdated(int progress) {
            mView.setProgress(progress);
        }
    };

    private Comparator<Music> timeASC = new Comparator<Music>() {
        @Override
        public int compare(Music music, Music t1) {
            return (int) (music.getAddTimeMsec() - t1.getAddTimeMsec());
        }
    };

    private Comparator<Music> timeDESC = Collections.reverseOrder(timeASC);

    private Comparator<Music> nameASC = new Comparator<Music>() {
        @Override
        public int compare(Music music, Music t1) {
            return music.getSongName().compareTo(t1.getSongName());
        }
    };

    private Comparator<Music> nameDESC = Collections.reverseOrder(nameASC);

    //用于更新主页面的“专辑”菜单项的描述和“歌手”菜单项的描述
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.what == 1) {
                if (mAlbumCount > 0) {
                    mView.setAlbumMenuDesc(mAlbumCount + "张专辑");
                } else {
                    mView.setAlbumMenuDesc("暂无专辑");
                }

                if (mArtistCount > 0) {
                    mView.setArtistMenuDesc(mArtistCount + "位歌手");
                } else {
                    mView.setArtistMenuDesc("暂无歌手");
                }
            }
            return true;
        }
    });

    public NavigationPresenter(Context context, NavigationContract.View view) {
        mContext = context;
        mView = view;
        mClient = MyApplication.getInstance().getPlayerClient();
        mAllMusicList = mClient.getMusicProvider().getMusicList("所有音乐");
        updateAlbumCountAndArtistCount();
    }

    @Override
    public void begin() {
        refreshControllerView();
        refreshAllMusicList();
        mView.setChoice(mClient.getPlayingMusicIndex());
        mClient.addMusicProgressListener(mProgressListener);
    }

    @Override
    public void end() {
        mClient.removeMusicProgressListener(mProgressListener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(MyApplication.TAG, "接收广播 : " + intent.getAction());
        switch (intent.getAction()) {
            case MusicPlayerClient.Action.ACTION_ERROR:
                Toast.makeText(mContext, "Sorry!发生了异常", Toast.LENGTH_SHORT).show();
                break;
            case MusicPlayerClient.Action.ACTION_PREPARED:
                refreshControllerView();
                break;
            case MusicPlayerClient.Action.ACTION_MUSIC_NOT_EXIST:
            case MusicPlayerClient.Action.ACTION_PLAY:
                Music playingMusic = (Music) intent.getSerializableExtra(MusicPlayerClient.Key.KEY_PLAYING_MUSIC);
                int position = mAllMusicList.indexOf(playingMusic);
                mView.setChoice(position);
                mView.scrollTo(position);     //发生歌曲切换时自动跳转到列表的指定位置
                Log.d("Position", "" + position);
                refreshControllerView();
                break;
            case MusicPlayerClient.Action.ACTION_PAUSE:
                refreshControllerView();
                break;
        }

        MusicStorage musicStorage = (MusicStorage) mClient.getMusicProvider();
        int count_RecentPlay = musicStorage.getListCount("最近播放");
        if (count_RecentPlay > 0) {
            mView.setRecentPlayMenuDesc(count_RecentPlay + "条记录");
        } else {
            mView.setRecentPlayMenuDesc("暂无记录");
        }
    }

    @Override
    public void onMenuClicked(Intent intent) {
        mContext.startActivity(intent);
    }

    @Override
    public void onPlayPauseClicked() {
        mClient.play_pause();
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
    public void onSeekBarStartSeeking() {

    }

    @Override
    public void onSeekBarStopSeeking(int progress) {
        mClient.seekTo(progress);
    }

    @Override
    public void onMenuItemCreated(final int which) {
        MusicStorage musicStorage = (MusicStorage) mClient.getMusicProvider();
        switch (which) {
            case 0:
                int count_ILove = musicStorage.getListCount("我喜欢");
                if (count_ILove > 0) {
                    mView.setILoveMenuDesc(count_ILove + "首音乐");
                } else {
                    mView.setILoveMenuDesc("暂无音乐");
                }
                break;
            case 1:
                int count_MusicList = musicStorage.getCustomMusicListCount();
                if (count_MusicList > 0) {
                    mView.setMusicListMenuDesc(count_MusicList + "首音乐");
                } else {
                    mView.setMusicListMenuDesc("暂无歌单");
                }
                break;
            case 2:
                if (mAlbumCount > 0) {
                    mView.setAlbumMenuDesc(mAlbumCount + "张专辑");
                } else {
                    mView.setAlbumMenuDesc("暂无专辑");
                }
                break;
            case 3:
                if (mArtistCount > 0) {
                    mView.setArtistMenuDesc(mArtistCount + "位歌手");
                } else {
                    mView.setArtistMenuDesc("暂无歌手");
                }
                break;
            case 4:
                int count_RecentPlay = musicStorage.getListCount("最近播放");
                if (count_RecentPlay > 0) {
                    mView.setRecentPlayMenuDesc(count_RecentPlay + "条记录");
                } else {
                    mView.setRecentPlayMenuDesc("暂无记录");
                }
                break;
        }
    }

    @Override
    public void onListItemClicked(String listName, int position) {
        refreshControllerView();
        if (mClient.getPlayingMusicIndex() == position) {
            mClient.play_pause();
        } else {
            mClient.play(listName, position);
            mView.setChoice(position);
        }
    }

    @Override
    public void onListItemMenuClicked(int position) {
        mView.showListItemMenu(mAllMusicList.get(position));
    }

    @Override
    public void onTitleSortButtonClicked() {
        mView.showSortMenu();
    }

    @Override
    public void onTitleMenuButtonClicked() {
        mView.showTitleMenu();
    }

    @Override
    public void onTitleLocateButtonClicked() {
        mView.scrollTo(mClient.getPlayingMusicIndex());
    }

    @Override
    public List<Music> getAllMusicList() {
        return mAllMusicList;
    }

    @Override
    public int getAllMusicListSize() {
        return mAllMusicList.size();
    }

    //****************private***************

    private void refreshControllerView() {
        Music music = mClient.getPlayingMusic();
        if (music == null) {
            return;
        }

        Log.d("Navigation", "刷新ControllerView");

        if (mClient.isPlaying()) {
            mView.toggleToPlay();
        } else {
            mView.toggleToPause();
        }

        if (mClient.isPrepared()) {
            mView.setProgressMax(mClient.getMusicLength());
            mView.setProgress(mClient.getMusicProgress());
        }else {
            mView.setProgressMax(100);
            mView.setProgress(0);
        }

        if (!music.equals(mPlayingMusic)) {
            mPlayingMusic = music;
            mView.setCtlSongName(mPlayingMusic.getSongName());
            mView.setCtlArtist(mPlayingMusic.getArtist());
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

    private void refreshAllMusicList() {
        if (((DurableList) mAllMusicList).isChanged()) {
            mView.updateAllMusicList();
            mView.updateAllMusicListTitle();
            //同时更新 mAlbumCount 和 mArtistCount
            updateAlbumCountAndArtistCount();
        }
    }

    private void updateAlbumCountAndArtistCount() {
        final SQLiteDatabase musicDB = MyApplication.getInstance().getMusicDB();
        new Thread() {
            @Override
            public void run() {
                Cursor cursor1 = musicDB.query(MusicDBHelper.TABLE_MUSIC_LIST,
                        new String[]{MusicDBHelper.COLUMN_ALBUM},
                        null, null, MusicDBHelper.COLUMN_ALBUM, null, null);
                mAlbumCount = cursor1.getCount();
                cursor1.close();

                Cursor cursor2 = musicDB.query(MusicDBHelper.TABLE_MUSIC_LIST,
                        new String[]{MusicDBHelper.COLUMN_ARTIST},
                        null, null, MusicDBHelper.COLUMN_ARTIST, null, null);
                mArtistCount = cursor2.getCount();
                cursor2.close();

                Message message = handler.obtainMessage();
                message.what = 1;
                handler.sendMessage(message);
            }
        }.start();
    }
}
