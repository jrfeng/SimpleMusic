package jrfeng.simplemusic.activity.main;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import jrfeng.player.mode.MusicStorage;
import jrfeng.player.player.MusicPlayerClient;
import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.main.controller.ControllerFragment;
import jrfeng.simplemusic.activity.main.controller.ControllerPresenter;
import jrfeng.simplemusic.activity.main.nav.NavigationFragment;
import jrfeng.simplemusic.activity.search.SearchActivity;
import jrfeng.player.base.BaseActivity;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private Toast mExitCue;
    private long mLastPressTime;

    private FragmentManager mFmManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //调试
        if (isClientConnect()) {
            log("Client : 音乐播放器已连接");
        } else {
            log("Client : 音乐播放器未连接");
        }
        log("Client : SavedInstanceState is null :" + String.valueOf(savedInstanceState == null));

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mFmManager = getSupportFragmentManager();

        initPlayerController();
        initNavigationFragment();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        int size = mFmManager.getBackStackEntryCount();
        if (size <= 0 && keyCode == KeyEvent.KEYCODE_BACK) {
            long pressedTime = System.currentTimeMillis();
            if (pressedTime - mLastPressTime > 2000) {
                if (mExitCue == null) {
                    mExitCue = Toast.makeText(this, "再按一次回到桌面", Toast.LENGTH_SHORT);
                }
                mExitCue.show();
                mLastPressTime = pressedTime;
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    //*******************public********************

    public void startFragment(Fragment fragment) {
        mFmManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_up,
                        R.anim.alpha_out,
                        R.anim.alpha_in,
                        R.anim.slide_out_down)
                .replace(R.id.navContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void startSearchActivity(MusicStorage.GroupType groupType, String groupName) {
        Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
        intent.putExtra(SearchActivity.KEY_GROUP_TYPE, groupType.name());
        intent.putExtra(SearchActivity.KEY_GROUP_NAME, groupName);
        startActivity(intent);
    }

    //*******************private*******************

    private void initPlayerController() {
        ControllerFragment controllerFragment = (ControllerFragment) getSupportFragmentManager().findFragmentById(R.id.ctlController);
        ControllerPresenter controllerPresenter = new ControllerPresenter(this, controllerFragment);
        controllerFragment.setPresenter(controllerPresenter);
    }

    private void initNavigationFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.navContainer);
        if (fragment == null) {
            NavigationFragment navigationFragment = new NavigationFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.navContainer, navigationFragment)
                    .commit();
        }
    }

    private boolean isClientConnect() {
        return MusicPlayerClient.getInstance().isConnect();
    }

    //***************调试***************

    private static void log(String msg) {
        if (MyApplication.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
