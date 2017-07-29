package jrfeng.simplemusic.service.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;

import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.model.MusicStorage;
import jrfeng.simplemusic.utils.durable.Durable;

public class MediaButtonControlReceiver extends BroadcastReceiver {
    public static final String PLAYER_PREVIOUS = "jrfeng.simplemusic.action.PLAYER_PREVIOUS";
    public static final String PLAYER_PLAY_PAUSE = "jrfeng.simplemusic.action.PLAYER_PLAY_PAUSE";
    public static final String PLAYER_NEXT = "jrfeng.simplemusic.action.PLAYER_NEXT";
    public static final String PLAYER_SHUTDOWN = "jrfeng.simplemusic.action.PLAYER_SHUTDOWN";

    @Override
    public void onReceive(Context context, final Intent intent) {
        MusicStorage musicStorage = MyApplication.getInstance().getMusicStorage();
        final PlayerClient client = MyApplication.getInstance().getPlayerClient();

        if (!musicStorage.isRestored()) {
            musicStorage.restoreAsync(new Durable.OnRestoredListener() {
                @Override
                public void onRestored() {
                    client.connect(new PlayerClient.OnConnectedListener() {
                        @Override
                        public void onConnected() {
                            onButtonClicked(intent);
                        }
                    });
                }
            });
        } else if (!client.isConnect()) {
            client.connect(new PlayerClient.OnConnectedListener() {
                @Override
                public void onConnected() {
                    onButtonClicked(intent);
                }
            });
        } else {
            onButtonClicked(intent);
        }
    }

    private void onButtonClicked(Intent intent) {
        PlayerClient client = MyApplication.getInstance().getPlayerClient();
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
                client.shutdown();
                break;
            //响应输出设备改变
            case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                client.pause();
                break;
            //响应MediaButtons
            case Intent.ACTION_MEDIA_BUTTON:
                onMediaButtonsClicked(intent, client);
                break;
        }
    }

    private void onMediaButtonsClicked(Intent intent, PlayerClient client) {
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
