package co.plook;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.annotation.Nullable;

public class ConfirmationDialog extends DialogFragment {

    Button buttonYes;
    Button buttonNo;
    OnDismissListener dismissListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @androidx.annotation.Nullable ViewGroup container, @androidx.annotation.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_confirmation_dialog, null);

        buttonNo = view.findViewById(R.id.buttonNo);
        buttonYes = view.findViewById(R.id.buttonYes);

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               getDialog().dismiss();
            }
        });

        buttonYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


                user.delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    SendUserToWelcomeActivity();


                                }
                            }


                        });

            }
        });

        return view;
    }

    private void SendUserToWelcomeActivity() {

        Intent mainIntent = new Intent(this.getContext(), WelcomeActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);

    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        if (dismissListener != null){

            dismissListener.onDismiss();
        }
    }

    public interface OnDismissListener {

        void onDismiss();
    }

    public void setOnDismissListener(OnDismissListener dismissListener)
    {
        this.dismissListener = dismissListener;
    }
}
