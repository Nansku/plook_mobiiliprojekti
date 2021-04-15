package co.plook;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.UUID;

import static android.view.View.*;
import static java.security.AccessController.*;


public class ImageUploadActivity extends ParentActivity {
    private ImageView profilePic;
    private Uri imageUri;
    private DatabaseWriter dbWriter;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    Button mCaptureBtn;
    Button uploadButton;
    Button chooseImgButton;
    RelativeLayout relativeLayout;
    EditText postCaption;
    EditText postDescription;
    EditText postTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);

        profilePic = findViewById(R.id.profilePic);
        mCaptureBtn = findViewById(R.id.capture_image_btn);
        uploadButton = (Button) findViewById(R.id.upload);
        chooseImgButton = (Button) findViewById(R.id.choose_image_btn);
        relativeLayout = (RelativeLayout) findViewById(R.id.textFields);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // navigation inflater
        getLayoutInflater().inflate(R.layout.activity_image_upload, contentGroup);

        // Visibility specifications
        profilePic.setVisibility(GONE);
        relativeLayout.setVisibility(INVISIBLE);
        uploadButton.setVisibility(INVISIBLE);

        // Choose img button listener
        chooseImgButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });

        // Capture img btn listener
        mCaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //jos android versio isompi kuin marshmallwo niin pittää tehä runtime permission checkki
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED ||
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                    PackageManager.PERMISSION_DENIED) {
                        //kysyy käyttäjälyä luvan käyttää kameraa
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        //näyttää pop upin mistä käyttäjä voi valita antaako luvan vai ei
                        requestPermissions(permission, PERMISSION_CODE);
                    } else {
                        //lupa jo annettu
                        openCamera();
                    }
                } else {
                    //os versio vanhempi kuin marshmallow (lupa annetaan automaattisesti (just incase joku käyttää vielä vanhoja versioita))
                    openCamera();
                }
            }
        });
    }

    // Function to choose img from gallery
    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    // Function to take picture with camera
    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //intentti kameralle
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    // Ask for permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //tätä kutsutaan kun käyttäjä painaa allowia tai deny nappulaa pop upista
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    //lupa pop upista annettiin
                    openCamera();
                } else {
                    //lupaa ei annettu
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();

                }
            }
        }
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
            activity.setAspectRatio(3, 4);
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
                uploadButton.setVisibility(View.VISIBLE);
                relativeLayout.setVisibility(View.VISIBLE);
                profilePic.setVisibility(View.VISIBLE);
            }

            // Jos tulee error
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

        //jos ok niin laita otetettu kuva image viewiin
        else if (resultCode == RESULT_OK) {
            //profilePic.setImageURI(imageUri);
            CropImage.ActivityBuilder activity = CropImage.activity(imageUri);
            activity.setGuidelines(CropImageView.Guidelines.ON);
            activity.setAspectRatio(4, 5);
            activity.start(this);
        }

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPicture(imageUri);
            }
        });
    }

    // Function to upload chosen picture to Firebase
    private void uploadPicture(Uri uri) {
        final String randomKey = UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child("images/" + randomKey);
        postCaption = findViewById(R.id.post_caption);
        postDescription = findViewById(R.id.post_description);
        postTags = findViewById(R.id.post_tags);

        String caption = postCaption.getText().toString();
        String description = postDescription.getText().toString();
        postTags.getText().toString();
        ArrayList<String> tags = new ArrayList<>();
        //String[] tags = {postTags.getText().toString()};
        tags.add(postTags.getText().toString());

        Task<Uri> urlTask = riversRef.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return riversRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    //TÄÄLLÄ on download token joka menee database kirjoittajaan jotenkin näin
                    //dbWriter.addPost("Caption", "Description", downloadUri.toString());
                    Uri downloadUri = task.getResult();

                    dbWriter = new DatabaseWriter();
                    boolean createTask = dbWriter.addPost("pztOy8uA63XqayPmUnDHBpbaETA3", caption, "DFDRLQyFsu2Fgzji3kHo", description, tags, downloadUri.toString());
                }
            }
        });
    }
}
