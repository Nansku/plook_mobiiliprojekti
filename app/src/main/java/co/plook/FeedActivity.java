package co.plook;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Map;

public class FeedActivity extends AppCompatActivity
{
    private ViewGroup content;

    private DatabaseDownloader dbDownloader;

    private ArrayList<Post> allPosts;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        textView = findViewById(R.id.textView);



        dbDownloader = new DatabaseDownloader();

        content = findViewById(R.id.feed_content);
        allPosts = new ArrayList<>();

        dbDownloader.setOnLoadedListener(new DatabaseDownloader.OnLoadedListener()
        {
            //Onloaded function for POSTS specifically
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
            public void onFailure(String errorMessage)
            {
                Log.d("onLoadedListener", errorMessage);
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

        Glide.with(getApplicationContext()).load(post.getImageUrl()).into(imageView_image);
    }

    private void removePosts()
    {
        content.removeAllViews();
        allPosts.removeAll(allPosts);
    }

    public void button1(View v)
    {
        removePosts();
        dbDownloader.loadCollection("posts", "tags", "rose");
    }

    public void button2(View v)
    {
        removePosts();
        /*ArrayList<String> list = new ArrayList<>();
        list.add("red");
        list.add("blue");*/
        String[] list = {"red", "blue"};
        dbDownloader.loadCollection("posts", "tags", list);
    }

    public void button3(View v)
    {
        removePosts();
        dbDownloader.loadCollection("posts", "tags", "outdoors");
    }
}