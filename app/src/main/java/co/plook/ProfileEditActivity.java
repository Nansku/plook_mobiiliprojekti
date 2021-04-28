package co.plook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ProfileEditActivity extends ParentActivity
{
    ////// UUTTA
    private ImageView profilePic;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseWriter dbWriter;
    Button saveAllButton;
    ////// UUTTA

    private DatabaseReader dbReader;
    private ArrayList<Post> userPosts;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        // INTENT FROM PROFILE ACTIVITY
        getIntent();


        userPosts = new ArrayList<Post>();
        dbReader = new DatabaseReader();

        super.onCreate(savedInstanceState);
        // INFLATER FOR NAV
        getLayoutInflater().inflate(R.layout.activity_profile_edit, contentGroup);


        /// UUTTA
        profilePic = findViewById(R.id.profilePic);

        saveAllButton = (Button) findViewById(R.id.editProfile);

        System.out.println("KRISU ON KIVA: "+ profilePic);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        /// UUTTA



        // FIND PHOTOS FROM FIREBASE
        dbReader.findDocumentsWhereEqualTo("posts", "userID", auth.getUid()).addOnCompleteListener(task ->
        {   QuerySnapshot snapshot = task.getResult();

            assert snapshot != null;
            System.out.println(snapshot.getDocuments().toString());
            for (QueryDocumentSnapshot document : snapshot)
            {   Post post = new Post();
                post.setPostID(document.getId());
                post.setCaption(document.getString("caption"));
                post.setDescription(document.getString("description"));
                post.setImageUrl(document.getString("url"));

                userPosts.add(post);
            }


        });




        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });

        saveAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {uploadPicture(imageUri);
            }
        });

    }

    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("APP_DEBUG", String.valueOf(requestCode));
        //tarkistaa onko kuvaa valittu ja jos on niin ottaa
        if (requestCode == 1 && data != null && data.getData() != null) {
            imageUri = data.getData();

            // avaa croppauksen
            CropImage.ActivityBuilder activity = CropImage.activity(imageUri);
            activity.setGuidelines(CropImageView.Guidelines.ON);
            activity.setAspectRatio(1, 1);
            activity.start(this);
        }
        // Jos request code == croppaa kuva
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            // jos result == ok
            if (resultCode == RESULT_OK) {
                //Log.d("APP_DEBUG",result.toString());
                imageUri = result.getUri();
                Log.d("APP_DEBUG", imageUri.toString());

                // Lisätään cropattu kuva image viewiin
                profilePic.setImageURI(imageUri);

            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }



    }

    private void uploadPicture(Uri uri) {
        final String randomKey = UUID.randomUUID().toString();
        StorageReference imageRef = storageReference.child("images/" + auth.getUid() + "/" + randomKey);

        imageRef.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return imageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String userID = auth.getUid();
                    dbWriter = new DatabaseWriter();
                    HashMap<String, Object> updatedUser = new HashMap<>();
                    updatedUser.put("url",downloadUri.toString());
                    dbWriter.updateField("users", auth.getUid(), updatedUser);

                    UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder().setPhotoUri(downloadUri).build();
                    auth.getCurrentUser().updateProfile(changeRequest);


                }
            }
        });
    }


}

