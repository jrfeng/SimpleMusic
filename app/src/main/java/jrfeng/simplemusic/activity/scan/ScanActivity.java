package jrfeng.simplemusic.activity.scan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import jrfeng.musicplayer.data.Music;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.scan.scannedmusics.ScannedMusicsActivity;
import jrfeng.simplemusic.base.BaseActivity;

public class ScanActivity extends BaseActivity implements ScanContract.View {
    private ScanContract.Presenter mPresenter;
    private Toolbar toolbar;
    private TextView tvMessage;
    private TextView tvScanningHint;
    private TextView tvScannedPathHint;
    private TextView tvScanCount;
    private TextView tvScanCountHint1;
    private TextView tvScanCountHint2;
    private Button btnScanSwitch;
    private TextView tvCustomScan;

    private boolean mScanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        setPresenter(new ScanPresenter(this));
        findViews();
        initViews();
        addViewListener();
    }

    @Override
    public void setPresenter(ScanContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mScanning) {
            mPresenter.stopScan();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void refreshView(int percent, String path, int count) {
        setScanningPercent(percent);
        setScanningHint(path);
        setScanMusicCount(count);
    }

    @Override
    public void resetView() {
        btnScanSwitch.setText("开始扫描");
        tvMessage.setText("扫描本地音乐");
        tvScanningHint.setVisibility(View.INVISIBLE);
        tvScannedPathHint.setVisibility(View.INVISIBLE);
        tvScanCount.setVisibility(View.INVISIBLE);
        tvScanCountHint1.setVisibility(View.INVISIBLE);
        tvScanCountHint2.setVisibility(View.INVISIBLE);
        mScanning = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            finish();
        }
    }

    @Override
    public void showScannedMusic(List<Music> scannedMusics) {
        //TODO 显示 添加 页面
        //调试
        Log.d("App", "显示 添加 页面");
        Intent intent = new Intent(this, ScannedMusicsActivity.class);
        ArrayList<Music> musicList = new ArrayList<>(scannedMusics.size());
        musicList.addAll(scannedMusics);
        intent.putExtra("musics", musicList);
        startActivityForResult(intent, 1);
    }

    private void findViews() {
        toolbar = (Toolbar) findViewById(R.id.tbScan);
        tvMessage = (TextView) findViewById(R.id.tvMessage);
        tvScanningHint = (TextView) findViewById(R.id.tvScanningHint);
        tvScannedPathHint = (TextView) findViewById(R.id.tvScannedPathHint);
        tvScanCount = (TextView) findViewById(R.id.tvScanCount);
        tvScanCountHint1 = (TextView) findViewById(R.id.tvScanCountHint1);
        tvScanCountHint2 = (TextView) findViewById(R.id.tvScanCountHint2);
        btnScanSwitch = (Button) findViewById(R.id.btnScanSwitch);
        tvCustomScan = (TextView) findViewById(R.id.tvCustomScan);
    }

    private void initViews() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("扫描");
        }
    }

    private void addViewListener() {
        btnScanSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mScanning) {
                    mPresenter.stopScan();
                    resetView();
                } else {
                    btnScanSwitch.setText("取消扫描");
                    tvScanningHint.setVisibility(View.VISIBLE);
                    tvScannedPathHint.setVisibility(View.VISIBLE);
                    tvScanCount.setVisibility(View.VISIBLE);
                    tvScanCountHint1.setVisibility(View.VISIBLE);
                    tvScanCountHint2.setVisibility(View.VISIBLE);
                    mScanning = true;
                    mPresenter.startScan();
                }
            }
        });

        tvCustomScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mScanning) {
                    Toast.makeText(getApplicationContext(), "正在扫描...", Toast.LENGTH_SHORT).show();
                } else {
                    //TODO 自定义扫描
                    Toast.makeText(getApplicationContext(), "暂不支持", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setScanningPercent(int percent) {
        tvMessage.setText("扫描中  " + percent + "%");
    }

    @SuppressLint("SetTextI18n")
    private void setScanningHint(String hint) {
        tvScannedPathHint.setText(hint.substring(Math.max(0, hint.length() - 24), hint.length()));
    }

    private void setScanMusicCount(int count) {
        tvScanCount.setText(String.valueOf(count));
    }
}
