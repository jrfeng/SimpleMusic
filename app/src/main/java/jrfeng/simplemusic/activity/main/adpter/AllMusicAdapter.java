package jrfeng.simplemusic.activity.main.adpter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;

import java.util.List;

import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.main.NavigationContract;
import jrfeng.musicplayer.data.Music;
import jrfeng.simplemusic.widget.Divider;

public class AllMusicAdapter extends DelegateAdapter.Adapter<AllMusicAdapter.ViewHolder> {
    private Context mContext;
    private List<Music> mMusicList;
    private NavigationContract.Presenter mPresenter;
    private SparseArray<View> mItemViewContainer;
    private static final String mListName = "所有音乐";
    private int mChoiceItem;

    public AllMusicAdapter(Context context, List<Music> musics, NavigationContract.Presenter presenter) {
        mContext = context;
        mMusicList = musics;
        mPresenter = presenter;
        mItemViewContainer = new SparseArray<>();
    }

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        return new LinearLayoutHelper(1, Math.max(mMusicList.size(), 1));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.widget_music_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tvSongName.setText(mMusicList.get(position).getSongName());
        holder.tvArtist.setText(mMusicList.get(position).getArtist());

        if(mChoiceItem == position){
            holder.dvMark.setVisibility(View.VISIBLE);
        }else{
            holder.dvMark.setVisibility(View.GONE);
        }

        final int index = position;
        holder.listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onListItemClicked(mListName, index);
            }
        });

        holder.ibMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onListItemMenuClicked(index);
            }
        });

        mItemViewContainer.put(index, holder.dvMark);
    }

    @Override
    public int getItemCount() {
        return mMusicList.size();
    }

    public void setChoice(int itemPosition) {
        View oldView = mItemViewContainer.get(mChoiceItem);
        View newView = mItemViewContainer.get(itemPosition);
        if (oldView != null) {
            oldView.setVisibility(View.GONE);
        }

        if (newView != null) {
            newView.setVisibility(View.VISIBLE);
        }
        mChoiceItem = itemPosition;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View listItem;
        TextView tvSongName;
        TextView tvArtist;
        ImageButton ibMenu;
        Divider dvMark;

        ViewHolder(View itemView) {
            super(itemView);

            listItem = itemView;
            tvSongName = itemView.findViewById(R.id.tvItemSongName);
            tvArtist = itemView.findViewById(R.id.tvItemArtist);
            ibMenu = itemView.findViewById(R.id.ibMenu);
            dvMark = itemView.findViewById(R.id.dvMark);
        }
    }
}
