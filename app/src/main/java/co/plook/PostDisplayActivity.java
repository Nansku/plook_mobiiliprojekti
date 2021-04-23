package co.plook;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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
    protected DatabaseWriter dbWriter;
    protected String[] querySettings = {"all", "", "time"};
    protected Query query;
    private DocumentSnapshot lastVisible;

    // Posts & loading
    protected Bundle extras;
    private ArrayList<Post> allPosts;
    private final int postLoadAmount = 10;
    private final int postLoadThreshold = 2;
    private boolean loading = false;
    private boolean loadedAll = false;
    private QuerySnapshot postSnapshot;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        dbReader = new DatabaseReader();
        dbWriter = new DatabaseWriter();

        allPosts = new ArrayList<>();
        userIDs = new ArrayList<>();

        extras = getIntent().getExtras();

        // Make a query based on the sent string (if one was sent, otherwise default to "all//time").
        // Syntax: "field/criteria,criteria,criteria/sorting"
        // Example: "tags/red/score" "userID/User1,User2/time"
        if(extras != null)
        {
            String queryString = extras.getString("query", "all//time");
            // Split the queryString into 3 parts (0 = field, 1 = criteria, 2 = sorting).
            querySettings = queryString.split("/");

            makeQuery(querySettings[0], querySettings[1], querySettings[2]);
        }
        else
        {
            makeQuery("all", "", "time");
        }
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();

        // Update all posts after coming back from another activity.
        for (int i = 0; i < allPosts.size(); i++)
            updatePostData(i);
    }

    protected void makeQuery(String field, String criteria, String sorting)
    {
        // Collection is always "posts".
        query = dbReader.db.collection("posts");

        // Split criteria into its ows array.
        String[] criteriaArray = criteria.split(",");

        // Check which field is used for filtering, should always be either "userID", "channel" or "tags" (or "", in which case we don't filter).
        // "tags" field is an array, so "whereArrayContainsAny" is used instead of "whereIn".
        if(criteriaArray.length > 0)
        {
            if (field.equals("userID") || field.equals("channel"))
                query = query.whereIn(field, Arrays.asList(criteriaArray));
            else if (field.equals("tags"))
                query = query.whereArrayContainsAny(field, Arrays.asList(criteriaArray));
        }

        // Sort by either "time" or "score"
        query = query.orderBy(sorting, Query.Direction.DESCENDING);

        // Get only a set amount of posts at once.
        query = query.limit(postLoadAmount);
    }

    protected void refreshPosts()
    {
        makeQuery(querySettings[0], querySettings[1], querySettings[2]);

        deletePosts();
        loadPosts();
    }

    protected void loadPosts()
    {
        loading = true;

        allPosts.add(null);
        feedContentAdapter.notifyItemInserted(allPosts.size() - 1);

        // Ignore posts before (and including) the last one. This way no duplicates should appear.
        // If this is the first time loading the activity, or if the user has refreshed the page, then start from the first post.
        if (lastVisible != null)
            query = query.startAfter(lastVisible);

        Map<String, String> usernamePairs = new HashMap<>();
        Map<String, Long> myVotesPerPost = new HashMap<>();

        List<Task<Void>> subTasks = new ArrayList<>();

        dbReader.findDocuments(query).addOnCompleteListener(task ->
        {
            postSnapshot = task.getResult();

            // Loop through the posts and get a list of unique userIDs and also gets the user's vote for each post.
            for (DocumentSnapshot snapshot : postSnapshot.getDocuments())
            {
                // Get unique userIDs.
                String userID = snapshot.getString("userID");
                if (!userIDs.contains(userID))
                    userIDs.add(userID);

                // Get user's vote per post.
                Task voteTask = dbReader.findDocumentByID("posts/" + snapshot.getId() + "/user_actions", auth.getUid()).addOnCompleteListener(task1 ->
                {
                    if(task1.getResult().getDocuments().size() > 0)
                    {
                        DocumentSnapshot doc = task1.getResult().getDocuments().get(0);
                        myVotesPerPost.put(snapshot.getId(), doc.getLong("vote"));
                    }
                    else
                        myVotesPerPost.put(snapshot.getId(), 0L);
                });

                subTasks.add(voteTask);
            }

            // For each unique userID, get that user's username.
            // Once we've done that, we can display the posts with proper data.
            Task nicknameTask = dbReader.requestNicknames(userIDs).addOnCompleteListener(task1 ->
            {
                List<QuerySnapshot> querySnapshots = (List<QuerySnapshot>) (List<?>) task1.getResult();

                // If there's nothing to show, remove the loading icon.
                if (querySnapshots == null || querySnapshots.size() <= 0)
                {
                    allPosts.remove(allPosts.size() - 1);
                    feedContentAdapter.notifyItemRemoved(allPosts.size());

                    loading = false;
                    loadedAll = true;

                    return;
                }

                for (int i = 0; i < querySnapshots.size(); i++)
                {
                    DocumentSnapshot doc = querySnapshots.get(i).getDocuments().get(0);
                    usernamePairs.put(userIDs.get(i), doc.getString("name"));
                }
            });

            subTasks.add(nicknameTask);

            // Wait for the other tasks to finish.
            Tasks.whenAllSuccess(subTasks).addOnCompleteListener(new OnCompleteListener<List<Object>>()
            {
                @Override
                public void onComplete(@NonNull Task<List<Object>> task)
                {
                    allPosts.remove(allPosts.size() - 1);
                    feedContentAdapter.notifyItemRemoved(allPosts.size());

                    loading = false;

                    if (postSnapshot.isEmpty() || postSnapshot.size() < postLoadAmount)
                        loadedAll = true;

                    createPosts(usernamePairs, myVotesPerPost, postSnapshot);
                }
            });
        });
    }

    private void createPosts(Map<String, String> usernamePairs, Map<String, Long> myVotesPerPost, QuerySnapshot snapshot)
    {
        int oldPostCount = allPosts.size();

        for (int i = 0; i < snapshot.size(); i++)
        {
            DocumentSnapshot postDocument = snapshot.getDocuments().get(i);

            Post post = new Post();

            post.setPostID(postDocument.getId());
            post.setCaption(postDocument.getString("caption"));
            post.setDescription(postDocument.getString("description"));
            post.setImageUrl(postDocument.getString("url"));
            post.setUserID(usernamePairs.get(postDocument.getString("userID")));

            post.setScore(postDocument.getLong("score"));
            post.setMyVote(myVotesPerPost.get(postDocument.getId()));

            allPosts.add(post);
        }

        feedContentAdapter.notifyItemRangeChanged(oldPostCount - 1, snapshot.size());

        if (snapshot.size() > 0)
            lastVisible = snapshot.getDocuments().get(snapshot.size() - 1);
    }

    protected void deletePosts()
    {
        recyclerView.removeAllViews();
        allPosts.clear();

        feedContentAdapter.notifyDataSetChanged();

        lastVisible = null;
        loadedAll = false;
    }

    protected void updatePostData(int position)
    {
        Post post = allPosts.get(position);

        List<Task<Void>> subTasks = new ArrayList<>();

        // Update post with new data from the database.
        dbReader.findDocumentByID("posts", post.getPostID()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                DocumentSnapshot postDocument = task.getResult().getDocuments().get(0);

                post.setPostID(postDocument.getId());
                post.setCaption(postDocument.getString("caption"));
                post.setDescription(postDocument.getString("description"));
                post.setImageUrl(postDocument.getString("url"));
                post.setScore(postDocument.getLong("score"));

                // Get user's vote data for this post.
                Task voteTask = dbReader.findDocumentByID("posts/" + post.getPostID() + "/user_actions", auth.getUid()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task1)
                    {
                        if(task1.getResult().getDocuments().size() > 0)
                        {
                            DocumentSnapshot doc = task1.getResult().getDocuments().get(0);
                            post.setMyVote(doc.getLong("vote"));
                        }
                        else
                            post.setMyVote(0L);
                    }
                });

                subTasks.add(voteTask);

                // Get the poster's username.
                Task usernameTask = dbReader.findDocumentByID("users", postDocument.getString("userID")).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task1)
                    {
                        DocumentSnapshot doc = task1.getResult().getDocuments().get(0);
                        post.setUserID(doc.getString("name"));
                    }
                });

                subTasks.add(usernameTask);

                // Wait for the other tasks to finish.
                Tasks.whenAllSuccess(subTasks).addOnCompleteListener(new OnCompleteListener<List<Object>>()
                {
                    @Override
                    public void onComplete(@NonNull Task<List<Object>> task)
                    {
                        feedContentAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        // Post data has been updated, so we can just return here.
        return;
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
                openPostActivity(position);
            }

            @Override
            public void onVoteClick(int position, int vote)
            {
                votePost(position, vote);
            }
        });

        recyclerView.setAdapter(feedContentAdapter);
        recyclerView.addItemDecoration(new LinearSpacesItemDecoration(context, 5));

        recyclerScrollListener();
    }

    // Load more posts as the user scrolls down.
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
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount - postLoadThreshold)
                            loadPosts();
                    }
                }
            }
        });
    }

    protected void initializeSwipeRefreshLayout(SwipeRefreshLayout swipeContainer)
    {
        swipeContainer.setOnRefreshListener(() ->
        {
            if(!loading)
            {
                refreshPosts();
                swipeContainer.setRefreshing(false);
            }
        });
    }

    private void votePost(int position, int vote)
    {
        Post post = allPosts.get(position);

        if(vote == post.getMyVote())
            vote = 0;

        dbWriter.addVote(auth.getUid(), post.getPostID(), vote);

        long difference = vote - post.getMyVote();

        post.setScore(post.getScore() + difference);
        post.setMyVote(vote);

        feedContentAdapter.notifyItemChanged(position);
    }

    private void openPostActivity(int position)
    {
        Post post = allPosts.get(position);

        Intent intent = new Intent(this, PostActivity.class);
        intent.putExtra("post_id", post.getPostID());

        startActivity(intent);
    }
}