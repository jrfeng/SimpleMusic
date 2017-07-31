package jrfeng.simplemusic.adpter.vlayout;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.GridLayoutHelper;

import java.util.HashMap;
import java.util.Map;

import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.album.AlbumActivity;
import jrfeng.simplemusic.activity.allmusic.AllMusicActivity;
import jrfeng.simplemusic.activity.artist.ArtistActivity;
import jrfeng.simplemusic.activity.lovemusic.LoveMusicActivity;
import jrfeng.simplemusic.activity.musiclist.MusicListActivity;
import jrfeng.simplemusic.activity.navigation.NavigationContract;
import jrfeng.simplemusic.activity.scan.ScanActivity;

public class NavigationMenuAdapter extends DelegateAdapter.Adapter<NavigationMenuAdapter.ViewHolder> {
    private Context mContext;
    private NavigationContract.Presenter mPresenter;

    private int[] imageIds = {R.mipmap.ic_all_music, R.mipmap.ic_love, R.mipmap.ic_music_list, R.mipmap.ic_album, R.mipmap.ic_artist, R.mipmap.ic_scan};
    private String[] titles = {"所有音乐", "我喜欢", "歌单", "专辑", "歌手", "扫描"};
    private String[] descriptions = {"暂无音乐", "暂无音乐", "暂无歌单", "暂无专辑", "暂无歌手", "扫描本地音乐"};
    private Class[] classes = {AllMusicActivity.class, LoveMusicActivity.class, MusicListActivity.class,
            AlbumActivity.class, ArtistActivity.class, ScanActivity.class};

    private Map<String, TextView> mMenuDescription;

    public NavigationMenuAdapter(Context context, NavigationContract.Presenter presenter) {
        mContext = context;
        mPresenter = presenter;
        mMenuDescription = new HashMap<>();
    }

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        GridLayoutHelper layoutHelper = new GridLayoutHelper(2, 6, 1, 1);
        layoutHelper.setBgColor(mContext.getResources().getColor(R.color.colorGrey300));
        return layoutHelper;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.widget_menu_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.ivIcon.setImageResource(imageIds[position]);
        holder.tvTitle.setText(titles[position]);
        holder.tvDescription.setText(descriptions[position]);

        mMenuDescription.put(titles[position], holder.tvDescription);

        Log.d(MyApplication.TAG, "Save : " + titles[position]);

        mPresenter.onMenuItemCreated(position);

        //点击事件监听器
        final int index = position;
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, classes[index]);
                Log.d("App", "Presenter is Null : " + (mPresenter == null));
                mPresenter.onMenuClicked(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return 6;
    }

    public void setAllMusicMenuDesc(String desc) {
        mMenuDescription.get("所有音乐").setText(desc);
    }

    public void setILoveMenuDesc(String desc) {
        mMenuDescription.get("我喜欢").setText(desc);
    }

    public void setMusicListMenuDesc(String desc) {
        mMenuDescription.get("歌单").setText(desc);
    }

    public void setAlbumMenuDesc(String desc) {
        mMenuDescription.get("专辑").setText(desc);
    }

    public void setArtistMenuDesc(String desc) {
        mMenuDescription.get("歌手").setText(desc);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View menu;
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvDescription;

        ViewHolder(View itemView) {
            super(itemView);

            menu = itemView.findViewById(R.id.menu);
            ivIcon = itemView.findViewById(R.id.ivMenuIcon);
            tvTitle = itemView.findViewById(R.id.tvMenuTitle);
            tvDescription = itemView.findViewById(R.id.tvMenuDescription);
        }
    }
}
