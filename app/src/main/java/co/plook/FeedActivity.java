package co.plook;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class FeedActivity extends PostDisplayActivity
{
    private View filtersLayout;

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

        filtersLayout = findViewById(R.id.feed_filters);

        loadPosts();
    }

    public void setFilterCriteriaAll(View v)
    {
        querySettings[0] = "all";
        querySettings[1] = "";

        refreshPosts();
    }

    public void setFilterCriteriaFollowing(View v)
    {
        // Get followed users and use their IDs as the criteria.
        dbReader.findDocumentByID("user_contacts", auth.getUid()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(Task<QuerySnapshot> task)
            {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);

                querySettings[0] = "userID";
                querySettings[1] = "";

                List<String> channelIDs = (List<String>) document.get("followed_users");
                if (channelIDs != null)
                {
                    for (String id : channelIDs)
                        querySettings[1] += id + ",";
                }

                refreshPosts();
            }
        });
    }

    public void setFilterSortingTime(View v)
    {
        querySettings[2] = "time";

        refreshPosts();

        toggleFiltersMenu(null);
    }

    public void setFilterSortingVotes(View v)
    {
        querySettings[2] = "score";

        refreshPosts();

        toggleFiltersMenu(null);
    }

    public void toggleFiltersMenu(View v)
    {
        if (filtersLayout.getVisibility() == View.VISIBLE)
            filtersLayout.setVisibility(View.GONE);
        else
            filtersLayout.setVisibility(View.VISIBLE);
    }
}