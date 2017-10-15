package jrfeng.simplemusic.activity.main.nav.vl_adpter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;

import java.util.List;

import jrfeng.musicplayer.data.Music;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.main.nav.NavigationContract;
import jrfeng.simplemusic.widget.Divider;

public class MusicListAdapter extends DelegateAdapter.Adapter<MusicListAdapter.ViewHolder> {
    private Context mContext;
    private NavigationContract.Presenter mPresenter;

    private List<Music> mAllMusic;
    private SparseArray<ViewHolder> mHolders;

    private int mLastPlayingPosition;

    private View mEffectsView;

    public MusicListAdapter(Context context, NavigationContract.Presenter presenter) {
        mContext = context;
        mPresenter = presenter;
        mAllMusic = mPresenter.getAllMusic();
        mHolders = new SparseArray<>(mAllMusic.size());
        mLastPlayingPosition = -1;
    }

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        return new LinearLayoutHelper();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.widget_music_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mHolders.append(position, holder);
        Music music = mAllMusic.get(position);
        holder.tvSongName.setText(music.getSongName());
        holder.tvSongArtist.setText(music.getArtist());

        //指示器
        if (mPresenter.getPlayingMusicPosition() == position) {
            Log.d("Indicator", "onBindViewHolder");
            setChoice(position);
        } else {
            setNotChoice(position);
        }

        final int index = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onMusicListItemClicked(index);
            }
        });

        holder.ibTempPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.addTempPlayMusic(mAllMusic.get(index));
                Toast.makeText(mContext, "临时播 已添加", Toast.LENGTH_SHORT).show();
                //临时播特效
                playEffects(view);
            }
        });

        holder.ibItemMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO 显示底部菜单
                Toast.makeText(mContext, "ItemMore " + index, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAllMusic.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        View playingIndicator;
        ImageButton ibTempPlay;
        TextView tvSongName;
        TextView tvSongArtist;
        TextView tvTempPlayMark;
        ImageButton ibItemMore;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            playingIndicator = itemView.findViewById(R.id.playingIndicator);
            ibTempPlay = itemView.findViewById(R.id.ibTempPlay);
            tvSongName = itemView.findViewById(R.id.tvSongName);
            tvSongArtist = itemView.findViewById(R.id.tvSongArtist);
            tvTempPlayMark = itemView.findViewById(R.id.tvTempPlayMark);
            ibItemMore = itemView.findViewById(R.id.ibItemMore);
        }
    }

    //******************private********************

    public void setChoice(int position) {
        setNotChoice(mLastPlayingPosition);
        mLastPlayingPosition = position;
        ViewHolder holder = mHolders.get(position);

        //调试
        Log.d("Indicator", "Position : " + position);

        if (holder != null && (holder.getAdapterPosition() - 9 == position)) {
            holder.playingIndicator.setVisibility(View.VISIBLE);
            holder.tvSongName.setTextColor(Color.parseColor("#64B5F6"));//Blue300
            holder.tvSongArtist.setTextColor(Color.parseColor("#64B5F6"));//Blue300
            if(mPresenter.isTempPlay()) {
                //调试
                Log.d("Indicator", "临时播");
                holder.tvTempPlayMark.setVisibility(View.VISIBLE);
            } else {
                holder.tvTempPlayMark.setVisibility(View.GONE);
                //调试
                Log.d("Indicator", "非临时播");
            }
        }
    }

    private void setNotChoice(int position) {
        if (position < 0) {
            return;
        }
        ViewHolder holder = mHolders.get(position);
        if (holder != null) {
            holder.playingIndicator.setVisibility(View.GONE);
            holder.tvSongName.setTextColor(Color.parseColor("#000000"));
            holder.tvSongArtist.setTextColor(Color.parseColor("#757575"));
            holder.tvTempPlayMark.setVisibility(View.GONE);
        }
    }

    //临时播特效
    private void playEffects(View emitView) {
        if (mEffectsView == null) {
            mEffectsView = new Divider(mContext, null);
            mEffectsView.setBackgroundResource(R.mipmap.ic_music_note);
        }
        final ViewGroup rootView = (ViewGroup) emitView.getRootView();
        int size = mContext.getResources().getDimensionPixelSize(R.dimen.EffectsSize);
        rootView.addView(mEffectsView, size, size);
        int[] locate = new int[2];
        emitView.getLocationInWindow(locate);
        mEffectsView.setX(locate[0] + emitView.getMeasuredWidth() / 2 - size / 2);
        mEffectsView.setY(locate[1] + emitView.getMeasuredHeight() / 2 - size / 2);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mEffectsView, "y",
                mEffectsView.getY(), rootView.getBottom());
        objectAnimator.setDuration(800);
        objectAnimator.setInterpolator(new BounceInterpolator());
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                rootView.removeView(mEffectsView);
            }
        });
        objectAnimator.start();
    }
}
