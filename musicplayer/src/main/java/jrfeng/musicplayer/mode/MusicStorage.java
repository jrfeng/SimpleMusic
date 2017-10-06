package jrfeng.musicplayer.mode;

import android.content.Context;

import java.util.List;

import jrfeng.musicplayer.data.Music;

public interface MusicStorage {
    String MUSIC_ALL = "all";
    String MUSIC_I_LOVE = "i_love";
    String MUSIC_RECENT_PLAY = "recent_play";

    /**
     * 初始化 MusicStorage。这个方法由音乐播放器调用。
     * @param context Context 对象。
     */
    void restore(Context context);

    /**
     * 保存修改。
     */
    void saveChanges();

    /**
     * 获取全部的音乐。
     * @return 全部的音乐。
     */
    List<Music> getAllMusic();

    /**
     * 获取全部音乐的数量。
     * @return 全部音乐的数量。
     */
    int getAllMusicCount();

    /**
     * 获取 “我喜欢” 列表。
     * @return “我喜欢” 列表。
     */
    List<Music> getILove();

    /**
     * 获取 “我喜欢” 列表中音乐的数量。
     * @return “我喜欢” 列表中音乐的数量。
     */
    int getILoveCount();

    /**
     * 获取 “最近播放” 列表中歌曲的数量。
     * @return “最近播放” 列表中歌曲的数量。
     */
    int getRecentPlayCount();

    /**
     * 获取最近播放列表。
     * @return 最近播放列表。
     */
    List<Music> getRecentPlayList();

    /**
     * 获取任意一个音乐组（包括 “所有音乐”、“我喜欢”、歌单、艺术家、专辑。但不包括 “最近播放” 列表）。
     * @param type 组类型。
     * @param name 关键字。
     * @return 音乐组。
     */
    List<Music> getMusicGroup(GroupType type, String name);

    /**
     * 根据歌单名或者指定歌单。
     * @param listName 歌单名。
     * @return 歌单。
     */
    List<Music> getMusicList(String listName);

    /**
     * 获取所有歌单的名字。
     * @return 所有歌单的名字。
     */
    List<String> getMusicListNames();

    /**
     * 获取歌单的数量（不包括所有音乐、我喜欢）。
     * @return 歌单的数量。
     */
    int getMusicListCount();

    /**
     * 添加一系列音乐（扫描器用）。
     * @param musics 要添加的音乐。
     */
    void addAll(List<Music> musics);

    /**
     * 添加新歌单。
     * @param listName 歌单的名字。
     * @return 添加的歌单。
     */
    List<Music> addMusicList(String listName);

    /**
     * 删除歌单。
     * @param name 要删除的歌单的名字。
     */
    void deleteMusicList(String name);

    /**
     * 获取专辑的数量。
     * @return 专辑的数量。
     */
    int getAlbumCount();

    /**
     * 获取所有专辑的名字。
     * @return 所有专辑的名字。
     */
    List<String> getAlbumNames();

    /**
     * 获取歌手的数量。
     * @return 歌手的数量。
     */
    int getArtistCount();

    /**
     * 获取所有歌手的名字。
     * @return 所有歌手的名字。
     */
    List<String> getArtistNames();

    /**
     * 获取某张专辑下的所有音乐。
     * @param album 专辑名称。
     * @return 专辑中的音乐。
     */
    List<Music> getAlbumList(String album);

    /**
     * 获取属于某位歌手的所有音乐。
     * @param artist 歌手的名字。
     * @return 属于该歌手的所有音乐。
     */
    List<Music> getArtistList(String artist);

    enum GroupType {
        MUSIC_LIST, ARTIST_LIST, ALBUM_LIST
    }
}
