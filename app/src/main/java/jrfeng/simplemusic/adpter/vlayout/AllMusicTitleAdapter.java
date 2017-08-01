package jrfeng.simplemusic.adpter.vlayout;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.StickyLayoutHelper;

import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.navigation.NavigationContract;

public class AllMusicTitleAdapter extends DelegateAdapter.Adapter<AllMusicTitleAdapter.ViewHolder> {
    private Context mContext;
    private NavigationContract.Presenter mPresenter;

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
        View view = LayoutInflater.from(mContext).inflate(R.layout.widget_all_music_title, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //TODO 响应排序按钮
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageButton ibClear;

        ViewHolder(View itemView) {
            super(itemView);

            ibClear = itemView.findViewById(R.id.ibSort);
        }
    }
}
