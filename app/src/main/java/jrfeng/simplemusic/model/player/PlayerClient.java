package jrfeng.simplemusic.model.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class PlayerClient implements ServiceConnection, PlayerController {
    private PlayerService.Controller mController;
    private Context mContext;
    private boolean isConnect;

    public PlayerClient(Context context) {
        mContext = context;
    }

    public void connect() {
        isConnect = true;
        mContext.bindService(new Intent(mContext, PlayerService.class), this, Context.BIND_AUTO_CREATE);
    }

    public void disconnect() {
        isConnect = false;
        mContext.unbindService(this);
    }

    public boolean isConnect() {
        return isConnect;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        isConnect = true;
        mController = (PlayerService.Controller) iBinder;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    }

    @Override
    public void previous() {
        mController.previous();
    }

    @Override
    public void next() {
        mController.next();
    }

    @Override
    public void play() {
        mController.play();
    }

    @Override
    public void pause() {
        mController.pause();
    }

    @Override
    public void play_pause() {
        mController.play_pause();
    }

    @Override
    public void stop() {
        mController.stop();
    }

    @Override
    public void seekTo(float percent) {
        mController.seekTo(percent);
    }

    @Override
    public void reload() {
        mController.reload();
    }

    @Override
    public void shutdown() {
        disconnect();
        mController.shutdown();
    }
}