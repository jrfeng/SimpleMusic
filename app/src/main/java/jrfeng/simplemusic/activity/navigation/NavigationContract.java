package jrfeng.simplemusic.activity.navigation;

import android.content.Intent;

import jrfeng.simplemusic.base.BasePresenter;
import jrfeng.simplemusic.base.BaseView;

public interface NavigationContract {
    interface View extends BaseView<Presenter> {
        void toggleToPlay();
        void toggleToPause();
        void setProgress(float percent);
    }

    interface Presenter extends BasePresenter {
        void menuClicked(Intent intent);

        void playPauseClicked();

        void nextClicked();

        void ctlMenuClicked();
    }
}
