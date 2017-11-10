package jrfeng.simplemusic.activity.search.adpter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import jrfeng.player.data.Music;
import jrfeng.player.utils.mp3.MP3Util;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.search.SearchContract;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
    private Context mContext;
    private SearchContract.Presenter mPresenter;
    private List<Music> mSearchResult;

    private LruCache<String, Bitmap> mIconCache;

    public SearchResultAdapter(Context context, SearchContract.Presenter presenter) {
        mContext = context;
        mPresenter = presenter;
        mSearchResult = new LinkedList<>();

        mIconCache = new LruCache<String, Bitmap>(4 * 1024 * 1024) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getAllocationByteCount();
            }
        };
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.widget_search_result_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        initViews(holder, position);
        addViewListener(holder, position);
    }

    @Override
    public int getItemCount() {
        return mSearchResult.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        CircleImageView ivSongIcon;
        TextView tvSongName;
        TextView tvSongArtist;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            ivSongIcon = itemView.findViewById(R.id.ivSongIcon);
            tvSongName = itemView.findViewById(R.id.tvSongName);
            tvSongArtist = itemView.findViewById(R.id.tvSongArtist);
        }
    }

    public void updateSearchResult(List<Music> result) {
        mSearchResult.clear();
        mSearchResult.addAll(result);
        notifyDataSetChanged();
    }

    private void initViews(ViewHolder holder, int position) {
        Music music = mSearchResult.get(position);
        setItemIcon(music, holder.ivSongIcon);
        holder.tvSongName.setText(music.getName());
        holder.tvSongArtist.setText(music.getArtist());
    }

    private void addViewListener(ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.play(mSearchResult.get(position));
            }
        });
    }

    private void setItemIcon(final Music music, final ImageView iconView) {
        //从内存加载
        Bitmap cacheIcon = mIconCache.get(music.getPath());
        if (cacheIcon != null) {
            iconView.setImageBitmap(cacheIcon);
            return;
        }

        //从本地加载
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... voids) {
                byte[] image = MP3Util.getMp3Image(new File(music.getPath()));
                if (image != null && image.length > 0) {
                    return createBitmap(image);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    mIconCache.put(music.getPath(), bitmap);
                    iconView.setImageBitmap(bitmap);
                } else {
                    Glide.with(mContext)
                            .load(R.mipmap.ic_launcher_round)
                            .into(iconView);
                }
            }
        }.execute();
    }

    private Bitmap createBitmap(byte[] data) {
        int size = mContext.getResources().getDimensionPixelSize(R.dimen.SearchResultItemIconSize);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        int rawWidth = options.outWidth;
        int rawHeight = options.outHeight;

        int sample = Math.max(rawWidth, rawHeight) / size;

        if (sample % 2 != 0) {
            sample = sample - 1;
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = sample;

        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }
}
