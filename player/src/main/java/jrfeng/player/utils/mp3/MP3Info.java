package jrfeng.player.utils.mp3;

public interface MP3Info {
    String getSongName();

    String getArtist();

    String getAlbum();

    String getYear();

    String getComment();

    long getLengthMesc();

    boolean hasImage();

    byte[] getImage();
}
