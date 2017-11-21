package jrfeng.simplemusic.activity.choice;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jrfeng.player.data.Music;
import jrfeng.player.mode.MusicStorage;
import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.choice.adapter.MultiChoiceAdapter;
import jrfeng.simplemusic.widget.BottomListDialog;
import jrfeng.simplemusic.widget.CustomAlertDialog;


public class MultiChoiceFragment extends Fragment implements MultiChoiceContract.View {
    private static final String TAG = "MultiChoiceFragment";

    private Context mContext;
    private MultiChoiceContract.Presenter mPresenter;

    private MusicStorage.GroupType mGroupType;
    private String mGroupName;
    private int mSelectedItem;

    private List<Music> mMusicGroup;

    private ImageButton ibClose;
    private TextView tvActionBarTitle;
    private CheckBox cbCheckedAll;
    private TextView tvCheckedAllLabel;
    private TextView tvCheckedItemCount;
    private RecyclerView rvList;

    private ImageButton ibMenuTempPlay;
    private ImageButton ibMenuLove;
    private ImageButton ibMenuAddTo;
    private ImageButton ibMenuRemove;
    private ImageButton ibMenuDelete;

    private MultiChoiceAdapter mMultiChoiceAdapter;

    private boolean mDisableLove;
    private boolean mDisableRemove;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();

        Bundle args = getArguments();
        mGroupType = MusicStorage.GroupType.valueOf(args.getString(MultiChoiceActivity.KEY_GROUP_TYPE, MusicStorage.GroupType.MUSIC_LIST.name()));
        mGroupName = args.getString(MultiChoiceActivity.KEY_GROUP_NAME, MusicStorage.MUSIC_LIST_ALL_MUSIC);
        mSelectedItem = args.getInt(MultiChoiceActivity.KEY_CHECKED_ITEM, 0);

        mPresenter = new MultiChoicePresenter(mContext, this, mGroupType, mGroupName);
        mMusicGroup = mPresenter.getMusicGroup();

        if (mGroupType == MusicStorage.GroupType.MUSIC_LIST
                && mGroupName.equals(MusicStorage.MUSIC_LIST_I_LOVE)) {
            mDisableLove = true;
        }

        if (mGroupType != MusicStorage.GroupType.MUSIC_LIST) {
            mDisableRemove = true;
        }

        //调试
        log("GroupType   : " + mGroupType.name());
        log("GroupName   : " + mGroupName);
        log("CheckedItem : " + mSelectedItem);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_multi_choice, container, false);
        findViews(contentView);
        initViews();
        addViewListener();
        return contentView;
    }

    @Override
    public void setPresenter(MultiChoiceContract.Presenter presenter) {
        mPresenter = presenter;
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

    //*******************View*****************

    @Override
    public void close() {
        getActivity().finish();
    }

    //*******************private**************

    private void findViews(View contentView) {
        ibClose = contentView.findViewById(R.id.ibClose);
        tvActionBarTitle = contentView.findViewById(R.id.tvActionBarTitle);
        cbCheckedAll = contentView.findViewById(R.id.cbCheckedAll);
        tvCheckedAllLabel = contentView.findViewById(R.id.tvCheckedAllLabel);
        tvCheckedItemCount = contentView.findViewById(R.id.tvCheckedItemCount);
        rvList = contentView.findViewById(R.id.rvList);

        ibMenuTempPlay = contentView.findViewById(R.id.ibMenuTempPlay);
        ibMenuLove = contentView.findViewById(R.id.ibMenuLove);
        TextView tvMenuLoveLabel = contentView.findViewById(R.id.tvMenuLoveLabel);
        ibMenuAddTo = contentView.findViewById(R.id.ibMenuAddTo);
        ibMenuRemove = contentView.findViewById(R.id.ibMenuRemove);
        TextView tvMenuRemoveLabel = contentView.findViewById(R.id.tvMenuRemoveLabel);
        ibMenuDelete = contentView.findViewById(R.id.ibMenuDelete);

        if(mDisableLove) {
            ibMenuLove.setVisibility(View.GONE);
            tvMenuLoveLabel.setVisibility(View.GONE);
        }

        if (mDisableRemove) {
            ibMenuRemove.setVisibility(View.GONE);
            tvMenuRemoveLabel.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        initActionBarTitle();

        mMultiChoiceAdapter = new MultiChoiceAdapter(mContext, mMusicGroup, mSelectedItem, new MultiChoiceAdapter.OnSelectedListener() {
            @Override
            public void onItemSelected(boolean selectedAll, int selectedCount) {
                cbCheckedAll.setChecked(selectedAll);
                tvCheckedItemCount.setText("已选" + selectedCount + "首");
            }
        });
        rvList.setAdapter(mMultiChoiceAdapter);
        rvList.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        rvList.scrollToPosition(mSelectedItem);
    }

    private void addViewListener() {
        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close();
            }
        });

        cbCheckedAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cbCheckedAll.isChecked()) {
                    mMultiChoiceAdapter.setSelectedAll();
                } else {
                    mMultiChoiceAdapter.setUnselectedAll();
                }
            }
        });

        tvCheckedAllLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!cbCheckedAll.isChecked()) {
                    cbCheckedAll.setChecked(true);
                    mMultiChoiceAdapter.setSelectedAll();
                } else {
                    cbCheckedAll.setChecked(false);
                    mMultiChoiceAdapter.setUnselectedAll();
                }
            }
        });

        tvCheckedItemCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final List<Integer> selectedOrders = mMultiChoiceAdapter.getSelectedItemsOrder();
                final List<BottomListDialog.Item> items = new ArrayList<>(selectedOrders.size());

                for (Integer i : selectedOrders) {
                    Music music = mMusicGroup.get(i);
                    items.add(new BottomListDialog.Item(music.getName(), music.getArtist()));
                }

                BottomListDialog dialog = new BottomListDialog.Builder(mContext)
                        .setTitle("已选 · " + items.size() + "首")
                        .setItems(items)
                        .create();

                dialog.setSupportSwipe(true);
                dialog.setOnSwipeListener(new BottomListDialog.OnSwipeListener() {
                    @Override
                    public void onSwipe(BottomListDialog dialog, int position) {
                        mMultiChoiceAdapter.unselected(selectedOrders.get(position));
                        dialog.setTitle("已选 · " + items.size() + "首");
                        if (items.size() == 0) {
                            dialog.dismiss();
                        }
                    }
                });

                dialog.show();
            }
        });

        //临时播
        ibMenuTempPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.addTempPlayMusics(getSelectedMusic());
            }
        });

        //我喜欢
        ibMenuLove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.addMusicsToILove(getSelectedMusic());
            }
        });

        //添加到
        ibMenuAddTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getSelectedMusic().size() < 1) {
                    Toast.makeText(mContext, "至少选择一首歌曲", Toast.LENGTH_SHORT).show();
                    return;
                }

                showAddToBottomDialog();
            }
        });

        //移除
        if (!mDisableRemove) {
            ibMenuRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getSelectedMusic().size() < 1) {
                        Toast.makeText(mContext, "至少选择一首歌曲", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    showRemoveAlertDialog();
                }
            });
        }

        //删除
        ibMenuDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getSelectedMusic().size() < 1) {
                    Toast.makeText(mContext, "至少选择一首歌曲", Toast.LENGTH_SHORT).show();
                    return;
                }

                showDeleteAlertDialog();
            }
        });
    }

    private void initActionBarTitle() {
        StringBuilder title = new StringBuilder();
        title.append("多选 · ");
        switch (mGroupType) {
            case MUSIC_LIST:
                title.append("歌单 · ");
                break;
            case ALBUM_LIST:
                title.append("专辑 · ");
                break;
            case ARTIST_LIST:
                title.append("歌手 · ");
                break;
        }

        switch (mGroupName) {
            case MusicStorage.MUSIC_LIST_ALL_MUSIC:
                title.replace(5, 9, "所有音乐");
                break;
            case MusicStorage.MUSIC_LIST_I_LOVE:
                title.replace(5, 9, "我喜欢");
                break;
            case MusicStorage.MUSIC_LIST_RECENT_PLAY:
                title.replace(5, 9, "最近播放");
                break;
            default:
                title.append(mGroupName);
                break;
        }

        title.append(" · ").append(mMusicGroup.size()).append("首");
        tvActionBarTitle.setText(title.toString());
    }

    private List<Music> getSelectedMusic() {
        List<Integer> selectedOrders = mMultiChoiceAdapter.getSelectedItemsOrder();
        List<Music> selectedMusics = new ArrayList<>(selectedOrders.size());
        for (Integer i : selectedOrders) {
            selectedMusics.add(mMusicGroup.get(i));
        }
        return selectedMusics;
    }

    private void showRemoveAlertDialog() {
        CustomAlertDialog dialog = new CustomAlertDialog(mContext);
        dialog.setTitle("移除音乐");
        dialog.setMessage("是否移除所选音乐？");
        dialog.setPositiveButtonListener(new CustomAlertDialog.OnButtonClickListener() {
            @Override
            public void onButtonClicked(String input, boolean optionChecked, int arg) {
                mPresenter.removeMusics(getSelectedMusic());
            }
        });
        dialog.show();
    }

    private void showDeleteAlertDialog() {
        CustomAlertDialog dialog = new CustomAlertDialog(mContext);
        dialog.setStyle(CustomAlertDialog.Style.MESSAGE_AND_OPTION);
        dialog.setTitle("删除音乐");
        dialog.setMessage("是否删除所选音乐？");
        dialog.setOptionText("同时删除本地文件");
        dialog.setPositiveButtonListener(new CustomAlertDialog.OnButtonClickListener() {
            @Override
            public void onButtonClicked(String input, boolean optionChecked, int arg) {
                if (optionChecked) {
                    mPresenter.deleteMusics(getSelectedMusic());
                } else {
                    mPresenter.removeMusicsFromAllMusic(getSelectedMusic());
                }
            }
        });
        dialog.show();
    }

    private void showAddToBottomDialog() {
        List<String> titles = mPresenter.getMusicListNames();
        List<Integer> describes = mPresenter.getMusicListsSize();

        List<BottomListDialog.Item> items = new ArrayList<>(titles.size());
        for (int i = 0; i < titles.size(); i++) {
            items.add(new BottomListDialog.Item(titles.get(i), describes.get(i).toString() + "首"));
        }

        BottomListDialog.OnAdditionButtonClickListener additionButtonClickListener = new BottomListDialog.OnAdditionButtonClickListener() {
            @Override
            public boolean onClick() {
                showInputDialog("新建歌单");
                return true;
            }
        };

        final BottomListDialog mBottomListDialog = new BottomListDialog.Builder(mContext)
                .setAdditionIconId(R.mipmap.ic_add, additionButtonClickListener)
                .setItems(items)
                .setOnItemClickListener(new BottomListDialog.OnItemClickListener() {
                    @Override
                    public void onItemClicked(BottomListDialog dialog, BottomListDialog.Item item, int position) {
                        mPresenter.addMusicsToMusicList(getSelectedMusic(), item.getTitle());
                        dialog.dismiss();
                    }
                })
                .create();

        if (items.size() > 0) {
            mBottomListDialog.setTitle("全部歌单");
        } else {
            mBottomListDialog.setTitle("全部歌单（空）");
        }

        mBottomListDialog.show();
    }

    private void showInputDialog(String title) {
        CustomAlertDialog dialog = new CustomAlertDialog(mContext);
        dialog.setStyle(CustomAlertDialog.Style.INPUT);
        dialog.setTitle(title);
        dialog.setInputHint("歌单名称");
        dialog.setPositiveButtonListener(new CustomAlertDialog.OnButtonClickListener() {
            @Override
            public void onButtonClicked(String input, boolean optionChecked, int arg) {
                mPresenter.createNewMusicList(input, getSelectedMusic());
            }
        });
        dialog.show();
    }

    //*******************调试用******************

    private static void log(String msg) {
        if (MyApplication.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
