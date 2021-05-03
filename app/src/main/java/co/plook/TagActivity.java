package co.plook;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class TagActivity extends PostDisplayActivity
{
    private View filtersLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.activity_tag, contentGroup);

        RecyclerView recyclerView = findViewById(R.id.tag_recycle);
        initializeRecyclerView(recyclerView);

        SwipeRefreshLayout swipeContainer = findViewById(R.id.tag_swipeRefresh);
        initializeSwipeRefreshLayout(swipeContainer);

        filtersLayout = findViewById(R.id.tag_filters);

        TextView textView_tagName = findViewById(R.id.tag_name);
        String taggedWithString = getResources().getString(R.string.feed_tag_postsTaggedWith) + " " + querySettings[1];

        textView_tagName.setText(taggedWithString);

        loadPosts();
    }

    public void setFilterSortingTime(View v)
    {
        querySettings[2] = "time";

        refreshPosts();

        toggleFiltersMenu(null);
    }

    public void setFilterSortingVotes(View v)
    {
        querySettings[2] = "score";

        refreshPosts();

        toggleFiltersMenu(null);
    }

    public void toggleFiltersMenu(View v)
    {
        if (filtersLayout.getVisibility() == View.VISIBLE)
            filtersLayout.setVisibility(View.GONE);
        else
            filtersLayout.setVisibility(View.VISIBLE);
    }
}