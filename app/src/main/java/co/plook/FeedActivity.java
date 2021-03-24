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

    final String urli = "https://firebasestorage.googleapis.com/v0/b/plook-67980.appspot.com/o/flower.jpg?alt=media&token=2a9a0e69-decf-4733-9306-f3848f8ae3f6";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        dbDownloader = new DatabaseDownloader();

        content = findViewById(R.id.feed_content);

        dbDownloader.setOnLoadedListener(new DatabaseDownloader.OnLoadedListener()
        {
            @Override
            public void onLoaded(Object[] o)
            {
                for (int i = 0; i < o.length; i++)
                {
                    Map map = (Map) o[i];

                    Post post = new Post();
                    post.setCaption(map.get("caption").toString());
                    post.setDescription(map.get("description").toString());
                    post.setImageUrl(map.get("url").toString());

                    showPost(post);
                }
            }

            @Override
            public void onFailure()
            {

            }
        });


        dbDownloader.loadCollection("posts");

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
}