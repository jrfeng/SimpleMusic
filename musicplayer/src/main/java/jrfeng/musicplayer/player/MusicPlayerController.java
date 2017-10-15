package jrfeng.musicplayer.player;

import android.content.Context;

import java.util.List;

import jrfeng.musicplayer.data.Music;
import jrfeng.musicplayer.mode.MusicStorage;

interface MusicPlayerController {

    void previous();

    void next();

    void play();

    void play(int position);

    void loadMusicGroup(MusicStorage.GroupType groupType, String groupName, int position, boolean play);

    void playMusicGroup(MusicStorage.GroupType groupType, String groupName, int position);

    void pause();

    void playPause();

    void stop();

    boolean isPlaying();

    boolean isLooping();

    boolean isPrepared();

    Music getPlayingMusic();

    int getPlayingMusicIndex();

    List<Music> getMusicList();

    MusicStorage.GroupType getMusicGroupType();

    String getMusicGroupName();

    List<Music> getRecentPlayList();

    int getRecentPlayCount();

    boolean setPlayMode(MusicPlayerClient.PlayMode mode);

    MusicPlayerClient.PlayMode getPlayMode();

    void addTempPlayMusic(Music music);

    boolean isTempPlay();

    void seekTo(int msec);

    int getMusicLength();

    int getMusicProgress();

    void shutdown(Context context);

    void addMusicProgressListener(MusicPlayerClient.MusicProgressListener listener);

    void removeMusicProgressListener(MusicPlayerClient.MusicProgressListener listener);
}
