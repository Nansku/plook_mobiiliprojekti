package co.plook;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
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

        loadPosts();
    }

    private void getChannelData()
    {
        channelID = extras.getString("channel_id", "");

        dbReader.findDocumentByID("channels", channelID).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(Task<QuerySnapshot> task)
            {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);

                TextView textView_channelName = findViewById(R.id.channel_name);
                textView_channelName.setText(document.getString("name"));

                List<String> followerIDs = (List<String>) document.get("followers");
                int count = followerIDs == null ? 0 : followerIDs.size();

                TextView textView_channelFollowers = findViewById(R.id.channel_follower_count);
                textView_channelFollowers.setText(count + " FOLLOWERS");
            }
        });

        dbReader.findDocumentByID("user_contacts", auth.getUid()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);

                List<String> followedChannels = (List<String>) document.get("followed_channels");
                isFollowing = followedChannels.contains(channelID);
            }
        });
    }

    public void toggleFollow(View v)
    {
        dbWriter.updateUserContacts(auth.getUid(), "followed_channels", channelID, isFollowing);
        isFollowing = !isFollowing;
    }
}