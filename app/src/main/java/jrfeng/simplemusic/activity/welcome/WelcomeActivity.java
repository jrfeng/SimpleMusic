package jrfeng.simplemusic.activity.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import java.util.Timer;
import java.util.TimerTask;

import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.navigation.NavigationActivity;
import jrfeng.simplemusic.base.BaseActivity;
import jrfeng.simplemusic.model.MusicStorage;
import jrfeng.simplemusic.service.player.PlayerClient;
import jrfeng.simplemusic.utils.durable.Durable;

public class WelcomeActivity extends BaseActivity {
    private boolean mIsActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        init();
    }

    private void init() {
        //全屏
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        MusicStorage musicStorage = MyApplication.getInstance().getMusicStorage();
        final PlayerClient client = MyApplication.getInstance().getPlayerClient();

        if (!musicStorage.isRestored()) {
            musicStorage.restoreAsync(new Durable.OnRestoredListener() {
                @Override
                public void onRestored() {
                    client.connect();
                }
            });
        } else if (!client.isConnect()) {
            client.connect();
        }

        //启动定时器, 延时2秒
        final Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mIsActive) {
                    //启动MainActivity
                    Intent intent = new Intent(WelcomeActivity.this, NavigationActivity.class);
                    startActivity(intent);
                }
                timer.cancel();
            }
        }, 2000);
    }


    @Override
    protected void onStop() {
        super.onStop();
        mIsActive = false;
        finish();
    }

}
