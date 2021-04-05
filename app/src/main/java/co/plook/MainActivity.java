package co.plook;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends ParentActivity
{

    DatabaseWriter dbWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Postin luonti tietokantaan
        /*dbWriter = new DatabaseUploader();
        dbWriter.addPost("Iikka", "Mun kasvi :3", "Tässä tää nyt on");*/

    }
}