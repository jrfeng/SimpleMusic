package jrfeng.musicplayer.mode;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;

import jrfeng.musicplayer.data.Music;
import jrfeng.musicplayer.utils.durable.DurableList;

/**
 * 音乐存储器。用于保存简单的音乐列表信息。
 */
public class MusicStorageImp implements MusicStorage {
    private static final String TAG = "MusicStorageImp";
    private static final String FILE_ALL_MUSIC = "all_music.dat";
    private static final String FILE_RECENT_PLAY = "recent_play_ids.dat";

    //MusicStorage分为以下3部分
    private DurableList<Music> mAllMusic;           //用于保存所有音乐
    private DurableList<String> mRecentPlayIds;     //用于保存最近播放的音乐唯一ID（Music的Path属性）
    private List<Music> mILove;                     //“我喜欢” 列表。

    private List<String> mMusicListNames;           //用于保存所有歌单的名称
    private List<List<Music>> mMusicLists;          //所有歌单

    private List<Music> mRecentPlay;

    private List<String> mAlbumNames;
    private List<List<Music>> mAlbumMusicLists;

    private List<String> mArtistNames;
    private List<List<Music>> mArtistMusicLists;

    @Override
    public void restore(Context context) {
        mILove = new LinkedList<>();
        mAlbumNames = new LinkedList<>();
        mAlbumMusicLists = new LinkedList<>();
        mArtistNames = new LinkedList<>();
        mArtistMusicLists = new LinkedList<>();
        mMusicListNames = new LinkedList<>();
        mMusicLists = new LinkedList<>();

        String mFileDir = context.getFilesDir().getAbsolutePath() + "/";

        mAllMusic = new DurableList<>(mFileDir + FILE_ALL_MUSIC);
        mRecentPlayIds = new DurableList<>(mFileDir + FILE_RECENT_PLAY);

        mAllMusic.restore();
        mRecentPlayIds.restore();

        mRecentPlay = new LinkedList<>();
        for (String id : mRecentPlayIds) {
            for (Music music : mAllMusic) {
                if (music.getPath().equals(id)) {
                    mRecentPlay.add(music);
                }
            }
        }

        updateILove();
        updateMusicLists();
        updateAlbumsAndArtists();
    }

    @Override
    public void saveChanges() {
        mAllMusic.save();
        mRecentPlayIds.clear();
        for (Music music : mRecentPlay) {
            mRecentPlayIds.add(music.getPath());
        }
        mRecentPlayIds.save();
    }

    @Override
    public List<Music> getAllMusic() {
        return mAllMusic;
    }

    @Override
    public int getAllMusicCount() {
        return mAllMusic.size();
    }

    @Override
    public List<Music> getILove() {
        //实时生成。因为是实时生成，所有会花费更多的计算时间。
        updateILove();
        return mILove;
    }

    @Override
    public int getILoveCount() {
        //实时生成。因为是实时生成，所有会花费更多的计算时间。
        updateILove();
        return mILove.size();
    }

    @Override
    public int getRecentPlayCount() {
        return mRecentPlay.size();
    }

    @Override
    public List<Music> getRecentPlayList() {
        return mRecentPlay;
    }

    @Override
    public List<Music> getMusicGroup(GroupType type, String name) {
        switch (type) {
            case MUSIC_LIST:
                switch (name) {
                    case MUSIC_ALL:
                        return getAllMusic();
                    case MUSIC_I_LOVE:
                        return getILove();
                    case MUSIC_RECENT_PLAY:
                        return getRecentPlayList();
                }
                return getMusicList(name);
            case ARTIST_LIST:
                return getArtistList(name);
            case ALBUM_LIST:
                return getAlbumList(name);
            default:
                return getAllMusic();
        }
    }

    @Override
    public List<Music> getMusicList(String listName) {
        //实时生成。因为是实时生成，所有会花费更多的计算时间。
        updateMusicLists();
        int index = mMusicListNames.indexOf(listName);
        if (index == -1) {
            return null;
        }
        return mMusicLists.get(index);
    }

    @Override
    public List<String> getMusicListNames() {
        //实时生成。因为是实时生成，所有会花费更多的计算时间。
        updateMusicLists();
        return mMusicListNames;
    }

    @Override
    public int getMusicListCount() {
        //实时生成。因为是实时生成，所有会花费更多的计算时间。
        updateMusicLists();
        return mMusicListNames.size();
    }

    @Override
    public void addAll(List<Music> list) {
        mAllMusic.addAll(list);
        updateAlbumsAndArtists();
        saveChanges();
    }

    @Override
    public List<Music> addMusicList(String name) {
        int index = mMusicListNames.indexOf(name);
        if (index != -1) {
            return mMusicLists.get(index);
        } else {
            mMusicListNames.add(name);
            List<Music> newList = new LinkedList<>();
            mMusicLists.add(newList);
            return newList;
        }
    }

    @Override
    public void deleteMusicList(String name) {
        int index = mMusicListNames.indexOf(name);
        if (index != -1) {
            mMusicListNames.remove(index);
            mMusicLists.remove(index);
        }
    }

    @Override
    public int getAlbumCount() {
        return mAlbumNames.size();
    }

    @Override
    public List<String> getAlbumNames() {
        return mAlbumNames;
    }

    @Override
    public int getArtistCount() {
        return mArtistNames.size();
    }

    @Override
    public List<String> getArtistNames() {
        return mArtistNames;
    }

    @Override
    public List<Music> getAlbumList(String album) {
        int index = mAlbumNames.indexOf(album);
        if (index == -1) {
            return null;
        }
        return mAlbumMusicLists.get(index);
    }

    @Override
    public List<Music> getArtistList(String artist) {
        int index = mArtistNames.indexOf(artist);
        if (index == -1) {
            return null;
        }
        return mArtistMusicLists.get(index);
    }

    //*************************private**********************

    private void updateILove() {
        mILove.clear();
        for (Music music : mAllMusic) {
            if (music.isILove()) {
                mILove.add(music);
            }
        }
    }

    private void updateMusicLists() {
        mMusicListNames.clear();
        mMusicLists.clear();
        for (Music music : mAllMusic) {
            for (String listName : music.getBelongMusicLists()) {
                int index = mMusicListNames.indexOf(listName);
                if (index == -1) {
                    mMusicListNames.add(listName);
                    List<Music> list = new LinkedList<>();
                    list.add(music);
                    mMusicLists.add(list);
                } else {
                    mMusicLists.get(index).add(music);
                }
            }
        }
    }

    private void updateAlbumsAndArtists() {
        for (Music music : mAllMusic) {
            //update albums
            int index1 = mAlbumNames.indexOf(music.getAlbum());
            if (index1 == -1) {
                mAlbumNames.add(music.getAlbum());
                List<Music> list = new LinkedList<>();
                list.add(music);
                mAlbumMusicLists.add(list);
            } else {
                mAlbumMusicLists.get(index1).add(music);
            }

            //update artists
            int index2 = mArtistNames.indexOf(music.getArtist());
            if (index2 == -1) {
                mArtistNames.add(music.getArtist());
                List<Music> list = new LinkedList<>();
                list.add(music);
                mArtistMusicLists.add(list);
            } else {
                mArtistMusicLists.get(index2).add(music);
            }
        }
    }

    //***************************调试************************

    //调试用, 相当于 Log.d()
//    private void log(String msg) {
//        Log.d(TAG, "MusicStorageImp : " + msg);
//    }
//
//    //调试用, 相当于 Log.e()
//    private void logE(String msg) {
//        Log.e(TAG, "MusicStorageImp : " + msg);
//    }
}
