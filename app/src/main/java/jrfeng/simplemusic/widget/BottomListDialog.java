package jrfeng.simplemusic.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;

/**
 * 底部列表式对话框。
 */
public class BottomListDialog {
    private static final String TAG = "BottomListDialog";

    private Context mContext;
    private AppCompatDialog mDialog;

    private View vgTitleContainer;
    private TextView tvTitle;
    private ImageView ivAdditionIcon;
    private RecyclerView rvList;

    private ListAdapter mListAdapter;
    private List<Item> mItems;
    private SparseArray<ListAdapter.ViewHolder> mHolders;
    private boolean mSupportDrag;
    private boolean mSupportSwipe;

    private int mTargetPosition;

    private OnAdditionButtonClickListener mAdditionButtonClickListener;
    private OnItemClickListener mItemClickListener;
    private OnDragListener mOnDragListener;
    private OnSwipeListener mOnSwipeListener;
    private OnDismissListener mOnDismissListener;

    private BottomListDialog(Builder builder) {
        mContext = builder.context;
        mItems = builder.items;
        mHolders = new SparseArray<>(mItems.size());
        mSupportDrag = builder.supportDrag;
        mSupportSwipe = builder.supportSwipe;
        mTargetPosition = -1;

        mAdditionButtonClickListener = builder.additionButtonClickListener;
        mItemClickListener = builder.onItemClickListener;
        mOnDragListener = builder.onDragListener;
        mOnSwipeListener = builder.onSwipeListener;
        mOnDismissListener = builder.onDismissListener;

        createDialog();
        findViews();
        initViews(builder);

        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (mOnDismissListener != null) {
                    mOnDismissListener.onDismiss();
                }
            }
        });
    }

    public void setTitle(String title) {
        vgTitleContainer.setVisibility(View.VISIBLE);
        tvTitle.setText(title);
    }

    public void setAdditionIcon(int resId) {
        ivAdditionIcon.setVisibility(View.VISIBLE);
        ivAdditionIcon.setImageResource(resId);
    }

    public void setSupportDrag(boolean supportDrag) {
        mSupportDrag = supportDrag;
    }

    public void setSupportSwipe(boolean supportSwipe) {
        mSupportSwipe = supportSwipe;
    }

    public void show() {
        mDialog.show();
    }

    public void dismiss() {
        mDialog.dismiss();
    }

    public void setTarget(int position) {
        int lastTarget = mTargetPosition;

        if (lastTarget != -1) {
            ListAdapter.ViewHolder lastHolder = mHolders.get(lastTarget);
            lastHolder.tvItemOder.setTextColor(mContext.getResources().getColor(R.color.colorListTitle));
            lastHolder.tvItemText.setTextColor(mContext.getResources().getColor(R.color.colorListTitle));
            lastHolder.tvItemDescribe.setTextColor(mContext.getResources().getColor(R.color.colorListDescribe));
        }

        mTargetPosition = position;
        ListAdapter.ViewHolder holder = mHolders.get(mTargetPosition);

        if (holder != null) {
            int color = mContext.getResources().getColor(R.color.colorBlue300);
            holder.tvItemOder.setTextColor(color);
            holder.tvItemText.setTextColor(color);
            holder.tvItemDescribe.setTextColor(color);
        }

        rvList.scrollToPosition(position);
    }

    public void setAdditionButtonClickListener(OnAdditionButtonClickListener listener) {
        mAdditionButtonClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void setOnDragListener(OnDragListener listener) {
        mOnDragListener = listener;
    }

    public void setOnSwipeListener(OnSwipeListener listener) {
        mOnSwipeListener = listener;
    }

    public void setOnDismissListener(OnDismissListener listener) {
        mOnDismissListener = listener;
    }

    public void scrollToPosition(int position) {
        rvList.scrollToPosition(position);
    }

    public void notifyDataSetChanged() {
        mListAdapter.notifyDataSetChanged();
    }

    //*****************private******************

    private void createDialog() {
        mDialog = new AppCompatDialog(mContext);
        mDialog.setContentView(R.layout.widget_bottom_list_dialog);

        Window window = mDialog.getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setWindowAnimations(R.style.BottomDialogAnim);
            View decorView = window.getDecorView();
            decorView.setBackgroundColor(Color.TRANSPARENT);
            decorView.setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(lp);
        }
    }

    private void findViews() {
        vgTitleContainer = mDialog.findViewById(R.id.vgTitleContainer);
        tvTitle = (TextView) mDialog.findViewById(R.id.tvTitle);
        ivAdditionIcon = (ImageView) mDialog.findViewById(R.id.ivAdditionIcon);
        rvList = (RecyclerView) mDialog.findViewById(R.id.rvList);
    }

    private void initViews(final Builder builder) {
        mListAdapter = new ListAdapter();
        rvList.setAdapter(mListAdapter);
        rvList.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));

        if (!builder.title.equals("")) {
            setTitle(builder.title);
        }

        if (builder.additionIconId != 0) {
            setAdditionIcon(builder.additionIconId);
        }

        ivAdditionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean result = false;
                if (mAdditionButtonClickListener != null) {
                    result = mAdditionButtonClickListener.onClick();
                }
                if (result) {
                    dismiss();
                }
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchCallback());
        itemTouchHelper.attachToRecyclerView(rvList);
    }

    //****************Adapter**************

    private class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext)
                    .inflate(R.layout.widget_bottom_list_dialog_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            mHolders.put(position, holder);
            final Item item = mItems.get(position);

            holder.tvItemOder.setText(String.valueOf(position + 1));
            holder.tvItemText.setText(mItems.get(position).getTitle());
            holder.tvItemDescribe.setText(mItems.get(position).getDescribe());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClicked(BottomListDialog.this, item, holder.getAdapterPosition());
                    }
                }
            });

            if (position == mTargetPosition) {
                int color = mContext.getResources().getColor(R.color.colorBlue300);
                holder.tvItemOder.setTextColor(color);
                holder.tvItemText.setTextColor(color);
                holder.tvItemDescribe.setTextColor(color);
            } else {
                holder.tvItemOder.setTextColor(mContext.getResources().getColor(R.color.colorListTitle));
                holder.tvItemText.setTextColor(mContext.getResources().getColor(R.color.colorListTitle));
                holder.tvItemDescribe.setTextColor(mContext.getResources().getColor(R.color.colorListDescribe));
            }

        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            View itemView;
            TextView tvItemOder;
            TextView tvItemText;
            TextView tvItemDescribe;

            ViewHolder(View itemView) {
                super(itemView);
                this.itemView = itemView;
                tvItemOder = itemView.findViewById(R.id.tvItemOrder);
                tvItemText = itemView.findViewById(R.id.tvItemText);
                tvItemDescribe = itemView.findViewById(R.id.tvItemDescribe);
            }
        }
    }

    //***********ItemTouchHelper**********

    private class ItemTouchCallback extends ItemTouchHelper.Callback {

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = 0;
            int swipeFlags = 0;
            if (mSupportDrag) {
                dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            }
            if (mSupportSwipe) {
                swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            }
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            if (mSupportDrag) {
                int currentPosition = viewHolder.getAdapterPosition();
                int targetPosition = target.getAdapterPosition();
                Collections.swap(mItems, currentPosition, targetPosition);
                if (mOnDragListener != null) {
                    mOnDragListener.onDrag(currentPosition, targetPosition);
                }
                mListAdapter.notifyItemMoved(currentPosition, targetPosition);

                int numLength = String.valueOf(mItems.size()).length();
                StringBuilder currentPosStr = new StringBuilder();
                currentPosStr.append(currentPosition + 1);
                for (int i = currentPosStr.length(); i < numLength; i++) {
                    currentPosStr.insert(0, '0');
                }

                StringBuilder targetPosStr = new StringBuilder();
                targetPosStr.append(targetPosition + 1);
                for (int i = targetPosStr.length(); i < numLength; i++) {
                    targetPosStr.insert(0, '0');
                }

                ((ListAdapter.ViewHolder) viewHolder).tvItemOder.setText(targetPosStr.toString());
                ((ListAdapter.ViewHolder) target).tvItemOder.setText(currentPosStr.toString());
                return true;
            }
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            if (mSupportSwipe) {
                int position = viewHolder.getAdapterPosition();
                mItems.remove(position);
                if (mOnSwipeListener != null) {
                    mOnSwipeListener.onSwipe(BottomListDialog.this, position);
                }
                notifyDataSetChanged();
            }
        }
    }

    //**************Listener**************

    public interface OnDismissListener {
        void onDismiss();
    }

    public interface OnItemClickListener {
        void onItemClicked(BottomListDialog dialog, Item item, int position);
    }

    public interface OnAdditionButtonClickListener {
        boolean onClick();
    }

    public interface OnDragListener {
        void onDrag(int current, int target);
    }

    public interface OnSwipeListener {
        void onSwipe(BottomListDialog dialog, int position);
    }

    //*************public class************

    public static class Item {
        String mTitle;
        String mDescribe;

        public Item(String title, String describe) {
            mTitle = title;
            mDescribe = describe;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getDescribe() {
            return mDescribe;
        }
    }

    public static class Builder {
        private Context context;
        private String title;
        private int additionIconId;
        private List<Item> items;
        private boolean supportDrag;
        private boolean supportSwipe;

        private OnAdditionButtonClickListener additionButtonClickListener;
        private OnItemClickListener onItemClickListener;
        private OnDragListener onDragListener;
        private OnSwipeListener onSwipeListener;
        private OnDismissListener onDismissListener;

        public Builder(Context context) {
            this.context = context;
            initMembers();
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setAdditionIconId(int resId, OnAdditionButtonClickListener listener) {
            this.additionIconId = resId;
            this.additionButtonClickListener = listener;
            return this;
        }

        public Builder setItems(List<Item> items) {
            this.items = items;
            return this;
        }

        public Builder supportDrag(boolean supportDrag, OnDragListener listener) {
            this.supportDrag = supportDrag;
            this.onDragListener = listener;
            return this;
        }

        public Builder supportSwipe(boolean supportSwipe, OnSwipeListener listener) {
            this.supportSwipe = supportSwipe;
            this.onSwipeListener = listener;
            return this;
        }

        public Builder setOnItemClickListener(OnItemClickListener listener) {
            this.onItemClickListener = listener;
            return this;
        }

        public Builder setOnDismissListener(OnDismissListener listener) {
            this.onDismissListener = listener;
            return this;
        }

        public BottomListDialog create() {
            return new BottomListDialog(this);
        }

        //************private***********

        private void initMembers() {
            this.title = "";
            this.additionIconId = 0;
            this.items = new LinkedList<>();
            this.supportSwipe = false;
            this.supportSwipe = false;
            this.additionButtonClickListener = null;
            this.onItemClickListener = null;
        }
    }

    //*****************调试用***************

    private static void log(String msg) {
        if (MyApplication.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
