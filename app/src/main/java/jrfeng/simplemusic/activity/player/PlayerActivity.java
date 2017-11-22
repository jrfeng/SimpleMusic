package jrfeng.simplemusic.activity.player;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import jrfeng.player.base.BaseActivity;
import jrfeng.player.player.MusicPlayerClient;
import jrfeng.simplemusic.GlideApp;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.dialog.PermissionRationaleDialog;
import jrfeng.simplemusic.widget.WaveFormView;

public class PlayerActivity extends BaseActivity implements PlayerContract.View {
    private PlayerPresenter mPresenter;

    private ImageButton ibBack;
    private TextView tvSongName;
    private TextView tvSongArtist;
    private ImageView ivSongImage;
    private WaveFormView waveForm;

    private TextView tvSongProgress;
    private TextView tvSongLength;
    private SeekBar sbProgress;

    private ObjectAnimator mRotateAnim;
    private boolean mSeeking;

    private ImageButton ibPlayMode;
    private ImageButton ibPrevious;
    private ImageButton ibPlayPause;
    private ImageButton ibNext;
    private ImageButton ibLoveMusic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        overridePendingTransition(R.anim.slide_in_up, R.anim.opacity_out);
        mPresenter = new PlayerPresenter(getApplicationContext(), this);
        findViews();
        initViews();
        addViewListener();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.opacity_in, R.anim.slide_out_down);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.begin();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.end();
    }

    @Override
    public void setPresenter(PlayerContract.Presenter presenter) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!(grantResults.length < 1
                || grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            initWaveForm();
        }
    }

    //******************private****************

    private void findViews() {
        ibBack = findViewById(R.id.ibBack);
        tvSongName = findViewById(R.id.tvSongName);
        tvSongArtist = findViewById(R.id.tvSongArtist);
        ivSongImage = findViewById(R.id.ivSongImage);
        waveForm = findViewById(R.id.waveForm);

        tvSongProgress = findViewById(R.id.tvSongProgress);
        tvSongLength = findViewById(R.id.tvSongLength);
        sbProgress = findViewById(R.id.sbProgress);

        ibPlayMode = findViewById(R.id.ibPlayMode);
        ibPrevious = findViewById(R.id.ibPrevious);
        ibPlayPause = findViewById(R.id.ibPlayPause);
        ibNext = findViewById(R.id.ibNext);
        ibLoveMusic = findViewById(R.id.ibLoveMusic);
    }

    private void initViews() {
        requestRecordAudioPermission();

        mRotateAnim = ObjectAnimator.ofFloat(ivSongImage, "rotation", 0, 360);
        mRotateAnim.setDuration(60_000);
        mRotateAnim.setInterpolator(new LinearInterpolator());
        mRotateAnim.setRepeatMode(ObjectAnimator.RESTART);
        mRotateAnim.setRepeatCount(-1);

        //使能跑马灯
        tvSongName.setSelected(true);
        //播放模式
        switch (mPresenter.getPlayMode()) {
            case MODE_ORDER:
                ibPlayMode.setImageLevel(1);
                ibPlayMode.setTag(1);
                break;
            case MODE_LOOP:
                ibPlayMode.setImageLevel(2);
                ibPlayMode.setTag(2);
                break;
            case MODE_RANDOM:
                ibPlayMode.setImageLevel(3);
                ibPlayMode.setTag(3);
                break;
        }
    }

    private void requestRecordAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                //没有则申请权限
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    PermissionRationaleDialog.show("需要 \"录音\" 权限，否则波形特效无法正常显示。",
                            this, Manifest.permission.RECORD_AUDIO, 1);
                } else {
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                }

            } else {
                initWaveForm();
            }
        } else {
            initWaveForm();
        }
    }

    private void initWaveForm() {
        waveForm.init(mPresenter.getAudioSessionId(), getLifecycle());
    }

    private void addViewListener() {
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvSongProgress.setText(createTimeString(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSeeking = false;//该语句必须位于最前面
                mPresenter.musicSeekTo(seekBar.getProgress());
            }
        });

        ibPlayMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mode = (int) ibPlayMode.getTag();
                switch (mode) {
                    case 1:
                        mPresenter.setPlayMode(MusicPlayerClient.PlayMode.MODE_LOOP);
                        ibPlayMode.setImageLevel(2);
                        ibPlayMode.setTag(2);
//                        Toast.makeText(getApplicationContext(), "循环播放", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        mPresenter.setPlayMode(MusicPlayerClient.PlayMode.MODE_RANDOM);
                        ibPlayMode.setImageLevel(3);
                        ibPlayMode.setTag(3);
//                        Toast.makeText(getApplicationContext(), "随机播放", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        mPresenter.setPlayMode(MusicPlayerClient.PlayMode.MODE_ORDER);
                        ibPlayMode.setImageLevel(1);
                        ibPlayMode.setTag(1);
//                        Toast.makeText(getApplicationContext(), "顺序播放", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        ibNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.playerNext();
            }
        });

        ibPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.playerPrevious();
            }
        });

        ibPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.playerPlayPause();
            }
        });

        ibLoveMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.loveOrNotLovePlayingMusic();
            }
        });
    }

    private String createTimeString(int msc) {
        int secondCount = msc / 1000;

        int seconds = secondCount % 60;
        int minutes = secondCount / 60;

        StringBuilder str = new StringBuilder();
        str.append(String.valueOf(seconds));
        if (str.length() < 2) {
            str.insert(0, '0');
        }
        str.insert(0, ":");
        str.insert(0, String.valueOf(minutes));
        if (str.length() < 5) {
            str.insert(0, '0');
        }
        return str.toString();
    }

    private void setKeepScreenOn(boolean keepScreenOn) {
        waveForm.setKeepScreenOn(keepScreenOn);
    }

    @Override
    public void setSongName(String name) {
        tvSongName.setText(name);
    }

    @Override
    public void setSongArtist(String artist) {
        if (artist.equals("未知")) {
            tvSongArtist.setText("未知歌手");
        } else {
            tvSongArtist.setText(artist);
        }
    }

    @Override
    public void setSongImage(byte[] bitmap) {
        if (bitmap != null && bitmap.length > 0) {
            GlideApp.with(this)
                    .load(bitmap)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.mipmap.ic_player_disk)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .transforms(new CircleCrop())
                    .into(ivSongImage);
        } else {
            GlideApp.with(this)
                    .load(R.mipmap.ic_player_disk)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.mipmap.ic_player_disk)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivSongImage);
        }
    }

    @Override
    public void setSongProgress(final int progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mSeeking) {
                    tvSongProgress.setText(createTimeString(progress));
                    sbProgress.setProgress(progress);
                }
            }
        });
    }

    @Override
    public void setSongProgressLength(int length) {
        tvSongLength.setText(createTimeString(length));
        sbProgress.setMax(length);
    }

    @Override
    public void viewStart() {
        setKeepScreenOn(true);
        ibPlayPause.setImageLevel(2);
        waveForm.setEnabled(true);

        if (!mRotateAnim.isRunning()) {
            mRotateAnim.start();
        } else if (mRotateAnim.isPaused()) {
            mRotateAnim.resume();
        }
    }

    @Override
    public void viewPause() {
        setKeepScreenOn(false);
        ibPlayPause.setImageLevel(1);

        mRotateAnim.pause();
    }

    @Override
    public void viewStop() {
        setKeepScreenOn(false);
        ibPlayPause.setImageLevel(1);

        mRotateAnim.end();
        ivSongImage.setRotation(0);
    }

    @Override
    public void love(boolean love) {
        if (love) {
            ibLoveMusic.setImageLevel(2);
        } else {
            ibLoveMusic.setImageLevel(1);
        }
    }
}
