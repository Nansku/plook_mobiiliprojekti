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
    private View filtersLayout;
    private String[] filterSettings = {"all/", "/", "time"};

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

        // token debug
        /*FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            System.out.println("MINUN TOKEN: " + task.getResult());;
        });*/
    }

    public void setFilterCriteriaAll(View v)
    {
        filterSettings[0] = "all/";
        filterSettings[1] = "/";

        refreshContent();
    }

    public void setFilterCriteriaFollowing(View v)
    {
        dbReader.findDocumentByID("user_contacts", auth.getUid()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(Task<QuerySnapshot> task)
            {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);

                filterSettings[0] = "userID/";
                filterSettings[1] = "";

                List<String> channelIDs = (List<String>) document.get("followed_users");
                if (channelIDs != null)
                {
                    for (String str : channelIDs)
                        filterSettings[1] += str + ",";

                }

                filterSettings[1] += "/";

                refreshContent();
            }
        });
    }

    public void setFilterSortingTime(View v)
    {
        filterSettings[2] = "time";

        refreshContent();

        toggleFiltersMenu(null);
    }

    public void setFilterSortingVotes(View v)
    {
        filterSettings[2] = "time";

        refreshContent();

        toggleFiltersMenu(null);
    }

    private void refreshContent()
    {
        StringBuilder queryString = new StringBuilder();
        for(String str : filterSettings)
            queryString.append(str);

        makeQuery(queryString.toString());

        removePosts();
        loadPosts();
    }

    public void toggleFiltersMenu(View v)
    {
        if (filtersLayout.getVisibility() == View.VISIBLE)
            filtersLayout.setVisibility(View.GONE);
        else
            filtersLayout.setVisibility(View.VISIBLE);
    }
}