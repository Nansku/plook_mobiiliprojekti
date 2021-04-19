package co.plook;

import androidx.annotation.NonNull;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class ProfileActivity extends ParentActivity
{
    private Button followButton;
    private TextView profileNameTextView;

    private GridAdapter gridAdapter;
    private GridView gridView;
    private DatabaseReader dbReader;
    private DatabaseWriter dbWriter;

    private ArrayList<Post> userPosts;
    private String userID;
    private boolean isFollowing = false;


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

        // get nickname for TextView (this should come from auth.getCurrentUser())
        dbReader.findDocumentByID("users", userID).addOnCompleteListener(task -> {
            String nickname = (String)task.getResult().getDocuments().get(0).get("name");
            profileNameTextView.setText(nickname);
        });

        // FIND PHOTOS FROM FIREBASE
        Task<QuerySnapshot> postTask = dbReader.findDocumentsWhereEqualTo("posts", "userID", userID).addOnCompleteListener(task ->
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
        String buttonString = isFollowing ? "Unfollow" : "Follow";
        followButton.setText(buttonString);
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

