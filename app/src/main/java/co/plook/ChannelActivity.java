package co.plook;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

                TextView textView_channelName = findViewById(R.id.channel_name);
                textView_channelName.setText(document.getString("name"));

                List<String> followerIDs = (List<String>) document.get("followers");
                followerCount = followerIDs == null ? 0 : followerIDs.size();

                TextView textView_channelFollowers = findViewById(R.id.channel_follower_count);
                textView_channelFollowers.setText(followerCount + " FOLLOWERS");

                // Check if already following this channel.
                isFollowing = followerIDs.contains(auth.getUid());
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
}