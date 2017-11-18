package jrfeng.player.utils.sort;

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
                return concatPinyinStringArray(PinyinHelper.toHanyuPinyinStringArray(ch1))
                        .compareTo(
                                concatPinyinStringArray(PinyinHelper.toHanyuPinyinStringArray(ch2))
                        );
            }
        }
    };

    public static final Comparator<Music> BY_NAME_REVERSE = new Comparator<Music>() {
        @Override
        public int compare(Music music, Music t1) {
            char ch1 = music.getName().charAt(0);
            char ch2 = t1.getName().charAt(0);
            if (ch1 < 127 || ch2 < 127) {
                return -(music.getName().compareTo(t1.getName()));
            } else {
                return -(concatPinyinStringArray(PinyinHelper.toHanyuPinyinStringArray(ch1))
                        .compareTo(
                                concatPinyinStringArray(PinyinHelper.toHanyuPinyinStringArray(ch2))
                        ));
            }
        }
    };

    private static String concatPinyinStringArray(String[] pinyinArray) {
        StringBuilder pinyinSbf = new StringBuilder();
        if ((pinyinArray != null) && (pinyinArray.length > 0)) {
            for (String aPinyinArray : pinyinArray) {
                pinyinSbf.append(aPinyinArray);
            }
        }
        return pinyinSbf.toString();
    }
}
