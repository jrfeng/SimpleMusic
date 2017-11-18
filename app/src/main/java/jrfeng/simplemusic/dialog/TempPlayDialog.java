package jrfeng.simplemusic.dialog;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jrfeng.player.data.Music;
import jrfeng.player.player.MusicPlayerClient;
import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.receiver.PlayerActionDisposerAdapter;
import jrfeng.simplemusic.receiver.PlayerActionReceiver;
import jrfeng.simplemusic.widget.BottomListDialog;
import jrfeng.simplemusic.widget.CustomAlertDialog;

public class TempPlayDialog {
    private static final String TAG = "TempPlayDialog";

    private TempPlayDialog() {
    }

    public static void show(final Context context) {
        final MusicPlayerClient client = MusicPlayerClient.getInstance();
        List<Music> musics = client.getTempList();
        final List<BottomListDialog.Item> items = new ArrayList<>(musics.size());
        for (int i = 0; i < musics.size(); i++) {
            Music music = musics.get(i);
            items.add(new BottomListDialog.Item(music.getName(), music.getArtist()));
        }

        final BottomListDialog tempListDialog = new BottomListDialog.Builder(context)
                .setItems(items)
                .setAdditionIconId(R.mipmap.ic_garbage_can, new BottomListDialog.OnAdditionButtonClickListener() {
                    @Override
                    public boolean onClick() {
                        if (client.tempListIsEmpty()) {
                            Toast.makeText(context, "临时列表为空", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        CustomAlertDialog alertDialog = new CustomAlertDialog(context);
                        alertDialog.setStyle(CustomAlertDialog.Style.JUST_MESSAGE);
                        alertDialog.setTitle("清空临时列表");
                        alertDialog.setMessage("是否清空临时列表？");
                        alertDialog.setPositiveButtonListener(new CustomAlertDialog.OnButtonClickListener() {
                            @Override
                            public void onButtonClicked(String input, boolean optionChecked, int arg) {
                                client.clearTempList();
                                Toast.makeText(context, "临时列表已清空", Toast.LENGTH_SHORT).show();
                            }
                        });
                        alertDialog.show();
                        return true;
                    }
                })
                .supportDrag(true, new BottomListDialog.OnDragListener() {
                    @Override
                    public void onDrag(int current, int target) {
                        Collections.swap(client.getTempList(), current, target);
                        //调试
                        log("交换 : " + current + ", " + target);
                    }
                })
                .supportSwipe(true, new BottomListDialog.OnSwipeListener() {
                    @Override
                    public void onSwipe(BottomListDialog dialog, int position) {
                        client.getTempList().remove(position);
                        Toast.makeText(context, "临时播 已移除", Toast.LENGTH_SHORT).show();
                        if (client.getTempList().size() == 0) {
                            dialog.dismiss();
                        }
                    }
                })
                .setOnItemClickListener(new BottomListDialog.OnItemClickListener() {
                    @Override
                    public void onItemClicked(BottomListDialog dialog, BottomListDialog.Item item, int position) {
                        dialog.dismiss();
                        Toast.makeText(context, "临时播", Toast.LENGTH_SHORT).show();

                        //调试
                        log("插队播放 : " + position);

                        client.playTempMusic(position, true);
                    }
                })
                .create();

        PlayerActionDisposerAdapter disposerAdapter = new PlayerActionDisposerAdapter() {
            @Override
            public void onPlay() {
                if (client.isPlayingTempMusic()
                        && client.getPlayingMusic().getName().equals(items.get(0).getTitle())) {
                    items.remove(0);
                    tempListDialog.notifyDataSetChanged();
                }
            }
        };

        final PlayerActionReceiver actionReceiver = new PlayerActionReceiver(context, disposerAdapter);
        actionReceiver.register();

        tempListDialog.setOnDismissListener(new BottomListDialog.OnDismissListener() {
            @Override
            public void onDismiss() {
                //调试
                log("dismiss");
                actionReceiver.unregister();
            }
        });

        if (items.size() > 0) {
            tempListDialog.setTitle("临时列表（待播放）");
        } else {
            tempListDialog.setTitle("临时列表（空）");
        }
        tempListDialog.show();
    }

    //**************调试用*****************

    private static void log(String msg) {
        if (MyApplication.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
