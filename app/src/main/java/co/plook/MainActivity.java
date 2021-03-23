package co.plook;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity
{

    DatabaseUploader uploader;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploader = new DatabaseUploader();

        uploader.addPost("Iikka", "Mun kasvi :3", "Tässä tää nyt on");

    }
}