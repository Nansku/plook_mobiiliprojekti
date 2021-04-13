package co.plook;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ChannelActivity extends PostDisplayActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_channel, contentGroup);

        RecyclerView recyclerView = findViewById(R.id.channel_recycle);
        initializeRecyclerView(recyclerView);

        loadPosts();
    }

    public void openChannelBrowse(View v)
    {
        Intent intent = new Intent(this, ChannelBrowseActivity.class);

        startActivity(intent);
    }
}