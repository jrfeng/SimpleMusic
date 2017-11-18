package jrfeng.simplemusic.activity.player;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.ColorFilterTransformation;
import jrfeng.player.base.BaseActivity;
import jrfeng.simplemusic.GlideApp;
import jrfeng.simplemusic.R;

public class PlayerActivity extends BaseActivity implements PlayerContract.View {
    private Context mContext;
    private ImageView ivBackground;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        mContext = getApplicationContext();
        findViews();
        initViews();
        addViewListener();
    }

    @Override
    public void setPresenter(PlayerContract.Presenter presenter) {

    }

    //******************private****************

    private void findViews() {
        ivBackground = (ImageView) findViewById(R.id.ivBackground);
    }

    private void initViews() {

    }

    private void addViewListener() {

    }
}
