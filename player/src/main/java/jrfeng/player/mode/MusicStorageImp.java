package jrfeng.player.mode;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jrfeng.player.data.Music;
import jrfeng.player.data.MusicListIndex;

/**
 * 音乐存储器。用于保存音乐列表信息。
 */
public class MusicStorageImp implements MusicStorage {
    private static final String TAG = "MusicStorageImp";

    private MusicDBOpenHelper mDBOpenHelper;

    private List<Music> mAllMusic;
    private List<Music> mILove;
    private List<Music> mRecentPlay;

    private List<MusicListIndex> mMusicListIndices;
    private List<List<Music>> mMusicLists;
    private List<String> mMusicListNames;

    private List<String> mArtistNames;
    private List<String> mAlbumNames;

    private List<List<Music>> mArtistLists;
    private List<List<Music>> mAlbumLists;

    private ExecutorService mSingleThreadPool;

    private List<OnMusicGroupChangListener> mMusicGroupChangeListenerList;

    public MusicStorageImp(Context context) {
        mDBOpenHelper = new MusicDBOpenHelper(context, null);

        mAllMusic = new LinkedList<>();
        mILove = new LinkedList<>();
        mRecentPlay = new LinkedList<>();
        mMusicListIndices = new LinkedList<>();
        mMusicLists = new LinkedList<>();
        mMusicListNames = new LinkedList<>();
        mArtistNames = new LinkedList<>();
        mAlbumNames = new LinkedList<>();
        mArtistLists = new LinkedList<>();
        mAlbumLists = new LinkedList<>();

        mSingleThreadPool = Executors.newSingleThreadExecutor();

        mMusicGroupChangeListenerList = new LinkedList<>();
    }

    //***************************调试************************

    //调试用, 相当于 Log.d()
    private static void log(String msg) {
        Log.d(TAG, "MusicStorageImp : " + msg);
    }

    //*******************MusicStorage******************

    @Override
    public void restore() {
        //调试
        log("【***开始恢复***】");

        SQLiteDatabase database = mDBOpenHelper.getReadableDatabase();

        loadAllMusic(database);
        loadILove(database);
        loadRecentPlay(database);
        loadMusicList(database);

        updateArtistAndAlbum(mAllMusic);

        database.close();

        //调试
        log("【***恢复完成***】");
    }

    @Override
    public int size() {
        return mAllMusic.size();
    }

    @Override
    public List<Music> getAllMusic() {
        return mAllMusic;
    }


    @Override
    public List<Music> getILove() {
        return mILove;
    }

    @Override
    public int getILoveSize() {
        return mILove.size();
    }

    @Override
    public List<Music> getRecentPlay() {
        return mRecentPlay;
    }

    @Override
    public int getRecentPlaySize() {
        return mRecentPlay.size();
    }

    @Override
    public List<Music> getMusicGroup(GroupType type, String name) {
        switch (type) {
            case MUSIC_LIST:
                return getMusicList(name);
            case ARTIST_LIST:
                int i1 = mArtistNames.indexOf(name);
                return i1 == -1 ? null : mArtistLists.get(i1);
            case ALBUM_LIST:
                int i2 = mAlbumNames.indexOf(name);
                return i2 == -1 ? null : mAlbumLists.get(i2);
            default:
                return getMusicList(name);
        }
    }

    @Override
    public List<Music> getMusicList(String name) {
        switch (name) {
            case MUSIC_LIST_ALL_MUSIC:
                return mAllMusic;
            case MUSIC_LIST_I_LOVE:
                return mILove;
            case MUSIC_LIST_RECENT_PLAY:
                return mRecentPlay;
            default:
                int i = mMusicListNames.indexOf(name);
                return i == -1 ? null : mMusicLists.get(i);
        }
    }

    @Override
    public List<String> getMusicListNames() {
        return mMusicListNames;
    }

    @Override
    public int getMusicListSize() {
        return mMusicLists.size();
    }

    @Override
    public boolean addAll(final List<Music> musics) {
        //剔除重复歌曲
        final List<Music> list = new ArrayList<>(musics.size());
        for (Music music : musics) {
            if (!mAllMusic.contains(music)) {
                list.add(music);
            }
        }
        boolean result = mAllMusic.addAll(list);
        if (result) {
            //保存修改
            mSingleThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    insertMusicsInto(MusicDBOpenHelper.TABLE_ALL_MUSIC, list, false);
                }
            });
            updateArtistAndAlbum(musics);
            notifyMusicGroupChanged(
                    GroupType.MUSIC_LIST,
                    MusicStorage.MUSIC_LIST_ALL_MUSIC,
                    GroupAction.ADD_MUSIC);
        }
        return result;
    }

    @Override
    public boolean createNewMusicList(String name) {
        if (!mMusicListNames.contains(name)) {
            boolean result = mMusicListNames.add(name);
            if (result) {
                final String listName = name;
                final String tableName = "_" + System.currentTimeMillis();
                mMusicListIndices.add(new MusicListIndex(listName, tableName));
                mMusicLists.add(new LinkedList<Music>());
                mSingleThreadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        SQLiteDatabase database = mDBOpenHelper.getWritableDatabase();

                        ContentValues values = new ContentValues();
                        values.put("name", listName);
                        values.put("tableName", tableName);
                        database.insert(MusicDBOpenHelper.TABLE_MUSIC_LIST_INDEX, null, values);
                        values.clear();
                        database.execSQL("CREATE TABLE " + tableName + " (\n" +
                                "    id       INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                                "    path     TEXT NOT NULL UNIQUE,\n" +
                                "    name     TEXT NOT NULL,\n" +
                                "    artist   TEXT DEFAULT \"未知\",\n" +
                                "    album    TEXT DEFAULT \"未知\",\n" +
                                "    year     TEXT DEFAULT \"未知\",\n" +
                                "    comment  TEXT DEFAULT \"未知\"\n" +
                                ");");

                        database.close();
                    }
                });
                notifyMusicGroupChanged(
                        GroupType.MUSIC_LIST,
                        name,
                        GroupAction.CREATE_GROUP);
            }
            return result;
        }
        return false;
    }

    @Override
    public boolean musicListExists(String name) {
        return mMusicListNames.contains(name);
    }

    @Override
    public boolean deleteMusicList(String name) {
        int index = mMusicListNames.indexOf(name);
        if (index != -1) {
            mMusicListNames.remove(index);
            final MusicListIndex listIndex = mMusicListIndices.remove(index);
            mMusicLists.remove(index);

            mSingleThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    SQLiteDatabase database = mDBOpenHelper.getWritableDatabase();

                    database.delete(MusicDBOpenHelper.TABLE_MUSIC_LIST_INDEX,
                            "tableName = ?", new String[]{listIndex.getTableName()});
                    database.execSQL("DROP TABLE " + listIndex.getTableName());

                    database.close();
                }
            });
            notifyMusicGroupChanged(
                    GroupType.MUSIC_LIST,
                    name,
                    GroupAction.DELETE_GROUP);
            return true;
        }
        return false;
    }

    @Override
    public int getAlbumSize() {
        return mAlbumLists.size();
    }

    @Override
    public List<String> getAlbumNames() {
        return mAlbumNames;
    }

    @Override
    public int getArtistCount() {
        return mArtistLists.size();
    }

    @Override
    public List<String> getArtistNames() {
        return mArtistNames;
    }

    @Override
    public boolean addMusicToMusicList(Music music, String name) {
        switch (name) {
            case MUSIC_LIST_I_LOVE:
                return addMusicToILove(music);
            case MUSIC_LIST_RECENT_PLAY:
                return addMusicToRecentPlay(music);
            default:
                int index = mMusicListNames.indexOf(name);
                if (index != -1) {
                    List<Music> musicList = mMusicLists.get(index);
                    if (musicList.contains(music)) {
                        return true;
                    }
                    boolean result = musicList.add(music);
                    if (result) {
                        insertMusicInto(mMusicListIndices.get(index).getTableName(), music);
                        notifyMusicGroupChanged(
                                GroupType.MUSIC_LIST,
                                name,
                                GroupAction.ADD_MUSIC);
                    }
                    return result;
                }
                return false;
        }
    }

    @Override
    public boolean addMusicsToMusicList(List<Music> musics, String name) {
        if (musics.size() < 1) {
            return false;
        }

        int index = mMusicListNames.indexOf(name);
        if (index != -1) {
            List<Music> musicList = mMusicLists.get(index);
            List<Music> needAdd = new LinkedList<>();
            for (Music music : musics) {
                if (!musicList.contains(music)) {
                    musicList.add(music);
                    needAdd.add(music);
                }
            }
            insertMusicsInto(mMusicListIndices.get(index).getTableName(), needAdd, false);
            notifyMusicGroupChanged(
                    GroupType.MUSIC_LIST,
                    name,
                    GroupAction.ADD_MUSIC);
        }
        return true;
    }

    @Override
    public boolean removeMusicFromMusicList(Music music, String name) {
        switch (name) {
            case MusicStorage.MUSIC_LIST_ALL_MUSIC:
                return removeMusicFromAllMusic(music);
            case MUSIC_LIST_I_LOVE:
                return removeMusicFromILove(music);
            case MUSIC_LIST_RECENT_PLAY:
                return removeMusicFromRecentPlay(music);
            default:
                int index = mMusicListNames.indexOf(name);
                if (index != -1 && mMusicLists.get(index).contains(music)) {
                    boolean result = mMusicLists.get(index).remove(music);
                    if (result) {
                        deleteMusicFrom(mMusicListIndices.get(index).getTableName(), music);
                        notifyMusicGroupChanged(
                                GroupType.MUSIC_LIST,
                                name,
                                GroupAction.REMOVE_MUSIC);
                    }
                    return result;
                }
                return false;
        }
    }

    @Override
    public boolean removeMusicsFromMusicGroup(List<Music> musics, GroupType groupType, String groupName) {
        switch (groupType) {
            case ALBUM_LIST:
                removeFromAlbum(musics, groupName);
                break;
            case ARTIST_LIST:
                removeFromArtist(musics, groupName);
                break;
            case MUSIC_LIST:
                removeFromMusicList(musics, groupName);
                break;
        }
        return true;
    }

    @Override
    public boolean addMusicToILove(Music music) {
        if (!mILove.contains(music)) {
            boolean result = mILove.add(music);
            if (result) {
                insertMusicInto(MusicDBOpenHelper.TABLE_I_LOVE, music);
                notifyMusicGroupChanged(
                        GroupType.MUSIC_LIST,
                        MusicStorage.MUSIC_LIST_I_LOVE,
                        GroupAction.ADD_MUSIC);
            }
            return result;
        }
        return false;
    }

    @Override
    public boolean addMusicsToILove(List<Music> musics) {
        List<Music> needAdd = new LinkedList<>();
        for (Music music : musics) {
            if (!mILove.contains(music)) {
                mILove.add(music);
                needAdd.add(music);
            }
        }
        insertMusicsInto(MusicDBOpenHelper.TABLE_I_LOVE, needAdd, false);
        notifyMusicGroupChanged(
                GroupType.MUSIC_LIST,
                MusicStorage.MUSIC_LIST_I_LOVE,
                GroupAction.ADD_MUSIC);
        return true;
    }

    @Override
    public boolean removeMusicFromILove(Music music) {
        if (mILove.contains(music)) {
            boolean result = mILove.remove(music);
            if (result) {
                deleteMusicFrom(MusicDBOpenHelper.TABLE_I_LOVE, music);
                notifyMusicGroupChanged(
                        GroupType.MUSIC_LIST,
                        MusicStorage.MUSIC_LIST_I_LOVE,
                        GroupAction.REMOVE_MUSIC);
            }
            return result;
        }
        return false;
    }

    @Override
    public boolean addMusicToRecentPlay(Music music) {
        if (!mRecentPlay.contains(music)) {
            mRecentPlay.add(0, music);
            insertMusicInto(MusicDBOpenHelper.TABLE_RECENT_PLAY, music);
            notifyMusicGroupChanged(
                    GroupType.MUSIC_LIST,
                    MusicStorage.MUSIC_LIST_RECENT_PLAY,
                    GroupAction.ADD_MUSIC);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeMusicFromRecentPlay(Music music) {
        if (mRecentPlay.contains(music)) {
            boolean result = mRecentPlay.remove(music);
            if (result) {
                deleteMusicFrom(MusicDBOpenHelper.TABLE_RECENT_PLAY, music);
                notifyMusicGroupChanged(
                        GroupType.MUSIC_LIST,
                        MusicStorage.MUSIC_LIST_RECENT_PLAY,
                        GroupAction.REMOVE_MUSIC);
            }
            return result;
        }
        return false;
    }

    @Override
    public boolean removeMusicFromAllMusic(Music music) {
        if (mAllMusic.contains(music)) {
            //测试
            log("从所有列表移除 : " + music.getName());

            boolean result = mAllMusic.remove(music);
            if (result) {
                deleteMusicFrom(MusicDBOpenHelper.TABLE_ALL_MUSIC, music);
                notifyMusicGroupChanged(
                        GroupType.MUSIC_LIST,
                        MusicStorage.MUSIC_LIST_ALL_MUSIC,
                        GroupAction.REMOVE_MUSIC);

                for (String listName : mMusicListNames) {
                    int index = mMusicListNames.indexOf(listName);
                    if (index != -1 && mMusicLists.get(index).contains(music)) {
                        mMusicLists.get(index).remove(music);
                        deleteMusicFrom(mMusicListIndices.get(index).getTableName(), music);
                        notifyMusicGroupChanged(
                                GroupType.MUSIC_LIST,
                                listName,
                                GroupAction.REMOVE_MUSIC);
                    }
                }
                removeFromArtist(music, music.getArtist());
                removeFromAlbum(music, music.getAlbum());
                removeMusicFromILove(music);
                removeMusicFromRecentPlay(music);
            }
            return result;
        }
        return false;
    }

    @Override
    public boolean deleteMusicFile(Music music) {
        String path = music.getPath();
        File file = new File(path);
        boolean result = file.delete();
        if (result) {
            removeMusicFromAllMusic(music);//该方法已经发送了通知，因此没必要再重复发送了。
        }
        return result;
    }

    @Override
    public boolean deleteMusics(List<Music> musics) {
        for (Music music : musics) {
            deleteMusicFile(music);
        }
        return true;
    }

    @Override
    public boolean contains(Music music) {
        return mAllMusic.contains(music);
    }

    @Override
    public void sortMusicList(String name, Comparator<Music> comparator) {
        List<Music> musicList = null;
        String tableName = null;
        switch (name) {
            case MusicStorage.MUSIC_LIST_ALL_MUSIC:
                musicList = mAllMusic;
                tableName = MusicDBOpenHelper.TABLE_ALL_MUSIC;
                break;
            case MusicStorage.MUSIC_LIST_I_LOVE:
                musicList = mILove;
                tableName = MusicDBOpenHelper.TABLE_I_LOVE;
                break;
            case MusicStorage.MUSIC_LIST_RECENT_PLAY:
                musicList = mRecentPlay;
                tableName = MusicDBOpenHelper.TABLE_RECENT_PLAY;
                break;
            default:
                int index = mMusicListNames.indexOf(name);
                if (index != -1) {
                    musicList = mMusicLists.get(index);
                    MusicListIndex listIndex = mMusicListIndices.get(index);
                    tableName = listIndex.getTableName();
                }
                break;
        }
        if (musicList != null && tableName != null) {
            Collections.sort(musicList, comparator);
            updateTable(tableName, musicList); //更新数据库表
            notifyMusicGroupChanged(
                    GroupType.MUSIC_LIST,
                    name,
                    GroupAction.SORT_GROUP);
        }
    }

    @Override
    public void clearRecentPlay() {
        mRecentPlay.clear();
        mSingleThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase database = mDBOpenHelper.getWritableDatabase();
                database.delete(MusicDBOpenHelper.TABLE_RECENT_PLAY, null, null);
                database.close();
            }
        });
        notifyMusicGroupChanged(
                GroupType.MUSIC_LIST,
                MusicStorage.MUSIC_LIST_RECENT_PLAY,
                GroupAction.REMOVE_MUSIC);
    }

    @Override
    public void addMusicGroupChangeListener(OnMusicGroupChangListener listener) {
        mMusicGroupChangeListenerList.add(listener);
    }

    @Override
    public void removeMusicGroupChangeListener(OnMusicGroupChangListener listener) {
        mMusicGroupChangeListenerList.remove(listener);
    }

    //**********************private*********************

    private void loadAllMusic(SQLiteDatabase database) {
        load(database, MusicDBOpenHelper.TABLE_ALL_MUSIC, mAllMusic);

        //调试
        log("加载【全部音乐】");
    }

    private void loadILove(SQLiteDatabase database) {
        load(database, MusicDBOpenHelper.TABLE_I_LOVE, mILove);

        //调试
        log("加载【我喜欢】");
    }

    private void loadRecentPlay(SQLiteDatabase database) {
        load(database, MusicDBOpenHelper.TABLE_RECENT_PLAY, mRecentPlay);
        Collections.reverse(mRecentPlay);
        //调试
        log("加载【最近播放】");
    }

    private void loadMusicList(SQLiteDatabase database) {
        Cursor cursor = database.query(
                MusicDBOpenHelper.TABLE_MUSIC_LIST_INDEX,
                null,
                null,
                null,
                null,
                null,
                null);
        if (cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex("name");
            int tableIndex = cursor.getColumnIndex("tableName");

            do {
                String name = cursor.getString(nameIndex);
                String tableName = cursor.getString(tableIndex);
                mMusicListIndices.add(new MusicListIndex(name, tableName));
            } while (cursor.moveToNext());
        }
        cursor.close();

        for (MusicListIndex listIndex : mMusicListIndices) {
            mMusicListNames.add(listIndex.getName());

            List<Music> list = new LinkedList<>();
            load(database, listIndex.getTableName(), list);
            mMusicLists.add(list);
        }

        //调试
        log("加载【自定义歌单】");
    }

    private void updateArtistAndAlbum(List<Music> musics) {
        for (Music music : musics) {
            File file = new File(music.getPath());
            if (!file.exists()) {
                continue;
            }
            int artistIndex = mArtistNames.indexOf(music.getArtist());
            int albumIndex = mAlbumNames.indexOf(music.getAlbum());
            if (artistIndex == -1) {
                mArtistNames.add(music.getArtist());
                List<Music> list = new LinkedList<>();
                list.add(music);
                mArtistLists.add(list);
            } else {
                mArtistLists.get(artistIndex).add(music);
            }

            if (albumIndex == -1) {
                mAlbumNames.add(music.getAlbum());
                List<Music> list = new LinkedList<>();
                list.add(music);
                mAlbumLists.add(list);
            } else {
                mAlbumLists.get(albumIndex).add(music);
            }
        }

        //调试
        log("更新【歌手、艺术家】");
    }

    private void removeFromArtist(Music music, String artistName) {
        List<Music> remove = new ArrayList<>(1);
        remove.add(music);
        removeFromArtist(remove, artistName);
    }

    private void removeFromArtist(List<Music> musics, String artistName) {
        List<Music> artist = mArtistLists.get(mArtistNames.indexOf(artistName));

        for (Music music : musics) {
            //调试
            log("从艺术家中移除音乐");
            log("艺术家 : " + artist);
            log("音乐  : " + music.getName());

            artist.remove(music);
        }

        notifyMusicGroupChanged(
                GroupType.ARTIST_LIST,
                artistName,
                GroupAction.REMOVE_MUSIC);

        if (artist.size() < 1) {
            //调试
            log("移除空的艺术家 : " + artistName);

            //删除空的 “艺术家” 组
            mArtistNames.remove(artistName);
            mArtistLists.remove(artist);
            notifyMusicGroupChanged(
                    GroupType.ARTIST_LIST,
                    artistName,
                    GroupAction.DELETE_GROUP);
        }
    }

    private void removeFromAlbum(Music music, String albumName) {
        List<Music> remove = new ArrayList<>(1);
        remove.add(music);
        removeFromAlbum(remove, albumName);
    }

    private void removeFromAlbum(List<Music> musics, String albumName) {
        List<Music> album = mAlbumLists.get(mAlbumNames.indexOf(albumName));

        for (Music music : musics) {
            //调试
            log("从专辑中移除音乐");
            log("专辑 : " + album);
            log("音乐 : " + music.getName());

            album.remove(music);
        }

        notifyMusicGroupChanged(
                GroupType.ALBUM_LIST,
                albumName,
                GroupAction.REMOVE_MUSIC);

        if (album.size() < 1) {
            //调试
            log("删除空专辑 : " + albumName);

            //删除空的 “专辑” 组
            mAlbumNames.remove(albumName);
            mAlbumLists.remove(album);
            notifyMusicGroupChanged(
                    GroupType.ALBUM_LIST,
                    albumName,
                    GroupAction.DELETE_GROUP);
        }
    }

    private void removeFromMusicList(List<Music> musics, String listName) {
        switch (listName) {
            case MUSIC_LIST_ALL_MUSIC:
                removeFromAllMusic(musics);
                break;
            case MUSIC_LIST_I_LOVE:
                removeFromILove(musics);
                break;
            case MUSIC_LIST_RECENT_PLAY:
                removeFromRecentPlay(musics);
                break;
            default:
                removeFromCustomMusicList(musics, listName);
                break;
        }
    }

    private void removeFromAllMusic(List<Music> musics) {
        if (musics.size() < 1) {
            return;
        }

        for (Music music : musics) {
            //调试
            log("从所有列表移除 : " + music.getName());
            mAllMusic.remove(music);
            removeFromArtist(music, music.getArtist());
            removeFromAlbum(music, music.getAlbum());
        }

        deleteMusicsFrom(MusicDBOpenHelper.TABLE_ALL_MUSIC, musics);
        notifyMusicGroupChanged(
                GroupType.MUSIC_LIST,
                MusicStorage.MUSIC_LIST_ALL_MUSIC,
                GroupAction.REMOVE_MUSIC);

        for (String listName : mMusicListNames) {
            removeFromCustomMusicList(musics, listName);
        }

        removeFromILove(musics);
        removeFromRecentPlay(musics);
    }

    private void removeFromILove(List<Music> musics) {
        if (musics.size() < 1) {
            return;
        }

        mILove.removeAll(musics);
        deleteMusicsFrom(MusicDBOpenHelper.TABLE_I_LOVE, musics);
        notifyMusicGroupChanged(
                GroupType.MUSIC_LIST,
                MusicStorage.MUSIC_LIST_I_LOVE,
                GroupAction.REMOVE_MUSIC);
    }

    private void removeFromRecentPlay(List<Music> musics) {
        if (musics.size() < 1) {
            return;
        }

        mRecentPlay.removeAll(musics);
        deleteMusicsFrom(MusicDBOpenHelper.TABLE_RECENT_PLAY, musics);
        notifyMusicGroupChanged(
                GroupType.MUSIC_LIST,
                MusicStorage.MUSIC_LIST_RECENT_PLAY,
                GroupAction.REMOVE_MUSIC);
    }

    private void removeFromCustomMusicList(List<Music> musics, String listName) {
        if (musics.size() < 1) {
            return;
        }
        int index = mMusicListNames.indexOf(listName);
        if (index != -1) {
            mMusicLists.get(index).removeAll(musics);
            MusicListIndex musicListIndex = mMusicListIndices.get(index);
            deleteMusicsFrom(musicListIndex.getTableName(), musics);
            notifyMusicGroupChanged(
                    GroupType.MUSIC_LIST,
                    listName,
                    GroupAction.REMOVE_MUSIC);
        }
    }

    private void load(SQLiteDatabase database, String tableName, List<Music> list) {
        Cursor cursor = database.query(
                tableName,
                null,
                null,
                null,
                null,
                null,
                null);
        if (cursor.moveToFirst()) {
            int pathIndex = cursor.getColumnIndex("path");
            int nameIndex = cursor.getColumnIndex("name");
            int artistIndex = cursor.getColumnIndex("artist");
            int albumIndex = cursor.getColumnIndex("album");
            int yearIndex = cursor.getColumnIndex("year");
            int commentIndex = cursor.getColumnIndex("comment");
            do {
                String path = cursor.getString(pathIndex);
                String name = cursor.getString(nameIndex);
                String artist = cursor.getString(artistIndex);
                String album = cursor.getString(albumIndex);
                String year = cursor.getString(yearIndex);
                String comment = cursor.getString(commentIndex);
                list.add(new Music(path, name, artist, album, year, comment));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void insertMusicInto(final String table, final Music music) {
        List<Music> list = new ArrayList<>(1);
        list.add(music);
        insertMusicsInto(table, list, false);
    }

    private void insertMusicsInto(final String table, final List<Music> musics, final boolean update) {
        mSingleThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase database = mDBOpenHelper.getWritableDatabase();

                if (update) {
                    database.delete(table, null, null);

                    //调试
                    log("更新表数据 : " + table);
                }

                for (Music music : musics) {
                    ContentValues values = new ContentValues();
                    values.put("path", music.getPath());
                    values.put("name", music.getName());
                    values.put("artist", music.getArtist());
                    values.put("album", music.getAlbum());
                    values.put("year", music.getYear());
                    values.put("comment", music.getComment());
                    database.insert(table, null, values);
                    values.clear();
                }

                database.close();
            }
        });
    }

    private void deleteMusicFrom(final String table, final Music music) {
        List<Music> list = new ArrayList<>(1);
        list.add(music);
        deleteMusicsFrom(table, list);
    }

    private void deleteMusicsFrom(final String table, final List<Music> musics) {
        mSingleThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase database = mDBOpenHelper.getWritableDatabase();

                for (Music music : musics) {
                    database.delete(table, "path = ?", new String[]{music.getPath()});
                }

                database.close();
            }
        });
    }

    private void updateTable(String tableName, List<Music> newData) {
        insertMusicsInto(tableName, newData, true);
    }

    private void notifyMusicGroupChanged(GroupType groupType, String groupName, GroupAction action) {
        for (OnMusicGroupChangListener listener : mMusicGroupChangeListenerList) {
            listener.onMusicGroupChanged(groupType, groupName, action);
        }
    }
}
