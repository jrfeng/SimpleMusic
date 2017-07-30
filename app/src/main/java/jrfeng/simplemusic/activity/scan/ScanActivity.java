package jrfeng.simplemusic.activity.scan;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.model.MusicStorage;
import jrfeng.simplemusic.utils.scanner.MusicScanner;

public class ScanActivity extends AppCompatActivity {
    private Button btnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        btnScan = (Button) findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicStorage musicStorage = MyApplication.getInstance().getMusicStorage();
                SQLiteDatabase musicDB = MyApplication.getInstance().getMusicDB();
                final MusicScanner musicScanner = new MusicScanner(musicStorage, musicDB);
                new Thread(){
                    @Override
                    public void run() {
                        musicScanner.scan(Environment.getExternalStorageDirectory(), new MusicScanner.OnScanListener() {
                            @Override
                            public void onStart() {
                                Log.d("App", "开始扫描");
                            }

                            @Override
                            public void onFinished(int count) {
                                Log.d("App", "扫描完成");
                                MyApplication.getInstance().getPlayerClient().reload();
                            }
                        });
                    }
                }.start();
            }
        });
    }
}
