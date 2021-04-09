package co.plook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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

public class PersonalActivity extends ParentActivity
{
    // Database stuff
    private DatabaseReader dbReader;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        dbReader = new DatabaseReader();

        loadChannels();
    }

    private void loadChannels()
    {
        // Followed channels
        dbReader.findDocumentByID("user_contacts", auth.getUid()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                ViewGroup content = findViewById(R.id.personal_channels_followed);

                List<String> group = (List<String>) document.get("followed_channels");
                for (String str : group)
                {
                    populateChannelsList(str, content);
                }
            }
        });

        // All channels
        Query query = dbReader.db.collection("channels");
        dbReader.findDocuments(query).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                ViewGroup content = findViewById(R.id.personal_channels_all);

                for (DocumentSnapshot document : task.getResult())
                {
                    populateChannelsList(document.getId(), content);
                }
            }
        });
    }

    private void populateChannelsList(String channelID, ViewGroup content)
    {
        View child = getLayoutInflater().inflate(R.layout.layout_personal_button, content, false);
        content.addView(child);

        TextView name = child.findViewById(R.id.channel_name);
        name.setText(channelID);

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
        Intent intent = new Intent(this, FeedActivity.class);
        intent.putExtra("query", query);

        startActivity(intent);
    }
}