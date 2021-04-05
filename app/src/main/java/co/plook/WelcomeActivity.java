package co.plook;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class WelcomeActivity extends ParentActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);
    }
}
