package jrfeng.simplemusic.activity.navigation;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;

import java.util.LinkedList;

import jrfeng.simplemusic.R;
import jrfeng.simplemusic.adpter.vlayout.DividerAdapter;
import jrfeng.simplemusic.adpter.vlayout.NavigationMenuAdapter;
import jrfeng.simplemusic.adpter.vlayout.RecentPlayAdapter;
import jrfeng.simplemusic.adpter.vlayout.RecentPlayTitleAdapter;
import jrfeng.simplemusic.data.Music;

public class NavigationFragment extends Fragment implements NavigationContract.View {
    private View mContentView;
    private NavigationContract.Presenter mPresenter;
    private Context mContext;

    private RecyclerView rvNavMenu;

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
        init();
        return mContentView;
    }

    @Override
    public void setPresenter(NavigationContract.Presenter presenter) {
        mPresenter = presenter;
    }

    //***********************private**********************

    private void findViews() {
        rvNavMenu = mContentView.findViewById(R.id.rvNavMenu);
    }

    private void init() {
        VirtualLayoutManager vLayoutManager = new VirtualLayoutManager(mContext);
        rvNavMenu.setLayoutManager(vLayoutManager);

        RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
        viewPool.setMaxRecycledViews(0, 10);
        rvNavMenu.setRecycledViewPool(viewPool);

        DelegateAdapter delegateAdapter = new DelegateAdapter(vLayoutManager);
        rvNavMenu.setAdapter(delegateAdapter);

        NavigationMenuAdapter menuAdapter = new NavigationMenuAdapter(mContext, mPresenter);
        delegateAdapter.addAdapter(menuAdapter);

        DividerAdapter dividerAdapter = new DividerAdapter(mContext);
        delegateAdapter.addAdapter(dividerAdapter);

        RecentPlayTitleAdapter recentPlayTitleAdapter = new RecentPlayTitleAdapter(mContext);
        delegateAdapter.addAdapter(recentPlayTitleAdapter);

        RecentPlayAdapter recentPlayAdapter = new RecentPlayAdapter(mContext, new LinkedList<Music>());
        delegateAdapter.addAdapter(recentPlayAdapter);
    }
}
