package co.plook;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;

public class ConfirmationActivity extends AppCompatActivity {

    private Button gotoLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        gotoLoginButton = findViewById(R.id.button);

      gotoLoginButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

              Intent loginintent =new Intent(ConfirmationActivity.this, LoginActivity.class);
              startActivity(loginintent);
          }
      });
    }
}