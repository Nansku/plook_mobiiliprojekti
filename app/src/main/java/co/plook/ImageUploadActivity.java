package co.plook;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.RelativeLayout;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.util.UUID;

import static android.view.View.*;

public class ImageUploadActivity extends ParentActivity {
    private ImageView profilePic;
    private Uri imageUri;
    private DatabaseWriter dbWriter;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    Button mCaptureBtn;
    ImageButton uploadButton;
    Button chooseImgButton;
    ImageButton cancel;
    RelativeLayout relativeLayout;
    EditText postCaption;
    EditText postDescription;
    AutoCompleteTextView tagSuggestions;
    String[] tags;
    TagLayout tagLayout;
    String tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);

        profilePic = findViewById(R.id.profilePic);
        mCaptureBtn = findViewById(R.id.capture_image_btn);
        uploadButton = (ImageButton) findViewById(R.id.upload);
        chooseImgButton = (Button) findViewById(R.id.choose_image_btn);
        relativeLayout = (RelativeLayout) findViewById(R.id.textFields);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        postCaption = findViewById(R.id.post_caption);
        postDescription = findViewById(R.id.post_description);
        tagLayout = findViewById(R.id.post_tags_layout);
        tagSuggestions = (AutoCompleteTextView) findViewById(R.id.tag_list);
        tagLayout = (TagLayout) findViewById(R.id.tagLayout);
        cancel = (ImageButton) findViewById(R.id.cancel);
        // navigation inflater
        getLayoutInflater().inflate(R.layout.activity_image_upload, contentGroup);

        // Visibility specifications
        // profilePic.setVisibility(GONE);
        relativeLayout.setVisibility(GONE);
        uploadButton.setVisibility(GONE);

        String[] example = {"tag1", "tag2", "tag3"};
        // Auto suggestion for tags
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, example);
        tagSuggestions.setAdapter(arrayAdapter);

        // AUTOFILL
        tagSuggestions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView textView = (TextView) view;
                addTag(textView.getText().toString());
            }
        });

        // Choose img button listener
        chooseImgButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });

        // Cancel-button listener
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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

    public void addTag(String string) {

        View child = getLayoutInflater().inflate(R.layout.layout_post_tag, tagLayout, false);
        tagLayout.addView(child);
        tag = string.trim().replaceAll("[^a-öA-Ö0-9,]", "");

        ImageView imageView = child.findViewById(R.id.tag_delete);
        imageView.setVisibility(VISIBLE);
        TextView tagText = child.findViewById(R.id.tag_text);
        tagText.setText(tag);
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
        StorageReference imageRef = storageReference.child("images/" + auth.getUid() + "/" + randomKey);

        postCaption = findViewById(R.id.post_caption);
        postDescription = findViewById(R.id.post_description);
        tagLayout = findViewById(R.id.post_tags_layout);

        String caption = postCaption.getText().toString();
        String description = postDescription.getText().toString();

        //ArrayList<String> tags = new ArrayList<>();
        //String[] tags = {postTags.getText().toString()};
        //tags.add(postTags.getText().toString());

        tags = tagSuggestions.getText().toString().trim().replaceAll("[^a-öA-Ö0-9,]", "").split(",");

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
                    dbWriter.addPost(userID, caption,"OIgwEfHvG29j6UIMvy7N", description, tags, downloadUri.toString());

                    Intent intent = new Intent(ImageUploadActivity.this, FeedActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}
