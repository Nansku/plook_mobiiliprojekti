package co.plook;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity
{

    private boolean isFollowOn = true;
    private boolean isCommentsOn = true;

    private DatabaseWriter dbWriter;
    private DatabaseReader dbReader;
    private FirebaseAuth auth;

    final String followField = "notification_follow";
    final String commentField = "notification_comment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbWriter = new DatabaseWriter();
        dbReader = new DatabaseReader();
        auth = FirebaseAuth.getInstance();

        SwitchCompat followSwitch = findViewById(R.id.switch_follow_notif);
        SwitchCompat commentSwitch = findViewById(R.id.switch_comment_notif);

        // get document from database and set the switches to current settings
        dbReader.findDocumentByID("tokens", auth.getUid()).addOnCompleteListener(task -> {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            try {
                isFollowOn = (boolean) documentSnapshot.get(followField);
                isCommentsOn = (boolean) documentSnapshot.get(commentField);
            }
            catch(Exception e) {
                System.out.println("get settings error: " + e.getMessage());
            }
            followSwitch.setChecked(isFollowOn);
            commentSwitch.setChecked(isCommentsOn);
        });

        // switch listeners to get the users input
        followSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isFollowOn = isChecked;
        });
        commentSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isCommentsOn = isChecked;
        });
    }

    public void saveSettings(View v)
    {
        // update new settings to database...
        HashMap<String, Object> updatedSettings = new HashMap<>();
        updatedSettings.put("notification_follow", isFollowOn);
        updatedSettings.put("notification_comment", isCommentsOn);
        dbWriter.updateField("tokens", auth.getUid(), updatedSettings);
        finish();
    }
}
