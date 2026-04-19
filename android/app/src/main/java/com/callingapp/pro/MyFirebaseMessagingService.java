package com.callingapp.pro;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "incoming_calls";
    private static final String CHANNEL_NAME = "Incoming Calls";
    private static final int NOTIF_ID = 1001;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String title = "📞 Incoming Call";
        String body = "Someone is calling you...";

        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null)
                title = remoteMessage.getNotification().getTitle();
            if (remoteMessage.getNotification().getBody() != null)
                body = remoteMessage.getNotification().getBody();
        }

        if (remoteMessage.getData().size() > 0) {
            String callerName = remoteMessage.getData().get("callerName");
            String callType = remoteMessage.getData().get("callType");
            if (callerName != null && !callerName.isEmpty())
                title = "📞 " + callerName + " is calling...";
            if (callType != null)
                body = "Incoming " + (callType.equals("video") ? "Video" : "Voice") + " Call";
        }

        showCallNotification(title, body);
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
    }

    private void showCallNotification(String title, String body) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            AudioAttributes audioAttr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 400, 150, 400, 150, 400});
            channel.setSound(sound, audioAttr);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            nm.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Intent answerIntent = new Intent(this, MainActivity.class);
        answerIntent.putExtra("action", "answer");
        answerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent answerPending = PendingIntent.getActivity(this, 1, answerIntent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Intent declineIntent = new Intent(this, MainActivity.class);
        declineIntent.putExtra("action", "decline");
        declineIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent declinePending = PendingIntent.getActivity(this, 2, declineIntent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pendingIntent, true)
            .setSound(ringtoneUri)
            .setVibrate(new long[]{0, 400, 150, 400, 150, 400})
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_call, "✅ Answer", answerPending)
            .addAction(android.R.drawable.ic_delete, "❌ Decline", declinePending);

        nm.notify(NOTIF_ID, builder.build());

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(new long[]{0, 400, 150, 400, 150, 400}, 0));
            } else {
                v.vibrate(new long[]{0, 400, 150, 400, 150, 400}, 0);
            }
        }
    }
}
