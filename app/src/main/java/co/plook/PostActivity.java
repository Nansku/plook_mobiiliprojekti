package co.plook;

import androidx.annotation.NonNull;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostActivity extends ParentActivity
{
    //views
    private Context context;
    private ViewGroup content;
    private ImageView imageView;

    //database stuff
    private DatabaseReader dbReader;
    private DatabaseWriter dbWriter;

    //objects
    private Post post;
    private ArrayList<String> userIDs;
    private ArrayList<Comment> allComments;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_post, contentGroup);

        context = getApplicationContext();

        dbReader = new DatabaseReader();
        dbWriter = new DatabaseWriter();

        content = findViewById(R.id.post_content);
        imageView = findViewById(R.id.image);

        post = new Post();
        userIDs = new ArrayList<>();
        allComments = new ArrayList<>();

        //postID from feed
        Bundle extras = getIntent().getExtras();
        String postID = extras.getString("post_id");
        post.setPostID(postID);

        dbReader.findDocumentByID("posts", postID).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                makePost(task.getResult());

                dbReader.findDocumentByID("users", post.getUserID()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        String username = task.getResult().getDocuments().get(0).getString("name");
                        TextView textView_username = findViewById(R.id.post_username);
                        textView_username.setText(username);

                        displayPostDetails(post);
                    }
                });
            }
        });

        loadComments();
    }

    private void makePost(QuerySnapshot documentSnapshots)
    {
        DocumentSnapshot document = documentSnapshots.getDocuments().get(0);

        post.setPostID(document.getId());
        post.setUserID(document.getString("userID"));
        post.setCaption(document.getString("caption"));
        post.setDescription(document.getString("description"));
        post.setImageUrl(document.getString("url"));

        // Add tag buttons.
        String[] tags = ((List<String>) document.get("tags")).toArray(new String[0]);
        post.setTags(tags);
    }

    private void displayPostDetails(Post post)
    {
        TextView textView_caption = findViewById(R.id.post_caption);
        TextView textView_description = findViewById(R.id.post_description);
        ViewGroup viewGroup_tags = findViewById(R.id.post_tags);

        textView_caption.setText(post.getCaption());
        textView_description.setText(post.getDescription());

        // Add tag buttons.
        for (String tag : post.getTags())
        {
            View child = getLayoutInflater().inflate(R.layout.layout_post_tag, content, false);
            viewGroup_tags.addView(child);

            TextView textView = child.findViewById(R.id.tag_text);
            textView.setText(tag);

            child.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    openFeedActivity("tags/" + tag + "/time");
                }
            });
        }

        // Add image.
        Glide.with(context).load(post.getImageUrl()).into(imageView);
    }

    private void loadComments()
    {
        dbReader.findSubcollection("comment_sections", post.getPostID(), "comments").addOnCompleteListener(task ->
        {
            QuerySnapshot commentSnapshots = task.getResult();

            //loop through userIDs and get a list of unique names
            for (DocumentSnapshot snapshot : commentSnapshots.getDocuments())
            {
                String userID = snapshot.getString("userID");
                if (!userIDs.contains(userID))
                    userIDs.add(userID);
            }

            dbReader.requestNicknames(userIDs).addOnCompleteListener(task1 ->
            {
                List<QuerySnapshot> querySnapshots = (List<QuerySnapshot>) (List<?>) task1.getResult(); //  @Iikka what even is this??
                Map<String, String> usernamePairs = new HashMap<>();

                for (int i = 0; i < querySnapshots.size(); i++)
                {
                    List<DocumentSnapshot> docs = querySnapshots.get(i).getDocuments();

                    if (docs == null || docs.size() <= 0)
                        continue;

                    usernamePairs.put(userIDs.get(i), docs.get(0).getString("name"));
                }

                addComments(usernamePairs, commentSnapshots);
            });
        });
    }

    private void addComments(Map<String, String> names, QuerySnapshot snapshot)
    {
        for (QueryDocumentSnapshot document : snapshot)
        {
            String displayName = names.get(document.getString("userID"));
            Comment comment = new Comment(document.getString("userID"), displayName, document.getString("text"), document.getString("repliedToID"), (Timestamp) document.get("time"));
            allComments.add(comment);
        }

        showComments(allComments);
    }

    private void showComments(List<Comment> comments)
    {
        //clear viewGroup before showing comments
        //content.removeAllViews();
        for (Comment comment : comments)
        {
            //create a view for comment
            View child = getLayoutInflater().inflate(R.layout.layout_comment, content, false);
            content.addView(child);
            //get textViews
            //ADD TEXTVIEWS FOR REPLIEDTOID AND TIMESTAMP
            TextView textView_username = child.findViewById(R.id.comment_username);
            TextView textView_commentText = child.findViewById(R.id.comment_text);
            TextView textView_timestamp = child.findViewById(R.id.comment_timestamp);
            //set texts
            textView_username.setText(comment.getUserName());
            textView_commentText.setText(comment.getText());
            textView_timestamp.setText(comment.getTimeDifference());
        }
    }

    private void removeComments()
    {
        content.removeAllViews();
    }

    public void writeComment(View v)
    {
        //get text and userID(s) from the view and pass them into a Comment object
        //for now lets use a ph (placeholder) comment
        Timestamp timeNow = Timestamp.now();
        String commentText = "Kello on: " + timeNow.toDate().toString();

        Comment commentToAdd = dbWriter.addComment(auth.getUid(), commentText, post.getPostID());
        commentToAdd.setUserName(auth.getCurrentUser().getDisplayName());

        //hmm does 'allComments' have to be global or do we remove the parameter from 'showComment'??
        allComments.add(commentToAdd);

        removeComments();
        showComments(allComments);
    }

    public void openFeedActivity(String query)
    {
        Intent intent = new Intent(context, FeedBaseActivity.class);
        intent.putExtra("query", query);

        startActivity(intent);
    }

    public void openProfileActivity(View v)
    {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra("user_id", post.getUserID());

        startActivity(intent);
    }
}