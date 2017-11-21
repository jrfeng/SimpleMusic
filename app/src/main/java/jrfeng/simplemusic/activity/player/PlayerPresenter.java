package jrfeng.simplemusic.activity.player;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import jrfeng.player.data.Music;
import jrfeng.player.mode.MusicStorage;
import jrfeng.player.player.MusicPlayerClient;
import jrfeng.player.utils.mp3.MP3Util;
import jrfeng.simplemusic.receiver.PlayerActionDisposerAdapter;
import jrfeng.simplemusic.receiver.PlayerActionReceiver;

public class PlayerPresenter extends PlayerActionDisposerAdapter implements PlayerContract.Presenter {
    private Context mContext;
    private PlayerContract.View mView;
    private MusicPlayerClient mClient;
    private MusicStorage mMusicStorage;
    private PlayerActionReceiver mPlayerActionReceiver;

    private MusicPlayerClient.PlayerProgressListener mProgressListener;

    private boolean mImageAlreadySet;

    public PlayerPresenter(Context context, PlayerContract.View view) {
        mContext = context;
        mView = view;
        mClient = MusicPlayerClient.getInstance();
        mMusicStorage = mClient.getMusicStorage();
        mPlayerActionReceiver = new PlayerActionReceiver(mContext, this);
        mProgressListener = new MusicPlayerClient.PlayerProgressListener() {
            @Override
            public void onProgressUpdated(int progress) {
                mView.setSongProgress(progress);
            }
        };
    }

    @Override
    public void onPlay() {
        mView.viewStart();
    }

    @Override
    public void onPause() {
        mView.viewPause();
    }

    @Override
    public void onStop() {
        mView.viewStop();
    }

    @Override
    public void onError() {
        Toast.makeText(mContext, "抱歉 出错了", Toast.LENGTH_SHORT).show();
        mView.viewStop();
    }

    @Override
    public void begin() {
        mPlayerActionReceiver.register();
        mClient.addMusicProgressListener(mProgressListener);
        updateView();
    }

    @Override
    public void end() {
        mPlayerActionReceiver.unregister();
        mClient.removeMusicProgressListener(mProgressListener);
        mView.viewStop();
    }

    @Override
    public void onPrepared() {
        mImageAlreadySet = false;
        updateView();
        mView.viewStop();
    }

    @Override
    public int getAudioSessionId() {
        return mClient.getAudioSessionId();
    }

    @Override
    public void musicSeekTo(int progress) {
        if (!mClient.isPrepared()) {
            mView.setSongProgress(0);
        }
        mClient.seekTo(progress);
    }

    @Override
    public void playerNext() {
        //调试
        Log.d("PlayerPresenter", "playerNext");

        mClient.next();
    }

    @Override
    public void playerPrevious() {
        mClient.previous();
    }

    @Override
    public void playerPlayPause() {
        mClient.playPause();
    }

    @Override
    public void loveOrNotLovePlayingMusic() {
        Music music = mClient.getPlayingMusic();
        if (music != null) {
            if (isILove(music)) {
                mView.love(false);
                Toast.makeText(mContext, "取消喜欢", Toast.LENGTH_SHORT).show();
                mMusicStorage.removeMusicFromILove(music);
            } else {
                mView.love(true);
                Toast.makeText(mContext, "我喜欢", Toast.LENGTH_SHORT).show();
                mMusicStorage.addMusicToILove(music);
            }
        }
    }

    @Override
    public void setPlayMode(MusicPlayerClient.PlayMode mode) {
        mClient.setPlayMode(mode);
    }

    @Override
    public MusicPlayerClient.PlayMode getPlayMode() {
        return mClient.getPlayMode();
    }

    private void updateView() {
        Music music = mClient.getPlayingMusic();
        if (music == null) {
            return;
        }

        mView.setSongName(music.getName());
        mView.setSongArtist(music.getArtist());

        mView.setSongProgressLength(mClient.getMusicLength());

        if (isPlaying()) {
            mView.viewStart();
        }

        mView.love(isILove(music));

        if (!mImageAlreadySet) {
            mImageAlreadySet = true;

            //从本地加载
            new AsyncTask<Void, Void, byte[]>() {
                @Override
                protected byte[] doInBackground(Void... voids) {
                    Music music = mClient.getPlayingMusic();
                    if (music == null) {
                        return null;
                    }

                    return MP3Util.getMp3Image(new File(music.getPath()));
                }

                @Override
                protected void onPostExecute(byte[] data) {
                    mView.setSongImage(data);
                }
            }.execute();
        }
    }

    private boolean isPlaying() {
        return mClient.isPlaying();
    }

    private boolean isILove(Music music) {
        return mMusicStorage.getILove().contains(music);
    }
}
