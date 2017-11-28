package jrfeng.player.mode;

import java.util.Comparator;
import java.util.List;

import jrfeng.player.data.Music;
import jrfeng.player.player.MusicPlayerClient;
import jrfeng.player.utils.sort.MusicComparator;

/**
 * 用于存储扫描到的音乐。警告！你不应该实例化该类。而是通过
 * 调用 {@link MusicPlayerClient#getMusicStorage()} 来获取 MusicStorage 实例。
 */
public interface MusicStorage {
    String MUSIC_LIST_ALL_MUSIC = "allMusic";
    String MUSIC_LIST_I_LOVE = "iLove";
    String MUSIC_LIST_RECENT_PLAY = "recentPlay";

    /**
     * 初始化 MusicStorage。这个方法由 MusicPlayerClient 负责调用。
     * 注意！千万不要将该方法设计成异步的（不要在该方法中开启任何新线
     * 程），如果设计成异步的反而会坏事！！
     */
    void restore();

    /**
     * 获取全部音乐的数量。
     *
     * @return 全部音乐的数量。
     */
    int size();

    /**
     * 获取全部的音乐。
     *
     * @return 全部的音乐。
     */
    List<Music> getAllMusic();

    /**
     * 获取 “我喜欢” 列表。
     *
     * @return “我喜欢” 列表。
     */
    List<Music> getILove();

    /**
     * 获取 “我喜欢” 列表中音乐的数量。
     *
     * @return “我喜欢” 列表中音乐的数量。
     */
    int getILoveSize();

    /**
     * 获取 “最近播放” 列表中歌曲的数量。
     *
     * @return “最近播放” 列表中歌曲的数量。
     */
    int getRecentPlayCount();

    /**
     * 获取最近播放列表。
     *
     * @return 最近播放列表。
     */
    List<Music> getRecentPlay();

    /**
     * 获取任意一个音乐组（包括 “所有音乐”、“我喜欢”、歌单、艺术家、专辑。但不包括 “最近播放” 列表）。
     *
     * @param type 组类型。
     * @param name 关键字。
     * @return 音乐组。
     */
    List<Music> getMusicGroup(GroupType type, String name);

    /**
     * 根据歌单名或者指定歌单。
     *
     * @param name 歌单名。
     * @return 歌单。
     */
    List<Music> getMusicList(String name);

    /**
     * 获取所有歌单的名字。
     *
     * @return 所有歌单的名字。
     */
    List<String> getMusicListNames();

    /**
     * 获取歌单的数量（不包括所有音乐、我喜欢）。
     *
     * @return 歌单的数量。
     */
    int getMusicListSize();

    /**
     * 添加一系列音乐（给扫描器用的）。
     *
     * @param musics 要添加的音乐。
     * @return 如果添加成功则返回 true，否则返回 false。
     */
    boolean addAll(List<Music> musics);

    /**
     * 创建新歌单。
     *
     * @param name 歌单的名字。
     * @return 创建成功则返回 true，否则返回 false。
     */
    boolean createNewMusicList(String name);

    /**
     * 判断歌单是否已经存在。
     *
     * @param name 歌单名。
     * @return 如果已经存在则返回 true，否则返回 false。
     */
    boolean musicListExists(String name);

    /**
     * 删除歌单。
     *
     * @param name 要删除的歌单的名字。
     * @return 如果删除成功则返回 true，否则返回 false。
     */
    boolean deleteMusicList(String name);

    /**
     * 获取专辑的数量。
     *
     * @return 专辑的数量。
     */
    int getAlbumSize();

    /**
     * 获取所有专辑的名字。
     *
     * @return 所有专辑的名字。
     */
    List<String> getAlbumNames();

    /**
     * 获取歌手的数量。
     *
     * @return 歌手的数量。
     */
    int getArtistCount();

    /**
     * 获取所有歌手的名字。
     *
     * @return 所有歌手的名字。
     */
    List<String> getArtistNames();

    /**
     * 添加音乐到用户的自建歌单（不包括默认的 “我喜欢”）。
     *
     * @param music 要添加的音乐。
     * @param name  用户自建歌单的名字。
     * @return 如果添加成功则返回 true，否则返回 false。
     */
    boolean addMusicToCustomMusicList(Music music, String name);

    /**
     * 添加一组音乐到用户的自建歌单（不包括默认的 “我喜欢”）。
     *
     * @param musics 要添加的音乐。
     * @param name   用户自建歌单的名字。
     * @return 如果添加成功则返回 true，否则返回 false。
     */
    boolean addMusicsToCustomMusicList(List<Music> musics, String name);

    /**
     * 从用户的自建歌单中移除指定音乐（注意，不包括 “所有音乐”、“我喜欢”、“最近播放”）。
     *
     * @param music 要移除的音乐。
     * @param name  歌单名称（不包括 “所有音乐”、“我喜欢”、“最近播放”）。
     * @return 如果移除成功则返回 true，否则返回 false。
     */
    boolean removeMusicFromCustomMusicList(Music music, String name);

    /**
     * 从用户的自建歌单中移除一组音乐（注意，不包括 “所有音乐”、“我喜欢”、“最近播放”）。
     *
     * @param musics 要移除的音乐。
     * @param name   歌单名称（不包括 “所有音乐”、“我喜欢”、“最近播放”）。
     * @return 如果移除成功则返回 true，否则返回 false。
     */
    boolean removeMusicsFromCustomMusicList(List<Music> musics, String name);

    /**
     * 添加音乐到 “我喜欢” 歌单。
     *
     * @param music 要添加的音乐。
     * @return 如果添加成功则返回 true，否则返回 false。
     */
    boolean addMusicToILove(Music music);

    /**
     * 添加一组音乐到 “我喜欢”。
     *
     * @param musics 要添加的音乐。
     * @return 如果添加成功则返回 true，否则返回 false。
     */
    boolean addMusicsToILove(List<Music> musics);

    /**
     * 从 “我喜欢” 列表移除指定音乐。
     *
     * @param music 要移除的音乐。
     * @return 如果移除成功则返回 true，否则返回 false。
     */
    boolean removeMusicFromILove(Music music);

    /**
     * 从 “我喜欢” 列表移除一组音乐。
     *
     * @param musics 要移除的音乐。
     * @return 如果移除成功则返回 true，否则返回 false。
     */
    boolean removeMusicsFromILove(List<Music> musics);

    /**
     * 记录最近播放的音乐。
     *
     * @param music 最近播放的音乐。
     * @return 如果添加成功则返回 true，否则返回 false。
     */
    boolean addMusicToRecentPlay(Music music);

    /**
     * 从 “最近播放” 记录中移除音乐。
     *
     * @param music 要移除的音乐。
     * @return 如果移除成功则返回 true，否则返回 false。
     */
    boolean removeMusicFromRecentPlay(Music music);

    /**
     * 从 “最近播放” 中移除一组音乐。
     *
     * @param musics 要移除的音乐。
     * @return 如果移除成功则返回 true，否则返回 false。
     */
    boolean removeMusicsFromRecentPlay(List<Music> musics);

    /**
     * 从所有歌单中移除指定音乐。
     *
     * @param music 要移除的音乐。
     * @return 如果移除成功则返回 true，否则返回 false。
     */
    boolean removeMusicFromAllMusic(Music music);

    /**
     * 从所有歌单中移除一组音乐。
     *
     * @param musics 要移除的音乐。
     * @return 如果移除成功则返回 true，否则返回 false。
     */
    boolean removeMusicsFromAllMusic(List<Music> musics);

    /**
     * 彻底删除音乐。注意，该方法会同时删除本地文件，请谨慎使用！
     *
     * @param music 要删除的音乐。
     * @return 如果删除成功，则返回 true，否则返回 false。
     */
    boolean deleteMusicFile(Music music);

    boolean deleteMusicsFile(List<Music> musics);

    /**
     * 判断音乐是否存在（主要用于避免从 “临时列表” 中播放已经彻底移除的音乐）。
     *
     * @param music 要判断的音乐。
     */
    boolean contains(Music music);

    /**
     * 排序歌单。
     *
     * @param name       歌单名（包括 “所有音乐”、“我喜欢”、“最近播放”）
     *                   {@link jrfeng.player.mode.MusicStorage#MUSIC_LIST_ALL_MUSIC}
     *                   {@link jrfeng.player.mode.MusicStorage#MUSIC_LIST_I_LOVE}
     *                   {@link jrfeng.player.mode.MusicStorage#MUSIC_LIST_RECENT_PLAY}。
     * @param comparator 比较器 {@link MusicComparator}
     */
    void sortMusicList(String name, Comparator<Music> comparator);

    /**
     * 清除 “最近播放”。
     */
    void clearRecentPlay();

    /**
     * 添加组监听器。用于监听组的状态。
     *
     * @param listener 要添加的监听器。
     */
    void addMusicGroupChangeListener(OnMusicGroupChangListener listener);

    /**
     * 移除组监听器。
     *
     * @param listener 要移除的监听器。
     */
    void removeMusicGroupChangeListener(OnMusicGroupChangListener listener);

    /**
     * 音乐组类型。
     */
    enum GroupType {
        /**
         * 歌单。
         */
        MUSIC_LIST,
        /**
         * 艺术家。
         */
        ARTIST_LIST,
        /**
         * 专辑。
         */
        ALBUM_LIST
    }

    enum GroupAction {
        /**
         * 组添加了新音乐。
         */
        ADD_MUSIC,
        /**
         * 从组中移除了音乐。
         */
        REMOVE_MUSIC,
        /**
         * 组被新创建。
         */
        CREATE_GROUP,
        /**
         * 组被删除。
         */
        DELETE_GROUP,
        /**
         * 组被排序。
         */
        SORT_GROUP
    }

    interface OnMusicGroupChangListener {
        void onMusicGroupChanged(GroupType groupType, String groupName, GroupAction action);
    }
}
