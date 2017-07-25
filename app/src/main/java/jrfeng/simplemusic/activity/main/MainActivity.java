package jrfeng.simplemusic.activity.main;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.base.BaseActivity;
import jrfeng.simplemusic.utils.MusicScanner;

public class MainActivity extends BaseActivity {
    private TextView tvMessage;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            String msg = (String)message.obj;
            if(msg != null && msg.length() > 0) {
                tvMessage.setText(msg);
                Log.d("App", (String) message.obj);
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvMessage = (TextView) findViewById(R.id.tvMessage);
    }

    public void onClick(View view) {
        new Thread() {
            @Override
            public void run() {
                MyApplication.getInstance().getMusicScanner().scan(
                        Environment.getExternalStorageDirectory(),
                        new MusicScanner.OnScanListener() {
                            @Override
                            public void onStart() {
                                Message message = handler.obtainMessage();
                                message.obj = "扫描中...";
                                handler.sendMessage(message);
                            }

                            @Override
                            public void onFinished(int count) {
                                Message message = handler.obtainMessage();
                                message.obj = "扫描完成, 共添加 " + count + " 首新歌";
                                handler.sendMessage(message);
                                MyApplication.getInstance().getPlayerClient().reload();
                            }
                        });
            }
        }.start();
    }
}
