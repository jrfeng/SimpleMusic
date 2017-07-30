package jrfeng.simplemusic.activity.navigation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.service.player.PlayerClient;
import jrfeng.simplemusic.service.player.PlayerService;

public class NavigationPresenter extends BroadcastReceiver implements NavigationContract.Presenter {
    private Context mContext;
    private NavigationContract.View mView;
    private PlayerClient mClient;

    public NavigationPresenter(Context context, NavigationContract.View view) {
        mContext = context;
        mView = view;
        mClient = MyApplication.getInstance().getPlayerClient();
    }

    @Override
    public void start() {
        if(mClient.isPlaying()){
            mView.toggleToPlay();
        }else {
            mView.toggleToPause();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("App", "Ctl Received : " + action);
        switch (action) {
            case PlayerService.ACTION_PAUSE:
                mView.toggleToPause();
                break;
            case PlayerService.ACTION_PLAY:
                mView.toggleToPlay();
                break;
        }
    }

    @Override
    public void menuClicked(Intent intent) {
        mContext.startActivity(intent);
    }

    @Override
    public void playPauseClicked() {
        if (mClient.isPlaying()) {
            mClient.pause();
        } else {
            mClient.play();
        }
    }

    @Override
    public void nextClicked() {
        mClient.next();
    }

    @Override
    public void ctlMenuClicked() {

    }
}
