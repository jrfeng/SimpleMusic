package jrfeng.simplemusic.activity.choice;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jrfeng.player.data.Music;
import jrfeng.player.mode.MusicStorage;
import jrfeng.player.player.MusicPlayerClient;

public class MultiChoicePresenter implements MultiChoiceContract.Presenter {
    private Context mContext;
    private MultiChoiceContract.View mView;
    private MusicStorage.GroupType mGroupType;
    private String mGroupName;

    private MusicPlayerClient mClient;
    private MusicStorage mMusicStorage;
    private List<Music> mMusicGroup;

    public MultiChoicePresenter(Context context,
                                MultiChoiceContract.View view,
                                MusicStorage.GroupType groupType,
                                String groupName) {
        mContext = context;
        mView = view;
        mGroupType = groupType;
        mGroupName = groupName;

        mClient = MusicPlayerClient.getInstance();
        mMusicStorage = mClient.getMusicStorage();
        mMusicGroup = mMusicStorage.getMusicGroup(mGroupType, mGroupName);
    }

    @Override
    public void begin() {

    }

    @Override
    public void end() {

    }

    @Override
    public List<Music> getMusicGroup() {
        return mMusicGroup;
    }

    @Override
    public void addTempPlayMusics(List<Music> musics) {
        if (musics.size() < 1) {
            Toast.makeText(mContext, "至少选择一首歌曲", Toast.LENGTH_SHORT).show();
            return;
        }

        mClient.addTempPlayMusics(musics);
        Toast.makeText(mContext, "临时播 已添加", Toast.LENGTH_SHORT).show();
        mView.close();
    }

    @Override
    public void addMusicsToILove(List<Music> musics) {
        if (musics.size() < 1) {
            Toast.makeText(mContext, "至少选择一首歌曲", Toast.LENGTH_SHORT).show();
            return;
        }

        mMusicStorage.addMusicsToILove(musics);
        Toast.makeText(mContext, "我喜欢 已添加", Toast.LENGTH_SHORT).show();
        mView.close();
    }

    @Override
    public void addMusicsToMusicList(List<Music> musics, String name) {
        if (musics.size() < 1) {
            Toast.makeText(mContext, "至少选择一首歌曲", Toast.LENGTH_SHORT).show();
            return;
        }

        mMusicStorage.addMusicsToMusicList(musics, name);
        Toast.makeText(mContext, "添加成功", Toast.LENGTH_SHORT).show();
        mView.close();
    }

    @Override
    public void removeMusics(List<Music> musics) {
        if (musics.size() < 1) {
            Toast.makeText(mContext, "至少选择一首歌曲", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mGroupType != MusicStorage.GroupType.MUSIC_LIST) {
            Toast.makeText(mContext, "不支持移除", Toast.LENGTH_SHORT).show();
            return;
        }

        mMusicStorage.removeMusicsFromMusicGroup(musics, mGroupType, mGroupName);
        Toast.makeText(mContext, "移除成功", Toast.LENGTH_SHORT).show();
        mView.close();
    }

    @Override
    public void removeMusicsFromAllMusic(List<Music> musics) {
        mMusicStorage.removeMusicsFromMusicGroup(musics,
                MusicStorage.GroupType.MUSIC_LIST,
                MusicStorage.MUSIC_LIST_ALL_MUSIC);
        Toast.makeText(mContext, "删除成功", Toast.LENGTH_SHORT).show();
        mView.close();
    }

    @Override
    public void deleteMusics(List<Music> musics) {
        if (musics.size() < 1) {
            Toast.makeText(mContext, "至少选择一首歌曲", Toast.LENGTH_SHORT).show();
            return;
        }

        mMusicStorage.deleteMusics(musics);
        Toast.makeText(mContext, "删除成功", Toast.LENGTH_SHORT).show();
        mView.close();
    }

    @Override
    public List<String> getMusicListNames() {
        return mMusicStorage.getMusicListNames();
    }

    @Override
    public List<Integer> getMusicListsSize() {
        List<String> listNames = getMusicListNames();
        List<Integer> listsSize = new ArrayList<>(listNames.size());
        for (String name : listNames) {
            listsSize.add(mMusicStorage.getMusicGroup(MusicStorage.GroupType.MUSIC_LIST, name).size());
        }
        return listsSize;
    }

    @Override
    public void createNewMusicList(String listName, List<Music> addMusics) {
        if (mMusicStorage.createNewMusicList(listName)) {
            mMusicStorage.addMusicsToMusicList(addMusics, listName);
            Toast.makeText(mContext, "添加成功", Toast.LENGTH_SHORT).show();
            mView.close();
        }
    }
}
