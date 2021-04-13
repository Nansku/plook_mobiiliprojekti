package co.plook;

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
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getChannelData();

        getLayoutInflater().inflate(R.layout.activity_channel, contentGroup);

        RecyclerView recyclerView = findViewById(R.id.channel_recycle);
        initializeRecyclerView(recyclerView);

        loadPosts();
    }

    private void getChannelData()
    {
        String channelID = extras.getString("channel_id", "");

        dbReader.findDocumentByID("channels", channelID).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(Task<QuerySnapshot> task)
            {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);

                TextView textView_channelName = findViewById(R.id.channel_name);
                textView_channelName.setText(document.getString("name"));
            }
        });
    }
}