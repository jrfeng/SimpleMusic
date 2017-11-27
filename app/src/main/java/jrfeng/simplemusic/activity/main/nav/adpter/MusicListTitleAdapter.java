package jrfeng.simplemusic.activity.main.nav.adpter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.StickyLayoutHelper;

import jrfeng.player.player.MusicPlayerClient;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.main.nav.NavigationContract;

public class MusicListTitleAdapter extends DelegateAdapter.Adapter<MusicListTitleAdapter.ViewHolder> {
    private Context mContext;
    private NavigationContract.Presenter mPresenter;

    private TextView tvAllMusicDescribe;
    private ImageButton ibPlayMode;

    public MusicListTitleAdapter(Context context, NavigationContract.Presenter presenter) {
        mContext = context;
        mPresenter = presenter;
    }

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        return new StickyLayoutHelper(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.widget_music_list_header, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        tvAllMusicDescribe = holder.tvAllMusicDescribe;
        ibPlayMode = holder.ibPlayMode;

        refreshMusicListTitle();

        holder.ibPlayMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onMusicListTitleMenuSelected(holder.ibPlayMode, 0);
            }
        });

        holder.ibLocateMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onMusicListTitleMenuSelected(holder.ibLocateMusic, 1);
            }
        });

        holder.ibMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onMusicListTitleMenuSelected(holder.ibMore, 2);
            }
        });
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public void refreshMusicListTitle() {
        if (tvAllMusicDescribe != null) {
            tvAllMusicDescribe.setText("所有音乐（共" + mPresenter.getAllMusicCount() + "首）");
        }

        setViewPlayMode(mPresenter.getPlayMode());
    }

    public void setViewPlayMode(MusicPlayerClient.PlayMode mode) {
        if (ibPlayMode == null) {
            return;
        }
        switch (mode) {
            case MODE_ORDER:
                ibPlayMode.setImageLevel(1);
                break;
            case MODE_LOOP:
                ibPlayMode.setImageLevel(2);
                break;
            case MODE_RANDOM:
                ibPlayMode.setImageLevel(3);
                break;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAllMusicDescribe;
        ImageButton ibPlayMode;
        ImageButton ibLocateMusic;
        ImageButton ibMore;

        ViewHolder(View itemView) {
            super(itemView);

            tvAllMusicDescribe = itemView.findViewById(R.id.tvAllMusicDescribe);
            ibPlayMode = itemView.findViewById(R.id.ibPlayMode);
            ibLocateMusic = itemView.findViewById(R.id.ibLocateMusic);
            ibMore = itemView.findViewById(R.id.ibMore);
        }
    }
}
