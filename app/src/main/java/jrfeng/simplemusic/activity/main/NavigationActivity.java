package jrfeng.simplemusic.activity.main;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import jrfeng.musicplayer.player.MusicPlayerClient;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.base.BaseActivity;

public class NavigationActivity extends BaseActivity {
    private NavigationContract.Presenter mPresenter;
    private NavigationContract.View mView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        mView = new NavigationFragment();
        mPresenter = new NavigationPresenter(this, mView);
        mView.setPresenter(mPresenter);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fmContainer, (Fragment) mView, "navigation")
                .commit();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_PLAY);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_PAUSE);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_NEXT);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_PREVIOUS);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_STOP);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_PREPARED);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_ERROR);
        intentFilter.addAction(MusicPlayerClient.Action.ACTION_MUSIC_NOT_EXIST);
        registerReceiver((BroadcastReceiver) mPresenter, intentFilter);
        mPresenter.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver((BroadcastReceiver) mPresenter);
        mPresenter.stop();
    }
}
