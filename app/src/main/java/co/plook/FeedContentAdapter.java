package co.plook;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class FeedContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private final int VIEW_TYPE_POST = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private final ArrayList<Post> localDataSet;
    private final Context context;

    private ClickListener clickListener;

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
        private final TextView textView_commentCount;

        private final ImageView imageView_voteUp;
        private final ImageView imageView_voteDown;

        private ClickListener clickListener;

        public PostViewHolder(View view, ClickListener clickListener)
        {
            super(view);

            textView_caption = view.findViewById(R.id.post_caption);
            textView_username = view.findViewById(R.id.post_username);
            textView_score = view.findViewById(R.id.post_score);
            imageView_image = view.findViewById(R.id.post_image);
            textView_commentCount = view.findViewById(R.id.post_comment_count);

            imageView_voteUp = view.findViewById(R.id.post_voteUp);
            imageView_voteDown = view.findViewById(R.id.post_voteDown);

            this.clickListener = clickListener;

            view.setOnClickListener(this);
            imageView_voteUp.setOnClickListener(v -> clickListener.onVoteClick(getAdapterPosition(), 1));
            imageView_voteDown.setOnClickListener(v -> clickListener.onVoteClick(getAdapterPosition(), -1));
        }

        public TextView getTextView_caption() { return textView_caption; }

        public TextView getTextView_username() { return textView_username; }

        public TextView getTextView_score() { return textView_score; }

        public TextView getTextView_commentCount() { return textView_commentCount; }

        public ImageView getImageView_image() { return imageView_image; }

        public ImageView getImageView_voteUp() { return imageView_voteUp; }

        public ImageView getImageView_voteDown() { return imageView_voteDown; }

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
            return new PostViewHolder(view, clickListener);
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
        if (post.getCommentCount() >= 1)
            viewHolder.getTextView_commentCount().setText(post.getCommentCount() + " comments");
        else
            viewHolder.getTextView_commentCount().setText("");

        Glide.with(context).load(post.getImageUrl()).into(viewHolder.getImageView_image());

        int colorGreen = ResourcesCompat.getColor(context.getResources(), R.color.vote_up, context.getTheme());
        int colorRed = ResourcesCompat.getColor(context.getResources(), R.color.vote_down, context.getTheme());
        int colorNeutral = ResourcesCompat.getColor(context.getResources(), R.color.vote_neutral, context.getTheme());

        if (post.getMyVote() > 0)
        {
            viewHolder.getImageView_voteUp().setColorFilter(colorGreen, PorterDuff.Mode.MULTIPLY);
            viewHolder.getImageView_voteDown().setColorFilter(colorNeutral, PorterDuff.Mode.MULTIPLY);
        }
        else if (post.getMyVote() < 0)
        {
            viewHolder.getImageView_voteUp().setColorFilter(colorNeutral, PorterDuff.Mode.MULTIPLY);
            viewHolder.getImageView_voteDown().setColorFilter(colorRed, PorterDuff.Mode.MULTIPLY);
        }
        else
        {
            viewHolder.getImageView_voteUp().setColorFilter(colorNeutral, PorterDuff.Mode.MULTIPLY);
            viewHolder.getImageView_voteDown().setColorFilter(colorNeutral, PorterDuff.Mode.MULTIPLY);
        }
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
