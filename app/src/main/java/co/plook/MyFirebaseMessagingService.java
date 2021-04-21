package co.plook;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Set;

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
        SharedPreferences preferences = this.getSharedPreferences("token", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("token", s).apply();
    }
}
