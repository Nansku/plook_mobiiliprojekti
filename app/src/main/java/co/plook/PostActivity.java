package co.plook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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
    private QuerySnapshot comments;

    //objects
    private Post post;
    private ArrayList<Comment> allComments;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        context = getApplicationContext();

        dbReader = new DatabaseReader();
        dbWriter = new DatabaseWriter();

        content = findViewById(R.id.post_content);
        imageView = findViewById(R.id.image);

        post = new Post();

        //postID from feed
        Bundle extras = getIntent().getExtras();
        String postID = extras.getString("post_id");
        post.setPostID(postID);



        dbReader.findDocumentByID("posts", postID)
                .addOnCompleteListener(task -> showPost((QuerySnapshot) task.getResult()));

        dbReader.findSubcollection("comment_sections", postID, "comments")
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {

            }
        });
    }

    private void showPost(QuerySnapshot documentSnapshots)
    {
        DocumentSnapshot document = documentSnapshots.getDocuments().get(0);
        post.setPostID(document.getId());
        post.setCaption(document.getString("caption"));
        post.setDescription(document.getString("description"));
        post.setImageUrl(document.getString("url"));

        TextView textView_caption = findViewById(R.id.post_caption);
        TextView textView_description = findViewById(R.id.post_description);
        TextView textView_tags = findViewById(R.id.post_tags);

        List<String> group = (List<String>) document.get("tags");
        String tags = "tags:\n";
        for (String str : group)
        {
            tags += "{ " + str + " } ";
        }

        textView_caption.setText(post.getCaption());
        textView_description.setText(post.getDescription());
        textView_tags.setText(tags);

        Glide.with(context).load(post.getImageUrl()).into(imageView);
    }

    /*private void addComments(Map<String, String> names)
    {
        allComments = new ArrayList<>();
        for (QueryDocumentSnapshot document : comments)
        {
            String displayName = names.get(document.get("userID").toString());
            Comment comment = new Comment(displayName, document.getString("text"), document.getString("repliedToID"), (Timestamp) document.get("time"));
            allComments.add(comment);
        }
        showComments(allComments);
    }*/

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
            textView_username.setText(comment.getUserID());
            textView_commentText.setText(comment.getText());
            textView_timestamp.setText(comment.getTimeDifference());
        }
    }

    public void writeComment(View v)
    {
        //get text and userID(s) from the view and pass them into a Comment object
        //for now lets use a ph (placeholder) comment
        Timestamp timeNow = Timestamp.now();
        String commentText = "Kello on: " + timeNow.toDate().toString();

        Comment commentToAdd = dbWriter.addComment("HkiNfJx7Vaaok6L9wo6x34D3Ol03", commentText, post.getPostID());

        //hmm does 'allComments' have to be global or do we remove the parameter from 'showComment'??
        allComments.add(commentToAdd);
        showComments(allComments);
    }

}