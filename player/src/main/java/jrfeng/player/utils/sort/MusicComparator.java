package jrfeng.player.utils.sort;

import android.support.annotation.NonNull;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.Comparator;

import jrfeng.player.data.Music;

public class MusicComparator {
    private MusicComparator() {
    }

    public static final Comparator<Music> BY_NAME = new Comparator<Music>() {
        @Override
        public int compare(Music music, Music t1) {
            char ch1 = music.getName().charAt(0);
            char ch2 = t1.getName().charAt(0);

            if (ch1 < 127 || ch2 < 127) {
                return music.getName().compareTo(t1.getName());
            } else {
                return comparePinyin(music.getName(), t1.getName());
            }
        }
    };

    public static final Comparator<Music> BY_NAME_REVERSE = new Comparator<Music>() {
        @Override
        public int compare(Music music, Music t1) {
            char ch1 = music.getName().charAt(0);
            char ch2 = t1.getName().charAt(0);
            if (ch1 < 127 || ch2 < 127) {
                return -music.getName().compareTo(t1.getName());
            } else {
                return -comparePinyin(music.getName(), t1.getName());
            }
        }
    };

    public static final Comparator<Music> BY_ARTIST = new Comparator<Music>() {
        @Override
        public int compare(Music music, Music t1) {
            char ch1 = music.getArtist().charAt(0);
            char ch2 = t1.getArtist().charAt(0);

            if (ch1 < 127 || ch2 < 127) {
                return music.getArtist().compareTo(t1.getArtist());
            } else {
                return comparePinyin(music.getArtist(), t1.getArtist());
            }
        }
    };

    public static final Comparator<Music> BY_ARTIST_REVERSE = new Comparator<Music>() {
        @Override
        public int compare(Music music, Music t1) {
            char ch1 = music.getArtist().charAt(0);
            char ch2 = t1.getArtist().charAt(0);

            if (ch1 < 127 || ch2 < 127) {
                return -music.getArtist().compareTo(t1.getArtist());
            } else {
                return -comparePinyin(music.getArtist(), t1.getArtist());
            }
        }
    };

    //***************private*************

    @NonNull
    private static String concatPinyinStringArray(String[] pinyinArray) {
        StringBuilder pinyinSbf = new StringBuilder();
        if ((pinyinArray != null) && (pinyinArray.length > 0)) {
            for (String aPinyinArray : pinyinArray) {
                pinyinSbf.append(aPinyinArray);
            }
        }
        return pinyinSbf.toString();
    }

    private static int comparePinyin(String s1, String s2) {
        int s1Length = s1.length();
        int s2Length = s2.length();

        for (int i = 0; i < Math.min(s1Length, s2Length); i++) {
            int result = comparePinyinChar(s1.charAt(i), s2.charAt(i));
            if (result != 0) {
                return result;
            }
        }

        return s2Length - s1Length;
    }

    private static int comparePinyinChar(char ch1, char ch2) {
        return concatPinyinStringArray(PinyinHelper.toHanyuPinyinStringArray(ch1))
                .compareTo(
                        concatPinyinStringArray(PinyinHelper.toHanyuPinyinStringArray(ch2))
                );
    }
}
