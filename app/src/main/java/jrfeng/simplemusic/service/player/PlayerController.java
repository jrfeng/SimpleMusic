package jrfeng.simplemusic.service.player;

import java.util.List;

import jrfeng.simplemusic.data.Music;

public interface PlayerController {
    void previous();

    void next();

    void play();

    void play(int position);
    void play(String listName, int position);

    void pause();

    void play_pause();

    void stop();

    boolean isPlaying();

    boolean isLooping();

    Music getPlayingMusic();

    List<Music> getMusicList();

    String getCurrentListName();

    void clearRecentPlayList();

    boolean setLooping(boolean looping);

    void seekTo(int msec);

    int getDuration();

    int getCurrentPosition();

    void shutdown();

    void reload();
}
