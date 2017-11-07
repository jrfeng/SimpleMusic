package jrfeng.simplemusic.activity.choice.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import jrfeng.player.data.Music;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.choice.MultiChoiceContract;

public class MultiChoiceAdapter extends RecyclerView.Adapter<MultiChoiceAdapter.ViewHolder> {
    private Context mContext;
    private List<Music> mMusicList;
    private OnSelectedListener mOnSelectedListener;

    private List<Integer> mSelectedItems;

    public MultiChoiceAdapter(@NonNull Context context,
                              @NonNull List<Music> musicList,
                              int selectedPosition,
                              @NonNull OnSelectedListener listener) {
        mContext = context;
        mMusicList = musicList;
        mOnSelectedListener = listener;

        mSelectedItems = new LinkedList<>();
        mSelectedItems.add(selectedPosition);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.widget_multi_choice_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        initItems(holder, position);
        addViewListener(holder, position);
    }

    @Override
    public int getItemCount() {
        return mMusicList.size();
    }

    //*************ViewHolder*************

    class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        CheckBox cbChecked;
        TextView tvSongName;
        TextView tvSongArtist;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.cbChecked = itemView.findViewById(R.id.cbChecked);
            this.tvSongName = itemView.findViewById(R.id.tvSongName);
            this.tvSongArtist = itemView.findViewById(R.id.tvSongArtist);
        }
    }

    //*************Listener************

    public interface OnSelectedListener {
        void onItemSelected(boolean selectedAll, int selectedCount);
    }

    //**************public**************

    public List<Integer> getSelectedItemsOrder() {
        return mSelectedItems;
    }

    public void setSelectedAll() {
        mSelectedItems.clear();
        for (int i = 0; i < mMusicList.size(); i++) {
            mSelectedItems.add(i);
        }
        notifyListener();
        notifyDataSetChanged();
    }

    public void setUnselectedAll() {
        mSelectedItems.clear();
        notifyListener();
        notifyDataSetChanged();
    }

    public void unselected(int position) {
        //不要使用自动装箱，可能会导致 BUG
        mSelectedItems.remove(Integer.valueOf(position));
        notifyListener();
        notifyDataSetChanged();
    }

    //**************private*************

    private void initItems(ViewHolder holder, int position) {
        Music music = mMusicList.get(position);
        holder.tvSongName.setText(music.getName());
        holder.tvSongArtist.setText(music.getArtist());

        holder.cbChecked.setChecked(mSelectedItems.contains(position));
    }

    private void addViewListener(final ViewHolder holder, final int position) {
        holder.cbChecked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.cbChecked.isChecked()) {
                    mSelectedItems.add(position);
                } else {
                    //不要使用自动装箱，可能会导致 BUG
                    mSelectedItems.remove(Integer.valueOf(position));
                }

                notifyListener();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!holder.cbChecked.isChecked()) {
                    mSelectedItems.add(position);
                    holder.cbChecked.setChecked(true);
                } else {
                    //不要使用自动装箱，有 BUG
                    mSelectedItems.remove(Integer.valueOf(position));
                    holder.cbChecked.setChecked(false);
                }

                notifyListener();
            }
        });
    }

    private void notifyListener() {
        mOnSelectedListener.onItemSelected(
                mSelectedItems.size() == mMusicList.size(),
                mSelectedItems.size());
    }
}
