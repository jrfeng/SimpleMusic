package jrfeng.simplemusic.activity.main;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import jrfeng.simplemusic.R;
import jrfeng.simplemusic.base.BaseActivity;
import jrfeng.musicplayer.player.MusicPlayerService;

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
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicPlayerService.ACTION_PLAY);
        intentFilter.addAction(MusicPlayerService.ACTION_PAUSE);
        intentFilter.addAction(MusicPlayerService.ACTION_NEXT);
        intentFilter.addAction(MusicPlayerService.ACTION_PREVIOUS);
        registerReceiver((BroadcastReceiver) mPresenter, intentFilter);
        mPresenter.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.stop();
        unregisterReceiver((BroadcastReceiver) mPresenter);
    }
}
