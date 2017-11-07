package jrfeng.simplemusic.activity.main.listnav;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.BitmapTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.List;
import java.util.WeakHashMap;

import jrfeng.player.data.Music;
import jrfeng.player.mode.MusicStorage;
import jrfeng.player.player.MusicPlayerClient;
import jrfeng.player.utils.mp3.MP3Util;
import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.main.list.MusicListFragment;
import jrfeng.simplemusic.receiver.PlayerActionDisposerAdapter;
import jrfeng.simplemusic.receiver.PlayerActionReceiver;

public class MusicListNavPresenter extends PlayerActionDisposerAdapter implements MusicListNavContract.Presenter {
    private static final String TAG = "MusicListNavPresenter";

    private Context mContext;
    private MusicListNavContract.View mView;

    private MusicStorage.GroupType mGroupType;
    private MusicPlayerClient mClient;
    private MusicStorage mMusicStorage;

    private PlayerActionReceiver mPlayerActionReceiver;

    private MusicStorage.OnMusicGroupChangListener mMusicGroupChangListener;

    private static final int CACHE_SIZE = 4 * 1024 * 1024;  //4M
    private LruCache<String, Bitmap> mIconCache;

    public MusicListNavPresenter(Context context,
                                 MusicListNavContract.View view,
                                 MusicStorage.GroupType groupType) {
        mContext = context;
        mView = view;
        mGroupType = groupType;
        mClient = MusicPlayerClient.getInstance();
        mMusicStorage = mClient.getMusicStorage();
        mPlayerActionReceiver = new PlayerActionReceiver(mContext, this);
        mMusicGroupChangListener = new MusicStorage.OnMusicGroupChangListener() {
            @Override
            public void onMusicGroupChanged(MusicStorage.GroupType groupType, String groupName, MusicStorage.GroupAction action) {
                if (groupType != mGroupType) {
                    return;
                }

                mView.refreshActionBarTitle();
                mView.refreshGroupList();
            }
        };

        mIconCache = new LruCache<String, Bitmap>(CACHE_SIZE) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getAllocationByteCount();
            }
        };
    }

    //********************调试用********************

    private static void log(String msg) {
        if (MyApplication.DEBUG) {
            Log.d(TAG, msg);
        }
    }

    //*******************Presenter******************

    @Override
    public void begin() {
        refreshViews();
        mPlayerActionReceiver.register();
        mMusicStorage.addMusicGroupChangeListener(mMusicGroupChangListener);
    }

    @Override
    public void end() {
        mPlayerActionReceiver.unregister();
        mMusicStorage.removeMusicGroupChangeListener(mMusicGroupChangListener);
    }

    @Override
    public void setPlayMode(MusicPlayerClient.PlayMode playMode) {
        mClient.setPlayMode(playMode);
    }

    @Override
    public MusicPlayerClient.PlayMode getPlayMode() {
        return mClient.getPlayMode();
    }

    @Override
    public void createNewMusicList(String listName) {
        mMusicStorage.createNewMusicList(listName);
    }

    @Override
    public int getMusicListCount() {
        return mMusicStorage.getMusicListSize();
    }

    @Override
    public int getAlbumCount() {
        return mMusicStorage.getAlbumSize();
    }

    @Override
    public int getArtistCount() {
        return mMusicStorage.getArtistCount();
    }

    @Override
    public List<String> getGroupNames() {
        switch (mGroupType) {
            case MUSIC_LIST:
                return mMusicStorage.getMusicListNames();
            case ALBUM_LIST:
                return mMusicStorage.getAlbumNames();
            case ARTIST_LIST:
                return mMusicStorage.getArtistNames();
            default:
                return mMusicStorage.getMusicListNames();
        }
    }

    @Override
    public int getGroupSize(MusicStorage.GroupType groupType, String groupName) {
        return mMusicStorage.getMusicGroup(groupType, groupName).size();
    }

    @Override
    public void setGroupIcon(final String groupName, final ImageView iconView) {
        //从内存加载
        Bitmap cacheIcon = mIconCache.get(groupName);
        if (cacheIcon != null) {
            iconView.setImageBitmap(cacheIcon);
            return;
        }

        //从本地加载
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... voids) {
                List<Music> musicGroup = mMusicStorage.getMusicGroup(mGroupType, groupName);
                for (Music music : musicGroup) {
                    byte[] image = MP3Util.getMp3Image(new File(music.getPath()));
                    if (image != null && image.length > 0) {
                        return createBitmap(image);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    mIconCache.put(groupName, bitmap);
                    iconView.setImageBitmap(bitmap);
                } else {
                    Glide.with(mContext)
                            .load(R.mipmap.ic_launcher_round)
                            .into(iconView);
                }
            }
        }.execute();
    }

    @Override
    public void openMusicList(MusicStorage.GroupType groupType, String groupName) {
        Fragment fragment = new MusicListFragment();
        Bundle args = new Bundle();
        args.putString(MusicListFragment.KEY_GROUP_TYPE, groupType.name());
        args.putString(MusicListFragment.KEY_GROUP_NAME, groupName);
        fragment.setArguments(args);
        mView.startFragment(fragment);
    }

    @Override
    public void deleteMusicList(String listName) {
        mMusicStorage.deleteMusicList(listName);
        Toast.makeText(mContext, "删除成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlay() {
        mView.refreshGroupList();
    }

    //*******************private********************

    private void refreshViews() {
        mView.refreshActionBarTitle();
        mView.refreshPlayMode();
        mView.refreshGroupList();
    }

    private Bitmap createBitmap(byte[] data) {
        int size = mContext.getResources().getDimensionPixelSize(R.dimen.ListNavItemIconSize);

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
