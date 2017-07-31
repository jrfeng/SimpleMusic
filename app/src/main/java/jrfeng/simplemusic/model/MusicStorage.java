package jrfeng.simplemusic.model;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;

import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.data.Music;
import jrfeng.simplemusic.utils.durable.Durable;
import jrfeng.simplemusic.utils.durable.DurableList;
import jrfeng.simplemusic.utils.log.L;

/**
 * 音乐存储器。用于保存简单的音乐列表信息。
 */
public class MusicStorage implements Durable {
    private static final String LIST_NAMES = "list_names.dat";
    private String mFileDir;
    //MusicStorage分为以下2部分
    private DurableList<String> mListNames;         //用于保存所有列表的名称
    private List<DurableList<Music>> mMusicLists;   //用于保存列表

    private boolean mAutoSave = false;              //自动保存。默认不自动保存
    private boolean mIsRestored = false;

    /**
     * 创建以 MusicStorage。注意！创建对象后，一定要记得
     * 调用 MusicStorage 的 @see #restoreAsync() 方法，
     *
     * @param context Context对象。
     * @see #restore() 方法会尝试从本地恢复上一次保存的信息。
     */
    public MusicStorage(Context context) {
        mFileDir = context.getFilesDir().getAbsolutePath() + "/";
    }

    //*********************Durable************************

    /**
     * 调用该方法后会尝试恢复上一次保存在本地的信息。
     * 注意！该方法有阻塞 UI 线程的风险！如果不希望
     * 阻塞 UI 线程，请看： @see #restoreAsync(OnRestoredListener listener) 。
     */
    @Override
    public void restore() {
        if (mIsRestored) {
            return;
        }

        mListNames = new DurableList<>(mFileDir + LIST_NAMES);  //恢复保存列表名称的列表
        mListNames.restore();
        mMusicLists = new LinkedList<>();

        if (mListNames.size() == 0) {
            mListNames.add("所有音乐");    //基础音乐列表
            mListNames.add("我喜欢");      //基础音乐列表
            mListNames.add("最近播放");    //最近播放列表
        }

        for (String listName : mListNames) {    //从本地恢复所有列表
            DurableList<Music> list = new DurableList<>(mFileDir + listName + ".dat");
            list.restore();
            mMusicLists.add(list);
        }

        mIsRestored = true;
    }

    /**
     * 调用该方法后会尝试恢复上一次保存在本地的信息。
     * 可以向该方法传递一个监听器，当恢复完成后该监听
     * 器会收到通知。
     */
    @Override
    public void restoreAsync(final OnRestoredListener listener) {
        new Thread() {
            @Override
            public void run() {
                restore();
                listener.onRestored();
            }
        }.start();
    }

    public boolean isRestored() {
        return mIsRestored;
    }

    /**
     * 保存修改。注意！该方法可能会阻塞 UI 线程。如果你希望异步保存，
     * 请看：@see #saveAsync()。
     */
    @Override
    public synchronized void save() {
        mListNames.save();
        for (int i = 0; i < mMusicLists.size(); i++) {
            mMusicLists.get(i).save();
        }
    }

    /**
     * 保存修改。该方法会异步保存，因此不会阻塞 UI 线程。
     * 如果你在 Activity/Service 中修改了列表，那么建
     * 议在 Activity/Service 的  onDestroy() 方法中
     * 调用该方法以保存当前 Activity/Service 所做的修改。
     */
    @Override
    public synchronized void saveAsync() {
        mListNames.saveAsync();
        for (int i = 0; i < mMusicLists.size(); i++) {
            mMusicLists.get(i).saveAsync();
        }
    }

    //***************************添加************************

    /**
     * 添加音乐到默认列表。
     *
     * @param music 要添加的 Music 对象。
     * @return 如果添加成功则返回 true，否则返回 false。
     */
    public boolean addMusic(Music music) {
        //调试
        log("addMusic : 所有音乐");

        if (isMusicExist(music)) {
            //调试
            log("addMusic : 所有音乐 : 拒绝添加 : " + music.getSongName() + " [已存在]");
            return false;
        }

        mMusicLists.get(0).add(music);

        //调试
        log("addMusic : 添加成功");
        return true;
    }

    /**
     * 将音乐添加到指定列表
     *
     * @param listName 要添加到的列表名。
     * @param music    要添加的音乐。
     * @return 如果添加成功则返回 true，否则返回 false。
     */
    public boolean addMusic(String listName, Music music) {
        if (!isListExist(listName)) {
            //调试
            log("addMusic : " + listName + " 添加失败 : 音乐列表不存在");
            return false;
        }

        addMusic(music); //添加到默认列表
        int index = mListNames.indexOf(listName);
        if (index > 0) {
            if (mMusicLists.get(index).contains(music)) {
                //调试
                log("addMusic : " + listName + " : 拒绝添加 : " + music.getSongName() + " [已存在]");
                return false;
            }
            mMusicLists.get(index).add(music); //添加到指定列表
        }

        if (mAutoSave) {  //是否自动保存
            saveAsync();
        }

        //调试
        log("addMusic : 添加成功");
        return true;
    }

    /**
     * 创建新的音乐列表
     *
     * @param listName 新列表的名称。
     * @return 如果创建成功则返回 true，否则返回 false。
     */
    public boolean createMusicList(String listName) {
        //调试
        log("createMusicList : 创建列表 : " + listName);
        if (isListExist(listName)) {
            //调试
            logE("createMusicList : " + listName + "创建失败, 列表已存在");
            return false;
        }

        mListNames.add(listName);
        mMusicLists.add(new DurableList<Music>(mFileDir + listName + ".dat"));

        if (mAutoSave) {  //是否自动保存
            saveAsync();
        }

        //调试
        log("createMusicList : " + listName + " : 创建成功");
        return true;
    }

    //***************************删除*************************

    /**
     * 从全部的列表中移除指定音乐。
     *
     * @param music 要移除的音乐。
     * @return 如果移除成功则返回 true，否则返回 false。
     */
    public boolean removeMusicFromAllList(Music music) {
        //调试
        log("removeMusicFromAllList : 从所有列表移除 : " + music.getSongName());

        if (!isMusicExist(music)) {
            //调试
            log("removeMusicFromAllList : 从所有列表移除 : 拒绝 : " + music.getSongName() + " [不存在]");
            return false;
        }

        for (int i = 0; i < mMusicLists.size(); i++) {
            mMusicLists.get(i).remove(music);
        }

        saveAsync();  //保存修改

        //调试
        log("removeMusicFromAllList : 从所有列表移除 : " + music.getSongName() + " : 成功");
        return true;
    }

    /**
     * 从指定列表移除指定音乐。
     *
     * @param listName 列表的名称。
     * @param music    要移除的 Music 对象
     * @return 如果移除成功则返回 true，否则返回 false。
     */
    public boolean removeMusic(String listName, Music music) {
        //调试
        log("removeMusic : " + listName + " : " + music.getSongName());

        if (!isListExist(listName)) {
            //调试
            log("removeMusic : 失败 : " + listName + " [不存在]");
            return false;
        }

        if (listName.equals("所有音乐")) {
            return removeMusicFromAllList(music);
        }

        int index = mListNames.indexOf(listName);

        mMusicLists.get(index).remove(music);

        saveAsync();  //保存修改
        //调试
        log("removeMusic : 成功 : " + listName + " : " + music.getSongName());
        return true;
    }

    /**
     * 移除整个列表。
     *
     * @param listName 列表的名称。
     * @return 如果移除成功则返回 true，否则返回 false。
     */
    public boolean removeMusicList(String listName) {
        if (!isListExist(listName)) {
            //调试
            log("removeMusicList : 失败" + listName + " [不存在]");
            return false;
        }

        int index = mListNames.indexOf(listName);

        if (index < 2) {
            //调试
            log("removeMusicList : 失败" + listName + "默认列表，不允许移除");
            return false;
        }

        mMusicLists.remove(index);
        mListNames.remove(listName);

        if (mAutoSave) {  //是否自动保存
            saveAsync();
        }

        //调试
        log("removeMusicList : 成功" + listName);
        return true;
    }

    //***************************查询/获取***********************

    /**
     * 获取指定音乐列表。
     *
     * @param listName 列表的名称。
     * @return 如果获取成功则返回 true，否则返回 false。
     */
    public DurableList<Music> getMusicList(String listName) {
        if (!isListExist(listName)) {
            //调试
            log("getMusicList : 失败 : " + listName + " [不存在]");
            return null;
        }

        //调试
        log("getMusicList : 成功 : " + listName);
        return mMusicLists.get(mListNames.indexOf(listName));
    }

    /**
     * 获取全部音乐列表的名称。
     *
     * @return 全部音乐列表的名称。
     */
    public String[] getMusicListNames() {
        //调试
        log("getMusicListNames : 成功");

        String[] names = new String[mListNames.size()];
        mListNames.toArray(names);
        return names;
    }

    //**********************public 其他方法******************

    /**
     * 判断指定音乐列表是否存在
     *
     * @param listName 列表的名称。
     * @return 如果存在则返回 true，否则返回 false。
     */
    public boolean isListExist(String listName) {
        return mListNames.contains(listName);
    }

    /**
     * 判断指定音乐是否已经存在。
     *
     * @param music Music 对象。
     * @return 如果存在则返回 true，否则返回 false。
     */
    public boolean isMusicExist(Music music) {
        return mMusicLists.get(0).contains(music);
    }

    /**
     * 设置是否自动保存。如果设为 true，那么所有 “修改” 功能的方法在修改完后
     * 会自动将修改保存到本地。默认值为 false。一般不进行设置，使用默认值即可。
     *
     * @param autoSave 是否自动保存。
     */
    public void setAutoSave(boolean autoSave) {
        mAutoSave = autoSave;
    }

    /**
     * 判断是否自动保存。
     *
     * @return 如果自动保存，则返回 true，否则返回 false。
     */
    public boolean isAutoSave() {
        return mAutoSave;
    }

    public int getListCount(String listName) {
        int i = mListNames.indexOf(listName);
        if (i < 0) {
            throw new IllegalArgumentException("not that list, please check your \"listName\" argument");
        }
        return mMusicLists.get(i).size();
    }

    public int getCustomMusicListCount() {
        return mMusicLists.size() - 3;
    }

    //***************************调试************************

    //调试用, 相当于 Log.d()
    private void log(String msg) {
        L.d(MyApplication.TAG, "MusicStorage : " + msg);
    }

    //调试用, 相当于 Log.e()
    private void logE(String msg) {
        L.e(MyApplication.TAG, "MusicStorage : " + msg);
    }
}
