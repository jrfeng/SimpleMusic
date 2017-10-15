package jrfeng.simplemusic.activity.main.controller;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import jrfeng.simplemusic.R;

public class ControllerFragment extends Fragment implements ControllerContract.View {
    private ControllerContract.Presenter mPresenter;

    private View mContentView;
    private ImageView ivCtlImage;
    private TextView tvCtlSongName;
    private TextView tvCtlArtist;
    private SeekBar sbCtlProgress;
    private ImageButton ibCtlPrevious;
    private ImageButton ibCtlPlayPause;
    private ImageButton ibCtlNext;

    //**********************private********************

    private void findViews(View contentView) {
        ivCtlImage = contentView.findViewById(R.id.ivCtlImage);
        tvCtlSongName = contentView.findViewById(R.id.tvCtlSongName);
        tvCtlArtist = contentView.findViewById(R.id.tvCtlArtist);
        sbCtlProgress = contentView.findViewById(R.id.sbCtlProgress);
        ibCtlPrevious = contentView.findViewById(R.id.ibCtlPrevious);
        ibCtlPlayPause = contentView.findViewById(R.id.ibCtlPlayPause);
        ibCtlNext = contentView.findViewById(R.id.ibCtlNext);

        //必须调用该方法，不然跑马灯无效
        tvCtlSongName.setSelected(true);
    }

    private void addViewListener() {
        ibCtlPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.previous();
            }
        });

        ibCtlPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.playPause();
            }
        });

        ibCtlNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.next();
            }
        });

        sbCtlProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mPresenter.setSeekingState(true);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPresenter.seekTo(seekBar.getProgress());
                mPresenter.setSeekingState(false);
            }
        });

        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO 启动播放器 Activity
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_player_controller, container, false);
        findViews(mContentView);
        addViewListener();
        return mContentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.begin();
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.end();
    }

    @Override
    public void setPresenter(ControllerContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void viewPlay() {
        ibCtlPlayPause.setImageLevel(1);
    }

    @Override
    public void viewPause() {
        ibCtlPlayPause.setImageLevel(2);
    }

    @Override
    public void viewSeekTo(int progress) {
        sbCtlProgress.setProgress(progress);
    }

    @Override
    public void refreshViews(String songName, String artist, int songProgress, int songLength, byte[] image, boolean isPlaying) {
        if (songName != null) {
            tvCtlSongName.setText(songName);
        }
        if (artist != null) {
            tvCtlArtist.setText(artist);
        }

        sbCtlProgress.setMax(songLength);
        ObjectAnimator animator = ObjectAnimator.ofInt(sbCtlProgress, "progress", 0, songProgress);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(1000);
        animator.start();
        if (image != null) {
            Glide.with(getContext())
                    .load(image)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(ivCtlImage);
        } else {
            Glide.with(getContext()).load(R.mipmap.ic_launcher).into(ivCtlImage);
        }
        if (isPlaying) {
            ibCtlPlayPause.setImageLevel(2);
        } else {
            ibCtlPlayPause.setImageLevel(1);
        }
    }

    @Override
    public void notifyPlayError() {
        viewPause();
        Toast.makeText(getContext(), "抱歉，出错了", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void notifyMusicNotExist() {
        viewPause();
        Toast.makeText(getContext(), "文件不存在", Toast.LENGTH_SHORT).show();
    }
}
