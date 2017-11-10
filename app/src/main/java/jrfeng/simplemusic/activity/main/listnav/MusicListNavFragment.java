package jrfeng.simplemusic.activity.main.listnav;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;

import jrfeng.player.mode.MusicStorage;
import jrfeng.player.player.MusicPlayerClient;
import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.main.MainActivity;
import jrfeng.simplemusic.activity.main.listnav.adpter.MusicGroupAdapter;
import jrfeng.simplemusic.adapter.vlayout.BackTopAdapter;
import jrfeng.simplemusic.widget.CustomAlertDialog;
import jrfeng.simplemusic.widget.CustomDropDownMenu;

public class MusicListNavFragment extends Fragment implements MusicListNavContract.View {
    public static final String KEY_GROUP_TYPE = "groupType";

    private static final String TAG = "MusicListNavFragment";

    private Context mContext;
    private MusicListNavContract.Presenter mPresenter;

    private MusicStorage.GroupType mGroupType;

    private ImageButton ibClose;
    private TextView tvActionBarTitle;
    private ImageButton ibPlayMode;
    private TextView tvPlayModeLabel;
    private ImageButton ibAdd;
    private RecyclerView rvMusicGroupList;

    private MusicGroupAdapter mAdapter;

    private CustomAlertDialog.OnButtonClickListener mInputListener;

    //*****************private**********

    private void findViews(View contentView) {
        ibClose = contentView.findViewById(R.id.ibClose);
        tvActionBarTitle = contentView.findViewById(R.id.tvActionBarTitle);
        ibPlayMode = contentView.findViewById(R.id.ibPlayMode);
        tvPlayModeLabel = contentView.findViewById(R.id.tvPlayModeLabel);
        ibAdd = contentView.findViewById(R.id.ibAdd);
        rvMusicGroupList = contentView.findViewById(R.id.rvMusicGroupList);
    }

    private void addViewListener() {
        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack();
            }
        });

        ibPlayMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPlayModeMenu(view);
            }
        });

        mInputListener = new CustomAlertDialog.OnButtonClickListener() {
            @Override
            public void onButtonClicked(String input, boolean optionChecked, int arg) {
                if (input != null && !input.equals("")) {
                    mPresenter.createNewMusicList(input);
                }
            }
        };

        ibAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddMusicListDialog();
            }
        });

        tvPlayModeLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ibPlayMode.callOnClick();
            }
        });
    }

    private void initViews() {
        VirtualLayoutManager vlManager = new VirtualLayoutManager(mContext);
        DelegateAdapter delegateAdapter = new DelegateAdapter(vlManager);

        delegateAdapter.addAdapter(new BackTopAdapter(mContext, vlManager));
        mAdapter = new MusicGroupAdapter(mContext, mPresenter, mGroupType);
        delegateAdapter.addAdapter(mAdapter);

        rvMusicGroupList.setAdapter(delegateAdapter);
        rvMusicGroupList.setLayoutManager(vlManager);
    }

    public void showPlayModeMenu(android.view.View anchorView) {
        CustomDropDownMenu playModeMenu = new CustomDropDownMenu(anchorView, R.menu.play_mode);
        playModeMenu.setOnItemClickedListener(new CustomDropDownMenu.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemId) {
                switch (itemId) {
                    case R.id.mode_order:
                        ibPlayMode.setImageLevel(1);
                        tvPlayModeLabel.setText("顺序播放");
                        mPresenter.setPlayMode(MusicPlayerClient.PlayMode.MODE_ORDER);
                        break;
                    case R.id.mode_loop:
                        ibPlayMode.setImageLevel(2);
                        tvPlayModeLabel.setText("循环播放");
                        mPresenter.setPlayMode(MusicPlayerClient.PlayMode.MODE_LOOP);
                        break;
                    case R.id.mode_random:
                        ibPlayMode.setImageLevel(3);
                        tvPlayModeLabel.setText("随机播放");
                        mPresenter.setPlayMode(MusicPlayerClient.PlayMode.MODE_RANDOM);
                        break;
                }
            }
        });
        playModeMenu.show();
    }

    private void showAddMusicListDialog() {
        CustomAlertDialog dialog = new CustomAlertDialog(mContext);
        dialog.setStyle(CustomAlertDialog.Style.INPUT);
        dialog.setTitle("新建歌单");
        dialog.setInputHint("歌单名称");
        dialog.setPositiveButtonListener(mInputListener);
        dialog.show();
    }

    //*****************Fragment**********

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_music_list_nav, container, false);
        findViews(contentView);
        addViewListener();
        initViews();
        return contentView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        Bundle args = getArguments();
        mGroupType = MusicStorage.GroupType.valueOf(args.getString(KEY_GROUP_TYPE,
                MusicStorage.GroupType.MUSIC_LIST.name()));
        mPresenter = new MusicListNavPresenter(mContext, this, mGroupType);
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.begin();
        if (mGroupType != MusicStorage.GroupType.MUSIC_LIST) {
            ibAdd.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.end();
    }

    //********************调试用********************

    private static void log(String msg) {
        if (MyApplication.DEBUG) {
            Log.d(TAG, msg);
        }
    }

    //*****************View***************

    @Override
    public void setPresenter(MusicListNavContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void refreshActionBarTitle() {
        switch (mGroupType) {
            case MUSIC_LIST:
                int listCount = mPresenter.getMusicListCount();
                tvActionBarTitle.setText("歌单 · " + listCount);
                break;
            case ALBUM_LIST:
                int albumCount = mPresenter.getAlbumCount();
                tvActionBarTitle.setText("专辑 · " + albumCount);
                break;
            case ARTIST_LIST:
                int artistCount = mPresenter.getArtistCount();
                tvActionBarTitle.setText("歌手 · " + artistCount);
        }
    }

    @Override
    public void refreshGroupList() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void refreshPlayMode() {
        switch (mPresenter.getPlayMode()) {
            case MODE_ORDER:
                ibPlayMode.setImageLevel(1);
                tvPlayModeLabel.setText("顺序播放");
                break;
            case MODE_LOOP:
                ibPlayMode.setImageLevel(2);
                tvPlayModeLabel.setText("循环播放");
                break;
            case MODE_RANDOM:
                ibPlayMode.setImageLevel(3);
                tvPlayModeLabel.setText("随机播放");
                break;
        }
    }

    @Override
    public void startFragment(Fragment fragment) {
        ((MainActivity) getActivity()).startFragment(fragment);
    }
}
