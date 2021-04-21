package co.plook;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage)
    {
        super.onMessageReceived(remoteMessage);
        System.out.println("onMessageReceived huutelee täältä: " + remoteMessage.getData().toString());
    }

    @Override
    public void onNewToken(@NonNull String s)
    {
        super.onNewToken(s);
        System.out.println("onNewToken: " + s);
        // send new token to somewhere...
    }
}
