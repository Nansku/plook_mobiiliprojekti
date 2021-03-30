package co.plook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class FeedActivity extends AppCompatActivity
{
    private DatabaseReader dbReader;

    private ArrayList<Post> allPosts;

    private Context context;
    private ViewGroup content;

    private boolean isNotReady = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        context = getApplicationContext();

        dbReader = new DatabaseReader();

        content = findViewById(R.id.feed_content);
        allPosts = new ArrayList<>();


        Task postTask = dbReader.findDocuments("posts", "tags", "red").addOnCompleteListener(task -> {
            QuerySnapshot snapshot = (QuerySnapshot) task.getResult();
            for (QueryDocumentSnapshot document : snapshot)
            {
                Post post = new Post();

                post.setPostID(document.getId());
                post.setCaption(document.getString("caption"));
                post.setDescription(document.getString("description"));
                post.setImageUrl(document.getString("url"));

                allPosts.add(post);
                showPost(post);

                isNotReady = false;
            }
        });

        //this should wait for postTask
        Task displayNameTask = dbReader.findDocumentByID("posts", "tags");

        /*synchronized (displayNameTask)
        {
            if (isNotReady)
            {
                try
                {
                    displayNameTask.wait();
                }
                catch (InterruptedException e)
                {
                    System.out.println(e.getMessage());
                }
            }
            System.out.println("ON VALMIS");
        }*/
    }

    private void showPost(Post post)
    {
        View child = getLayoutInflater().inflate(R.layout.layout_feed_post, content, false);
        content.addView(child);

        TextView textView_caption = child.findViewById(R.id.post_caption);
        TextView textView_description = child.findViewById(R.id.post_description);
        ImageView imageView_image = child.findViewById(R.id.image);

        textView_caption.setText(post.getCaption());
        textView_description.setText(post.getDescription());

        Glide.with(context).load(post.getImageUrl()).into(imageView_image);

        setListener(child);
    }

    private void setListener(View v)
    {
        v.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int i = content.indexOfChild(v);

                openPostActivity(allPosts.get(i).getPostID());
            }
        });
    }

    private void openPostActivity(String postID)
    {
        Intent intent = new Intent(this, PostActivity.class);

        intent.putExtra("post_id", postID);

        startActivity(intent);
    }

    private void removePosts()
    {
        content.removeAllViews();
        allPosts.clear();
    }

    public void button1(View v)
    {
        dbReader.findDocuments("posts", "tags", "blue");
    }

    public void button2(View v)
    {
        String[] list = {"red", "blue"};
        dbReader.findDocuments("posts", "tags", list);
    }

    public void button3(View v)
    {
        dbReader.findDocuments("posts", "tags", "outdoors");
    }
}