package jrfeng.simplemusic.receiver;

public interface PlayerActionDisposer {
    void onPlay();

    void onPause();

    void onNext();

    void onPrevious();

    void onStop();

    void onPrepared();

    void onError();

    void onMusicNotExist();
}
