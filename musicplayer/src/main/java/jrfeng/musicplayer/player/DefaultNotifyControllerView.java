package jrfeng.musicplayer.player;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.StringReader;
import java.util.Scanner;

import jrfeng.musicplayer.R;

class DefaultNotifyControllerView implements MusicPlayerClient.NotifyControllerView {
    private RemoteViews mView;
    private Context mContext;
    private int mNotifyId;
    private PendingIntent mWelcomeActivityPendingIntent;

    @Override
    public Notification getNotification(Context context, int notifyId) {
        mContext = context;
        mNotifyId = notifyId;
        mView = new RemoteViews(mContext.getPackageName(), R.layout.widget_notify_controller);

        mView.setOnClickPendingIntent(R.id.ibPrevious,
                PendingIntent.getBroadcast(mContext, 0, new Intent(MediaButtonReceiver.PLAYER_PREVIOUS), 0));

        mView.setOnClickPendingIntent(R.id.ibPlayPause,
                PendingIntent.getBroadcast(mContext, 0, new Intent(MediaButtonReceiver.PLAYER_PLAY_PAUSE), 0));

        mView.setOnClickPendingIntent(R.id.ibNext,
                PendingIntent.getBroadcast(mContext, 0, new Intent(MediaButtonReceiver.PLAYER_NEXT), 0));

        mView.setOnClickPendingIntent(R.id.ibCancel,
                PendingIntent.getBroadcast(mContext, 0, new Intent(MediaButtonReceiver.PLAYER_SHUTDOWN), 0));

        Notification notify;
        Class cl;
        try {
            cl = Configure.getPendingActivityClass();
            mWelcomeActivityPendingIntent = PendingIntent.getActivity(
                    mContext,
                    0,
                    new Intent(mContext, cl),
                    0);
            notify = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentIntent(mWelcomeActivityPendingIntent)//
                    .setCustomContentView(mView)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            notify = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setCustomContentView(mView)
                    .build();
        }

        return notify;
    }

    @Override
    public void play() {
        mView.setImageViewResource(R.id.ibPlayPause, R.drawable.btn_pause);
        updateView();
    }

    @Override
    public void pause() {
        mView.setImageViewResource(R.id.ibPlayPause, R.drawable.btn_play);
        updateView();
    }

    @Override
    public void updateText(String songName, String artist) {
        mView.setTextViewText(R.id.tvTitle, songName);
        mView.setTextViewText(R.id.tvItemArtist, artist);
        updateView();
    }

    //*******************private*********************

    private void updateView() {
        Notification notification = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(mWelcomeActivityPendingIntent)
                .setCustomContentView(mView)
                .setOnlyAlertOnce(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(mNotifyId, notification);
    }
}
