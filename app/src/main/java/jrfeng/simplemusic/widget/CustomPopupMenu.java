package jrfeng.simplemusic.widget;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.MenuRes;
import android.support.v7.view.menu.MenuBuilder;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import jrfeng.simplemusic.R;

public class CustomPopupMenu {
    private View mAnchorView;
    private Context mContext;
    private MenuBuilder mMenuBuilder;
    private OnItemClickListener mClickListener;

    private PopupWindow mMenuContainer;

    public CustomPopupMenu(View anchorView, @MenuRes int menuRes) {
        mAnchorView = anchorView;
        mContext = mAnchorView.getContext();
        mMenuBuilder = new MenuBuilder(mContext);
        MenuInflater menuInflater = new MenuInflater(mContext);
        menuInflater.inflate(menuRes, mMenuBuilder);
        initView();
    }

    public void show() {
        mMenuContainer.showAsDropDown(mAnchorView);
    }

    public void show(int gravity) {
        mMenuContainer.showAsDropDown(mAnchorView, 0, 0, gravity);
    }

    public CustomPopupMenu setOnItemClickedListener(OnItemClickListener listener) {
        mClickListener = listener;
        return this;
    }

    public interface OnItemClickListener {
        void onItemClicked(int itemId);
    }

    //*******************private******************

    private void initView() {
        View contentView = LayoutInflater.from(mContext)
                .inflate(R.layout.widget_drop_down_menu, null, false);
        int width = mContext.getResources().getDimensionPixelSize(R.dimen.customPopupMenuMaxWidth);
        int height = mContext.getResources()
                .getDimensionPixelSize(R.dimen.customPopupMenuItemHeight) * mMenuBuilder.size();
        mMenuContainer = new PopupWindow(contentView);
        mMenuContainer.setWidth(width);
        mMenuContainer.setHeight(height);
        mMenuContainer.setTouchable(true);
        mMenuContainer.setFocusable(true);
        mMenuContainer.setBackgroundDrawable(new BitmapDrawable());
        mMenuContainer.setOutsideTouchable(true);

        LinearLayout menuList = contentView.findViewById(R.id.menuList);
        for (int i = 0; i < mMenuBuilder.size(); i++) {
            View itemView = LayoutInflater.from(mContext)
                    .inflate(R.layout.widget_drop_down_menu_item,
                            menuList, false);
            ImageView ivMenuIcon = itemView.findViewById(R.id.ivMenuIcon);
            TextView tvMenuText = itemView.findViewById(R.id.tvMenuText);

            final MenuItem menuItem = mMenuBuilder.getItem(i);
            ivMenuIcon.setImageDrawable(menuItem.getIcon());
            tvMenuText.setText(menuItem.getTitle());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mMenuContainer.dismiss();
                    if (mClickListener != null) {
                        mClickListener.onItemClicked(menuItem.getItemId());
                    }
                }
            });
            menuList.addView(itemView);
        }
    }
}
