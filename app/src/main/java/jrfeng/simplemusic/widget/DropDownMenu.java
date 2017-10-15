package jrfeng.simplemusic.widget;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.List;

import jrfeng.simplemusic.R;

public class DropDownMenu {
    private View mAnchorView;
    private Context mContext;
    private List<Item> mMenuItems;
    private OnItemClickListener mClickListener;

    private PopupWindow mMenuContainer;

    public DropDownMenu(View anchorView, List<Item> menuItems) {
        mAnchorView = anchorView;
        mContext = mAnchorView.getContext();
        mMenuItems = menuItems;
        initView();
    }

    public void show() {
        mMenuContainer.showAsDropDown(mAnchorView);
    }

    public DropDownMenu setOnItemClickedListener(OnItemClickListener listener) {
        mClickListener = listener;
        return this;
    }

    public static class Item {
        private int mIconId;
        private String mText;

        public Item(int iconId, String text) {
            mIconId = iconId;
            mText = text;
        }

        public int getIconId() {
            return mIconId;
        }

        public String getItemText() {
            return mText;
        }
    }

    public interface OnItemClickListener {
        void onItemClicked(int position);
    }

    //*******************private******************

    private void initView() {
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.widget_drop_down_menu, null, false);
        int width = mContext.getResources().getDimensionPixelSize(R.dimen.menuWidth);
        int height = mContext.getResources().getDimensionPixelSize(R.dimen.menuItemHeight) * mMenuItems.size() + 2;
        mMenuContainer = new PopupWindow(contentView);
        mMenuContainer.setWidth(width);
        mMenuContainer.setHeight(height);
        mMenuContainer.setTouchable(true);
        mMenuContainer.setFocusable(true);
        mMenuContainer.setBackgroundDrawable(new BitmapDrawable());
        mMenuContainer.setOutsideTouchable(true);

        LinearLayout menuList = contentView.findViewById(R.id.menuList);
        for (int i = 0; i < mMenuItems.size(); i++) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.widget_drop_down_menu_item,
                    menuList, false);
            ImageView ivMenuIcon = itemView.findViewById(R.id.ivMenuIcon);
            TextView tvMenuText = itemView.findViewById(R.id.tvMenuText);
            ivMenuIcon.setImageResource(mMenuItems.get(i).getIconId());
            tvMenuText.setText(mMenuItems.get(i).getItemText());
            final int position = i;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mMenuContainer.dismiss();
                    if (mClickListener != null) {
                        mClickListener.onItemClicked(position);
                    }
                }
            });
            menuList.addView(itemView);
        }
    }
}
