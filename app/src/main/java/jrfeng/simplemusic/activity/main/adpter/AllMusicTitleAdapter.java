package jrfeng.simplemusic.activity.main.adpter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.StickyLayoutHelper;

import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.main.NavigationContract;

public class AllMusicTitleAdapter extends DelegateAdapter.Adapter<AllMusicTitleAdapter.ViewHolder> {
    private Context mContext;
    private NavigationContract.Presenter mPresenter;
    private TextView tvTitle;

    public AllMusicTitleAdapter(Context context, NavigationContract.Presenter presenter) {
        mContext = context;
        mPresenter = presenter;
    }


    @Override
    public LayoutHelper onCreateLayoutHelper() {
        return new StickyLayoutHelper(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.widget_all_music_title, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tvTitle.setText("所有音乐（" + mPresenter.getAllMusicListSize() + "首）");
        this.tvTitle = holder.tvTitle;

        //响应定位按钮
        holder.ibLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onTitleLocateButtonClicked();
            }
        });

        //响应排序按钮
        holder.ibSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onTitleSortButtonClicked();
            }
        });

        //响应菜单按钮
        holder.ibMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onTitleMenuButtonClicked();
            }
        });
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public void updateTitle(){
        tvTitle.setText("所有音乐（" + mPresenter.getAllMusicListSize() + "首）");
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageButton ibLocate;
        ImageButton ibSort;
        ImageButton ibMenu;

        ViewHolder(View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            ibLocate = itemView.findViewById(R.id.ibLocate);
            ibSort = itemView.findViewById(R.id.ibSort);
            ibMenu = itemView.findViewById(R.id.ibMenu);
        }
    }
}
