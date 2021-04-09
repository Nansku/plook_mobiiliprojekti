package co.plook;

import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.media.Image;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class GridAdapter extends ArrayAdapter<Post> {
    ArrayList userPosts;
    Context context;
    public GridAdapter(Context context, int textViewResourceId, ArrayList objects) {
        super(context, textViewResourceId, objects);
        userPosts = objects;
        this.context = context ;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.activity_profile_post, null);
        Post post = getItem(position);
        ImageView imageView = view.findViewById(R.id.image);
        Glide.with(context).load(post.getImageUrl()).into(imageView);

        //imageView.setImageResource(Integer.parseInt(post.getImageUrl()));
        return view;
    }

    /*public GridAdapter(@NonNull Context context, ArrayList<Post> postArrayList) {
        super(context, 0, postArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull  ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.activity_profile_post, parent, false) ;
        }

        Post post = getItem(position);

        ImageView imageView = listItemView.findViewById(R.id.image);

        Picasso.get().load(Post.getImageUrl()).into(imageView);

        listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Item clicked", Toast.LENGTH_SHORT).show();
            }
        });
        return listItemView;
    }*/
    /*Context context;
    private final int[] images ;
    LayoutInflater inflater ;

    public GridAdapter(Context context, int[] images) {
        this.context = context;
        this.images = images;
        inflater = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public Object getItem(int i) {
        return null ;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.activity_profile_post, null);
        ImageView image = (ImageView) view.findViewById(R.id.image);
        image.setImageResource(images[i]);
        return view;

    }*/

}
