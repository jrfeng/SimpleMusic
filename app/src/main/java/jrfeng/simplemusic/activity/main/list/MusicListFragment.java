package jrfeng.simplemusic.activity.main.list;

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
import android.widget.Toast;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;

import jrfeng.player.mode.MusicStorage;
import jrfeng.player.player.MusicPlayerClient;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.main.MainActivity;
import jrfeng.simplemusic.adapter.vlayout.BackTopAdapter;
import jrfeng.simplemusic.adapter.vlayout.musiclist.MusicListAdapter;
import jrfeng.simplemusic.dialog.SortMusicListDialog;
import jrfeng.simplemusic.dialog.TempPlayDialog;
import jrfeng.simplemusic.widget.CustomAlertDialog;
import jrfeng.simplemusic.widget.CustomPopupMenu;

public class MusicListFragment extends Fragment implements MusicListContract.View {
    public static final String KEY_GROUP_TYPE = "groupType";
    public static final String KEY_GROUP_NAME = "groupName";

    private static final String TAG = "MusicListFragment";

    private Context mContext;
    private MusicListContract.Presenter mPresenter;

    private MusicStorage.GroupType mGroupType;
    private String mGroupName;

    private String mGroupTypeDescribe;
    private String mRealGroupName;

    private ImageButton ibClose;
    private TextView tvActionBarTitle;
    private ImageButton ibPlayMode;
    private TextView tvPlayModeLabel;
    private ImageButton ibSearch;
    private ImageButton ibLocateMusic;
    private ImageButton ibMore;
    private RecyclerView rvMusicList;

    private VirtualLayoutManager mVLManager;
    private MusicListAdapter mListAdapter;

    private int mMore_MenuResId;

    //*******************private*****************

    private void findViews(View contentView) {
        ibClose = contentView.findViewById(R.id.ibClose);
        tvActionBarTitle = contentView.findViewById(R.id.tvActionBarTitle);
        ibPlayMode = contentView.findViewById(R.id.ibPlayMode);
        tvPlayModeLabel = contentView.findViewById(R.id.tvPlayModeLabel);
        ibSearch = contentView.findViewById(R.id.ibSearch);
        ibLocateMusic = contentView.findViewById(R.id.ibLocateMusic);
        ibMore = contentView.findViewById(R.id.ibMore);
        rvMusicList = contentView.findViewById(R.id.rvMusicList);
    }

    private void addViewListener() {
        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close();
            }
        });

        ibPlayMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPlayModeMenu(view);
            }
        });

        tvPlayModeLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ibPlayMode.callOnClick();
            }
        });

        ibSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //打开搜索界面
                ((MainActivity) getActivity()).startSearchActivity(mGroupType, mGroupName);
            }
        });

        ibLocateMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = mPresenter.getPlayingMusicPosition();
                if (index != -1) {
                    musicListScrollTo(index);
                }
            }
        });

        ibMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMore_Menu(view);
            }
        });
    }

    private void initViews() {
        mMore_MenuResId = R.menu.list_title_more;

        switch (mGroupType) {
            case MUSIC_LIST:
                if (!mGroupName.equals(MusicStorage.MUSIC_LIST_ALL_MUSIC)
                        && !mGroupName.equals(MusicStorage.MUSIC_LIST_I_LOVE)
                        && !mGroupName.equals(MusicStorage.MUSIC_LIST_RECENT_PLAY)) {
                    mGroupTypeDescribe = "歌单 · ";
                } else {
                    mGroupTypeDescribe = "";
                }
                break;
            case ARTIST_LIST:
                mGroupTypeDescribe = "歌手 · ";
                break;
            case ALBUM_LIST:
                mGroupTypeDescribe = "专辑 · ";
                break;
        }

        switch (mGroupName) {
            case MusicStorage.MUSIC_LIST_ALL_MUSIC:
                tvActionBarTitle.setText("所有音乐");
                mRealGroupName = "所有音乐";
                break;
            case MusicStorage.MUSIC_LIST_I_LOVE:
                tvActionBarTitle.setText("我喜欢");
                mRealGroupName = "我喜欢";
                break;
            case MusicStorage.MUSIC_LIST_RECENT_PLAY:
                tvActionBarTitle.setText("最近播放");
                mRealGroupName = "最近播放";
                mMore_MenuResId = R.menu.music_list_recent_play;
                break;
            default:
                tvActionBarTitle.setText(mGroupName);
                mRealGroupName = mGroupName;
                break;
        }

        mVLManager = new VirtualLayoutManager(mContext);
        DelegateAdapter delegateAdapter = new DelegateAdapter(mVLManager);

        mListAdapter = new MusicListAdapter(mContext, mGroupType, mGroupName, 1);

        delegateAdapter.addAdapter(new BackTopAdapter(mContext, mVLManager));
        delegateAdapter.addAdapter(mListAdapter);

        rvMusicList.setAdapter(delegateAdapter);
        rvMusicList.setLayoutManager(mVLManager);
    }

    public void showPlayModeMenu(android.view.View anchorView) {
        CustomPopupMenu playModeMenu = new CustomPopupMenu(anchorView, R.menu.play_mode);
        playModeMenu.setOnItemClickedListener(new CustomPopupMenu.OnItemClickListener() {
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

    public void showMore_Menu(View anchorView) {
        CustomPopupMenu more_menu = new CustomPopupMenu(anchorView, mMore_MenuResId);
        more_menu.setOnItemClickedListener(new CustomPopupMenu.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemId) {
                switch (itemId) {
                    case R.id.clearRecentPlay:
                        showClearRecentPlayDialog();
                        break;
                    case R.id.sort:
                        showSortMenu();
                        break;
                    case R.id.tempList:
                        showTempList();
                        break;
                }
            }
        });
        more_menu.show();
    }

    private void showClearRecentPlayDialog() {
        CustomAlertDialog dialog = new CustomAlertDialog(mContext);
        dialog.setStyle(CustomAlertDialog.Style.JUST_MESSAGE);
        dialog.setTitle("清空记录");
        dialog.setMessage("是否清空记录？");
        dialog.setPositiveButtonListener(new CustomAlertDialog.OnButtonClickListener() {
            @Override
            public void onButtonClicked(String input, boolean optionChecked, int arg) {
                mPresenter.clearRecentPlayRecord();
            }
        });
        dialog.show();
    }

    private void showSortMenu() {
        if (mGroupType != MusicStorage.GroupType.MUSIC_LIST) {
            Toast.makeText(mContext, "暂不支持", Toast.LENGTH_SHORT).show();
        } else {
            SortMusicListDialog.show(getActivity(), mGroupName);
        }
    }

    private void showTempList() {
        TempPlayDialog.show(getContext());
    }

    @Override
    public void musicListScrollTo(int position) {
        //定位播放中的歌曲
        if (mPresenter.getMusicGroupSize() > 0) {
            //调试
            log("定位音乐");
            log("Position : " + position);
            int index = position + mListAdapter.getOffset();
            int p1 = mVLManager.findFirstCompletelyVisibleItemPosition();
            int p2 = mVLManager.findLastCompletelyVisibleItemPosition();

            //调试
            log("P1 : " + p1);
            log("P2 : " + p2);

            int offset = (p2 - p1 + 1) / 2;
            if (index >= p2) {
                //调试
                log(">= P2 : ScrollTo : " + Math.min(index + offset,
                        mPresenter.getMusicGroupSize()));
                mVLManager.scrollToPosition(Math.min(index + offset, mPresenter.getMusicGroupSize()));
            } else if (index <= p1) {
                //调试
                log("<= P1 : ScrollTo : " + Math.max((index - offset), 0));
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
    public void close() {
        getFragmentManager().popBackStack();
    }

    //************调试**************

    private static void log(String msg) {
        Log.d(TAG, msg);
    }

    //*******************Fragment****************


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        Bundle args = getArguments();
        mGroupType = MusicStorage.GroupType.valueOf(args.getString(KEY_GROUP_TYPE,
                MusicStorage.GroupType.MUSIC_LIST.name()));
        mGroupName = args.getString(KEY_GROUP_NAME, MusicStorage.MUSIC_LIST_ALL_MUSIC);
        mPresenter = new MusicListPresenter(mContext, this, mGroupType, mGroupName);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_music_list, container, false);
        findViews(contentView);
        addViewListener();
        initViews();
        return contentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.begin();
        if (mPresenter.isPlayingCurrentMusicGroup()) {
            mVLManager.scrollToPosition(Math.max(0, (mPresenter.getPlayingMusicPosition() - 2)));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.end();
    }

    //********************View*******************

    @Override
    public void setPresenter(MusicListContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void refreshMusicList() {
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void refreshPlayingMusicPosition(int position) {
        mListAdapter.setChoice(position);
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
    public void refreshTitle() {
        int size = mPresenter.getMusicGroupSize();
        tvActionBarTitle.setText(mGroupTypeDescribe + mRealGroupName + " · " + size + "首");
    }
}
