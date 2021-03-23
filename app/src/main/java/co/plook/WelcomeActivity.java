package co.plook;

import androidx.appcompat.app.AppCompatActivity;

import android.view.WindowManager;
import android.os.Bundle;

public class WelcomeActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /*try {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e) {}*/

        setContentView(R.layout.activity_welcome);
    }
}
