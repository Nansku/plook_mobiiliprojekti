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

                List<String> group = (List<String>) document.get("followed_channels");

                if(group == null)
                    return;

                String[] arrayOfIDs = group.toArray(new String[0]);

                // Get channel names
                dbReader.findDocumentsWhereIn("channels", "__name__", arrayOfIDs).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
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
            }
        });

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
                    populateChannelsList(document.getId(), document.getString("name"), content);
                }
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
                openFeedActivity("channel/" + channelID + "/time");
            }
        });
    }

    void openFeedActivity(String query)
    {
        Intent intent = new Intent(this, ChannelActivity.class);
        intent.putExtra("query", query);

        startActivity(intent);
    }
}