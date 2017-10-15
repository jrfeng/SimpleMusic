package jrfeng.simplemusic.activity.main.nav;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;

import java.util.ArrayList;
import java.util.List;

import jrfeng.musicplayer.player.MusicPlayerClient;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.main.nav.vl_adpter.BackTopAdapter;
import jrfeng.simplemusic.activity.main.nav.vl_adpter.MusicListAdapter;
import jrfeng.simplemusic.activity.main.nav.vl_adpter.MusicListTitleAdapter;
import jrfeng.simplemusic.activity.main.nav.vl_adpter.NavMenuAdapter;
import jrfeng.simplemusic.adpter.vlayout.DividerAdapter;
import jrfeng.simplemusic.widget.DropDownMenu;

public class NavigationFragment extends Fragment implements NavigationContract.View {
    private NavigationContract.Presenter mPresenter;

    private View abSearch;
    private RecyclerView rvNavList;

    private VirtualLayoutManager mVLManager;
    private NavMenuAdapter mNavMenuAdapter;
    private MusicListTitleAdapter mMusicListTitleAdapter;
    private MusicListAdapter mMusicListAdapter;

    private DropDownMenu mPlayModeMenu;

    //*************************private**************************

    private void findViews(View contentView) {
        abSearch = contentView.findViewById(R.id.abSearch);
        rvNavList = contentView.findViewById(R.id.rvNavList);
    }

    private void addViewListener() {
        abSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO 打开搜索页面
            }
        });
    }

    private void initViews() {
        //初始化RecyclerView
        mVLManager = new VirtualLayoutManager(getContext());
        rvNavList.setLayoutManager(mVLManager);
        DelegateAdapter delegateAdapter = new DelegateAdapter(mVLManager);
        rvNavList.setAdapter(delegateAdapter);

        RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
        rvNavList.setRecycledViewPool(viewPool);
        viewPool.setMaxRecycledViews(0, 10);

        mNavMenuAdapter = new NavMenuAdapter(getContext(), mPresenter);
        delegateAdapter.addAdapter(mNavMenuAdapter);

        delegateAdapter.addAdapter(new DividerAdapter(getContext()));
        delegateAdapter.addAdapter(new BackTopAdapter(getContext(), this));

        mMusicListTitleAdapter = new MusicListTitleAdapter(getContext(), mPresenter);
        delegateAdapter.addAdapter(mMusicListTitleAdapter);

        mMusicListAdapter = new MusicListAdapter(getContext(), mPresenter);
        delegateAdapter.addAdapter(mMusicListAdapter);
    }

    //************************Override**************************

    @Override
    public void setPresenter(NavigationContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_navigation, container, false);
        findViews(contentView);
        initViews();
        addViewListener();
        return contentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.begin();
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.end();
    }

    @Override
    public void refreshMenusDescribe() {
        mNavMenuAdapter.refreshMenusDescribe();
    }

    @Override
    public void refreshRecentPlayCount() {
        mNavMenuAdapter.refreshRecentPlayCount();
    }

    @Override
    public void refreshMusicListTitle() {
        mMusicListTitleAdapter.refreshMusicListTitle();
    }

    @Override
    public void refreshMusicList() {
        mMusicListAdapter.notifyDataSetChanged();
    }

    @Override
    public void refreshPlayingMusicPosition(int position) {
        mMusicListAdapter.setChoice(position);
    }

    @Override
    public void setViewPlayMode(MusicPlayerClient.PlayMode mode) {
        mMusicListTitleAdapter.setViewPlayMode(mode);
    }

    @Override
    public void musicListScrollTo(int position) {
        //定位播放中的歌曲
        if (mMusicListAdapter.getItemCount() > 0) {
            final int index = position + 9;
            int p1 = mVLManager.findFirstCompletelyVisibleItemPosition();
            int p2 = mVLManager.findLastCompletelyVisibleItemPosition();

            Log.d("Indicator", "P1 : " + p1);
            Log.d("Indicator", "P2 : " + p2);

            int offset = (p2 - p1 + 1) / 2;
            if (index >= p2) {
                Log.d("Indicator", ">= P2");
                mVLManager.scrollToPosition(index + offset);
            } else if (index <= p1) {
                Log.d("Indicator", "<= P1");
                mVLManager.scrollToPosition(index - offset);
            }
        }
    }

    @Override
    public void backTop() {
        rvNavList.scrollToPosition(0);
    }

    @Override
    public void showPlayModeMenu(android.view.View anchorView) {
        if (mPlayModeMenu == null) {
            initPlayModeMenu(anchorView);
        }
        mPlayModeMenu.show();
    }

    private void initPlayModeMenu(View anchorView) {
        List<DropDownMenu.Item> items = new ArrayList<>(3);
        items.add(new DropDownMenu.Item(R.mipmap.ic_mode_order, "顺序播放"));
        items.add(new DropDownMenu.Item(R.mipmap.ic_mode_loop, "循环播放"));
        items.add(new DropDownMenu.Item(R.mipmap.ic_mode_random, "随机播放"));
        mPlayModeMenu = new DropDownMenu(anchorView, items).setOnItemClickedListener(new DropDownMenu.OnItemClickListener() {
            @Override
            public void onItemClicked(int position) {
                switch (position) {
                    case 0:
                        mPresenter.setPlayMode(MusicPlayerClient.PlayMode.MODE_ORDER);
                        break;
                    case 1:
                        mPresenter.setPlayMode(MusicPlayerClient.PlayMode.MODE_LOOP);
                        break;
                    case 2:
                        mPresenter.setPlayMode(MusicPlayerClient.PlayMode.MODE_RANDOM);
                        break;
                }
            }
        });
    }
}
