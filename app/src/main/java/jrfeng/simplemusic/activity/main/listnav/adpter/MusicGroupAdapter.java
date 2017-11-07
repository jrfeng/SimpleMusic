package jrfeng.simplemusic.activity.main.listnav.adpter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import jrfeng.player.data.Music;
import jrfeng.player.mode.MusicStorage;
import jrfeng.player.player.MusicPlayerClient;
import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.main.listnav.MusicListNavContract;
import jrfeng.simplemusic.widget.CustomAlertDialog;


public class MusicGroupAdapter extends DelegateAdapter.Adapter<MusicGroupAdapter.ViewHolder> {
    private static final String TAG = "MusicGroupAdapter";

    private Context mContext;
    private MusicListNavContract.Presenter mPresenter;

    private MusicStorage.GroupType mGroupType;

    private List<String> mGroupNames;

    private MusicPlayerClient mClient;
    private MusicStorage mMusicStorage;

    public MusicGroupAdapter(Context context,
                             MusicListNavContract.Presenter presenter,
                             MusicStorage.GroupType groupType) {
        mContext = context;
        mPresenter = presenter;
        mGroupType = groupType;
        mGroupNames = mPresenter.getGroupNames();

        mClient = MusicPlayerClient.getInstance();
        mMusicStorage = mClient.getMusicStorage();
    }

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        return new LinearLayoutHelper();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.widget_music_list_nav_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String groupName = mGroupNames.get(position);
        mPresenter.setGroupIcon(groupName, holder.ivItemIcon);
        holder.tvGroupName.setText(groupName);
        holder.tvGroupDescribe.setText(mPresenter.getGroupSize(mGroupType, groupName) + "首");

        final int index = position;
        holder.vgGroupItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.openMusicList(mGroupType, mGroupNames.get(index));
            }
        });

        if (mGroupType == MusicStorage.GroupType.MUSIC_LIST) {
            holder.vgGroupItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    CustomAlertDialog dialog = new CustomAlertDialog(mContext);
                    dialog.setStyle(CustomAlertDialog.Style.JUST_MESSAGE);
                    dialog.setTitle(mGroupNames.get(index));
                    dialog.setMessage("删除此歌单？");
                    dialog.setPositiveButtonListener(new CustomAlertDialog.OnButtonClickListener() {
                        @Override
                        public void onButtonClicked(String input, boolean optionChecked, int arg) {
                            mPresenter.deleteMusicList(mGroupNames.get(index));
                        }
                    });
                    dialog.show();
                    return true;
                }
            });
        }

        int i = -1;

        switch (mGroupType) {
            case MUSIC_LIST:
                List<Music> list = mMusicStorage.getMusicList(mGroupNames.get(position));
                if (list.indexOf(mClient.getPlayingMusic()) != -1) {
                    i = position;
                }
                break;
            case ARTIST_LIST:
                i = mGroupNames.indexOf(mClient.getPlayingMusic().getArtist());
                break;
            case ALBUM_LIST:
                i = mGroupNames.indexOf(mClient.getPlayingMusic().getAlbum());
                break;
        }

        if (i != -1 && i == position) {
            holder.dot.setVisibility(View.VISIBLE);
        } else {
            holder.dot.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mGroupNames.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View vgGroupItem;
        CircleImageView ivItemIcon;
        TextView tvGroupName;
        TextView tvGroupDescribe;
        View dot;

        ViewHolder(View itemView) {
            super(itemView);

            vgGroupItem = itemView.findViewById(R.id.vgGroupItem);
            ivItemIcon = itemView.findViewById(R.id.ivGroupIcon);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvGroupDescribe = itemView.findViewById(R.id.tvGroupDescribe);
            dot = itemView.findViewById(R.id.ivDot);
        }
    }

    //********************调试用********************

    private static void log(String msg) {
        if (MyApplication.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
