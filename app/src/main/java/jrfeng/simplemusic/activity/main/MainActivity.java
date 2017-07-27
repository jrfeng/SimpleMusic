package jrfeng.simplemusic.activity.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.base.BaseActivity;
import jrfeng.simplemusic.data.Music;
import jrfeng.simplemusic.model.MusicDBHelper;
import jrfeng.simplemusic.service.player.PlayerService;
import jrfeng.simplemusic.utils.scanner.MusicScanner;

public class MainActivity extends BaseActivity {
    private TextView tvMessage;
    private ImageView ivMusicImage;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    String msg = (String) message.obj;
                    if (msg != null && msg.length() > 0) {
                        tvMessage.setText(msg);
                        Log.d("App", (String) message.obj);
                    }
                    break;
                case 2:
                    Log.d("App", "Handler change Image");
                    ivMusicImage.setImageBitmap((Bitmap) message.obj);
                    break;
            }
            return true;
        }
    });

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvMessage = (TextView) findViewById(R.id.tvMessage);
        ivMusicImage = (ImageView) findViewById(R.id.ivMusicImage);
        receiver = new PlayActionReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayerService.ACTION_PLAY);
        intentFilter.addAction(PlayerService.ACTION_PAUSE);
        intentFilter.addAction(PlayerService.ACTION_NEXT);
        intentFilter.addAction(PlayerService.ACTION_PREVIOUS);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    public void onClick(View view) {
        new Thread() {
            @Override
            public void run() {
                MyApplication myApplication = MyApplication.getInstance();
                MusicScanner musicScanner = new MusicScanner(myApplication.getMusicStorage(), myApplication.getMusicDB());
                musicScanner.scan(
                        Environment.getExternalStorageDirectory(),
                        new MusicScanner.OnScanListener() {
                            @Override
                            public void onStart() {
                                Message message = handler.obtainMessage();
                                message.what = 1;
                                message.obj = "扫描中...";
                                handler.sendMessage(message);
                            }

                            @Override
                            public void onFinished(int count) {
                                Message message = handler.obtainMessage();
                                message.what = 1;
                                message.obj = "扫描完成, 共添加 " + count + " 首新歌";
                                handler.sendMessage(message);
                                MyApplication.getInstance().getPlayerClient().reload();
                            }
                        });
            }
        }.start();
    }

    private class PlayActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PlayerService.ACTION_PLAY)) {
                Music music = (Music) intent.getSerializableExtra(PlayerService.KEY_PLAYING_MUSIC);
                SQLiteDatabase database = MyApplication.getInstance().getMusicDB();

                Cursor cursor = database.query(MusicDBHelper.TABLE_MUSIC_LIST,
                        new String[]{MusicDBHelper.COLUMN_PATH, MusicDBHelper.COLUMN_IMAGE},
                        "songPath = '" + music.getPath() + "'",
                        null,
                        null,
                        null,
                        null);

                byte[] image = null;
                if (cursor.moveToFirst()) {
                    image = cursor.getBlob(cursor.getColumnIndex(MusicDBHelper.COLUMN_IMAGE));
                }
                cursor.close();

                if (image != null) {
                    Log.d("App", "Bitmap Not Null, Length : " + image.length);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                    Message message = handler.obtainMessage();
                    message.what = 2;
                    message.obj = bitmap;
                    handler.sendMessage(message);
                }else{
                    Log.e("App", "Bitmap is Null");
                }
            }
        }
    }
}
