package co.plook;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

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

        editUsername = (EditText) findViewById(R.id.editUserName);
        editBio = (EditText) findViewById(R.id.editBio);
        editLocation = (EditText) findViewById(R.id.editLocation);
        profilePic = (ImageView) findViewById(R.id.profilePic);


        // INFLATER FOR NAV
        getLayoutInflater().inflate(R.layout.activity_profile_edit, contentGroup);

    }

    public void saveData(View view){

        if (editUsername.getText().toString().length()>3)
        {
            HashMap <String, Object> updateData = new HashMap<>();

            updateData.put("name", editUsername.getText());
            updateData.put("location", editLocation.getText());
            updateData.put("bio", editBio.getText());

            dbWriter.updateField("users", auth.getUid(), updateData);

        }


    }
}

