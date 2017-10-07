package jrfeng.simplemusic.activity.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jrfeng.simplemusic.R;
import jrfeng.musicplayer.data.Music;

public class NavigationFragment extends Fragment implements NavigationContract.View {
    private NavigationContract.Presenter mPresenter;
    private Context mContext;

    private View abSearch;
    private RecyclerView rvNavList;
    private View playerController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_navigation, container, false);
        findViews(contentView);
        initView();
        addViewListener();
        return contentView;
    }

    //*****************private***********

    private void findViews(View contentView) {
        abSearch = contentView.findViewById(R.id.abSearch);
        rvNavList = contentView.findViewById(R.id.rvNavList);
        playerController = contentView.findViewById(R.id.playerController);
    }

    private void initView() {

    }

    private void addViewListener() {

    }

    //***********************************

    @Override
    public void setPresenter(NavigationContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void viewPause() {

    }

    @Override
    public void viewPlay() {

    }

    @Override
    public void refreshRecentPlay(int count) {

    }

    @Override
    public void disableListItem(int position) {

    }

    @Override
    public void refreshPlayerView(Music music) {

    }

    @Override
    public void refreshPlayingProgress(int progress) {

    }

    @Override
    public void refreshMusicListView() {

    }
}
