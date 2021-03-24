package co.plook;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DatabaseUploader
{

    FirebaseFirestore db;

    public DatabaseUploader()
    {
        db = FirebaseFirestore.getInstance();
    }



    public boolean addPost(String userID, String caption, String description, String[] tags) //WIP image??
    {
        //First upload the picture to storage and return the imageurl
        //Then add a post document

        Map<String, Object> post = new HashMap<>();
        post.put("caption", caption);
        post.put("description", description);
        post.put("tags", tags);

        addToCollection(post, "posts");

        return true;
    }

    public boolean addComment()
    {

        return true;
    }

    public boolean addVote(boolean upOrDown)
    {

        return true;
    }

    public boolean addUser()
    {

        return true;
    }

    private void addToCollection(Map document, String collectionPath)
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
}
