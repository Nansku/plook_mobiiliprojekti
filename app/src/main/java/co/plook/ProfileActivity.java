package co.plook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;
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
    private TextView profileNameTextView;
    private TextView profileBioTextView;
    private TextView profileLocationTextView;
    private Button followButton;
    private Button unfollowButton;
    private Button editProfileButton;
    private GridAdapter gridAdapter;
    private GridView gridView;
    private DatabaseReader dbReader;
    private DatabaseWriter dbWriter;
    private String nickname;
    private String location;
    private String bio;



    private ArrayList<Post> userPosts;
    private String userID;
    private boolean isFollowing = false;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        currentActivity = this;
        navigationView.getMenu().getItem(3).setChecked(true);

        userPosts = new ArrayList<>();
        dbReader = new DatabaseReader();
        dbWriter = new DatabaseWriter();

        // INFLATER FOR NAV
        getLayoutInflater().inflate(R.layout.activity_profile, contentGroup);

        gridView = findViewById(R.id.postGrid);

        followButton = findViewById(R.id.followButton);
        profileNameTextView = findViewById(R.id.usernameTextview);
        profileBioTextView = findViewById(R.id.bioTxt);
        profileLocationTextView = findViewById(R.id.country);

        followButton = findViewById(R.id.followButton);
        unfollowButton = findViewById(R.id.unfollowButton);
        editProfileButton = findViewById(R.id.editProfile);
        editProfileButton.setVisibility(View.GONE);
        unfollowButton.setVisibility(View.GONE);
        // Get userID. If none was passed, use the current user's ID instead.
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            userID = extras.getString("user_id");
        } else {
            userID = auth.getUid();
        }


        if (userID.equals(auth.getUid())) {
            followButton.setVisibility(View.GONE);
            unfollowButton.setVisibility(View.GONE);
            editProfileButton.setVisibility(View.VISIBLE);

        } else {
            checkIfFollowing();
        }

        // GRIDVIEW
        gridView = (ExpandableHeightGridView) findViewById(R.id.postGrid);
        // HACK TO EXPAND GRIDVIEW TO BOTTOM
        ((ExpandableHeightGridView) gridView).setExpanded(true);




        Query q = dbReader.db.collection("posts").whereEqualTo("userID", userID).orderBy("time", Query.Direction.DESCENDING);

        // FIND PHOTOS FROM FIREBASE
        dbReader.findDocuments(q).addOnCompleteListener(task ->
        {   QuerySnapshot snapshot = task.getResult();

            assert snapshot != null;
            for (QueryDocumentSnapshot document : snapshot)
            {   Post post = new Post();
                post.setPostID(document.getId());
                post.setCaption(document.getString("caption"));
                post.setDescription(document.getString("description"));
                post.setImageUrl(document.getString("url"));

                userPosts.add(post);
            }

            gridAdapter = new GridAdapter(this, R.layout.activity_profile_post, userPosts);

            gridView.setAdapter(gridAdapter);

            gridAdapter.notifyDataSetChanged();

            editProfileButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class );

                    intent.putExtra("name", nickname);
                    intent.putExtra("location", location);
                    intent.putExtra("bio", bio);

                    startActivity(intent);

                }


            });
        });

        // MAKES GRID NOT SCROLLABLE
        /*gridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return event.getAction() == MotionEvent.ACTION_MOVE;
            }
        });*/

        // ON ITEM LISTENER FOR GRID VIEW
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String postID = userPosts.get(position).getPostID();
                openPostActivity(postID);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // get nickname for TextView (this should come from auth.getCurrentUser())
        dbReader.findDocumentByID("users", userID).addOnCompleteListener(task -> {
            nickname = (String)task.getResult().getDocuments().get(0).get("name");
            location = (String)task.getResult().getDocuments().get(0).get("location");
            bio = (String)task.getResult().getDocuments().get(0).get("bio");
            profileNameTextView.setText(nickname);
            profileBioTextView.setText(bio);
            profileLocationTextView.setText(location);
        });

        System.out.println("Onresume kutsuttu");
    }

    private void checkIfFollowing()
    {
        dbReader.findDocumentByID("user_contacts", auth.getUid()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                List<String> followedList = (List<String>)task.getResult().getDocuments().get(0).get("followed_users");
                if (followedList == null)
                    isFollowing = false;
                else
                    isFollowing = followedList.contains(userID);

                followButton.setEnabled(true);
                updateFollowButton();
            }
        });
    }

    private void updateFollowButton()
    {
        if (isFollowing) {
            followButton.setVisibility(View.GONE);
            unfollowButton.setVisibility(View.VISIBLE);
        } else {
            followButton.setVisibility(View.VISIBLE);
            unfollowButton.setVisibility(View.GONE);
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
        dbWriter.updateUserContacts(auth.getUid(), "followed_users", userID, isFollowing);
        isFollowing = !isFollowing;
        updateFollowButton();
    }

}

