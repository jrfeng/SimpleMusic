package jrfeng.simplemusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import jrfeng.player.player.MusicPlayerClient;

public class PlayerActionReceiver extends BroadcastReceiver {
    private PlayerActionDisposer mDisposer;
    private Context mContext;

    public PlayerActionReceiver(Context context, PlayerActionDisposer disposer) {
        mContext = context;
        mDisposer = disposer;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case MusicPlayerClient.Action.ACTION_PLAY:
                mDisposer.onPlay();
                break;
            case MusicPlayerClient.Action.ACTION_PAUSE:
                mDisposer.onPause();
                break;
            case MusicPlayerClient.Action.ACTION_NEXT:
                mDisposer.onNext();
                break;
            case MusicPlayerClient.Action.ACTION_PREVIOUS:
                mDisposer.onPrevious();
                break;
            case MusicPlayerClient.Action.ACTION_STOP:
                mDisposer.onStop();
                break;
            case MusicPlayerClient.Action.ACTION_PREPARED:
                mDisposer.onPrepared();
                break;
            case MusicPlayerClient.Action.ACTION_ERROR:
                mDisposer.onError();
                break;
            case MusicPlayerClient.Action.ACTION_MUSIC_NOT_EXIST:
                mDisposer.onMusicNotExist();
                break;
            case MusicPlayerClient.Action.ACTION_RESET:
                mDisposer.onReset();
                break;
        }
    }

    public void register() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_PLAY);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_PAUSE);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_NEXT);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_PREVIOUS);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_STOP);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_PREPARED);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_ERROR);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_MUSIC_NOT_EXIST);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_RESET);
        mContext.registerReceiver(this, intentFilter);
    }

    public void unregister() {
        mContext.unregisterReceiver(this);
    }
}
