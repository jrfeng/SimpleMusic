package jrfeng.musicplayer.player;

import android.content.Context;

import java.util.List;

import jrfeng.musicplayer.data.Music;

interface MusicPlayerController {

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

    boolean isPrepared();

    Music getPlayingMusic();

    int getPlayingMusicIndex();

    List<Music> getMusicList();

    String getCurrentListName();

    boolean setLooping(boolean looping);

    void seekTo(int msec);

    int getMusicLength();

    int getMusicProgress();

    void shutdown(Context context);

    MusicProvider getMusicProvider();

    void addMusicProgressListener(MusicPlayerClient.MusicProgressListener listener);

    void removeMusicProgressListener(MusicPlayerClient.MusicProgressListener listener);
}
