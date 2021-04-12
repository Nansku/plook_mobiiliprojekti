package co.plook;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DatabaseWriter
{

    FirebaseFirestore db;
    FirebaseAuth auth;

    public DatabaseWriter()
    {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    //User gets an automatically generated userID.
    //Maybe the user is also asked for a nickname that is displayed on screen
    //and a 'real' name (firstname)
    public void addUser(String userID, String nickname, String email, String token)
    {
        Map<String, Object> user = new HashMap<>();
        user.put("id", userID);
        user.put("name", nickname);
        user.put("email", email);
        user.put("token", token);

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

    public void updateUser(String collectionPath, String userID,  HashMap<String, Object> updatedUserMap)
    {
        db.collection(collectionPath)
                .document(userID)
                .update(updatedUserMap)
                .addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("UpdateUserLog", userID + " successfully updated!");
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Log.d("UpdateUserLog", "ERROR: " + e.getMessage());
            }
        });
    }

    public void updateUserFollows(String userID, String field, String followID, boolean remove)
    {
        FieldValue fieldValue = remove ? FieldValue.arrayRemove(followID) : FieldValue.arrayUnion(followID);
        db.collection("user_contacts")
                .document(userID)
                .update(field, fieldValue)
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("UpdateUserLog", userID + " successfully updated!");
                    }
                });
    }

    public void removeFollower(String userID)
    {
        //Map<String, Object> updates = new HashMap<>();
        //updates.put("followers", FieldValue.arrayRemove(auth.getUid()));
        System.out.println("MINUN OMA UID: " + auth.getUid());
        db.collection("user_contacts")
                .document(userID)
                .update("followers", FieldValue.arrayRemove(auth.getUid()))
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {

                    }
                });
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
