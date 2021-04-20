package co.plook;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class ChannelBrowseActivity extends ParentActivity
{

    // Database stuff
    private DatabaseReader dbReader;

    private List<String> followed_channels;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_channel_browse, contentGroup);

        dbReader = new DatabaseReader();

        loadChannels();
    }

    private void loadChannels()
    {
        // Get followed channels
        dbReader.findDocumentByID("user_contacts", auth.getUid()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                ViewGroup content = findViewById(R.id.personal_channels_followed);

                followed_channels = (List<String>) document.get("followed_channels");

                String[] channelIDs = followed_channels.toArray(new String[0]);

                if (channelIDs.length > 0)
                {
                    // Get followed channels
                    dbReader.findDocumentsWhereIn("channels", "__name__", channelIDs).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            for (DocumentSnapshot document : task.getResult()) {
                                populateChannelsList(document, content);
                            }
                        }
                    });
                }

                // Get all channels
                Query query = dbReader.db.collection("channels");
                dbReader.findDocuments(query).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        ViewGroup content = findViewById(R.id.personal_channels_all);

                        for (DocumentSnapshot document : task.getResult())
                        {
                            if (followed_channels == null || !followed_channels.contains(document.getId()))
                                populateChannelsList(document, content);
                        }
                    }
                });
            }
        });
    }

    private void populateChannelsList(DocumentSnapshot channelData, ViewGroup content)
    {
        View child = getLayoutInflater().inflate(R.layout.layout_channel_browser_button, content, false);
        content.addView(child);

        // Channel name
        TextView textView_name = child.findViewById(R.id.channel_name);
        textView_name.setText(channelData.getString("name"));

        // Channel follower count
        List<String> followerIDs = (List<String>) channelData.get("followers");
        int followerCount = followerIDs == null ? 0 : followerIDs.size();
        TextView textView_channelFollowers = child.findViewById(R.id.channel_follower_count);
        textView_channelFollowers.setText(followerCount + " FOLLOWERS");

        onButtonClicked(child, channelData.getId());
    }

    private void onButtonClicked(View v, String channelID)
    {
        v.setOnClickListener(v1 -> openFeedActivity(channelID));
    }

    void openFeedActivity(String channelID)
    {
        Intent intent = new Intent(this, ChannelActivity.class);

        intent.putExtra("query", "channel/" + channelID + "/time");
        intent.putExtra("channel_id", channelID);

        startActivity(intent);
    }
}