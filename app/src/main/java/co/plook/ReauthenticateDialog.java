package co.plook;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ReauthenticateDialog extends DialogFragment {

    Button buttonConfirmDelete, buttonCancel;
    EditText editTextEmailAddress, editTextPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reauthorizedialog, null);

        buttonCancel = view.findViewById(R.id.buttonCancel);
        buttonConfirmDelete = view.findViewById(R.id.buttonConfirmDelete);
        editTextEmailAddress = view.findViewById(R.id.editTextEmailAddress);
        editTextPassword = view.findViewById(R.id.editTextPassword);

        buttonCancel.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v)
            {
                getDialog().dismiss();
            }
        });

        buttonConfirmDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String emailAddress = editTextEmailAddress.getText().toString();
                String password = editTextPassword.getText().toString();
              
                if (!emailAddress.equals("") && !password.equals("")) {
                    AuthCredential credential = EmailAuthProvider.getCredential(emailAddress, password);
                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                ConfirmationDialog fragmentDialog = new ConfirmationDialog();
                                fragmentDialog.show(getParentFragmentManager(), "ConfirmationDialog");
                                fragmentDialog.setOnDismissListener(new ConfirmationDialog.OnDismissListener() {
                                    @Override
                                    public void onDismiss() {
                                        getDialog().dismiss();
                                    }
                                });
                            }
                        }
                    });
                 }
             }
        });
        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }


    private void SendUserToWelcomeActivity() {
        Intent mainIntent =new Intent (this.getContext(), WelcomeActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }
}
