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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        NavigationContract.View mView = new NavigationFragment();
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
        mPresenter.begin();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.end();
    }
}
