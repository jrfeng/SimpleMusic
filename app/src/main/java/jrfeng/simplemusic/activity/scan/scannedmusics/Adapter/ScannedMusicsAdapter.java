package jrfeng.simplemusic.activity.scan.scannedmusics.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import jrfeng.musicplayer.data.Music;
import jrfeng.simplemusic.R;

public class ScannedMusicsAdapter extends RecyclerView.Adapter<ScannedMusicsAdapter.ViewHolder> {
    private Context mContext;
    private List<Music> mScannedMusicList;
    private boolean[] mItemsState;

    private CheckBox cbCheckedAll;
    private TextView tvCheckedItemCount;

    private ViewHolder[] mViewHolders;

    public ScannedMusicsAdapter(Context context, List<Music> musics, View titleMenu) {
        mContext = context;
        mScannedMusicList = musics;
        mItemsState = new boolean[musics.size()];
        mViewHolders = new ViewHolder[musics.size()];
        cbCheckedAll = titleMenu.findViewById(R.id.cbCheckedAll);
        tvCheckedItemCount = titleMenu.findViewById(R.id.tvCheckedItemCount);
        cbCheckedAll.setChecked(true);
        setAllChecked();

        cbCheckedAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cbCheckedAll.isChecked()) {
                    cbCheckedAll.setChecked(true);
                    setAllChecked();
                    updateItemView();
                } else {
                    cbCheckedAll.setChecked(false);
                    setAllUnchecked();
                    updateItemView();
                }
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.widget_scanned_music_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.tvSongName.setText(mScannedMusicList.get(position).getSongName());
        holder.tvSongArtist.setText(mScannedMusicList.get(position).getArtist());
        holder.cbChecked.setChecked(mItemsState[position]);
        mViewHolders[position] = holder;
        if (!holder.cbChecked.isChecked()) {
            holder.itemView.setBackgroundResource(R.drawable.shape_rect_grey);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.shape_rect_white);
        }

        final int index = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.cbChecked.toggle();
                mItemsState[index] = holder.cbChecked.isChecked();
                if (!holder.cbChecked.isChecked()) {
                    holder.itemView.setBackgroundResource(R.drawable.shape_rect_grey);
                } else {
                    holder.itemView.setBackgroundResource(R.drawable.shape_rect_white);
                }
                int count = 0;
                for (boolean item : mItemsState) {
                    if (item) {
                        count++;
                    }
                }
                tvCheckedItemCount.setText("已选" + count + "首");
                cbCheckedAll.setChecked(count == mItemsState.length);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mScannedMusicList.size();
    }

    public boolean[] getItemsChoiceState() {
        return mItemsState;
    }

    private void setAllChecked() {
        for (int i = 0; i < mItemsState.length; i++) {
            mItemsState[i] = true;
        }
        tvCheckedItemCount.setText("已选" + mScannedMusicList.size() + "首");
    }

    private void setAllUnchecked() {
        for (int i = 0; i < mItemsState.length; i++) {
            mItemsState[i] = false;
        }
        tvCheckedItemCount.setText("已选" + 0 + "首");
    }

    private void updateItemView() {
        for (int i = 0; i < mViewHolders.length; i++) {
            if (mViewHolders[i] != null) {
                mViewHolders[i].cbChecked.setChecked(mItemsState[i]);
                if (!mViewHolders[i].cbChecked.isChecked()) {
                    mViewHolders[i].itemView.setBackgroundResource(R.drawable.shape_rect_grey);
                } else {
                    mViewHolders[i].itemView.setBackgroundResource(R.drawable.shape_rect_white);
                }
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        CheckBox cbChecked;
        TextView tvSongName;
        TextView tvSongArtist;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            cbChecked = itemView.findViewById(R.id.cbNeedAdd);
            tvSongName = itemView.findViewById(R.id.tvSongName);
            tvSongArtist = itemView.findViewById(R.id.tvSongArtist);
        }
    }
}
