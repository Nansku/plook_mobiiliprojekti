package co.plook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class ProfileActivity extends ParentActivity
{
    private Context context;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private Button followButton;

    private NavigationView navigationView;
    private ViewGroup content;
    private ViewGroup contentRight;
    private GridAdapter gridAdapter;
    private GridView gridView;
    private DatabaseReader dbReader;
    private DatabaseWriter dbWriter;

    private ArrayList<Post> userPosts;
    private String userID;
    private boolean isFollowing = false;
    GridAdapter gridAdapter;
    GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        userPosts = new ArrayList<Post>();
        dbReader = new DatabaseReader();
        dbWriter = new DatabaseWriter();
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_profile, contentGroup);
        gridView = findViewById(R.id.postGrid);

        // DRAWER MENU
        toolbar = (Toolbar)findViewById(R.id.bar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        followButton = findViewById(R.id.followButton);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // INFLATER FOR NAV
        getLayoutInflater().inflate(R.layout.activity_profile, contentGroup);

        // Get userID. If none was passed, use the current user's ID instead.
        Bundle extras = getIntent().getExtras();
        if(extras != null)
            userID = extras.getString("user_id");
        else
            userID = auth.getUid();

        if (userID.equals(auth.getUid()))
            followButton.setVisibility(View.GONE);
        else
            checkIfFollowing();
            
        // GRIDVIEW
        gridView = findViewById(R.id.postGrid);

        // FIND PHOTOS FROM FIREBASE
        Task<QuerySnapshot> postTask = dbReader.findDocumentsWhereEqualTo("posts", "userID", userID).addOnCompleteListener(task ->
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

            System.out.println(userPosts.size());

            gridAdapter = new GridAdapter(this, R.layout.activity_profile_post, userPosts);

            gridView.setAdapter(gridAdapter);

            gridAdapter.notifyDataSetChanged();

            Button button = findViewById(R.id.editProfile);
            button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class );
                    startActivity(intent);
                }
            });
        });

        // ON ITEM LISTENER FOR GRID VIEW
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String postID = userPosts.get(position).getPostID();
                openPostActivity(postID);
            }

        });
    }

    private void checkIfFollowing()
    {
        dbReader.findDocumentByID("user_contacts", userID).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                List<String> followerList = (List<String>)task.getResult().getDocuments().get(0).get("followers");
                if (followerList == null)
                    isFollowing = false;
                else
                    isFollowing = followerList.contains(auth.getUid());

                followButton.setEnabled(true);
                updateFollowButton();
            }
        });
    }

    private void updateFollowButton()
    {
        String buttonString = isFollowing ? "Unfollow" : "Follow";
        followButton.setText(buttonString);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // OPEN SINGLE POST IN PostActivity
    private void openPostActivity(String postID) {

        Intent intent = new Intent(ProfileActivity.this, PostActivity.class);
        intent.putExtra("post_id", postID);
        startActivity(intent);
    }

    public void followUser(View v)
    {
        // auth.getUid() == MINUN ID
        dbWriter.updateUserContacts(auth.getUid(), "followers", userID, isFollowing);
        isFollowing = !isFollowing;
        updateFollowButton();
    }
}

