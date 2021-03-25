package co.plook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class FeedActivity extends AppCompatActivity
{
    private ViewGroup content;

    private DatabaseDownloader dbDownloader;

    private ArrayList<Post> allPosts;

    private TextView textView;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        context = getApplicationContext();

        textView = findViewById(R.id.textView);

        dbDownloader = new DatabaseDownloader();

        content = findViewById(R.id.feed_content);
        allPosts = new ArrayList<>();


        dbDownloader.setOnLoadedListener(new DatabaseDownloader.OnLoadedListener()
        {
            //Onloaded function for POSTS specifically
            @Override
            public void onLoaded(QuerySnapshot documentSnapshots)
            {
                for (QueryDocumentSnapshot document : documentSnapshots)
                {
                    Post post = new Post();

                    post.setPostID(document.getId());
                    post.setCaption(document.get("caption").toString());
                    post.setDescription(document.get("description").toString());
                    post.setImageUrl(document.get("url").toString());

                    allPosts.add(post);
                    showPost(post);
                }
            }

            @Override
            public void onLoadedComments(Map[] m)
            {
                for (Map map : m)
                {
                    textView.append(map.get("text").toString() + " ");
                    //Log.d("MAP", map.get("text").toString());
                }

            }

            @Override
            public void onFailure(String error)
            {
                Log.d("onLoadedListener error", error);
            }
        });

        dbDownloader.loadCollection("posts");
        dbDownloader.loadComments("<postID>");

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

                Toast.makeText(context, allPosts.get(i).getPostID(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removePosts()
    {
        content.removeAllViews();
        allPosts.clear();
    }

    public void button1(View v)
    {
        removePosts();
        dbDownloader.loadCollection("posts");
    }

    public void button2(View v)
    {
        removePosts();
        String[] list = {"red", "blue"};
        dbDownloader.loadCollection("posts", "tags", list);
    }

    public void button3(View v)
    {
        removePosts();
        dbDownloader.loadCollection("posts", "tags", "outdoors");
    }
}