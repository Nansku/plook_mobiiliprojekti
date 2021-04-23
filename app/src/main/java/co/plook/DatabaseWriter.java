package co.plook;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DatabaseWriter
{

    FirebaseFirestore db;
    FirebaseAuth auth;

    public DatabaseWriter()
    {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void addUser(String userID, String nickname, String token)
    {
        Map<String, Object> user = new HashMap<>();
        user.put("name", nickname);
        user.put("token", token);

        addToCollectionWithName("users", user, userID);
    }


    public void addPost(String userID, String caption, String channel, String description, String[] tags, String url)
    {
        Map<String, Object> post = new HashMap<>();
        Timestamp timeNow = Timestamp.now();

        post.put("caption", caption);
        post.put("channel", channel);
        post.put("description", description);
        post.put("score", 0);
        post.put("tags", Arrays.asList(tags));

        post.put("time", timeNow);
        post.put("url", url);
        post.put("userID", userID);

        addToCollection("posts", post);
    }

    // deletes a post using a postID. cloud will do a check on authorization to decide whether deleting is allowed or not
    public void deletePost(String postID)
    {
        db.collection("posts")
                .document(postID)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("dbWrite", "DocumentSnapshot successfully deleted!");
                })
                .addOnFailureListener(e -> {
                    Log.d("dbWrite", "Error deleting document", e);
                });
    }

    public void updateField(String collectionPath, String docID, HashMap<String, Object> updatedMap)
    {
        db.collection(collectionPath)
                .document(docID)
                .update(updatedMap)
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("UpdateField", docID + "'s field successfully updated!");
                    }
                }).addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Log.d("UpdateField", "ERROR: " + e.getMessage());
                    }
                });
    }

    public void updateUserContacts(String userID, String field, String followID, boolean remove)
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

    public void addVote(String userID, String postID, int vote)
    {
        Map<String, Object> action = new HashMap<>();

        action.put("vote", vote);

        addToSubcollectionWithName("posts", postID, "user_actions", action, userID);
        //After this we could use cloud functions to count increment post scores
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
                        System.out.println("Document: " + documentReference.getId() + " added to " + collectionPath);
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
                        System.out.println("Document: " + documentReference.getId() + " added to path: " + collectionPath + "/" + documentID + "/" + subcollectionPath);
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

    private void addToSubcollectionWithName(String collectionPath, String documentID, String subcollectionPath, Map document, String docName)
    {
        // Add a new document with a generated ID
        db.collection(collectionPath)
                .document(documentID)
                .collection(subcollectionPath)
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
}
