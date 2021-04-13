package co.plook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class FeedActivity extends PostDisplayActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_feed, contentGroup);

        RecyclerView recyclerView = findViewById(R.id.feed_recycle);
        initializeRecyclerView(recyclerView);

        loadPosts();
    }

    public void getFollowedPosts(View v)
    {
        removePosts();

        dbReader.findDocumentByID("user_contacts", auth.getUid()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                QuerySnapshot snapshots = task.getResult();

                DocumentSnapshot document = snapshots.getDocuments().get(0);

                String queryString = "channel/";

                List<String> group = (List<String>) document.get("followed_channels");
                if (group != null)
                {
                    for (String str : group)
                        queryString += str + ",";

                    queryString += "/time";

                    makeQuery(queryString);

                    loadPosts();
                }
            }
        });
    }

    public void openChannelBrowse(View v)
    {
        Intent intent = new Intent(this, ChannelBrowseActivity.class);

        startActivity(intent);
    }
}