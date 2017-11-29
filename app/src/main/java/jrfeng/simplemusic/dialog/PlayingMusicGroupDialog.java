package jrfeng.simplemusic.dialog;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import jrfeng.player.data.Music;
import jrfeng.player.mode.MusicStorage;
import jrfeng.player.player.MusicPlayerClient;
import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.receiver.PlayerActionDisposerAdapter;
import jrfeng.simplemusic.receiver.PlayerActionReceiver;
import jrfeng.simplemusic.widget.BottomListDialog;

public class PlayingMusicGroupDialog {
    private static final String TAG = "PlayingMusicGroupDialog";

    private PlayingMusicGroupDialog() {
    }

    public static void show(Context context, MusicStorage.GroupType groupType,
                            String groupName, int position) {
        final MusicPlayerClient client = MusicPlayerClient.getInstance();
        MusicStorage musicStorage = client.getMusicStorage();
        List<Music> musicGroup = musicStorage.getMusicGroup(groupType, groupName);

        List<BottomListDialog.Item> items = new ArrayList<>(musicGroup.size());

        for (Music music : musicGroup) {
            items.add(new BottomListDialog.Item(music.getName(), music.getArtist()));
        }

        StringBuilder title = new StringBuilder();
        switch (groupType) {
            case MUSIC_LIST:
                switch (groupName) {
                    case MusicStorage.MUSIC_LIST_ALL_MUSIC:
                        title.append("所有音乐");
                        break;
                    case MusicStorage.MUSIC_LIST_I_LOVE:
                        title.append("我喜欢");
                        break;
                    case MusicStorage.MUSIC_LIST_RECENT_PLAY:
                        title.append("最近播放");
                        break;
                    default:
                        title.append("歌单 · ").append(groupName);
                        break;
                }
                break;
            case ARTIST_LIST:
                title.append("歌手 · ").append(groupName);
                break;
            case ALBUM_LIST:
                title.append("专辑 · ").append(groupName);
                break;
        }

        final BottomListDialog dialog = new BottomListDialog.Builder(context)
                .setTitle(title.toString() + " · " + items.size() + "首")
                .setItems(items)
                .setOnItemClickListener(new BottomListDialog.OnItemClickListener() {
                    @Override
                    public void onItemClicked(BottomListDialog dialog, BottomListDialog.Item item, int position) {
                        client.play(position);
                    }
                })
                .create();

        PlayerActionDisposerAdapter disposerAdapter = new PlayerActionDisposerAdapter() {
            @Override
            public void onPlay() {
                dialog.setTarget(client.getPlayingMusicIndex());
            }

            @Override
            public void onError() {
                dialog.setTarget(client.getPlayingMusicIndex());
            }
        };

        final PlayerActionReceiver actionReceiver = new PlayerActionReceiver(context, disposerAdapter);
        actionReceiver.register();

        dialog.setOnDismissListener(new BottomListDialog.OnDismissListener() {
            @Override
            public void onDismiss() {
                //调试
                log("dismiss");
                actionReceiver.unregister();
            }
        });

        dialog.setAdditionIcon(R.drawable.btn_locate);
        dialog.setAdditionButtonClickListener(new BottomListDialog.OnAdditionButtonClickListener() {
            @Override
            public boolean onClick() {
                dialog.scrollToPosition(client.getPlayingMusicIndex());
                return false;
            }
        });

        dialog.show();
        dialog.setTarget(position);
    }

    //**************调试用*****************

    private static void log(String msg) {
        if (MyApplication.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
