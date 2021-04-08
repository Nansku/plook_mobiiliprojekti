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

        return view;
    }



}
