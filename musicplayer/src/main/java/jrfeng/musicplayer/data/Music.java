package jrfeng.musicplayer.data;

import java.io.Serializable;

public class Music implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    private String path;        //路径
    private String songName;    //歌曲名
    private String artist;      //作者
    private String album;       //专辑
    private String year;        //出品年份
    private String comment;     //备注信息

    private String belongMusicList = "所有音乐";    //所属歌单，默认为“所有音乐”

    private long addTimeMsec;

    public Music(String path, String songName, String artist,
                 String album, String year, String comment) {
        this.path = path;
        this.songName = songName;
        this.artist = artist;
        this.album = album;
        this.year = year;
        this.comment = comment;

        addTimeMsec = System.currentTimeMillis();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getBelongMusicList() {
        return belongMusicList;
    }

    public void setBelongMusicList(String musicList) {
        this.belongMusicList = musicList;
    }

    public long getAddTimeMsec() {
        return addTimeMsec;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        super.clone();
        return new Music(path, songName, artist, album, year, comment);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Music) {
            Music other = (Music) obj;
            return path.equals(other.getPath());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = 20;
        result = result * 31 + path.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "[" + path + ", " +
                songName + ", " +
                artist + ", " +
                album + ", " +
                year + ", " +
                comment + "]";
    }
}
