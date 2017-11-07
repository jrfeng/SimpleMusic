package jrfeng.player.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;

public class PlayerCommandReceiver extends BroadcastReceiver {
    public static final String PLAYER_PREVIOUS = "jrfeng.simplemusic.action.PLAYER_PREVIOUS";
    public static final String PLAYER_PLAY_PAUSE = "jrfeng.simplemusic.action.PLAYER_PLAY_PAUSE";
    public static final String PLAYER_NEXT = "jrfeng.simplemusic.action.PLAYER_NEXT";
    public static final String PLAYER_SHUTDOWN = "jrfeng.simplemusic.action.PLAYER_SHUTDOWN";

    private Context mContext;

    public PlayerCommandReceiver(Context context) {
        mContext = context;
    }

    public void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("jrfeng.simplemusic.action.PLAYER_PREVIOUS");
        filter.addAction("jrfeng.simplemusic.action.PLAYER_PLAY_PAUSE");
        filter.addAction("jrfeng.simplemusic.action.PLAYER_NEXT");
        filter.addAction("jrfeng.simplemusic.action.PLAYER_SHUTDOWN");
        filter.addAction("android.media.AUDIO_BECOMING_NOISY");
        mContext.registerReceiver(this, filter);
    }

    public void unregister() {
        mContext.unregisterReceiver(this);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d("MediaButtonReceiver", intent.getAction());

        MusicPlayerClient client = MusicPlayerClient.getInstance();

        if (!client.isConnect()) {
            client.connect(context.getApplicationContext(), new MusicPlayerClient.OnConnectedListener() {
                @Override
                public void onConnected() {
                    onButtonClicked(context.getApplicationContext(), intent);
                }
            });
        } else {
            onButtonClicked(context, intent);
        }
    }

    protected void onButtonClicked(Context context, Intent intent) {
        MusicPlayerClient client = MusicPlayerClient.getInstance();
        String action = intent.getAction();
        switch (action) {
            case PLAYER_PREVIOUS:
                client.previous();
                break;
            case PLAYER_PLAY_PAUSE:
                client.playPause();
                break;
            case PLAYER_NEXT:
                client.next();
                break;
            case PLAYER_SHUTDOWN:
                client.shutdown(context);
                break;
            //响应输出设备改变
            case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                client.pause();
                break;
        }
    }
}
