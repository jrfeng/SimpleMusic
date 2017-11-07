package jrfeng.player.player;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import jrfeng.player.R;

class DefaultNotifyControllerView implements MusicPlayerClient.NotifyControllerView {
    private RemoteViews mNotifyView;
    private Context mContext;
    private int mNotifyId;
    private PendingIntent mActivityPendingIntent;

    @Override
    public Notification getNotification(Context context, int notifyId) {
        mContext = context;
        mNotifyId = notifyId;
        mNotifyView = new RemoteViews(mContext.getPackageName(), R.layout.widget_notify_controller);

        mNotifyView.setOnClickPendingIntent(R.id.ibPrevious,
                PendingIntent.getBroadcast(mContext, 0, new Intent(PlayerCommandReceiver.PLAYER_PREVIOUS), 0));

        mNotifyView.setOnClickPendingIntent(R.id.ibPlayPause,
                PendingIntent.getBroadcast(mContext, 0, new Intent(PlayerCommandReceiver.PLAYER_PLAY_PAUSE), 0));

        mNotifyView.setOnClickPendingIntent(R.id.ibNext,
                PendingIntent.getBroadcast(mContext, 0, new Intent(PlayerCommandReceiver.PLAYER_NEXT), 0));

        mNotifyView.setOnClickPendingIntent(R.id.ibCancel,
                PendingIntent.getBroadcast(mContext, 0, new Intent(PlayerCommandReceiver.PLAYER_SHUTDOWN), 0));

        Notification notify;
        Class cl;
        try {
            cl = Configure.getPendingActivityClass();
            mActivityPendingIntent = PendingIntent.getActivity(
                    mContext,
                    0,
                    new Intent(mContext, cl),
                    0);
            notify = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentIntent(mActivityPendingIntent)//
                    .setCustomContentView(mNotifyView)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            notify = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setCustomContentView(mNotifyView)
                    .build();
        }

        return notify;
    }

    @Override
    public void play() {
        mNotifyView.setImageViewResource(R.id.ibPlayPause, R.drawable.btn_pause);
        updateView();
    }

    @Override
    public void pause() {
        mNotifyView.setImageViewResource(R.id.ibPlayPause, R.drawable.btn_play);
        updateView();
    }

    @Override
    public void updateText(String songName, String artist) {
        mNotifyView.setTextViewText(R.id.tvTitle, songName);
        mNotifyView.setTextViewText(R.id.tvItemArtist, artist);
        updateView();
    }

    @Override
    public void setNotifyIcon(Bitmap bitmap) {
        mNotifyView.setImageViewBitmap(R.id.ivIcon, bitmap);
        updateView();
    }

    @Override
    public void setNotifyIcon(int resId) {
        mNotifyView.setImageViewResource(R.id.ivIcon, resId);
        updateView();
    }

    @Override
    public void showTempPlayMark() {
        mNotifyView.setImageViewResource(R.id.ivTempPlayMark, R.mipmap.ic_temp_play_mark);
        updateView();
    }

    @Override
    public void hideTempPlayMark() {
        mNotifyView.setImageViewResource(R.id.ivTempPlayMark, 0);
        updateView();
    }

    //*******************private*********************

    private void updateView() {
        Notification notification = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(mActivityPendingIntent)
                .setCustomContentView(mNotifyView)
                .setOnlyAlertOnce(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(mNotifyId, notification);
    }
}
