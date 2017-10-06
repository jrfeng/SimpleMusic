package jrfeng.simplemusic.activity.scan.scannedmusics;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jrfeng.musicplayer.data.Music;
import jrfeng.musicplayer.player.MusicPlayerClient;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.scan.scannedmusics.Adapter.ScannedMusicsAdapter;
import jrfeng.simplemusic.base.BaseActivity;
import jrfeng.musicplayer.mode.MusicStorageImp;

public class ScannedMusicsActivity extends BaseActivity {
    private MusicStorageImp mMusicStorageImp;
    private List<Music> mNewMusic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_musics);
        overridePendingTransition(R.anim.enter_from_bottom, R.anim.exit_no_anim);
        MusicPlayerClient client = MusicPlayerClient.getInstance();
        mMusicStorageImp = (MusicStorageImp) client.getMusicStorage();
        init();
    }

    private void init() {
        //获取值
        ArrayList<Music> musics = (ArrayList<Music>) getIntent().getSerializableExtra("musics");

        //初始化 Toolbar
        Toolbar tbScannedMusics = (Toolbar) findViewById(R.id.tbScanned);
        setSupportActionBar(tbScannedMusics);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        //初始化 RecyclerView
        RecyclerView rvScannedMusics = (RecyclerView) findViewById(R.id.rvScannedMusics);
        RecyclerView.LayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvScannedMusics.setLayoutManager(lm);

        TextView tvScannedMusicCount = (TextView) findViewById(R.id.tvScannedMusicCount);
        TextView tvNewMusicCount = (TextView) findViewById(R.id.tvNewMusicCount);

        initNewMusic(musics);

        tvScannedMusicCount.setText("扫描到" + musics.size() + "首歌曲");
        tvNewMusicCount.setText(mNewMusic.size() + "首新歌曲");

        final ScannedMusicsAdapter adapter = new ScannedMusicsAdapter(this, mNewMusic, findViewById(R.id.scannedMusicsTitle));
        rvScannedMusics.setAdapter(adapter);

        Button btnCommitAdd = (Button) findViewById(R.id.btnCommitAdd);
        btnCommitAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean[] choice = adapter.getItemsChoiceState();
                mMusicStorageImp.addMusics(choice, mNewMusic);
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void initNewMusic(List<Music> scannedMusic) {
        List<Music> allMusic = mMusicStorageImp.getAllMusic();

        mNewMusic = new LinkedList<>();
        for (Music music : scannedMusic) {
            if (!allMusic.contains(music)) {
                mNewMusic.add(music);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
