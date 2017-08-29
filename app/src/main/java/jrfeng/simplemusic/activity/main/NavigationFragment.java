package jrfeng.simplemusic.activity.main;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.bumptech.glide.Glide;

import java.util.List;

import jrfeng.simplemusic.R;
import jrfeng.simplemusic.adpter.vlayout.AllMusicTitleAdapter;
import jrfeng.simplemusic.adpter.vlayout.DividerAdapter;
import jrfeng.simplemusic.adpter.vlayout.NavigationMenuAdapter;
import jrfeng.simplemusic.adpter.vlayout.AllMusicAdapter;
import jrfeng.musicplayer.data.Music;

public class NavigationFragment extends Fragment implements NavigationContract.View {

    private View mContentView;
    private NavigationContract.Presenter mPresenter;
    private Context mContext;

    private RecyclerView rvNavMenu;
    private ImageButton ibCtlPlayPause;
    private ImageButton ibCtlNext;
    private ImageButton ibCtlMenu;

    private ImageView ivCtlImage;
    private TextView tvCtlSongName;
    private TextView tvCtlArtist;
    private SeekBar sbProgress;

    private NavigationMenuAdapter mMenuAdapter;
    private AllMusicAdapter mAllMusicAdapter;
    private AllMusicTitleAdapter mAllMusicTitleAdapter;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            byte[] imageData = (byte[]) message.obj;
            if (imageData != null) {
                Glide.with(mContext).load(imageData).into(ivCtlImage);
            } else {
                Glide.with(mContext).load(R.mipmap.ic_launcher).into(ivCtlImage);
            }
            return true;
        }
    });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_navigation, container, false);
        findViews();
        addViewListener();
        initRecyclerView();
        return mContentView;
    }

    @Override
    public void setPresenter(NavigationContract.Presenter presenter) {
        mPresenter = presenter;
    }

    //***********************private**********************

    private void initRecyclerView() {
        VirtualLayoutManager vLayoutManager = new VirtualLayoutManager(mContext);
        rvNavMenu.setLayoutManager(vLayoutManager);

        RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
        viewPool.setMaxRecycledViews(0, 20);
        rvNavMenu.setRecycledViewPool(viewPool);

        DelegateAdapter delegateAdapter = new DelegateAdapter(vLayoutManager);
        rvNavMenu.setAdapter(delegateAdapter);

        mMenuAdapter = new NavigationMenuAdapter(mContext, mPresenter);
        delegateAdapter.addAdapter(mMenuAdapter);

        DividerAdapter dividerAdapter = new DividerAdapter(mContext);
        delegateAdapter.addAdapter(dividerAdapter);

        mAllMusicTitleAdapter = new AllMusicTitleAdapter(mContext, mPresenter);
        delegateAdapter.addAdapter(mAllMusicTitleAdapter);

        List<Music> allMusicList =mPresenter.getAllMusicList();
        mAllMusicAdapter = new AllMusicAdapter(mContext, allMusicList, mPresenter);
        delegateAdapter.addAdapter(mAllMusicAdapter);
    }

    private void findViews() {
        rvNavMenu = mContentView.findViewById(R.id.rvNavMenu);
        ibCtlPlayPause = mContentView.findViewById(R.id.ibCtlPlayPause);
        ibCtlNext = mContentView.findViewById(R.id.ibCtlNext);
        ibCtlMenu = mContentView.findViewById(R.id.ibCtlMenu);

        ivCtlImage = mContentView.findViewById(R.id.ivCtlImage);
        tvCtlSongName = mContentView.findViewById(R.id.tvCtlSongName);
        tvCtlArtist = mContentView.findViewById(R.id.tvCtlArtist);
        sbProgress = mContentView.findViewById(R.id.sbProgress);
    }

    private void addViewListener() {
        ibCtlPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onPlayPauseClicked();
            }
        });

        ibCtlNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onNextClicked();
            }
        });

        ibCtlMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onCtlMenuClicked();
            }
        });

        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mPresenter.onSeekBarStartSeeking();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPresenter.onSeekBarStopSeeking(seekBar.getProgress());
            }
        });
    }

    @Override
    public void toggleToPlay() {
        ibCtlPlayPause.setImageLevel(2);
    }

    @Override
    public void toggleToPause() {
        ibCtlPlayPause.setImageLevel(1);
    }

    @Override
    public void setProgressMax(int max) {
        sbProgress.setMax(max);
    }

    @Override
    public void setProgress(int progress) {
        sbProgress.setProgress(progress);
    }

    @Override
    public void setCtlSongName(String songName) {
        tvCtlSongName.setText(songName);
    }

    @Override
    public void setCtlArtist(String artist) {
        tvCtlArtist.setText(artist);
    }

    @Override
    public void setCtlImage(byte[] imageData) {
        Message message = handler.obtainMessage();
        message.obj = imageData;
        handler.sendMessage(message);
    }

    @Override
    public void setILoveMenuDesc(String desc) {
        mMenuAdapter.setILoveMenuDesc(desc);
    }

    @Override
    public void setMusicListMenuDesc(String desc) {
        mMenuAdapter.setMusicListMenuDesc(desc);
    }

    @Override
    public void setAlbumMenuDesc(String desc) {
        mMenuAdapter.setAlbumMenuDesc(desc);
    }

    @Override
    public void setArtistMenuDesc(String desc) {
        mMenuAdapter.setArtistMenuDesc(desc);
    }

    @Override
    public void setRecentPlayMenuDesc(String desc) {
        mMenuAdapter.setRecentPlayMenuDesc(desc);
    }

    @Override
    public void updateAllMusicList() {
        mAllMusicAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateAllMusicListTitle() {
        mAllMusicTitleAdapter.updateTitle();
    }

    @Override
    public void showSortMenu() {
        //TODO
    }

    @Override
    public void showTitleMenu() {
        //TODO
    }

    @Override
    public void showListItemMenu(Music music) {
        //TODO
    }


    @Override
    public void setChoice(int itemPosition) {
        mAllMusicAdapter.setChoice(itemPosition);
    }

    @Override
    public void scrollTo(int position) {
        rvNavMenu.scrollToPosition(position + 8);
    }
}
