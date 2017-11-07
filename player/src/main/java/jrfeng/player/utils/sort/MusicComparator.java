package jrfeng.player.utils.sort;

import java.util.Comparator;

import jrfeng.player.data.Music;

public class MusicComparator {
    private MusicComparator(){}

    public static final Comparator<Music> BY_NAME = new Comparator<Music>() {
        @Override
        public int compare(Music music, Music t1) {
            return music.getName().compareTo(t1.getName());
        }
    };

    public static final Comparator<Music> BY_NAME_REVERSE = new Comparator<Music>() {
        @Override
        public int compare(Music music, Music t1) {
            return -(music.getName().compareTo(t1.getName()));
        }
    };
}
