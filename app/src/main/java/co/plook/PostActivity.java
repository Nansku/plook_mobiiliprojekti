package co.plook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.CountDownLatch;

public class PostActivity extends AppCompatActivity
{
    private DatabaseReader dbDownloader;

    private Context context;
    private ImageView imageView;
    private String postID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        context = getApplicationContext();

        dbDownloader = new DatabaseReader();

        imageView = findViewById(R.id.image);

        Bundle extras = getIntent().getExtras();
        postID = extras.getString("post_id");

        dbDownloader.setOnLoadedListener(new DatabaseReader.OnLoadedListener()
        {
            @Override
            public void onLoaded(QuerySnapshot documentSnapshots)
            {
                for (QueryDocumentSnapshot document : documentSnapshots)
                {
                    Glide.with(context).load(document.get("url").toString()).into(imageView);
                }
            }

            @Override
            public void onFailure()
            {

            }
        });

        dbDownloader.findById("posts", postID);
    }
}