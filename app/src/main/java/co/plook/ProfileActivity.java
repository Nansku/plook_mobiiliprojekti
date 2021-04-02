package co.plook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.GridView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class ProfileActivity extends AppCompatActivity
{
    private DatabaseReader dbReader;
    private ArrayList<Post> userPosts;
    private Context context;
    private ViewGroup content;
    private ViewGroup contentRight;

    GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {   userPosts = new ArrayList<Post>();

        dbReader = new DatabaseReader();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        gridView = findViewById(R.id.postGrid);

        Task<QuerySnapshot> postTask = dbReader.findDocuments("posts", "userID", "HkiNfJx7Vaaok6L9wo6x34D3Ol03").addOnCompleteListener(task ->
        {   QuerySnapshot snapshot = task.getResult();
            System.out.println(snapshot.getDocuments().toString());
            for (QueryDocumentSnapshot document : snapshot)
            {
                Post post = new Post();

                post.setPostID(document.getId());
                post.setCaption(document.getString("caption"));
                post.setDescription(document.getString("description"));
                post.setImageUrl(document.getString("url"));

                userPosts.add(post);
            }
            System.out.println(userPosts.size());
            GridAdapter gridAdapter = new GridAdapter(this, R.layout.activity_profile_post, userPosts);
            gridView.setAdapter(gridAdapter);
            gridAdapter.notifyDataSetChanged();
        });





        /*gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ProfileActivity.this, Post.class);
                intent.putExtra("image", images[position]);
                startActivity(intent);
            }
        });*/
    }
}

