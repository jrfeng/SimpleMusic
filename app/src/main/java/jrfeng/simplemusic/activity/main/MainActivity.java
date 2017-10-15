package jrfeng.simplemusic.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.main.controller.ControllerFragment;
import jrfeng.simplemusic.activity.main.controller.ControllerPresenter;
import jrfeng.simplemusic.activity.main.nav.NavigationFragment;
import jrfeng.simplemusic.activity.main.nav.NavigationPresenter;
import jrfeng.simplemusic.activity.scan.ScanActivity;
import jrfeng.simplemusic.base.BaseActivity;
import jrfeng.simplemusic.base.BasePresenter;

public class MainActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPlayerController();
        initNavigationFragment();
    }

    private void initPlayerController() {
        ControllerFragment controllerFragment = (ControllerFragment) getSupportFragmentManager().findFragmentById(R.id.fmController);
        ControllerPresenter controllerPresenter = new ControllerPresenter(this, controllerFragment);
        controllerFragment.setPresenter(controllerPresenter);
    }

    private void initNavigationFragment() {
        NavigationFragment navigationFragment = new NavigationFragment();
        NavigationPresenter navigationPresenter = new NavigationPresenter(this, navigationFragment);
        navigationFragment.setPresenter(navigationPresenter);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fmContainer, navigationFragment)
                .commit();
    }
}
