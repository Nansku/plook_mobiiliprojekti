package co.plook;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        dbDownloader = new DatabaseDownloader();

        content = findViewById(R.id.feed_content);

        Post[] posts = dbDownloader.getPosts();
        System.out.println();
        System.out.println("GETPOSTS CALLED");
        System.out.println();

        System.out.println("\nPOSTS LENGTH" + posts.length);
        for (Post post : posts)
        {
            showPost(post);
            System.out.println("SHOWPOST CALLED");
        }
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
        System.out.println("\nADDED TEXT\n");

        Glide.with(getApplicationContext()).load(post.getImageUrl()).into(imageView_image);
    }
}