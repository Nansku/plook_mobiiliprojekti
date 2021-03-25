package co.plook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class PostActivity extends AppCompatActivity
{
    private DatabaseDownloader dbDownloader;

    private Context context;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        dbDownloader = new DatabaseDownloader();

        imageView = findViewById(R.id.image);

        Bundle extras = getIntent().getExtras();
        String postID = extras.getString("postID");

        dbDownloader.setOnLoadedListener(new DatabaseDownloader.OnLoadedListener()
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

        //dbDownloader.loadCollection("posts", "__name__", postID);
    }
}