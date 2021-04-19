package co.plook;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

public class FeedActivity extends PostDisplayActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        currentActivity = this;
        navigationView.getMenu().getItem(1).setChecked(true);
        getLayoutInflater().inflate(R.layout.activity_feed, contentGroup);

        RecyclerView recyclerView = findViewById(R.id.feed_recycle);
        initializeRecyclerView(recyclerView);

        SwipeRefreshLayout swipeContainer = findViewById(R.id.feed_swipeRefresh);
        initializeSwipeRefreshLayout(swipeContainer);

        loadPosts();

        // token debug
        /*FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            System.out.println("MINUN TOKEN: " + task.getResult());;
        });*/
    }

    public void getAllPosts(View v)
    {
        makeQuery("");

        removePosts();
        loadPosts();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> System.out.println("TOKENINI: " + task.getResult()));
    }

    public void getFollowedPosts(View v)
    {
        removePosts();

        dbReader.findDocumentByID("user_contacts", auth.getUid()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(Task<QuerySnapshot> task)
            {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);

                String queryString = "userID/";

                List<String> channelIDs = (List<String>) document.get("followed_users");
                if (channelIDs != null)
                {
                    for (String str : channelIDs)
                        queryString += str + ",";

                    queryString += "/time";

                    makeQuery(queryString);

                    loadPosts();
                }
            }
        });
    }
}