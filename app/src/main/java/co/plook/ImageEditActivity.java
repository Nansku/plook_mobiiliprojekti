/*package co.plook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import static android.app.PendingIntent.getActivity;

public class ImageEditActivity extends ParentActivity {
    public static Uri imageUri;
    private ImageView profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);
        profilePic = findViewById(R.id.profilePic);
        //Intent intent = getIntent();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("imageUri")) {
            imageUri = Uri.parse(bundle.getString("imageUri"));
        }

        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(3, 4)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                profilePic.setImageURI(imageUri);




            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();

            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClass(ImageEditActivity.this,  CroppedImageUpload.class);
            intent.putExtra("imageUri", imageUri.toString());
            startActivity(intent);
        }

    }
}*/