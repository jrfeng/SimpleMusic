package jrfeng.simplemusic.adapter.vlayout;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.ScrollFixLayoutHelper;

import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.main.nav.NavigationContract;

public class BackTopAdapter extends DelegateAdapter.Adapter<BackTopAdapter.ViewHolder> {
    private Context mContext;
    private RecyclerView.LayoutManager mLayoutManager;

    public BackTopAdapter(Context context, RecyclerView.LayoutManager layoutManager) {
        mContext = context;
        mLayoutManager = layoutManager;
    }

    //******************Adapter****************

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        return new ScrollFixLayoutHelper(ScrollFixLayoutHelper.BOTTOM_RIGHT,
                mContext.getResources().getDimensionPixelSize(R.dimen.BackToTopXOffset),
                mContext.getResources().getDimensionPixelSize(R.dimen.BackToTopYOffset));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.widget_back_top, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.ibBackTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayoutManager.scrollToPosition(0);
//                Toast.makeText(mContext, "已返回顶部", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View ibBackTop;

        ViewHolder(View itemView) {
            super(itemView);
            ibBackTop = itemView.findViewById(R.id.ibBackTop);
        }
    }
}
