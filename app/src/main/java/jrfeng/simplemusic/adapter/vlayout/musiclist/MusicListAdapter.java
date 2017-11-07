package jrfeng.simplemusic.adapter.vlayout.musiclist;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;

import java.util.ArrayList;
import java.util.List;

import jrfeng.player.data.Music;
import jrfeng.player.mode.MusicStorage;
import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.choice.MultiChoiceActivity;
import jrfeng.simplemusic.utils.wrapper.ViewHeightWrapper;
import jrfeng.simplemusic.widget.CustomAlertDialog;
import jrfeng.simplemusic.widget.BottomListDialog;
import jrfeng.simplemusic.widget.Divider;

public class MusicListAdapter extends DelegateAdapter.Adapter<MusicListAdapter.ViewHolder> {
    private static final String TAG = "MusicListAdapter";
    private Context mContext;
    private MusicStorage.GroupType mGroupType;
    private String mGroupName;
    private MusicListPresenter mPresenter;

    private List<Music> mMusicGroup;
    private SparseArray<ViewHolder> mHolders;

    private int mLastPlayingPosition;
    private int mLastClickedMenuPosition;

    private int mClickedItemPosition;
    private int mOffset;

    private CustomAlertDialog mAlertDialog;
    private BottomListDialog mBottomListDialog;

    private CustomAlertDialog.OnButtonClickListener mRemoveListener;
    private CustomAlertDialog.OnButtonClickListener mDeleteListener;
    private CustomAlertDialog.OnButtonClickListener mInputListener;

    private BottomListDialog.OnAdditionButtonClickListener mAdditionButtonClickListener;

    private boolean mDisableRemove;

    public MusicListAdapter(Context context, MusicStorage.GroupType groupType,
                            String groupName, int offset) {
        mContext = context;
        mGroupType = groupType;
        mGroupName = groupName;
        mPresenter = new MusicListPresenter(context, groupType, groupName);
        mOffset = offset;
        mMusicGroup = mPresenter.getMusicGroup();
        mHolders = new SparseArray<>(mMusicGroup.size());
        mLastPlayingPosition = -1;
        mLastClickedMenuPosition = -1;
        mAlertDialog = new CustomAlertDialog(mContext);

        if (groupType == MusicStorage.GroupType.ALBUM_LIST
                || groupType == MusicStorage.GroupType.ARTIST_LIST) {
            mDisableRemove = true;
        }

        initViewListeners();
    }

    public int getOffset() {
        return mOffset;
    }

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        return new LinearLayoutHelper();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.widget_music_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mHolders.append(position, holder);
        Music music = mMusicGroup.get(position);
        holder.tvSongName.setText(music.getName());
        holder.tvSongArtist.setText(music.getArtist());

        //指示器
        if (mPresenter.getPlayingMusicPosition() == position) {
            //调试
            log("onBindViewHolder");
            setChoice(position);
        } else {
            setNotChoice(position);
        }

        int i = mMusicGroup.indexOf(mPresenter.getPlayingMusic());
        if (!mPresenter.playingCurrentMusicGroup() && i == position) {
            holder.dot.setVisibility(View.VISIBLE);
        } else {
            holder.dot.setVisibility(View.GONE);
        }

        if (mLastClickedMenuPosition == position) {
            showItemMenuNoAnim(position);
        } else {
            hideItemMenuNoAnim(position);
        }
        addViewListener(holder, position);
    }

    @Override
    public int getItemCount() {
        return mMusicGroup.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View listItem;
        View playingIndicator;
        ImageButton ibTempPlay;
        TextView tvSongName;
        TextView tvSongArtist;
        TextView tvTempPlayMark;
        View dot;
        ImageButton ibMore;
        View vgItemMenu;
        boolean isMenuShowing;

        //MenuItems
        ImageButton ibMenuTempPlay;
        ImageButton ibMenuLove;
        ImageButton ibMenuAddTo;
        ImageButton ibMenuRemove;
        TextView tvMenuRemoveLabel;
        ImageButton ibMenuDelete;

        ViewHolder(View itemView) {
            super(itemView);
            this.listItem = itemView.findViewById(R.id.vgListItem);
            playingIndicator = itemView.findViewById(R.id.playingIndicator);
            ibTempPlay = itemView.findViewById(R.id.ibTempPlay);
            tvSongName = itemView.findViewById(R.id.tvSongName);
            tvSongArtist = itemView.findViewById(R.id.tvSongArtist);
            tvTempPlayMark = itemView.findViewById(R.id.tvTempPlayMark);
            dot = itemView.findViewById(R.id.dot);
            ibMore = itemView.findViewById(R.id.ibItemMore);
            vgItemMenu = itemView.findViewById(R.id.vgItemMenu);

            ibMenuTempPlay = itemView.findViewById(R.id.ibMenuTempPlay);
            ibMenuLove = itemView.findViewById(R.id.ibMenuLove);
            ibMenuAddTo = itemView.findViewById(R.id.ibMenuAddTo);
            ibMenuRemove = itemView.findViewById(R.id.ibMenuRemove);
            tvMenuRemoveLabel = itemView.findViewById(R.id.tvMenuRemoveLabel);
            ibMenuDelete = itemView.findViewById(R.id.ibMenuDelete);

            if (mDisableRemove) {
                ibMenuRemove.setVisibility(View.GONE);
                tvMenuRemoveLabel.setVisibility(View.GONE);
            }
        }
    }

    //******************public*********************

    public void setChoice(int position) {
        setNotChoice(mLastPlayingPosition);
        mLastPlayingPosition = position;
        ViewHolder holder = mHolders.get(position);

        //调试
        Log.d("Indicator", "Position : " + position);

        if (holder != null && (holder.getAdapterPosition() - mOffset == position)) {
            holder.playingIndicator.setVisibility(View.VISIBLE);
            holder.tvSongName.setTextColor(Color.parseColor("#64B5F6"));//Blue300
            holder.tvSongArtist.setTextColor(Color.parseColor("#64B5F6"));//Blue300
            if (mPresenter.isPlayingTempMusic()) {
                //调试
                Log.d("Indicator", "临时播");
                holder.tvTempPlayMark.setVisibility(View.VISIBLE);
            } else {
                holder.tvTempPlayMark.setVisibility(View.GONE);
                //调试
                Log.d("Indicator", "非临时播");
            }
        }
    }

    //******************private********************

    private void initViewListeners() {
        mRemoveListener = new CustomAlertDialog.OnButtonClickListener() {
            @Override
            public void onButtonClicked(String input, boolean optionChecked, int arg) {
                mPresenter.removeMusicFromCurrentList(mMusicGroup.get(arg));
            }
        };

        mDeleteListener = new CustomAlertDialog.OnButtonClickListener() {
            @Override
            public void onButtonClicked(String input, boolean optionChecked, int arg) {
                Music music = mMusicGroup.get(arg);
                if (optionChecked) {
                    mPresenter.deleteMusicFile(music);
                } else {
                    mPresenter.removeMusicFromAllMusic(music);
                }
            }
        };

        mInputListener = new CustomAlertDialog.OnButtonClickListener() {
            @Override
            public void onButtonClicked(String input, boolean optionChecked, int arg) {
                if (input != null && !input.equals("")) {
                    mPresenter.createNewMusicList(input);
                    mPresenter.addMusicToMusicList(mMusicGroup.get(arg), input);
                }
            }
        };

        mAdditionButtonClickListener = new BottomListDialog.OnAdditionButtonClickListener() {
            @Override
            public boolean onClick() {
                showInputDialog("新建歌单", mClickedItemPosition);
                return true;
            }
        };
    }

    private void addViewListener(ViewHolder holder, final int position) {
        holder.listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.playPause(position);
            }
        });

        holder.listItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(mContext, MultiChoiceActivity.class);
                intent.putExtra(MultiChoiceActivity.KEY_GROUP_TYPE, mGroupType.name());
                intent.putExtra(MultiChoiceActivity.KEY_GROUP_NAME, mGroupName);
                intent.putExtra(MultiChoiceActivity.KEY_CHECKED_ITEM, position);
                mContext.startActivity(intent);
                return true;
            }
        });

        holder.ibTempPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempPlay(view, position);
            }
        });

        holder.ibMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewHolder viewHolder = mHolders.get(position);
                if (!viewHolder.isMenuShowing) {
                    hideItemMenuNoAnim(mLastClickedMenuPosition);
                    mLastClickedMenuPosition = -1;
                    showItemMenu(position);
                } else {
                    hideItemMenu(position);
                }
            }
        });

        //*********************列表项菜单*****************

        //临时播
        holder.ibMenuTempPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempPlay(mHolders.get(position).ibTempPlay, position);
                hideItemMenu(position);
            }
        });

        //我喜欢
        holder.ibMenuLove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewHolder viewHolder = mHolders.get(position);
                Music music = mMusicGroup.get(position);
                if (!mPresenter.isILove(music)) {
                    mPresenter.addMusicToILove(music);
                    viewHolder.ibMenuLove.setImageLevel(2);
                } else {
                    mPresenter.removeMusicFromILove(music);
                    viewHolder.ibMenuLove.setImageLevel(1);
                }
            }
        });

        //添加到
        final int index = position;
        holder.ibMenuAddTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> titles = mPresenter.getMusicListNames();
                List<Integer> describes = mPresenter.getMusicListsSize();

                List<BottomListDialog.Item> items = new ArrayList<>(titles.size());
                for (int i = 0; i < titles.size(); i++) {
                    items.add(new BottomListDialog.Item(titles.get(i), describes.get(i).toString() + "首"));
                }

                mBottomListDialog = new BottomListDialog.Builder(mContext)
                        .setAdditionIconId(R.mipmap.ic_add, mAdditionButtonClickListener)
                        .setItems(items)
                        .setOnItemClickListener(new BottomListDialog.OnItemClickListener() {
                            @Override
                            public void onItemClicked(BottomListDialog dialog, BottomListDialog.Item item, int position) {
                                mPresenter.addMusicToMusicList(mMusicGroup.get(index), item.getTitle());
                                mBottomListDialog.dismiss();
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
        });

        //移除
        if (!mDisableRemove) {
            holder.ibMenuRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideItemMenu(position);
                    showRemoveDialog(mMusicGroup.get(position).getName(), position);
                }
            });
        }

        //删除
        holder.ibMenuDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideItemMenu(position);
                showDeleteDialog(mMusicGroup.get(position).getName(), position);
            }
        });
    }

    //临时播特效
    private void playEffects(View emitView) {
        final View effectsView = new Divider(mContext, null);
        effectsView.setBackgroundResource(R.mipmap.ic_music_note);
        final ViewGroup rootView = (ViewGroup) emitView.getRootView();
        int size = mContext.getResources().getDimensionPixelSize(R.dimen.EffectsSize);
        rootView.addView(effectsView, size, size);
        int[] locate = new int[2];
        emitView.getLocationInWindow(locate);
        effectsView.setX(locate[0] + emitView.getMeasuredWidth() / 2 - size / 2);
        effectsView.setY(locate[1] + emitView.getMeasuredHeight() / 2 - size / 2);
        ObjectAnimator animator = ObjectAnimator.ofFloat(effectsView, "y",
                effectsView.getY(), rootView.getBottom());
        animator.setDuration(800);
        animator.setInterpolator(new BounceInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                rootView.removeView(effectsView);
            }
        });
        animator.start();
    }

    private void setNotChoice(int position) {
        if (position < 0) {
            return;
        }
        ViewHolder holder = mHolders.get(position);
        if (holder != null) {
            holder.playingIndicator.setVisibility(View.GONE);
            holder.tvSongName.setTextColor(mContext.getResources().getColor(R.color.colorListTitle));
            holder.tvSongArtist.setTextColor(mContext.getResources().getColor(R.color.colorListDescribe));
            holder.tvTempPlayMark.setVisibility(View.GONE);
        }
    }

    private void showItemMenu(int position) {
        ViewHolder viewHolder = mHolders.get(position);
        viewHolder.vgItemMenu.setVisibility(View.VISIBLE);
        viewHolder.isMenuShowing = true;
        mLastClickedMenuPosition = position;
        mClickedItemPosition = position;
        Music music = mMusicGroup.get(position);
        if (mPresenter.isILove(music)) {
            viewHolder.ibMenuLove.setImageLevel(2);
        } else {
            viewHolder.ibMenuLove.setImageLevel(1);
        }
        Animator in = AnimatorInflater.loadAnimator(mContext,
                R.animator.list_itme_menu_transition_in);
        in.setTarget(new ViewHeightWrapper(viewHolder.vgItemMenu));
        in.start();
        //调试
        log("show menu");
    }

    private void showItemMenuNoAnim(int position) {
        ViewHolder viewHolder = mHolders.get(position);
        viewHolder.vgItemMenu.setVisibility(View.VISIBLE);
        viewHolder.isMenuShowing = true;
        Music music = mMusicGroup.get(position);
        if (mPresenter.isILove(music)) {
            viewHolder.ibMenuLove.setImageLevel(2);
        } else {
            viewHolder.ibMenuLove.setImageLevel(1);
        }
        Animator in = AnimatorInflater.loadAnimator(mContext,
                R.animator.list_itme_menu_transition_in);
        in.setTarget(new ViewHeightWrapper(viewHolder.vgItemMenu));
        in.setDuration(0);
        in.start();
        //调试
        log("show menu");
    }

    private void hideItemMenu(int position) {
        if (position > -1) {
            final ViewHolder viewHolder = mHolders.get(position);
            viewHolder.isMenuShowing = false;
            mLastClickedMenuPosition = -1;
            Animator in = AnimatorInflater.loadAnimator(mContext,
                    R.animator.list_itme_menu_transition_out);
            in.setTarget(new ViewHeightWrapper(viewHolder.vgItemMenu));
            in.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    viewHolder.vgItemMenu.setVisibility(View.GONE);
                }
            });
            in.start();
            //调试
            log("hide menu");
        }
    }

    private void hideItemMenuNoAnim(int position) {
        if (position > -1) {
            ViewHolder viewHolder = mHolders.get(position);
            viewHolder.vgItemMenu.setVisibility(View.GONE);
            viewHolder.isMenuShowing = false;
            Animator in = AnimatorInflater.loadAnimator(mContext,
                    R.animator.list_itme_menu_transition_out);
            in.setTarget(new ViewHeightWrapper(viewHolder.vgItemMenu));
            in.setDuration(0);
            in.start();
        }
    }

    private void tempPlay(View emitView, int position) {
        mPresenter.addMusicToTempPlay(mMusicGroup.get(position));
        Toast.makeText(mContext, "临时播 已添加", Toast.LENGTH_SHORT).show();
        //特效
        playEffects(emitView);
    }

    private void showRemoveDialog(String title, int position) {
        mAlertDialog.setStyle(CustomAlertDialog.Style.JUST_MESSAGE);
        mAlertDialog.setTitle(title);
        mAlertDialog.setMessage("移除音乐？");
        mAlertDialog.setPositiveButtonListener(mRemoveListener);
        mAlertDialog.show(position);
    }

    private void showDeleteDialog(String title, int position) {
        mAlertDialog.setStyle(CustomAlertDialog.Style.MESSAGE_AND_OPTION);
        mAlertDialog.setTitle(title);
        mAlertDialog.setMessage("删除音乐？");
        mAlertDialog.setOptionText("同时删除本地文件");
        mAlertDialog.setPositiveButtonListener(mDeleteListener);
        mAlertDialog.show(position);
    }

    private void showInputDialog(String title, int position) {
        CustomAlertDialog dialog = new CustomAlertDialog(mContext);
        dialog.setStyle(CustomAlertDialog.Style.INPUT);
        dialog.setTitle(title);
        dialog.setInputHint("歌单名称");
        dialog.setPositiveButtonListener(mInputListener);
        dialog.show(position);
    }

    //******************调试用******************

    private static void log(String msg) {
        if (MyApplication.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
