package co.plook;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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


    }

    public void saveData(View view){

        if (editUsername.getText().toString().length()>3)
        {
            HashMap <String, Object> updateData = new HashMap<>();

            updateData.put("name", editUsername.getText().toString());
            updateData.put("location", editLocation.getText().toString());
            updateData.put("bio", editBio.getText().toString());

            dbWriter.updateField("users", auth.getUid(), updateData);



            finish();

        }

        else {

            Toast.makeText(this, "Käyttäjänimen pitää olla pitempi kuin 3 merkkiä", Toast.LENGTH_SHORT).show();
        }


    }
}

