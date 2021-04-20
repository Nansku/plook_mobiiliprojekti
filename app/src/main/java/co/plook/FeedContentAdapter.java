package co.plook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class FeedContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private final int VIEW_TYPE_POST = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private final ArrayList<Post> localDataSet;
    private final Context context;

    private static ClickListener clickListener;

    public FeedContentAdapter(ArrayList<Post> dataSet, Context context)
    {
        localDataSet = dataSet;
        this.context = context;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private final TextView textView_caption;
        private final TextView textView_username;
        private final TextView textView_score;
        private final ImageView imageView_image;

        private View view_voteUp;
        private View view_voteDown;

        public PostViewHolder(View view)
        {
            super(view);

            textView_caption = view.findViewById(R.id.post_caption);
            textView_username = view.findViewById(R.id.post_username);
            textView_score = view.findViewById(R.id.post_score);
            imageView_image = view.findViewById(R.id.post_image);

            view_voteUp = view.findViewById(R.id.post_voteUp);
            view_voteDown = view.findViewById(R.id.post_voteDown);

            view.setOnClickListener(this);

            view_voteUp.setOnClickListener(v -> clickListener.onVoteClick(getAdapterPosition(), 1));
            view_voteDown.setOnClickListener(v -> clickListener.onVoteClick(getAdapterPosition(), -1));
        }

        public TextView getTextView_caption() { return textView_caption; }

        public TextView getTextView_username() { return textView_username; }

        public TextView getTextView_score() { return textView_score; }

        public ImageView getImageView_image() { return imageView_image; }

        @Override
        public void onClick(View view)
        {
            clickListener.onItemClick(getAdapterPosition(), view);
        }
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder
    {
        public LoadingViewHolder(View view)
        {
            super(view);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
    {
        if (viewType == VIEW_TYPE_POST)
        {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_feed_post, viewGroup, false);
            return new PostViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_feed_loading, viewGroup, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position)
    {
        if (viewHolder instanceof PostViewHolder)
            populateItemRows((PostViewHolder) viewHolder, position);
        else if (viewHolder instanceof LoadingViewHolder)
            showLoadingView((LoadingViewHolder) viewHolder, position);
    }

    @Override
    public int getItemCount()
    {
        return localDataSet.size();
    }

    @Override
    public int getItemViewType(int position)
    {
        return localDataSet.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_POST;
    }

    private void populateItemRows(PostViewHolder viewHolder, int position)
    {
        Post post = localDataSet.get(position);

        viewHolder.getTextView_caption().setText(post.getCaption());
        viewHolder.getTextView_username().setText(post.getUserID());
        viewHolder.getTextView_score().setText(String.valueOf(post.getScore()));

        Glide.with(context).load(post.getImageUrl()).into(viewHolder.getImageView_image());
    }

    private void showLoadingView(LoadingViewHolder viewHolder, int position)
    {
        //ProgressBar would be displayed
    }

    public interface ClickListener
    {
        void onItemClick(int position, View view);
        void onVoteClick(int position, int vote);
    }

    public void setOnItemClickedListener(ClickListener clickedListener)
    {
        this.clickListener = clickedListener;
    }
}
