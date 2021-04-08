package co.plook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class WelcomeActivity extends AppCompatActivity
{
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);
    }

    public void login(View v)
    {
        intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void signup(View v)
    {
        intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }
}
