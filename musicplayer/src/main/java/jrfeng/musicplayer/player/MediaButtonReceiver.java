package jrfeng.musicplayer.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

public class MediaButtonReceiver extends BroadcastReceiver {
    public static final String PLAYER_PREVIOUS = "jrfeng.simplemusic.action.PLAYER_PREVIOUS";
    public static final String PLAYER_PLAY_PAUSE = "jrfeng.simplemusic.action.PLAYER_PLAY_PAUSE";
    public static final String PLAYER_NEXT = "jrfeng.simplemusic.action.PLAYER_NEXT";
    public static final String PLAYER_SHUTDOWN = "jrfeng.simplemusic.action.PLAYER_SHUTDOWN";

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
        }else {
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
                client.play_pause();
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
            //响应MediaButtons
            case Intent.ACTION_MEDIA_BUTTON:
                onMediaButtonsClicked(intent);
                break;
        }
    }

    private void onMediaButtonsClicked(Intent intent) {
        MusicPlayerClient client = MusicPlayerClient.getInstance();
        KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    client.play_pause();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    client.play();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    client.pause();
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    client.stop();
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    client.next();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    client.previous();
                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    //耳机中间的按键，功能与PLAY_PAUSE一样
                    client.play_pause();
                    break;
            }
        }
    }
}
