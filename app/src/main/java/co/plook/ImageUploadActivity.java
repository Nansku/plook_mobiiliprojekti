package co.plook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.UUID;

import static android.view.View.*;


public class ImageUploadActivity extends AppCompatActivity
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

        Button chooseButton = (Button)findViewById(R.id.choosePic);


        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        chooseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
                chooseButton.setVisibility(INVISIBLE);
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
        if(requestCode==1 && resultCode==RESULT_OK && data!= null && data.getData()!= null){
            imageUri = data.getData();

            /*Intent intent = new Intent(ImageUploadActivity.this, ImageEditActivity.class);
            intent.putExtra("imageUri", imageUri);
            startActivity(intent);*/

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClass(ImageUploadActivity.this, ImageEditActivity.class);
            intent.putExtra("imageUri", imageUri.toString());
            startActivity(intent);

            /*Bundle bundle = getIntent().getExtras();
            if (bundle != null && bundle.containsKey("imageUri")) {
                imageUri = Uri.parse(bundle.getString("resultUri"));
            }*/
        }

        /*Button uploadButton = (Button)findViewById(R.id.upload);

        uploadButton.setVisibility(View.VISIBLE);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPicture();
            }
        });
*/      uploadPicture(imageUri);
    }

    private void uploadPicture(Uri uri) {

        final String randomKey = UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child("images/"+ randomKey);

        riversRef.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Snackbar.make(findViewById(android.R.id.content), "Kuva ladattu", Snackbar.LENGTH_LONG).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(getApplicationContext(), "Lataus epäonnistui", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                    }
                });
    }

    }
