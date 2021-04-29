package co.plook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.palette.graphics.Palette;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
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
    private Context context;

    // views
    private Toolbar toolbar;
    private ScrollView scrollView;

    private RelativeLayout headerLayout;
    private RelativeLayout footerLayout;

    private ImageView imageView;
    private ViewGroup viewGroup_tags;
    private TextView textView_score;
    private ImageView imageView_thumbUp;
    private ImageView imageView_thumbDown;

    private ViewGroup tagsViewGroup;
    private ViewGroup commentsViewGroup;
    private ViewGroup controlsViewGroup;

    private TextView captionTextView;
    private TextView deletePostTextView;
    private TextView nicknameTextView;
    private TextView channelTextView;

    private TextView descriptionTextView;
    private TextView tagsTextView;
    private TextView commentsTextView;
    private TextView postCommentTextView;

    private TextView scoreTextView;

    private ImageView thumbUpImageView;
    private ImageView thumpDownImageView;
    private TextView space;

    // database stuff
    private DatabaseReader dbReader;
    private DatabaseWriter dbWriter;

    // objects
    private Post post;
    private ArrayList<String> userIDs;
    private ArrayList<Comment> allComments;

    private List<Palette.Swatch> swatches;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_post, contentGroup);

        context = getApplicationContext();

        dbReader = new DatabaseReader();
        dbWriter = new DatabaseWriter();

        toolbar = findViewById(R.id.toolbar);
        scrollView = findViewById(R.id.post_scrollView);
        headerLayout = findViewById(R.id.post_header_layout);
        footerLayout = findViewById(R.id.post_footer_layout);
        imageView = findViewById(R.id.image);

        tagsViewGroup = findViewById(R.id.post_tags_layout);
        commentsViewGroup = findViewById(R.id.post_comments_layout);
        controlsViewGroup = findViewById(R.id.post_controls_layout);

        captionTextView = findViewById(R.id.post_caption);
        deletePostTextView = findViewById(R.id.post_delete);
        nicknameTextView = findViewById(R.id.post_username);
        channelTextView = findViewById(R.id.post_channel);

        descriptionTextView = findViewById(R.id.post_description);
        tagsTextView = findViewById(R.id.post_tags_textView);
        commentsTextView = findViewById(R.id.post_comments_textView);
        postCommentTextView = findViewById(R.id.post_comment_button);

        space = findViewById(R.id.empty_space);

        // voting
        scoreTextView = findViewById(R.id.post_score);
        thumbUpImageView = findViewById(R.id.post_voteUp);
        thumpDownImageView = findViewById(R.id.post_voteDown);

        post = new Post();
        userIDs = new ArrayList<>();
        allComments = new ArrayList<>();

        initializeSwipeRefreshLayout(findViewById(R.id.post_swipeRefresh));

        //postID from feed
        Bundle extras = getIntent().getExtras();

        String postID = extras.getString("post");
        if (postID == null)
            postID = extras.getString("post_id");

        post.setPostID(postID);

        loadNavUserData();
        loadPostData();
        loadComments(false);
    }

    private void refreshPost()
    {
        //swatches.clear();

        loadPostData();
        loadComments(false);
    }

    private void loadPostData()
    {
        dbReader.findDocumentByID("posts", post.getPostID()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                makePost(task.getResult());

                if (post.getUserID().equals(auth.getUid()))
                    deletePostTextView.setVisibility(View.VISIBLE);

                // Get username
                dbReader.findDocumentByID("users", post.getUserID()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        String nickname = task.getResult().getDocuments().get(0).getString("name");

                        nicknameTextView.setText(nickname);

                        if (post.getChannelID().equals(""))
                        {
                            loadComments(false);
                            return;
                        }

                        // Get channel's name
                        dbReader.findDocumentByID("channels", post.getChannelID()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task)
                            {
                                String channelName = task.getResult().getDocuments().get(0).getString("name");
                                TextView textView_channel = findViewById(R.id.post_channel);
                                textView_channel.setText(channelName);

                                dbReader.findDocumentByID("posts/" + post.getPostID() + "/user_actions", auth.getUid()).addOnCompleteListener(task1 ->
                                {
                                    if (task1.getResult().getDocuments().size() > 0)
                                    {
                                        DocumentSnapshot doc = task1.getResult().getDocuments().get(0);
                                        post.setMyVote(doc.getLong("vote"));
                                    }

                                    displayPostDetails(post);
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private void makePost(QuerySnapshot documentSnapshots)
    {
        try
        {
            DocumentSnapshot document = documentSnapshots.getDocuments().get(0);

            post.setPostID(document.getId());
            post.setUserID(document.getString("userID"));
            post.setChannelID(document.getString("channel"));
            post.setCaption(document.getString("caption"));
            post.setDescription(document.getString("description"));
            post.setImageUrl(document.getString("url"));

            long score = document.getLong("score") == null ? 0 : document.getLong("score");
            post.setScore(score);

            // Add tag buttons.
            String[] tags = ((List<String>) document.get("tags")).toArray(new String[0]);
            post.setTags(tags);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    private void displayPostDetails(Post post)
    {
        captionTextView.setText(post.getCaption());
        descriptionTextView.setText(post.getDescription());

        // Remove tags buttons.
        tagsViewGroup.removeAllViews();

        // Add tag buttons.
        for (String tag : post.getTags())
        {
            View child = getLayoutInflater().inflate(R.layout.layout_post_tag, tagsViewGroup, false);
            tagsViewGroup.addView(child);

            TextView textView = child.findViewById(R.id.tag_text);
            textView.setText(tag);

            child.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    openTagActivity("tags/" + tag + "/time");
                }
            });
        }

        updateVotingVisuals();

        if(swatches == null)
        {
            Glide.with(context)
                    .asBitmap()
                    .load(post.getImageUrl())
                    .centerInside()
                    .into(new CustomTarget<Bitmap>(imageView.getMeasuredWidth(), imageView.getMeasuredHeight()) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            imageView.setImageBitmap(resource);
                            Palette.from(resource).generate(palette ->
                            {
                                swatches = palette.getSwatches();

                                if (!swatches.isEmpty())
                                    setColors();
                            });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        }
        else if (!swatches.isEmpty())
            setColors();

        /*// Add image.
        Glide.with(context)
                .load(post.getImageUrl())
                .into(imageView);*/
    }

    public void setColors()
    {
        int mainColor = swatches.get(0).getRgb();
        if (Color.luminance(mainColor) > 0.4)
            invertTextColor("main");

        int buttonColor = swatches.get(1).getRgb();
        if (Color.luminance(buttonColor) > 0.4)
            invertTextColor("content");

        System.out.println("Luminance: " + Color.luminance(buttonColor));

        // all the views we need to set the colors on:
        // header_layout + (post_caption, post_delete_button, post_username, post_channel)
        // footer_layout + (post_description, post_tags_textView, post_tags_linearLayout, post_comments_textView, post_comments_layout, empty_space)
        // post_controls_layout

        this.getWindow().setStatusBarColor(mainColor);
        toolbar.setBackgroundColor(mainColor);

        headerLayout.setBackgroundColor(mainColor);
        footerLayout.setBackgroundColor(mainColor);
        space.setBackgroundColor(mainColor);
        commentsViewGroup.setBackgroundColor(mainColor);
        controlsViewGroup.setBackgroundColor(buttonColor);
        findViewById(R.id.post_comment_window).setBackgroundColor(buttonColor);

        ViewGroup navigationLayout = findViewById(R.id.nav_header_parent_layout);
        navigationLayout.setBackgroundColor(mainColor);

        ImageView circleCrop = findViewById(R.id.circle_crop);
        circleCrop.getDrawable().setTint(mainColor);

        // set comments' and tags' body color
        for (int i = 0; i < commentsViewGroup.getChildCount(); i++)
            ((ViewGroup) commentsViewGroup.getChildAt(i)).setBackgroundColor(buttonColor);
        for (int i = 0; i < tagsViewGroup.getChildCount(); i++)
            ((ViewGroup) tagsViewGroup.getChildAt(i)).getChildAt(0).setBackgroundColor(buttonColor);
    }

    private void invertTextColor(String mode)
    {
        switch(mode)
        {
            case "main":
                captionTextView.setTextColor(Color.BLACK);
                nicknameTextView.setTextColor(Color.BLACK);
                channelTextView.setTextColor(Color.BLACK);
                descriptionTextView.setTextColor(Color.BLACK);
                tagsTextView.setTextColor(Color.BLACK);
                commentsTextView.setTextColor(Color.BLACK);
                break;

            case "content":
                // set comment textView colors
                for (int i = 0; i < commentsViewGroup.getChildCount(); i++)
                {
                    ViewGroup childGroup = (ViewGroup) commentsViewGroup.getChildAt(i);
                    for (int j = 0; j < childGroup.getChildCount(); j++)
                        ((TextView)childGroup.getChildAt(j)).setTextColor(Color.BLACK);
                }

                for (int i = 0; i < tagsViewGroup.getChildCount(); i++)
                {
                    ViewGroup childGroup = (ViewGroup) tagsViewGroup.getChildAt(i);
                    TextView tagText = childGroup.findViewById(R.id.tag_text);
                    tagText.setTextColor(Color.BLACK);
                }

                scoreTextView.setTextColor(Color.BLACK);
                postCommentTextView.setTextColor(Color.BLACK);

                TextView commentTitle = findViewById(R.id.post_comment_title);
                commentTitle.setTextColor(Color.BLACK);

                EditText commentEditText = findViewById(R.id.post_comment_editText);
                commentEditText.setTextColor(Color.BLACK);

                ImageView closeCommentImage = findViewById(R.id.post_comment_close);
                closeCommentImage.setColorFilter(Color.BLACK);

                ImageView sendCommentImage = findViewById(R.id.post_comment_send);
                sendCommentImage.setColorFilter(Color.BLACK);

                break;
        }
    }

    private void loadComments(boolean scrollToBottom)
    {
        removeComments();

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
                List<QuerySnapshot> querySnapshots = (List<QuerySnapshot>) (List<?>) task1.getResult();
                Map<String, String> usernamePairs = new HashMap<>();

                for (int i = 0; i < querySnapshots.size(); i++)
                {
                    List<DocumentSnapshot> docs = querySnapshots.get(i).getDocuments();

                    if (docs == null || docs.size() <= 0)
                        continue;

                    usernamePairs.put(userIDs.get(i), docs.get(0).getString("name"));
                }

                addComments(usernamePairs, commentSnapshots);
                showComments(allComments);

                if(scrollToBottom)
                    scrollView.fullScroll(View.FOCUS_DOWN);
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
    }

    private void showComments(List<Comment> comments)
    {
        //clear viewGroup before showing comments
        //content.removeAllViews();
        for (Comment comment : comments)
        {
            //create a view for comment
            View child = getLayoutInflater().inflate(R.layout.layout_comment, commentsViewGroup, false);
            commentsViewGroup.addView(child);
            //get textViews
            //ADD TEXTVIEWS FOR REPLIEDTOID AND TIMESTAMP
            TextView textView_username = child.findViewById(R.id.comment_username);
            TextView textView_commentText = child.findViewById(R.id.comment_text);
            TextView textView_timestamp = child.findViewById(R.id.comment_timestamp);
            //set texts
            textView_username.setText(comment.getUserName());
            textView_commentText.setText(comment.getText());
            textView_timestamp.setText(comment.getTimeDifference());

            textView_username.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    openProfileActivity(comment.getUserID());
                }
            });
        }

        if (swatches != null && !swatches.isEmpty())
            setColors();
    }

    private void updateVotingVisuals()
    {
        scoreTextView.setText(String.valueOf(post.getScore()));

        int colorGreen = ResourcesCompat.getColor(context.getResources(), R.color.vote_up, context.getTheme());
        int colorRed = ResourcesCompat.getColor(context.getResources(), R.color.vote_down, context.getTheme());
        int colorNeutral = ResourcesCompat.getColor(context.getResources(), R.color.vote_neutral, context.getTheme());

        if (post.getMyVote() > 0)
        {
            thumbUpImageView.setColorFilter(colorGreen, PorterDuff.Mode.MULTIPLY);
            thumpDownImageView.setColorFilter(colorNeutral, PorterDuff.Mode.MULTIPLY);
        }
        else if (post.getMyVote() < 0)
        {
            thumbUpImageView.setColorFilter(colorNeutral, PorterDuff.Mode.MULTIPLY);
            thumpDownImageView.setColorFilter(colorRed, PorterDuff.Mode.MULTIPLY);
        }
        else
        {
            thumbUpImageView.setColorFilter(colorNeutral, PorterDuff.Mode.MULTIPLY);
            thumpDownImageView.setColorFilter(colorNeutral, PorterDuff.Mode.MULTIPLY);
        }
    }

    private void removeComments()
    {
        allComments.clear();
        commentsViewGroup.removeAllViews();
    }

    public void deletePost(View v)
    {
        dbWriter.deletePost(post.getPostID());
        finish();
    }

    public void openCommentWindow(View v)
    {
        findViewById(R.id.post_comment_window).setVisibility(View.VISIBLE);
        findViewById(R.id.post_controls).setVisibility(View.GONE);

        EditText editText_comment = findViewById(R.id.post_comment_editText);
        editText_comment.setShowSoftInputOnFocus(true);
        editText_comment.requestFocus();
    }

    public void closeCommentWindow(View v)
    {
        findViewById(R.id.post_comment_window).setVisibility(View.GONE);
        findViewById(R.id.post_controls).setVisibility(View.VISIBLE);

        EditText editText_comment = findViewById(R.id.post_comment_editText);
        editText_comment.setText("");
    }

    public void sendComment(View v)
    {
        EditText editText_comment = findViewById(R.id.post_comment_editText);
        String commentText = editText_comment.getText().toString();

        commentText = commentText.trim();

        if(!commentText.equals(""))
        {
            Comment commentToAdd = dbWriter.addComment(auth.getUid(), commentText, post.getPostID());
            commentToAdd.setUserName(auth.getCurrentUser().getDisplayName());
        }

        closeCommentWindow(null);

        loadComments(true);
    }

    public void openFeedActivity(String query)
    {
        Intent intent = new Intent(context, FeedActivity.class);
        intent.putExtra("query", query);

        startActivity(intent);
    }

    public void openChannelActivity(View v)
    {
        Intent intent = new Intent(this, ChannelActivity.class);

        intent.putExtra("query", "channel/" + post.getChannelID() + "/time");
        intent.putExtra("channel_id", post.getChannelID());

        startActivity(intent);
    }

    public void openTagActivity(String query)
    {
        Intent intent = new Intent(context, TagActivity.class);
        intent.putExtra("query", query);

        startActivity(intent);
    }

    public void openProfileActivity(View v)
    {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra("user_id", post.getUserID());

        startActivity(intent);
    }

    public void openProfileActivity(String userID)
    {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra("user_id", userID);

        startActivity(intent);
    }

    public void onVoteUp(View v)
    {
        votePost(1);
    }

    public void onVoteDown(View v)
    {
        votePost(-1);
    }

    private void votePost(int vote)
    {
        if(vote == post.getMyVote())
            vote = 0;

        dbWriter.addVote(auth.getUid(), post.getPostID(), vote);

        long difference = vote - post.getMyVote();

        post.setScore(post.getScore() + difference);
        post.setMyVote(vote);

        updateVotingVisuals();
    }

    private void initializeSwipeRefreshLayout(SwipeRefreshLayout swipeContainer)
    {
        swipeContainer.setOnRefreshListener(() ->
        {
            refreshPost();
            swipeContainer.setRefreshing(false);
        });
    }
}