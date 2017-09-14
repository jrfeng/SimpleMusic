package jrfeng.musicplayer.utils.mp3info;

import java.io.File;

import jrfeng.musicplayer.data.Music;

public class Mp3Util {
    public static byte[] getImage(Music music) {
        Mp3Info mp3Info = new Mp3Info();
        mp3Info.load(new File(music.getPath()));
        if(mp3Info.hasId3v2()) {
            Id3v2Info id3v2Info = mp3Info.getId3v2Info();
            if(id3v2Info.hasImage()) {
                return id3v2Info.getImage();
            }
        }
        return null;
    }
}
