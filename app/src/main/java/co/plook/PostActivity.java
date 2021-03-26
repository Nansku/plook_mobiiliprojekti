package co.plook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class PostActivity extends AppCompatActivity
{
    private DatabaseReader dbReader;
    private DatabaseWriter dbWriter;

    private Context context;
    private ViewGroup content;
    private ImageView imageView;

    private Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        context = getApplicationContext();

        dbReader = new DatabaseReader();

        content = findViewById(R.id.post_content);
        imageView = findViewById(R.id.image);

        post = new Post();

        Bundle extras = getIntent().getExtras();
        post.setPostID(extras.getString("post_id"));

        dbReader.setOnLoadedListener(new DatabaseReader.OnLoadedListener()
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
        dbReader.findById(CollectionType.post,"posts", post.getPostID());
        dbReader.findSubcollection(CollectionType.comment_section,"comment_sections", "<postID>", "comments");
    }

    private void showPost(QuerySnapshot documentSnapshots)
    {
        for (QueryDocumentSnapshot document : documentSnapshots)
        {
            post.setPostID(document.getId());
            post.setCaption(document.getString("caption"));
            post.setDescription(document.getString("description"));
            post.setImageUrl(document.getString("url"));

            TextView textView_caption = findViewById(R.id.post_caption);
            TextView textView_description = findViewById(R.id.post_description);
            TextView textView_tags = findViewById(R.id.post_tags);

            List<String> group = (List<String>) document.get("tags");
            String tags = "tags:\n";
            for (String str : group)
            {
                tags += "{ " + str + " } ";
            }

            textView_caption.setText(post.getCaption());
            textView_description.setText(post.getDescription());
            textView_tags.setText(tags);

            Glide.with(context).load(post.getImageUrl()).into(imageView);
        }
    }

    private void showComments(QuerySnapshot documentSnapshots)
    {
        for (QueryDocumentSnapshot document : documentSnapshots)
        {
            View child = getLayoutInflater().inflate(R.layout.layout_comment, content, false);
            content.addView(child);

            TextView textView_username = child.findViewById(R.id.comment_username);
            TextView textView_commentText = child.findViewById(R.id.comment_text);

            textView_username.setText(document.getString("userID"));
            textView_commentText.setText(document.getString("text"));
        }
    }

    public void addComment(View v)
    {
        dbWriter.addComment("Username", "Lorem ipsum", post.getPostID());
    }
}