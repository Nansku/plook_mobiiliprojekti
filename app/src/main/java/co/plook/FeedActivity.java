package co.plook;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedActivity extends ParentActivity
{
    // Views & UI
    private Context context;
    private RecyclerView recyclerView;
    private ArrayList<String> userIDs;
    private FeedContentAdapter feedContentAdapter;

    // Database stuff
    private DatabaseReader dbReader;
    private DatabaseWriter dbWriter;
    private Query query;
    private DocumentSnapshot lastVisible;

    // Posts & loading
    private ArrayList<Post> allPosts;
    private final int postLoadAmount = 2;
    private boolean loading = false;
    private boolean loadedAll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_feed, contentGroup);

        context = getApplicationContext();

        dbReader = new DatabaseReader();
        dbWriter = new DatabaseWriter();

        allPosts = new ArrayList<>();
        userIDs = new ArrayList<>();

        initializeRecyclerView();

        // Make a query based on the sent string (if one was sent, otherwise default to empty).
        Bundle extras = getIntent().getExtras();
        String queryString = "";
        if(extras != null)
            queryString = extras.getString("query", "");

        makeQuery(queryString);

        loadPosts();
    }

    private void initializeRecyclerView()
    {
        recyclerView = findViewById(R.id.feed_recycle);

        feedContentAdapter = new FeedContentAdapter(allPosts, context);
        feedContentAdapter.setOnItemClickedListener((position, view) -> openPostActivity(allPosts.get(position).getPostID()));

        recyclerView.setAdapter(feedContentAdapter);
        recyclerView.addItemDecoration(new LinearSpacesItemDecoration(context, 5));

        recyclerScrollListener();
    }

    private void recyclerScrollListener()
    {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (dy > 0)
                {
                    assert layoutManager != null;
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (!loading && !loadedAll)
                    {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount - 2)
                            loadPosts();
                    }
                }
            }
        });
    }

    private void makeQuery(String queryString)
    {
        if(queryString.equals(""))
            queryString = "all//time";

        String[] queryParts = queryString.split("/");

        query = dbReader.db.collection("posts");

        if (queryParts[0].equals("userID") || queryParts[0].equals("channel"))
        {
            query = query.whereEqualTo(queryParts[0], queryParts[1]);
        }
        else if (queryParts[0].equals("tags"))
        {
            query = query.whereArrayContains(queryParts[0], queryParts[1]);
        }

        query = query.orderBy(queryParts[2], Query.Direction.DESCENDING);

        // Get only a set amount of posts at once.
        query = query.limit(postLoadAmount);
    }

    private void loadPosts()
    {
        loading = true;

        allPosts.add(null);
        feedContentAdapter.notifyItemInserted(allPosts.size() - 1);

        // Ignore posts before (and including) the last one. This way no duplicates should appear.
        // If this is the first time loading the activity, or if the user somehow refreshes, then start from the first post.
        if (lastVisible != null)
            query = query.startAfter(lastVisible);

        dbReader.findDocuments(query).addOnCompleteListener(task ->
        {
            QuerySnapshot postSnapshot = task.getResult();

            // Loop through userIDs and get a list of unique names
            for (DocumentSnapshot snapshot : postSnapshot.getDocuments())
            {
                String userID = snapshot.getString("userID");
                if (!userIDs.contains(userID))
                    userIDs.add(userID);
            }

            dbReader.requestNicknames(userIDs).addOnCompleteListener(task1 ->
            {
                List<QuerySnapshot> querySnapshots = (List<QuerySnapshot>) (List<?>) task1.getResult(); //  @Iikka what even is this??
                Map<String, String> usernamePairs = new HashMap<>();

                for (int i = 0; i < querySnapshots.size(); i++)
                {
                    List<DocumentSnapshot> docs = querySnapshots.get(i).getDocuments();
                    usernamePairs.put(userIDs.get(i), docs.get(0).getString("name"));
                }

                allPosts.remove(allPosts.size() - 1);
                feedContentAdapter.notifyItemRemoved(allPosts.size());

                createPosts(usernamePairs, postSnapshot);

                loading = false;

                if(postSnapshot.isEmpty() || postSnapshot.size() < postLoadAmount)
                    loadedAll = true;
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
        }

        feedContentAdapter.notifyDataSetChanged();

        if(snapshot.size() > 0)
            lastVisible = snapshot.getDocuments().get(snapshot.size() - 1);
    }

    private void removePosts()
    {
        recyclerView.removeAllViews();
        allPosts.clear();

        feedContentAdapter.notifyDataSetChanged();
        lastVisible = null;

        loadedAll = false;
    }

    private void openPostActivity(String postID)
    {
        Intent intent = new Intent(this, PostActivity.class);

        intent.putExtra("post_id", postID);

        startActivity(intent);
    }

    public void button1(View v)
    {
        makeQuery("");

        removePosts();
        loadPosts();
        //auth.signOut();
        dbWriter.updateUser("qzTMn9YNS4NKYCPF97Ks7dVOjyX2");
    }

    @Override
    public void onBackPressed()
    {
        //block the user from going back to blank
        //maybe add a feed refresh function here?
        //super.onBackPressed();
    }
}