package jrfeng.simplemusic.service.player;

public interface PlayerController {
    void previous();

    void next();

    void play();

    void pause();

    void play_pause();

    void stop();

    boolean isPlaying();

    boolean isLooping();

    boolean setLooping(boolean looping);

    void seekTo(int msec);

    int getDuration();

    int getCurrentPosition();

    void shutdown();

    void reload();
}
