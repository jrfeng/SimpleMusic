package jrfeng.musicplayer.player;

import android.content.Context;

import java.util.List;

import jrfeng.musicplayer.data.Music;

public interface MusicProvider {
    String DEFAULT_MUSIC_LIST = "default_music_list";
    void initDataSet(Context context);
    void saveDataSet();
    List<Music> getMusicList(String listName);
}
