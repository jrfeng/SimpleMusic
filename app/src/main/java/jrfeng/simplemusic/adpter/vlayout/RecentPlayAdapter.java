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
import com.alibaba.android.vlayout.layout.SingleLayoutHelper;
import com.alibaba.android.vlayout.layout.StickyLayoutHelper;

import java.util.List;

import jrfeng.simplemusic.R;
import jrfeng.simplemusic.data.Music;

public class RecentPlayAdapter extends DelegateAdapter.Adapter<RecentPlayAdapter.ViewHolder> {
    private Context mContext;
    private List<Music> mMusicList;

    public RecentPlayAdapter(Context context, List<Music> musics) {
        mContext = context;
        mMusicList = musics;
    }

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        return new LinearLayoutHelper(1, Math.max(mMusicList.size(), 1));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        if (mMusicList.size() == 0) {
            view = layoutInflater.inflate(R.layout.widget_recent_play_enpty_view, parent, false);
        } else {
            view = layoutInflater.inflate(R.layout.widget_recent_play_item, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mMusicList.size() > 0) {
            holder.tvSongName.setText(mMusicList.get(position).getSongName());
        }
        //TODO 添加点击事件监听器
    }

    @Override
    public int getItemCount() {
        return Math.max(mMusicList.size(), 1);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSongName;

        ViewHolder(View itemView) {
            super(itemView);
            if (mMusicList.size() > 0) {
                tvSongName = itemView.findViewById(R.id.tvSongName);
            }
        }
    }
}
