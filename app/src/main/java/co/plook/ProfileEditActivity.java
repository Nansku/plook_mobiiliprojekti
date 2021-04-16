package co.plook;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;

public class ProfileEditActivity extends ParentActivity
{

    private DatabaseReader dbReader;
    private ArrayList<Post> userPosts;
    GridAdapter gridAdapter;
    GridView gridView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // INTENT FROM PROFILE ACTIVITY
        getIntent();

        userPosts = new ArrayList<Post>();
        dbReader = new DatabaseReader();

        super.onCreate(savedInstanceState);

        // INFLATER FOR NAV
        getLayoutInflater().inflate(R.layout.activity_profile_edit, contentGroup);

        // GRIDVIEW
        gridView = findViewById(R.id.postGrid);

        // FIND PHOTOS FROM FIREBASE
        Task<QuerySnapshot> postTask = dbReader.findDocumentsWhereEqualTo("posts", "userID", "pztOy8uA63XqayPmUnDHBpbaETA3").addOnCompleteListener(task ->
        {   QuerySnapshot snapshot = task.getResult();

            assert snapshot != null;
            System.out.println(snapshot.getDocuments().toString());
            for (QueryDocumentSnapshot document : snapshot)
            {   Post post = new Post();
                post.setPostID(document.getId());
                post.setCaption(document.getString("caption"));
                post.setDescription(document.getString("description"));
                post.setImageUrl(document.getString("url"));

                userPosts.add(post);
            }

            // GRID ADAPTER
            gridAdapter = new GridAdapter(this, R.layout.activity_profile_post, userPosts);
            gridView.setAdapter(gridAdapter);
            gridAdapter.notifyDataSetChanged();

        });


        // MAKES GRID VIEW UNCLICKABLE AND SETS ALPHA TO MORE TRANSLUCENT
        gridView.setAlpha((float) 0.5);

        gridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

}

