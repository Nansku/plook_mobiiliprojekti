package co.plook;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.HashMap;
import java.util.Map;

public class DatabaseWriter
{

    FirebaseFirestore db;
    FirebaseStorage storage;


    public DatabaseWriter()
    {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

    }

    public void uploadImage()
    {
        StorageReference ref = storage.getReference();

        StorageReference imageRef = ref.child("images");

        /*// Get the data from an ImageView as bytes
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });*/
    }


    //User gets an automatically generated userID.
    //Maybe the user is also asked for a nickname that is displayed on screen
    //and a 'real' name (firstname)
    public void addUser(String userID, String email)
    {
        Map<String, Object> user = new HashMap<>();
        user.put("id", userID);
        user.put("email", email);

        addToCollection("users", user);
    }

    public boolean addPost(String userID, String caption, String description, String[] tags) //WIP image??
    {
        //First upload the picture to storage and return the imageurl
        //Then add a post document

        Map<String, Object> post = new HashMap<>();
        post.put("caption", caption);
        post.put("description", description);
        post.put("tags", tags);

        addToCollection("posts", post);

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
}
