package co.plook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

// Parent class for all of the other activities to check if user is logged in on every onCreate()
public class ParentActivity extends AppCompatActivity
{
    protected FirebaseAuth auth;
    protected ViewGroup contentGroup;
    private Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer);

        contentGroup = findViewById(R.id.frameLayout);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null)
        {
            // send user to login page if not logged in
            // startSplashScreenActivity();
            intent = new Intent(this, MainActivity.class);

            // comment out this line if you want to ignore login check
            startActivity(intent);
        }
    }

    public void openProfile(View v)
    {
        intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void openFeed(View v)
    {
        intent = new Intent(this, FeedActivity.class);
        startActivity(intent);
    }

    public void logout(View v)
    {
        auth.signOut();
        intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }
}
