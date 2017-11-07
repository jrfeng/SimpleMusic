package jrfeng.player.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class MediaButtonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d("MediaButtonReceiver", intent.getAction());

        MusicPlayerClient client = MusicPlayerClient.getInstance();

        if (!client.isConnect()) {
            client.connect(context.getApplicationContext(), new MusicPlayerClient.OnConnectedListener() {
                @Override
                public void onConnected() {
                    onMediaButtonsClicked(intent);
                }
            });
        } else {
            onMediaButtonsClicked(intent);
        }
    }

    private void onMediaButtonsClicked(Intent intent) {
        MusicPlayerClient client = MusicPlayerClient.getInstance();
        KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    client.playPause();
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
                    client.playPause();
                    break;
            }
        }
    }
}
