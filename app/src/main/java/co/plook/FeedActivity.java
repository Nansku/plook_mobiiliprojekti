package co.plook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
    private RecyclerView recyclerView;

    private ArrayList<String> userIDs;
    private FeedContentAdapter feedContentAdapter;

    private boolean loading = false;

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

        allPosts = new ArrayList<>();

        initializeRecyclerView();

        loadPosts();
    }

    private void initializeRecyclerView()
    {
        recyclerView = findViewById(R.id.feed_recycle);

        feedContentAdapter = new FeedContentAdapter(allPosts, context);
        feedContentAdapter.setOnItemClickedListener((position, view) -> openPostActivity(allPosts.get(position).getPostID()));

        recyclerView.setAdapter(feedContentAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if(dy > 0)
                {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

                    if (!loading)
                    {
                        System.out.println("SCOLLING");
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount - 2)
                            loadPosts();
                    }
                }
            }
        });
    }

    private void loadPosts()
    {
        loading = true;

        // Posts are loaded from the "posts" collection. Wow.
        Query q = dbReader.db.collection("posts");

        // Make a query for posts in a specific channel.
        q.whereEqualTo("channel", "qgFpTggcuMNLzOMbIbyD");

        // Posts are ordered by "new" by default.
        q.orderBy("time", Query.Direction.DESCENDING);

        // Ignore posts before (and including) the last one. This way no duplicates should appear.
        // If this is the first time loading the activity, or if the user somehow refreshes, then start from the first post.
        if(lastVisible != null)
            q = q.startAfter(lastVisible);

        // Get only the first few posts.
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

                loading = false;
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
            feedContentAdapter.notifyItemInserted(allPosts.size() - 1);
        }

        if(snapshot.size() > 0)
            lastVisible = snapshot.getDocuments().get(snapshot.size() - 1);
    }

    private void removePosts()
    {
        recyclerView.removeAllViews();
        allPosts.clear();

        feedContentAdapter.notifyDataSetChanged();
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
}