package co.plook;

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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
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
    private DocumentSnapshot lastVisible;

    private Context context;
    private ViewGroup content;

    private ArrayList<String> userIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        context = getApplicationContext();

        dbReader = new DatabaseReader();

        Spinner spinner = findViewById(R.id.feed_filter);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.feed_filters, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        content = findViewById(R.id.feed_content);
        allPosts = new ArrayList<>();

        loadPosts();
    }

    private void loadPosts()
    {
        Query q = dbReader.db.collection("posts").whereEqualTo("channel", "qgFpTggcuMNLzOMbIbyD").orderBy("time", Query.Direction.DESCENDING);

        if(lastVisible != null)
            q = q.startAfter(lastVisible);

        q = q.limit(2);

        dbReader.findDocuments(q).addOnCompleteListener(task ->
        {
            QuerySnapshot snapshot = task.getResult();
            requestNicknames(snapshot).addOnCompleteListener(task1 ->
            {
                List<QuerySnapshot> querySnapshots = (List<QuerySnapshot>) task1.getResult();
                Map<String, String> usernamePairs = new HashMap<>();

                for (int i = 0; i < querySnapshots.size(); i++)
                {
                    List<DocumentSnapshot> docs = querySnapshots.get(i).getDocuments();

                    usernamePairs.put(userIDs.get(i), docs.get(0).get("name").toString());
                }

                createPosts(usernamePairs, snapshot);
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

        if(snapshot.size() > 0)
            lastVisible = snapshot.getDocuments().get(snapshot.size() - 1);
    }

    private void showPost(Post post)
    {
        View child = getLayoutInflater().inflate(R.layout.layout_feed_post, content, false);

        content.addView(child, content.getChildCount() - 1);

        TextView textView_caption = child.findViewById(R.id.post_caption);
        //TextView textView_description = child.findViewById(R.id.post_description);
        TextView textView_username = child.findViewById(R.id.post_username);
        ImageView imageView_image = child.findViewById(R.id.image);

        textView_caption.setText(post.getCaption());
        //textView_description.setText(post.getDescription());
        textView_username.setText(post.getName());

        Glide.with(context).load(post.getImageUrl()).into(imageView_image);

        setListener(child);
    }

    private void removePosts()
    {
        for(int i = content.getChildCount() - 2; i >= 0; i--)
            content.removeViewAt(i);

        allPosts.clear();

        lastVisible = null;
    }

    private Task requestNicknames(QuerySnapshot querySnapshot)
    {
        List<DocumentSnapshot> docSnapshots = querySnapshot.getDocuments();
        userIDs = new ArrayList<>();

        //loop through userIDs and get a list of unique names
        for (DocumentSnapshot snapshot : docSnapshots)
        {
            String userID = snapshot.get("userID").toString();
            if (!userIDs.contains(userID))
                userIDs.add(userID);
        }

        Task[] tasks = new Task[userIDs.size()];

        for (int i = 0; i < userIDs.size(); i++)
        {
            Task<QuerySnapshot> userNameTask = dbReader.findDocumentByID("users", userIDs.get(i))
                    .addOnCompleteListener(task -> { });
            tasks[i] = userNameTask;
        }

        Task<List<Object>> parallelTask = Tasks.whenAllSuccess(tasks).addOnSuccessListener(objects -> { });

        return parallelTask;
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

    public void button1(View v)
    {
        removePosts();
        loadPosts();
    }

    public void loadMore(View v)
    {
        loadPosts();
    }
}