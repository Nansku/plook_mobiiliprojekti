package co.plook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.CountDownLatch;

public class PostActivity extends AppCompatActivity
{
    private DatabaseReader dbDownloader;

    private Context context;
    private ViewGroup content;
    private ImageView imageView;

    private String postID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        context = getApplicationContext();

        dbDownloader = new DatabaseReader();

        content = findViewById(R.id.post_content);
        imageView = findViewById(R.id.image);

        Bundle extras = getIntent().getExtras();
        postID = extras.getString("post_id");

        dbDownloader.setOnLoadedListener(new DatabaseReader.OnLoadedListener()
        {
            @Override
            public void onLoaded(CollectionType type, QuerySnapshot documentSnapshots)
            {
                switch (type)
                {
                    case post:
                        showPost(documentSnapshots);
                        break;
                    case comment_section:
                        showComments(documentSnapshots);
                        break;
                }
            }

            @Override
            public void onFailure()
            {

            }
        });

        // Passing the enum so later (in onLoaded) we can process the received data further.
        dbDownloader.findById(CollectionType.post,"posts", postID);
        dbDownloader.findSubcollection(CollectionType.comment_section,"comment_sections", "<postID>", "comments");
    }

    private void showPost(QuerySnapshot documentSnapshots)
    {
        for (QueryDocumentSnapshot document : documentSnapshots)
        {
            TextView textView_caption = findViewById(R.id.post_caption);
            textView_caption.setText(document.get("caption").toString());

            TextView textView_description = findViewById(R.id.post_description);
            textView_description.setText(document.get("description").toString());

            Glide.with(context).load(document.get("url").toString()).into(imageView);
        }
    }

    private void showComments(QuerySnapshot documentSnapshots)
    {
        for (QueryDocumentSnapshot document : documentSnapshots)
        {
            System.out.println("TASSA " + document.toString());

            View child = getLayoutInflater().inflate(R.layout.layout_comment, content, false);
            content.addView(child);

            TextView textView_username = child.findViewById(R.id.comment_username);
            TextView textView_commentText = child.findViewById(R.id.comment_text);

            textView_username.setText(document.get("userID").toString());
            textView_commentText.setText(document.get("text").toString());
        }
    }
}