package co.plook;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.HashMap;

public class ProfileEditActivity extends ParentActivity {

    private DatabaseReader dbReader;
    private DatabaseWriter dbWriter;

    private EditText editUsername;
    private EditText editBio;
    private EditText editLocation;
    private ImageView profilePic;

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
            UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(updatedUsername)
                    .build();

            auth.getCurrentUser().updateProfile(changeRequest)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            System.out.println("User updated");
                        }
                    });


            finish();

        }

        else {

            Toast.makeText(this, "Käyttäjänimen pitää olla pitempi kuin 3 merkkiä", Toast.LENGTH_SHORT).show();
        }


    }
}


