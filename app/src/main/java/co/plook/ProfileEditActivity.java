package co.plook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.UUID;

public class ProfileEditActivity extends ParentActivity {

    private DatabaseReader dbReader;
    private DatabaseWriter dbWriter;

    private EditText editUsername;
    private EditText editBio;
    private EditText editLocation;
    private ImageView profilePic;
    private Button deleteButton;

    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    Button saveAllButton;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // INTENT FROM PROFILE ACTIVITY
        getIntent();

        dbReader = new DatabaseReader();
        dbWriter = new DatabaseWriter();

        super.onCreate(savedInstanceState);

        // INFLATER FOR NAV
        getLayoutInflater().inflate(R.layout.activity_profile_edit, contentGroup);

        editUsername = findViewById(R.id.editUserName);
        editBio = findViewById(R.id.editBio);
        editLocation = findViewById(R.id.editLocation);
        profilePic =  findViewById(R.id.profilePic);
        deleteButton = findViewById(R.id.deleteButton);

        deleteButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){

                ReauthenticateDialog fragmentDialog = new ReauthenticateDialog();
                fragmentDialog.show(getSupportFragmentManager(), "ReauthenticateDialog");

            }


        });


        Bundle extras = getIntent().getExtras();

        editLocation.setText(extras.getString("location"));
        editBio.setText(extras.getString("bio"));
        editUsername.setText(auth.getCurrentUser().getDisplayName());
    }

    public void saveData(View view){
      
        String updatedUsername = editUsername.getText().toString();

        if (updatedUsername.length()>3)
        {
            HashMap <String, Object> updateData = new HashMap<>();
            updateData.put("name", updatedUsername);
            updateData.put("location", editLocation.getText().toString());
            updateData.put("bio", editBio.getText().toString());
          
            dbWriter.updateField("users", auth.getUid(), updateData);
            UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder().setDisplayName(updatedUsername);
            if (imageUri != null){
                builder.setPhotoUri(imageUri);
                uploadPicture(imageUri);
            }

            auth.getCurrentUser().updateProfile(builder.build())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            finish();
                            System.out.println("User updated");
                        }
                    });

        }

        else {

            Toast.makeText(this, "Käyttäjänimen pitää olla pitempi kuin 3 merkkiä", Toast.LENGTH_SHORT).show();
        }

    }


    public void choosePicture(View v) {
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
