package co.plook;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity
{
    FirebaseAuth auth;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null)
        {
            //user is signed in
            intent = new Intent(this, FeedActivity.class);
        }
        else
        {
            //if user is NOT signed in
            intent = new Intent(this, WelcomeActivity.class);
        }
        startActivity(intent);
    }
}