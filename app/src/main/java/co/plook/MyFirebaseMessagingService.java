package co.plook;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Set;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{

    @Override
    public void onNewToken(@NonNull String s)
    {
        super.onNewToken(s);
        System.out.println("onNewToken: " + s);
        SharedPreferences preferences = this.getSharedPreferences("token", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("token", s).apply();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage)
    {
        super.onMessageReceived(remoteMessage);
        System.out.println("onMessageReceived huutelee täältä: " + remoteMessage.getData().toString());
        sendNotification(remoteMessage);
    }

    private void sendNotification(RemoteMessage message)
    {
        Intent intent;
        if (message.getData().containsKey("user_id"))
        {
            intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("user_id", message.getData().get("user_id"));
        }

        else if (message.getData().containsKey("post"))
        {
            intent = new Intent(this, PostActivity.class);
            intent.putExtra("post", message.getData().get("post"));
        }
        else
            intent = new Intent(this, MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        RemoteMessage.Notification notification = message.getNotification();
        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(message.getTitle())
                        .setContentText(message.getBody())
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
