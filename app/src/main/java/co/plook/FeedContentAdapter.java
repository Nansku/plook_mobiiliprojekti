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

public class FeedContentAdapter extends RecyclerView.Adapter<FeedContentAdapter.ViewHolder>
{
    private ArrayList<Post> localDataSet;
    private Context context;

    private static ClickListener clickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private final TextView textView_caption;
        private final TextView textView_username;
        private final ImageView imageView_image;

        public ViewHolder(View view)
        {
            super(view);
            view.setOnClickListener(this);

            textView_caption = view.findViewById(R.id.post_caption);
            textView_username = view.findViewById(R.id.post_username);
            imageView_image = view.findViewById(R.id.post_image);
        }

        public TextView getTextView_caption() { return textView_caption; }

        public TextView getTextView_username() { return textView_username; }

        public ImageView getImageView_image() { return imageView_image; }

        @Override
        public void onClick(View view)
        {
            clickListener.onItemClick(getAdapterPosition(), view);
        }
    }

    public FeedContentAdapter(ArrayList<Post> dataSet, Context context)
    {
        localDataSet = dataSet;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_feed_post, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position)
    {
        Post post = localDataSet.get(position);

        viewHolder.getTextView_caption().setText(post.getCaption());
        viewHolder.getTextView_username().setText(post.getName());

        Glide.with(context).load(post.getImageUrl()).into(viewHolder.getImageView_image());
    }

    @Override
    public int getItemCount()
    {
        return localDataSet.size();
    }

    public interface ClickListener
    {
        void onItemClick(int position, View view);
    }

    public void setOnItemClickedListener(ClickListener clickedListener)
    {
        this.clickListener = clickedListener;
    }
}
