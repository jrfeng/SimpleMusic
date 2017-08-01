package jrfeng.simplemusic.adpter.vlayout;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;

import java.util.List;

import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.navigation.NavigationContract;
import jrfeng.simplemusic.data.Music;

public class AllMusicAdapter extends DelegateAdapter.Adapter<AllMusicAdapter.ViewHolder> {
    private Context mContext;
    private List<Music> mMusicList;
    private NavigationContract.Presenter mPresenter;
    private static final String mListName = "所有音乐";

    public AllMusicAdapter(Context context, List<Music> musics, NavigationContract.Presenter presenter) {
        mContext = context;
        mMusicList = musics;
        mPresenter = presenter;
    }

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        return new LinearLayoutHelper(1, Math.max(mMusicList.size(), 1));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.widget_all_music_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tvSongName.setText(mMusicList.get(position).getSongName());
        holder.tvArtist.setText(mMusicList.get(position).getArtist());

        final int index = position;
        holder.listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onListItemClicked(mListName, index);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMusicList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View listItem;
        TextView tvSongName;
        TextView tvArtist;

        ViewHolder(View itemView) {
            super(itemView);
            if (mMusicList.size() > 0) {
                listItem = itemView;
                tvSongName = itemView.findViewById(R.id.tvItemSongName);
                tvArtist = itemView.findViewById(R.id.tvItemArtist);
            }
        }
    }
}
