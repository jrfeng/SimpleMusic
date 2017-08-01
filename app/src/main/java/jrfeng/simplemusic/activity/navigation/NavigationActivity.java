package jrfeng.simplemusic.activity.navigation;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;

import jrfeng.simplemusic.R;
import jrfeng.simplemusic.base.BaseActivity;
import jrfeng.simplemusic.service.player.PlayerService;

public class NavigationActivity extends BaseActivity {
    private NavigationContract.Presenter mPresenter;
    private NavigationContract.View mView;

    private LocalBroadcastManager lbManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        mView = new NavigationFragment();
        mPresenter = new NavigationPresenter(this, mView);
        mView.setPresenter(mPresenter);
        lbManager = LocalBroadcastManager.getInstance(this);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fmContainer, (Fragment) mView, "navigation")
                .commit();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayerService.ACTION_PLAY);
        intentFilter.addAction(PlayerService.ACTION_PAUSE);
        intentFilter.addAction(PlayerService.ACTION_NEXT);
        intentFilter.addAction(PlayerService.ACTION_PREVIOUS);
        lbManager.registerReceiver((BroadcastReceiver) mPresenter, intentFilter);
        mPresenter.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.stop();
        lbManager.unregisterReceiver((BroadcastReceiver) mPresenter);
    }
}
