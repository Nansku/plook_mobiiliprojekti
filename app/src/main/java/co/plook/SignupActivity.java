package co.plook;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.messaging.FirebaseMessaging;


public class SignupActivity extends AppCompatActivity  {

    private EditText textPersonName;
    private EditText textEmailAddress;
    private EditText textPassword;
    private EditText textPassword2;
    private Button SignUpButton;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseWriter dbWriter;

    private Boolean emailAddressChecker;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();
        dbWriter = new DatabaseWriter();

        textPersonName = (EditText) findViewById(R.id.editText);
        textEmailAddress = (EditText) findViewById(R.id.editText2);
        textPassword = findViewById(R.id.editText3);
        textPassword2 = findViewById(R.id.editText4);
        SignUpButton = findViewById(R.id.SignUpButton);

        SignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                registerNewUser();

                //Intent intent_one =new Intent(SignupActivity.this, MainActivity.class);
                //startActivity(intent_one);
            }

        });
    }

    private void registerNewUser() {
        String username, email, confirmpassword, password;
        username = textPersonName.getText().toString();
        email = textEmailAddress.getText().toString();
        password = textPassword.getText().toString();
        confirmpassword = textPassword2.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Write your email...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Write your password...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(confirmpassword)) {
            Toast.makeText(this, "Confirm your password", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(confirmpassword)) {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> signupTask) {

                        if (signupTask.isSuccessful()) {

                            SendEmailVerification();

                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenTask -> {
                                FirebaseUser taskUser = signupTask.getResult().getUser();
                                dbWriter.addUser(taskUser.getUid(), username);
                                UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                                mAuth.getCurrentUser().updateProfile(changeRequest);
                                Toast.makeText(SignupActivity.this, "You are registered", Toast.LENGTH_SHORT).show();
                            });

                        } else {
                            String message = signupTask.getException().getMessage();
                            Toast.makeText(SignupActivity.this, "Error in registering" + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }
    }

    private void SendEmailVerification()
    {
        FirebaseUser user= mAuth.getCurrentUser();


        if (user !=null)
        {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful())
                {
                    Toast.makeText(SignupActivity.this, "Registering was successfull, confirm your email.", Toast.LENGTH_SHORT).show();
                    SendUserToConfirmationActivity();
                    mAuth.signOut();
                }

                else
                {
                    String error = task.getException().getMessage();
                    Toast.makeText(SignupActivity.this, "Error" + error, Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                }
            }

        });

    }

}

    private void SendUserToConfirmationActivity() {
        Intent confirmationintent = new Intent(SignupActivity.this, ConfirmationActivity.class);
        confirmationintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(confirmationintent);
        finish();
    }
}





