package jrfeng.simplemusic.widget;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.utils.statusbar.QMUIStatusBarHelper;

public class TopMenuDialog {
    private static final String TAG = "TopMenuDialog";

    private Context mContext;
    private AppCompatDialog mDialog;
    private int mMenuResId;
    private Menu mMenu;
    private OnItemClickListener mItemClickListener;
    private DialogInterface.OnDismissListener mOnDismissListener;

    private ImageButton ibCancel;
    private RecyclerView rvMenu;

    private Activity mActivity;

    public TopMenuDialog(Context context, int menuResId, OnItemClickListener listener) {
        mContext = context;
        mMenuResId = menuResId;
        mItemClickListener = listener;

        initDialog();
        findViews();
        initMenu();
        addViewListener();
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        mOnDismissListener = listener;
    }

    public void show() {
        //调试
        log("显示菜单");

        mDialog.show();
    }

    public void show(@NonNull Activity activity) {
        mActivity = activity;
        QMUIStatusBarHelper.setStatusBarLightMode(activity);
        mDialog.show();
    }

    //*******************Adapter******************

    private class TopMenuAdapter extends RecyclerView.Adapter<TopMenuAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext)
                    .inflate(R.layout.widget_top_menu_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final int index = position;
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemClickListener != null) {
                        mDialog.dismiss();
                        mItemClickListener.onItemClick(mMenu.getItem(index));
                    }
                }
            });

            holder.tvItem.setText(mMenu.getItem(position).getTitle().toString());
        }

        @Override
        public int getItemCount() {
            return mMenu.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            View itemView;
            TextView tvItem;

            ViewHolder(View itemView) {
                super(itemView);
                this.itemView = itemView;
                tvItem = itemView.findViewById(R.id.tvItem);
            }
        }
    }

    //******************Listener******************

    public interface OnItemClickListener {
        void onItemClick(MenuItem item);
    }

    //********************private*****************

    private void initDialog() {
        mDialog = new AppCompatDialog(mContext);
        mDialog.setContentView(R.layout.widget_top_menu);

        Window window = mDialog.getWindow();
        if (window != null) {
            window.setGravity(Gravity.TOP);
            window.setWindowAnimations(R.style.TopMenuAnim);
            View decorView = window.getDecorView();
            decorView.setBackgroundColor(Color.TRANSPARENT);
            decorView.setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
    }

    private void findViews() {
        ibCancel = mDialog.findViewById(R.id.ibCancel);
        rvMenu = mDialog.findViewById(R.id.rvMenu);
        rvMenu.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
    }

    private void initMenu() {
        mMenu = new MenuBuilder(mContext);
        new MenuInflater(mContext).inflate(mMenuResId, mMenu);
        rvMenu.setAdapter(new TopMenuAdapter());
    }

    private void addViewListener() {
        ibCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });

        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (mActivity != null) {
                    QMUIStatusBarHelper.setStatusBarDarkMode(mActivity);
                }
                if (mOnDismissListener != null) {
                    mOnDismissListener.onDismiss(dialogInterface);
                }
            }
        });
    }

    //**************调试用***************

    private static void log(String msg) {
        if (MyApplication.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
