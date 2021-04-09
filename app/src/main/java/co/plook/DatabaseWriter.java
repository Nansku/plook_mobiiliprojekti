package co.plook;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class DatabaseWriter
{

    FirebaseFirestore db;


    public DatabaseWriter()
    {
        db = FirebaseFirestore.getInstance();
    }

    //User gets an automatically generated userID.
    //Maybe the user is also asked for a nickname that is displayed on screen
    //and a 'real' name (firstname)
    public void addUser(String userID, String nickname, String email)
    {
        Map<String, Object> user = new HashMap<>();
        user.put("id", userID);
        user.put("name", nickname);
        user.put("email", email);

        addToCollectionWithName("users", user, userID);
    }

    public boolean addPost(String userID, String caption, String channel, String description, String[] tags, String url)
    {
        //First upload the picture to storage and return the imageurl
        //Then add a post document

        Map<String, Object> post = new HashMap<>();
        Timestamp timeNow = Timestamp.now();

        post.put("caption", caption);
        post.put("channel", channel);
        post.put("description", description);
        post.put("tags", tags);
        post.put("time", timeNow);
        post.put("url", url);
        post.put("userID", userID);

        addToCollection("posts", post);

        return true;
    }

    public Comment addComment(String userID, String text, String postID)
    {
        Map<String, Object> comment = new HashMap<>();
        Timestamp timeNow = Timestamp.now();

        comment.put("userID", userID);
        comment.put("text", text);
        comment.put("time", timeNow);

        addToSubcollection("comment_sections", postID, "comments", comment);

        return new Comment(userID, "", text, "vastattu tälle", timeNow);
    }

    public boolean addVote(String userID, String postID, boolean upOrDown)
    {
        Map<String, Object> vote = new HashMap<>();

        vote.put("userID", userID);
        vote.put("postID", postID);
        vote.put("vote", upOrDown);

        addToCollectionWithName("votes", vote, userID + "_" + postID);
        //After this we could use cloud functions to count increment post scores
        return true;
    }

    public void addChannel(String ownerUserID, String name, String bio)
    {

    }




    private void addToCollection(String collectionPath, Map document)
    {
        // Add a new document with a generated ID
        db.collection(collectionPath)
                .add(document)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>()
                {
                    @Override
                    public void onSuccess(DocumentReference documentReference)
                    {
                        System.out.println("DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        System.out.println("Error adding document" + e.getMessage());
                    }
                });
    }

    private void addToCollectionWithName(String collectionPath, Map document, String docName)
    {
        // Add a new document with a specified document name
        db.collection(collectionPath)
                .document(docName)
                .set(document)
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        System.out.println("Document add successful! -> " + collectionPath);
                    }
                });
    }

    private void addToSubcollection(String collectionPath, String documentID, String subcollectionPath, Map document)
    {
        // Add a new document with a generated ID
        db.collection(collectionPath)
                .document(documentID)
                .collection(subcollectionPath)
                .add(document)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>()
                {
                    @Override
                    public void onSuccess(DocumentReference documentReference)
                    {
                        System.out.println("DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        System.out.println("Error adding document" + e.getMessage());
                    }
                });
    }
}
