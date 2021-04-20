package co.plook;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class ChannelActivity extends PostDisplayActivity
{
    private DatabaseWriter dbWriter;

    private String channelID;
    private int followerCount;
    private boolean isFollowing;

    private View filtersLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getChannelData();

        getLayoutInflater().inflate(R.layout.activity_channel, contentGroup);

        dbWriter = new DatabaseWriter();

        RecyclerView recyclerView = findViewById(R.id.channel_recycle);
        initializeRecyclerView(recyclerView);

        SwipeRefreshLayout swipeContainer = findViewById(R.id.channel_swipeRefresh);
        initializeSwipeRefreshLayout(swipeContainer);

        filtersLayout = findViewById(R.id.channel_filters);

        loadPosts();
    }

    private void getChannelData()
    {
        channelID = extras.getString("channel_id", "");

        // Get channel name and follower count.
        dbReader.findDocumentByID("channels", channelID).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(Task<QuerySnapshot> task)
            {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);

                // Channel name
                TextView textView_channelName = findViewById(R.id.channel_name);
                textView_channelName.setText(document.getString("name"));

                // Channel follower count
                List<String> followerIDs = (List<String>) document.get("followers");
                followerCount = followerIDs == null ? 0 : followerIDs.size();
                TextView textView_channelFollowers = findViewById(R.id.channel_follower_count);
                textView_channelFollowers.setText(followerCount + " FOLLOWERS");

                // Check if already following this channel.
                isFollowing = followerIDs == null ? false : followerIDs.contains(auth.getUid());
                System.out.println("MINUN FOLLOW STATE: " + isFollowing);
            }
        });
    }

    public void toggleFollow(View v)
    {
        dbWriter.updateUserContacts(auth.getUid(), "followed_channels", channelID, isFollowing);

        isFollowing = !isFollowing;
        followerCount += isFollowing ? 1 : -1;

        TextView textView_channelFollowers = findViewById(R.id.channel_follower_count);
        textView_channelFollowers.setText(followerCount + " FOLLOWERS");
    }

    public void setFilterSortingTime(View v)
    {
        querySettings[2] = "time";

        refreshContent();

        toggleFiltersMenu(null);
    }

    public void setFilterSortingVotes(View v)
    {
        querySettings[2] = "score";

        refreshContent();

        toggleFiltersMenu(null);
    }

    private void refreshContent()
    {
        makeQuery(querySettings[0], querySettings[1], querySettings[2]);

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