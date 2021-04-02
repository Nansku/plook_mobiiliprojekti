package co.plook;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//Parent class for all of the other activities to check if user is logged in on every onCreate()
public class ParentActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
        {
            startSplashScreenActivity();
        }
    }

    private void startSplashScreenActivity()
    {
        //Open Splash screen and login activity
    }
}
