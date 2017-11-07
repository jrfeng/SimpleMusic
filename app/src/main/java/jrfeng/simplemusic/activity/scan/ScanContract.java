package jrfeng.simplemusic.activity.scan;

import java.util.List;

import jrfeng.player.data.Music;
import jrfeng.simplemusic.base.BasePresenter;
import jrfeng.simplemusic.base.BaseView;

/**
 * Created by ASUS on 2017/9/14.
 * Author JRFeng
 */
class ScanContract {
    interface View extends BaseView<Presenter> {
        void refreshView(int percent, String path, int count);
        void resetView();
        void showScannedMusic(List<Music> scannedMusics);
    }

    interface Presenter extends BasePresenter {
        void startScan();
        void stopScan();
    }
}
