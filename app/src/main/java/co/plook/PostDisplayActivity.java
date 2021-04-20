package co.plook;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostDisplayActivity extends ParentActivity
{
    // Views & UI
    protected Context context;
    private ArrayList<String> userIDs;
    private RecyclerView recyclerView;
    private FeedContentAdapter feedContentAdapter;

    // Database stuff
    protected DatabaseReader dbReader;
    protected Query query;
    private String queryString;
    private DocumentSnapshot lastVisible;

    // Posts & loading
    protected Bundle extras;
    private ArrayList<Post> allPosts;
    private final int postLoadAmount = 10;
    private boolean loading = false;
    private boolean loadedAll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        dbReader = new DatabaseReader();

        allPosts = new ArrayList<>();
        userIDs = new ArrayList<>();

        extras = getIntent().getExtras();

        // Make a query based on the sent string (if one was sent, otherwise default to empty).
        if(extras != null)
            queryString = extras.getString("query", "");
        else
            queryString = "";

        makeQuery(queryString);
    }

    // Syntax: "field/criteria/sorting"
    // Example: "tags/red/time" "userID/insert userID here/time"
    protected void makeQuery(String queryString)
    {
        if(queryString.equals(""))
            queryString = "all//time";

        String[] queryParts = queryString.split("/");
        String[] criteria = queryParts[1].split(",");

        query = dbReader.db.collection("posts");

        // Field is a single item.
        if (queryParts[0].equals("userID") || queryParts[0].equals("channel"))
            query = query.whereIn(queryParts[0], Arrays.asList(criteria));

        // Field is an array of items.
        else if (queryParts[0].equals("tags"))
            query = query.whereArrayContains(queryParts[0], queryParts[1]);

        // Sort by
        query = query.orderBy(queryParts[2], Query.Direction.DESCENDING);

        // Get only a set amount of posts at once.
        query = query.limit(postLoadAmount);

        this.queryString = queryString;
    }

    private void refreshPosts()
    {
        makeQuery(queryString);

        removePosts();
        loadPosts();
    }

    protected void loadPosts()
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
                List<QuerySnapshot> querySnapshots = (List<QuerySnapshot>) (List<?>) task1.getResult(); // @Iikka what/how is this??

                if(querySnapshots == null || querySnapshots.size() <= 0)
                    return;

                Map<String, String> usernamePairs = new HashMap<>();

                for (int i = 0; i < querySnapshots.size(); i++)
                {
                    List<DocumentSnapshot> docs = querySnapshots.get(i).getDocuments();
                    usernamePairs.put(userIDs.get(i), docs.get(0).getString("name"));
                }

                allPosts.remove(allPosts.size() - 1);
                feedContentAdapter.notifyItemRemoved(allPosts.size());

                makePosts(usernamePairs, postSnapshot);

                loading = false;

                if(postSnapshot.isEmpty() || postSnapshot.size() < postLoadAmount)
                    loadedAll = true;
            });
        });
    }

    private void makePosts(Map<String, String> usernamePairs, QuerySnapshot snapshot)
    {
        for (QueryDocumentSnapshot document : snapshot)
        {
            Post post = new Post();

            post.setPostID(document.getId());
            post.setCaption(document.getString("caption"));
            post.setDescription(document.getString("description"));
            post.setImageUrl(document.getString("url"));
            post.setUserID(usernamePairs.get(document.get("userID")));

            allPosts.add(post);
        }

        feedContentAdapter.notifyDataSetChanged();

        if(snapshot.size() > 0)
            lastVisible = snapshot.getDocuments().get(snapshot.size() - 1);
    }

    protected void removePosts()
    {
        recyclerView.removeAllViews();
        allPosts.clear();

        feedContentAdapter.notifyDataSetChanged();

        lastVisible = null;
        loadedAll = false;
    }

    protected void initializeRecyclerView(RecyclerView recyclerView)
    {
        this.recyclerView = recyclerView;

        feedContentAdapter = new FeedContentAdapter(allPosts, context);
        feedContentAdapter.setOnItemClickedListener(new FeedContentAdapter.ClickListener()
        {
            @Override
            public void onItemClick(int position, View view)
            {
                openPostActivity(allPosts.get(position).getPostID());
            }

            @Override
            public void onVoteClick(int position, int vote)
            {
                votePost(allPosts.get(position).getPostID(), vote);
            }
        });

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

    protected void initializeSwipeRefreshLayout(SwipeRefreshLayout swipeContainer)
    {
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                refreshPosts();
                swipeContainer.setRefreshing(false);
            }
        });
    }

    private void votePost(String postID, int vote)
    {
        System.out.println("YOU VOTED: " + vote + " ON POST: " + postID);
    }

    private void openPostActivity(String postID)
    {
        Intent intent = new Intent(this, PostActivity.class);
        intent.putExtra("post_id", postID);

        startActivity(intent);
    }
}