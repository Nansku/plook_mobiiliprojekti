package co.plook;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.security.AccessController;
import java.util.UUID;

import static android.view.View.*;
import static java.security.AccessController.*;


public class ImageUploadActivity extends ParentActivity
{
    private ImageView profilePic;
    public static Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);
        profilePic = findViewById(R.id.profilePic);
        Button uploadButton = (Button) findViewById(R.id.upload);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        uploadButton.setVisibility(INVISIBLE);
        profilePic.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
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

            if(requestCode==1 && resultCode==RESULT_OK && data!= null && data.getData()!= null){
                imageUri = data.getData();

                CropImage.ActivityBuilder activity = CropImage.activity(imageUri);
                activity.setGuidelines(CropImageView.Guidelines.ON);
                activity.setAspectRatio(3, 4);
                activity.start(this);
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Log.d("APP_DEBUG",result.toString());
                if (resultCode == RESULT_OK) {
                    imageUri = result.getUri();
                    Log.d("APP_DEBUG",imageUri.toString());
                    profilePic.setImageURI(imageUri);

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();

                }
        }

        Button uploadButton = (Button)findViewById(R.id.upload);

        uploadButton.setVisibility(View.VISIBLE);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPicture(imageUri);
            }
        });

    }

    private void uploadPicture(Uri uri) {

        final String randomKey = UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child("images/" + randomKey);

        Task<Uri> urlTask = riversRef.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
        {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
            {
                if (!task.isSuccessful()){
                    throw task.getException();
                }

                return riversRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>()
        {
            @Override
            public void onComplete(@NonNull Task<Uri> task)
            {
                if (task.isSuccessful())
                {
                    //TÄÄLLÄ on download token joka menee database kirjoittajaan jotenkin näin
                    //dbWriter.addPost("Caption", "Description", downloadUri.toString());
                    Uri downloadUri = task.getResult();
                }
            }
        })

                ;
                /*.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        Snackbar.make(findViewById(android.R.id.content), "Kuva ladattu", Snackbar.LENGTH_LONG).show();
                        //System.out.println("UPLOAD SESSION URI: " + taskSnapshot.getUploadSessionUri());
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(getApplicationContext(), "Lataus epäonnistui", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot)
                    {

                    }
                });*/
        riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri uri)
            {
                System.out.println("GET DOWNLOAD URI STRING: " + uri.toString());
            }
        });
    }
}

