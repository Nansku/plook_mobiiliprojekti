package co.plook;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{
    DatabaseWriter dbWriter;
    FirebaseAuth auth;

    MyFirebaseMessagingService()
    {
        dbWriter = new DatabaseWriter();
        auth = FirebaseAuth.getInstance();
    }
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage)
    {
        super.onMessageReceived(remoteMessage);
        Log.d("MSG", remoteMessage.getData().toString());
    }

    @Override
    public void onNewToken(@NonNull String s)
    {
        super.onNewToken(s);
        Log.d("TOKEN", "onNewToken: " + s);
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("token", s);
        dbWriter.updateUser("users", auth.getUid(), userMap);
    }
}
