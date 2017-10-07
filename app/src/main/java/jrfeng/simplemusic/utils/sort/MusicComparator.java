package jrfeng.simplemusic.utils.sort;

import java.util.Comparator;

import jrfeng.musicplayer.data.Music;

public class MusicComparator {
    private MusicComparator(){}

    public static final Comparator<Music> BY_NAME = new Comparator<Music>() {
        @Override
        public int compare(Music music, Music t1) {
            return music.getSongName().compareTo(t1.getSongName());
        }
    };

    public static final Comparator<Music> BY_NAME_REVERSE = new Comparator<Music>() {
        @Override
        public int compare(Music music, Music t1) {
            return -(music.getSongName().compareTo(t1.getSongName()));
        }
    };
}
