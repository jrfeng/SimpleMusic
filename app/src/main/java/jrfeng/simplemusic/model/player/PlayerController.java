package jrfeng.simplemusic.model.player;

public interface PlayerController {
    void previous();

    void next();

    void play();

    void pause();

    void play_pause();

    void stop();

    void seekTo(float percent);

    void shutdown();

    void reload();
}
