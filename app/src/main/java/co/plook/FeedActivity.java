package co.plook;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Map;

public class FeedActivity extends AppCompatActivity
{
    private ViewGroup content;

    private DatabaseDownloader dbDownloader;

    final String tag = "TULOSTUS";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        dbDownloader = new DatabaseDownloader();

        content = findViewById(R.id.feed_content);

        content.post(timer);

        /*Post[] posts = dbDownloader.getPosts();
        System.out.println("POSTS LENGTH " + posts.length);


        //SHOW POSTS
        for (Post post : posts)
        {
            showPost(post);
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

        Glide.with(getApplicationContext()).load(post.getImageUrl()).into(imageView_image);
    }

    private final Runnable timer = new Runnable()
    {
        @Override
        public void run()
        {
            dbDownloader.loadCollection("posts");
            Log.d(tag, "ENNEN WHILEA");

            Map[] maps = dbDownloader.getMaps();
            //Log.d(tag, "MAPS LENGTH: " + maps.length);
            //int mapCount = maps.length;
            /*Post[] posts = new Post[mapCount];

            for (int i = 0; i < mapCount; i++)
            {
                Post post = new Post();
                post.setCaption(maps[i].get("caption").toString());
                post.setDescription(maps[i].get("description").toString());

                dbDownloader.url2Uri(maps[i].get("url").toString());

                post.setImageUrl(dbDownloader.getUri());

                posts[i] = post;

                showPost(posts[i]);
            }*/
        }
    };
}