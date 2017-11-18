package jrfeng.simplemusic.activity.scan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jrfeng.player.data.Music;
import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.scan_result.ScannedMusicsActivity;
import jrfeng.player.base.BaseActivity;

public class ScanActivity extends BaseActivity implements ScanContract.View {
    private static final String TAG = "ScanActivity";
    private static final int REQUEST_CODE_SHOW_SCANNED = 1;
    private static final int REQUEST_CODE_CUSTOM_SCAN = 2;

    private ScanContract.Presenter mPresenter;
    private Toolbar toolbar;
    private TextView tvMessage;
    private TextView tvScanningHint;
    private TextView tvScannedPathHint;
    private TextView tvScanCount;
    private TextView tvScanCountHint1;
    private TextView tvScanCountHint2;
    private TextView tvScanSwitch;
//    private TextView tvCustomScan;//“自定义扫描功能” 的控件（目前暂不提供该功能）

    private boolean mScanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        overridePendingTransition(R.anim.slide_in_up, R.anim.alpha_out);
        requestExternalStoragePermission();

        setPresenter(new ScanPresenter(this));
        findViews();
        initViews();
        addViewListener();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.alpha_in, R.anim.slide_out_down);
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
        tvScanSwitch.setText("开始扫描");
        tvMessage.setText("扫描本地音乐");
        tvScanningHint.setVisibility(View.INVISIBLE);
        tvScannedPathHint.setVisibility(View.INVISIBLE);
        tvScanCount.setVisibility(View.INVISIBLE);
        tvScanCountHint1.setVisibility(View.INVISIBLE);
        tvScanCountHint2.setVisibility(View.INVISIBLE);
//        tvCustomScan.setVisibility(View.VISIBLE);
        mScanning = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SHOW_SCANNED:
                    finish();
                    break;
                case REQUEST_CODE_CUSTOM_SCAN:
                    //开始自定义扫描（注：暂不支持）
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length < 1
                || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "需要存储器访问权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showScannedMusic(List<Music> scannedMusics) {
        //调试
        log("显示 添加 页面");
        Intent intent = new Intent(this, ScannedMusicsActivity.class);
        ArrayList<Music> musicList = new ArrayList<>(scannedMusics.size());
        musicList.addAll(scannedMusics);
        intent.putExtra("musics", musicList);
        startActivityForResult(intent, REQUEST_CODE_SHOW_SCANNED);
    }

    //******************private*****************

    private void requestExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                //没有则申请权限
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    private void findViews() {
        toolbar = (Toolbar) findViewById(R.id.tbScan);
        tvMessage = (TextView) findViewById(R.id.tvMessage);
        tvScanningHint = (TextView) findViewById(R.id.tvScanningHint);
        tvScannedPathHint = (TextView) findViewById(R.id.tvScannedPathHint);
        tvScanCount = (TextView) findViewById(R.id.tvScanCount);
        tvScanCountHint1 = (TextView) findViewById(R.id.tvScanCountHint1);
        tvScanCountHint2 = (TextView) findViewById(R.id.tvScanCountHint2);
        tvScanSwitch = (TextView) findViewById(R.id.tvScanSwitch);
//        tvCustomScan = (TextView) findViewById(R.id.tvCustomScan);
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
        tvScanSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mScanning) {
                    mPresenter.stopScan();
                    resetView();
                } else {
                    tvScanSwitch.setText("取消扫描");
                    tvScanningHint.setVisibility(View.VISIBLE);
                    tvScannedPathHint.setVisibility(View.VISIBLE);
                    tvScanCount.setVisibility(View.VISIBLE);
                    tvScanCountHint1.setVisibility(View.VISIBLE);
                    tvScanCountHint2.setVisibility(View.VISIBLE);
//                    tvCustomScan.setVisibility(View.INVISIBLE);
                    mScanning = true;
                    mPresenter.startScan();
                }
            }
        });

//        tvCustomScan.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //自定义扫描（注：暂不支持）
//                Toast.makeText(getApplicationContext(), "暂不支持", Toast.LENGTH_SHORT).showAsDropDown();
//            }
//        });
    }

    private void setScanningPercent(int percent) {
        tvMessage.setText("扫描目录  " + percent + "%");
    }

    @SuppressLint("SetTextI18n")
    private void setScanningHint(String hint) {
        tvScannedPathHint.setText(hint.substring(Math.max(0, hint.length() - 24), hint.length()));
    }

    private void setScanMusicCount(int count) {
        tvScanCount.setText(String.valueOf(count));
    }

    private void log(String msg) {
        if (MyApplication.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
