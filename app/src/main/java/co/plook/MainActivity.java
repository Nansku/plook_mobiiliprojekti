package co.plook;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
{
    FirebaseAuth auth;
    Intent intent;
    MyFirebaseMessagingService service;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        service = new MyFirebaseMessagingService();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = this.getSharedPreferences("token", Context.MODE_PRIVATE);
        String token = preferences.getString("token", "");

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null)
        {
            // user is signed in
            intent = new Intent(this, FeedActivity.class);

            // if token is not blank, we have a new token!
            if (!token.equals(""))
                updateToken(token);
        }
        else
        {
            // if user is NOT signed in
            intent = new Intent(this, WelcomeActivity.class);
        }
        startActivity(intent);

        finish();
    }

    public void updateToken(String token)
    {
        System.out.println("Uploading new token!");

        DatabaseWriter dbWriter = new DatabaseWriter();

        HashMap<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("token", token);

        dbWriter.updateField("tokens", auth.getUid(), tokenMap);

        // clear stored data so we won't upload any unchanged tokens
        preferences.edit().clear().apply();
    }
}