package jrfeng.simplemusic.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.MenuItem;
import android.widget.Toast;

import jrfeng.player.mode.MusicStorage;
import jrfeng.player.player.MusicPlayerClient;
import jrfeng.player.utils.sort.MusicComparator;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.utils.statusbar.QMUIStatusBarHelper;
import jrfeng.simplemusic.widget.TopMenuDialog;

public class SortMusicListDialog {
    private SortMusicListDialog() {
    }

    public static void show(final Activity activity, final String musicListName) {
        TopMenuDialog sortMenu = new TopMenuDialog(activity, R.menu.music_list_sort, new TopMenuDialog.OnItemClickListener() {
            @Override
            public void onItemClick(MenuItem item) {
                MusicStorage musicStorage = MusicPlayerClient.getInstance().getMusicStorage();
                switch (item.getItemId()) {
                    case R.id.sortByName:
                        musicStorage.sortMusicList(musicListName, MusicComparator.BY_NAME);
                        break;
                    case R.id.sortByNameReverse:
                        musicStorage.sortMusicList(musicListName, MusicComparator.BY_NAME_REVERSE);
                        break;
                }
                Toast.makeText(activity, "排序完成", Toast.LENGTH_SHORT).show();
            }
        });

        sortMenu.show(activity);
    }
}
