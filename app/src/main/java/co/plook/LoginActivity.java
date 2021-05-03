package co.plook;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private Button LogInButton;
    private EditText Email;
    private EditText Password;
    private TextView ForgotPassword;
    private TextView MakeNewAccount;
    private Context context;

    private FirebaseAuth mAuth;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        ForgotPassword = (TextView) findViewById(R.id.forgotPassword);
        Email = (EditText) findViewById(R.id.editEmail);
        Password = (EditText) findViewById(R.id.editPassword);
        LogInButton = (Button) findViewById(R.id.LogInButton);
        MakeNewAccount = (TextView) findViewById(R.id.NewAccount);
        context = getApplicationContext();


        MakeNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendUserToSignupActivity();
            }
        });


        LogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowLogIn();
            }
        });

        ForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText forgotPassword = new EditText(v.getContext());
                forgotPassword.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Set new password?");
                passwordResetDialog.setMessage("Write your email to receive a reset link.");
                passwordResetDialog.setView(forgotPassword).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String email = forgotPassword.getText().toString();
                        mAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Toast.makeText(LoginActivity.this, "Reset link sent.", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(LoginActivity.this, "Reset link not sent.", Toast.LENGTH_SHORT).show();
                            }
                        });


                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                passwordResetDialog.create().show();
            }
        });
    }


    private void SendUserToSignupActivity() {
        Intent signupIntent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(signupIntent);
        finish();
    }



    private void AllowLogIn() {
        String email = Email.getText().toString();
        String password = Password.getText().toString();

        if (TextUtils.isEmpty(email)) {
            ShowMessage("Write your email.");

        }
        else if (TextUtils.isEmpty(password)) {
            ShowMessage("Write your password.");
        }

        else {
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            ShowMessage("You are logged in.");
                            checkToken();
                            SendUserToMainActivity();


                        } else {
                            String message = task.getException().getMessage();
                            ShowMessage("Error with log in."+message);
                        }
                    }
                });
        }
    }


    private void SendUserToMainActivity()
    {
        Intent mainIntent =new Intent (LoginActivity.this, FeedActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void ShowMessage(String message)
    {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void checkToken()
    {
        preferences = this.getSharedPreferences("token", Context.MODE_PRIVATE);
        String token = preferences.getString("token", "");
        if (!token.equals(""))
        {
            DatabaseWriter dbWriter = new DatabaseWriter();

            HashMap<String, Object> tokenMap = new HashMap<>();
            tokenMap.put("token", token);

            System.out.println("Uploading new token!");
            dbWriter.updateField("tokens", mAuth.getUid(), tokenMap);

            // clear stored data so we won't upload any unchanged tokens
            preferences.edit().clear().apply();
        }
    }
}


