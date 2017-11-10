package jrfeng.simplemusic.activity.main.nav;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;

import jrfeng.player.mode.MusicStorage;
import jrfeng.player.player.MusicPlayerClient;
import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.main.MainActivity;
import jrfeng.simplemusic.adapter.vlayout.BackTopAdapter;
import jrfeng.simplemusic.adapter.vlayout.musiclist.MusicListAdapter;
import jrfeng.simplemusic.activity.main.nav.adpter.MusicListTitleAdapter;
import jrfeng.simplemusic.activity.main.nav.adpter.NavMenuAdapter;
import jrfeng.simplemusic.adapter.vlayout.DividerAdapter;
import jrfeng.simplemusic.dialog.SortMusicListDialog;
import jrfeng.simplemusic.dialog.TempPlayDialog;
import jrfeng.simplemusic.widget.CustomDropDownMenu;

public class NavigationFragment extends Fragment implements NavigationContract.View {
    private static final String TAG = "NavigationFragment";
    private NavigationContract.Presenter mPresenter;

    private TextView tvSearch;
    private RecyclerView rvNavList;

    private VirtualLayoutManager mVLManager;
    private NavMenuAdapter mNavMenuAdapter;
    private MusicListTitleAdapter mMusicListTitleAdapter;
    private MusicListAdapter mMusicListAdapter;

    private MainActivity mMainActivity;

    //*************************private**************************

    private void findViews(View contentView) {
        tvSearch = contentView.findViewById(R.id.etSearchInput);
        rvNavList = contentView.findViewById(R.id.rvNavList);
    }

    private void addViewListener() {
        tvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //启动搜索 Activity
                mMainActivity.startSearchActivity(
                        MusicStorage.GroupType.MUSIC_LIST,
                        MusicStorage.MUSIC_LIST_ALL_MUSIC);
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

        delegateAdapter.addAdapter(new BackTopAdapter(getContext(), mVLManager));

        mMusicListTitleAdapter = new MusicListTitleAdapter(getContext(), mPresenter);
        delegateAdapter.addAdapter(mMusicListTitleAdapter);

        mMusicListAdapter = new MusicListAdapter(
                getContext(),
                MusicStorage.GroupType.MUSIC_LIST,
                MusicStorage.MUSIC_LIST_ALL_MUSIC,
                9);
        delegateAdapter.addAdapter(mMusicListAdapter);
    }

    private void showTempList() {
        TempPlayDialog.show(getContext());
    }

    //************************Override**************************

    @Override
    public void setPresenter(NavigationContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new NavigationPresenter(getContext(), this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_navigation, container, false);
        findViews(contentView);
        addViewListener();
        initViews();
        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMainActivity = (MainActivity) getActivity();
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
    public void refreshAllView() {
        refreshMenusDescribe();
        refreshRecentPlayCount();
        refreshMusicListTitle();
        refreshMusicList();
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
        if (mPresenter.getAllMusicCount() > 0) {
            final int index = position + 9;
            int p1 = mVLManager.findFirstCompletelyVisibleItemPosition();
            int p2 = mVLManager.findLastCompletelyVisibleItemPosition();

            //调试
            log("P1 : " + p1);
            log("P2 : " + p2);

            int offset = (p2 - p1 + 1) / 2;
            if (index >= p2) {
                log(">= P2");
                mVLManager.scrollToPosition(Math.min(index + offset, mPresenter.getAllMusicCount() + 8));
            } else if (index <= p1) {
                log("<= P1");
                mVLManager.scrollToPosition(Math.max((index - offset), 0));
            } /*else {
                int offset2 = (index - (p1 + p2) / 2);
                if (offset2 < 0) {
                    mVLManager.scrollToPosition(p1 + offset2);
                } else {
                    mVLManager.scrollToPosition(p2 + offset2);
                }
            }*/
        }
    }

    @Override
    public void showPlayModeMenu(android.view.View anchorView) {
        CustomDropDownMenu playModeMenu = new CustomDropDownMenu(anchorView, R.menu.play_mode);
        playModeMenu.setOnItemClickedListener(new CustomDropDownMenu.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemId) {
                switch (itemId) {
                    case R.id.mode_order:
                        mPresenter.setPlayMode(MusicPlayerClient.PlayMode.MODE_ORDER);
                        break;
                    case R.id.mode_loop:
                        mPresenter.setPlayMode(MusicPlayerClient.PlayMode.MODE_LOOP);
                        break;
                    case R.id.mode_random:
                        mPresenter.setPlayMode(MusicPlayerClient.PlayMode.MODE_RANDOM);
                        break;
                }
            }
        });
        playModeMenu.show();
    }

    @Override
    public void showMore_Menu(View anchorView) {
        CustomDropDownMenu more_menu = new CustomDropDownMenu(anchorView, R.menu.list_title_more);
        more_menu.setOnItemClickedListener(new CustomDropDownMenu.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemId) {
                switch (itemId) {
                    case R.id.sort:
                        SortMusicListDialog.show(getActivity(), MusicStorage.MUSIC_LIST_ALL_MUSIC);
                        break;
                    case R.id.tempList:
                        showTempList();
                        break;
                }
            }
        });
        more_menu.show();
    }

    @Override
    public void startFragment(Fragment fragment) {
        mMainActivity.startFragment(fragment);
    }

    //******************调试用********************

    private static void log(String msg) {
        if (MyApplication.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
