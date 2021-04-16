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

                if (followed_channels == null)
                    return;

                String[] channelIDs = followed_channels.toArray(new String[0]);

                if (channelIDs.length <= 0)
                    return;

                // Get channel names
                dbReader.findDocumentsWhereIn("channels", "__name__", channelIDs).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        for (DocumentSnapshot document : task.getResult())
                        {
                            populateChannelsList(document.getId(), document.getString("name"), content);
                        }
                    }
                });

                // Get all channels
                Query query = dbReader.db.collection("channels");
                dbReader.findDocuments(query).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        if (followed_channels == null)
                            return;

                        ViewGroup content = findViewById(R.id.personal_channels_all);

                        for (DocumentSnapshot document : task.getResult())
                        {
                            if (!followed_channels.contains(document.getId()))
                                populateChannelsList(document.getId(), document.getString("name"), content);
                        }
                    }
                });
            }
        });
    }

    private void populateChannelsList(String channelID, String channelName, ViewGroup content)
    {
        View child = getLayoutInflater().inflate(R.layout.layout_personal_button, content, false);
        content.addView(child);

        TextView name = child.findViewById(R.id.channel_name);
        name.setText(channelName);

        onButtonClicked(child, channelID);
    }

    private void onButtonClicked(View v, String channelID)
    {
        v.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openFeedActivity(channelID);
            }
        });
    }

    void openFeedActivity(String channelID)
    {
        Intent intent = new Intent(this, ChannelActivity.class);

        intent.putExtra("query", "channel/" + channelID + "/time");
        intent.putExtra("channel_id", channelID);

        startActivity(intent);
    }
}