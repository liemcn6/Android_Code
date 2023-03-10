package com.example.musicplayernotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.musicplayernotification.Service.NotificationActionService;

public class CreateNotification {
    public static final String CHANNEL_ID = "channell";
    public static final String ACTION_PREVIOUS = "actionprevious";
    public static final String ACTION_PLAY = "actionplay";
    public static final String ACTION_NEXT = "actionnext";

    public static Notification notification;

    public static void createNotification(Context context, Track track, int playbutton, int position, int size){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "tag");

            Bitmap ic = BitmapFactory.decodeResource(context.getResources(),track.getImage());

            PendingIntent pendingIntentPrevious;
            int drw_previous;
            if(position == 0){
                pendingIntentPrevious = null;
                drw_previous = 0;
            }else{
                Intent intentPrevious = new Intent(context, NotificationActionService.class)
                        .setAction(ACTION_PREVIOUS);
                pendingIntentPrevious = PendingIntent.getBroadcast(context, 0,intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
                drw_previous = R.drawable.ic__skip_previous;
            }

            Intent intentPlay= new Intent(context, NotificationActionService.class)
                    .setAction(ACTION_PREVIOUS);
            PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0,intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);

            PendingIntent pendingIntentNext;
            int drw_next;
            if(position == size){
                pendingIntentNext = null;
                drw_next = 0;
            }else{
                Intent intentNext = new Intent(context, NotificationActionService.class)
                        .setAction(ACTION_NEXT);
                pendingIntentNext = PendingIntent.getBroadcast(context, 0,intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
                drw_next = R.drawable.ic_skip_next;
            }

            notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_music)
                    .setContentTitle(track.getTitle())
                    .setContentText(track.getArtist())
                    .setLargeIcon(ic)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(false)
                    .addAction(drw_previous,"Previous",pendingIntentPrevious)
                    .addAction(playbutton,"Play",pendingIntentPlay)
                    .addAction(drw_next,"Next",pendingIntentNext)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0,1,2)
                    .setMediaSession(mediaSessionCompat.getSessionToken()))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();

            notificationManagerCompat.notify(1,notification);
        }
    }
}
