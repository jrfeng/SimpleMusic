package jrfeng.simplemusic.activity.main.nav.adpter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.GridLayoutHelper;

import de.hdodenhof.circleimageview.CircleImageView;
import jrfeng.simplemusic.GlideApp;
import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.main.nav.NavigationContract;

public class NavMenuAdapter extends DelegateAdapter.Adapter<NavMenuAdapter.ViewHolder> {
    private static final String TAG = "NavMenuAdapter";
    private int[] mMenuIconId = {R.mipmap.ic_love, R.mipmap.ic_music_list,
            R.mipmap.ic_album, R.mipmap.ic_artist,
            R.mipmap.ic_recent_play, R.mipmap.ic_scan};
    private String[] mMenuTitle = {"我喜欢", "歌单", "专辑", "歌手", "最近播放", "扫描"};
    private String[] mMenuDefaultDescribe = {"暂无音乐", "暂无歌单", "暂无专辑", "暂无歌手", "暂无记录", "扫描音乐"};
    private String[] mMenuCountDescribe = {"首音乐", "张歌单", "张专辑", "位歌手", "条记录"};

    private int[] mMusicListsSize;

    private Context mContext;
    private NavigationContract.Presenter mPresenter;

    private TextView[] mDescribeViews;

    public NavMenuAdapter(Context context, NavigationContract.Presenter presenter) {
        mContext = context;
        mPresenter = presenter;
        mMusicListsSize = new int[5];
        mDescribeViews = new TextView[5];
    }

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        GridLayoutHelper gridLayoutHelper = new GridLayoutHelper(2, 6, 1, 1);
        gridLayoutHelper.setBgColor(Color.parseColor("#E6E6E6"));
        return gridLayoutHelper;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.widget_nav_menu_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GlideApp.with(mContext).load(mMenuIconId[position])
                .dontAnimate()
                .into(holder.civMenuIcon);
        holder.tvMenuTitle.setText(mMenuTitle[position]);

        if (position < 5 && mMusicListsSize[position] > 0) {
            holder.tvMenuDescribe.setText(mMusicListsSize[position] + mMenuCountDescribe[position]);
        } else {
            holder.tvMenuDescribe.setText(mMenuDefaultDescribe[position]);
        }

        if (position < 5) {
            mDescribeViews[position] = holder.tvMenuDescribe;
        }

        final int which = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onNavMenuItemSelected(which);
            }
        });
    }

    @Override
    public int getItemCount() {
        return 6;
    }

    @SuppressLint("SetTextI18n")
    public void refreshMenusDescribe() {
        refreshMusicListsSize();
        for (TextView view : mDescribeViews) {
            if (view == null) {
                //调试
                log("不刷新 Menu");
                return;
            }
        }

        //调试
        log("刷新 Menu");
        for (int i = 0; i < mDescribeViews.length; i++) {
            int size = mMusicListsSize[i];
            if (size > 0) {
                mDescribeViews[i].setText(size + mMenuCountDescribe[i]);
            } else {
                mDescribeViews[i].setText(mMenuDefaultDescribe[i]);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void refreshRecentPlayCount() {
        if (mDescribeViews[4] == null) {
            return;
        }
        int count = mPresenter.getRecentPlayCount();
        mMusicListsSize[4] = count;//更新以前缓存的值
        if (count > 0) {
            mDescribeViews[4].setText(count + mMenuCountDescribe[4]);
        } else {
            mDescribeViews[4].setText(mMenuDefaultDescribe[4]);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        CircleImageView civMenuIcon;
        TextView tvMenuTitle;
        TextView tvMenuDescribe;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            civMenuIcon = itemView.findViewById(R.id.civMenuIcon);
            tvMenuTitle = itemView.findViewById(R.id.tvMenuTitle);
            tvMenuDescribe = itemView.findViewById(R.id.tvMenuDescribe);
        }
    }

    //*************private****************

    private void refreshMusicListsSize() {
        //调试
        log("更新 Menu Describe");
        mMusicListsSize[0] = mPresenter.getILoveCount();
        mMusicListsSize[1] = mPresenter.getMusicListCount();
        mMusicListsSize[2] = mPresenter.getAlbumCount();
        mMusicListsSize[3] = mPresenter.getArtistCount();
        mMusicListsSize[4] = mPresenter.getRecentPlayCount();
    }

    private void log(String msg) {
        if (MyApplication.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
