package jrfeng.musicplayer.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Music implements Serializable {
    private static final long serialVersionUID = 1L;
    private String path;        //路径
    private String songName;    //歌曲名
    private String artist;      //作者
    private String album;       //专辑
    private String year;        //出品年份
    private String comment;     //备注信息

    private boolean iLove;
    private ArrayList<String> belongMusicLists;

    public Music(String path, String songName, String artist,
                 String album, String year, String comment) {
        this.path = path;
        this.songName = songName;
        this.artist = artist;
        this.album = album;
        this.year = year;
        this.comment = comment;
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

    public void setILove(boolean love) {
        iLove = love;
    }

    public boolean isILove() {
        return iLove;
    }

    public List<String> getBelongMusicLists() {
        if(belongMusicLists == null) {
            belongMusicLists = new ArrayList<>();
        }
        return belongMusicLists;
    }

    public boolean belongMusicList(String name) {
        for (String list : belongMusicLists) {
            if (list.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void addToMusicList(String name) {
        if (belongMusicLists == null) {
            belongMusicLists = new ArrayList<>();
        }
        belongMusicLists.add(name);
    }

    public void removeFromMusicList(String name) {
        if (belongMusicLists != null) {
            belongMusicLists.remove(name);
        }
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
