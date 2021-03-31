package co.plook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedActivity extends AppCompatActivity
{
    private DatabaseReader dbReader;

    private ArrayList<Post> allPosts;

    private Context context;
    private ViewGroup content;
    private ViewGroup contentRight;

    int lane = 0;

    private ArrayList<String> userIDs;

    private boolean isNotReady = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        context = getApplicationContext();

        dbReader = new DatabaseReader();

        Spinner spinner = (Spinner) findViewById(R.id.feed_filter);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.feed_filters, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        allPosts = new ArrayList<>();


        Task<QuerySnapshot> postTask = dbReader.findDocuments("posts", "tags", "flower").addOnCompleteListener(task -> {

            QuerySnapshot querySnapshot = task.getResult();
            List<DocumentSnapshot> docSnapshots = querySnapshot.getDocuments();
            ArrayList<String> userIDs = new ArrayList<>();

            //loop through userIDs and get a list of unique names
            for (DocumentSnapshot snapshot : docSnapshots)
            {
                String userID = snapshot.get("userID").toString();
                if (!userIDs.contains(userID))
                    userIDs.add(userID);
            }

            //request nicknames with a list of unique names and onComplete parse it to usernamePairs
            dbReader.requestNicknames(userIDs).addOnCompleteListener(new OnCompleteListener()
            {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    List<QuerySnapshot> querySnapshots = (List<QuerySnapshot>) (List<?>) task.getResult();
                    Map<String, String> usernamePairs = new HashMap<>();
                    for (int i = 0; i < querySnapshots.size(); i++)
                    {
                        List<DocumentSnapshot> docs = querySnapshots.get(i).getDocuments();
                        usernamePairs.put(userIDs.get(i), docs.get(0).get("name").toString());
                    }
                    System.out.println("PARIT: " + usernamePairs);
                    createPosts(usernamePairs, querySnapshot);
                }
            });
        });
    }

    private void createPosts(Map<String, String> usernamePairs, QuerySnapshot snapshot)
    {
        for (QueryDocumentSnapshot document : snapshot)
        {
            Post post = new Post();

            post.setPostID(document.getId());
            post.setCaption(document.getString("caption"));
            post.setDescription(document.getString("description"));
            post.setImageUrl(document.getString("url"));
            post.setName(usernamePairs.get(document.get("userID")));

            allPosts.add(post);
            showPost(post);
        }
    }

    private void showPost(Post post)
    {
        View child = getLayoutInflater().inflate(R.layout.layout_feed_post, content, false);

        content.addView(child);

        TextView textView_caption = child.findViewById(R.id.post_caption);
        TextView textView_description = child.findViewById(R.id.post_description);
        TextView textView_username = child.findViewById(R.id.post_username);
        ImageView imageView_image = child.findViewById(R.id.image);

        textView_caption.setText(post.getCaption());
        textView_description.setText(post.getDescription());
        textView_username.setText(post.getName());

        Glide.with(context).load(post.getImageUrl()).into(imageView_image);

        setListener(child);

        lane++;
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
        contentRight.removeAllViews();
        allPosts.clear();

        lane = 0;
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