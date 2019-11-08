package com.ysteimle.segproject.easywalkin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    private EditText emailEdit;
    private EditText passwordEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailEdit = findViewById(R.id.LogInScreenUserNameEdit);
        passwordEdit = findViewById(R.id.LogInScreenPwdEdit);

        // Initialise Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if there is a signed in user
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), PersonalProfileActivity.class));
        }
    }

    public void onClickNewUserMsg (View view) {
        // Application context and activity
        Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
        startActivity(intent);
    }

    public void logIn (View view) {
        String email = emailEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter your email address and password", Toast.LENGTH_LONG).show();
        }
        else if (validLogInInput(email, password)) {
            // empty textview fields
            passwordEdit.setText("");
            emailEdit.setText("");

            // sign in to authentication scheme
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //Toast.makeText(getApplicationContext(), "Successfully logged in", Toast.LENGTH_LONG).show();
                                // if task successful, go to Personal Profile Activity
                                startActivity(new Intent(getApplicationContext(), PersonalProfileActivity.class));
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Error: Log in failed.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
        else {
            Toast.makeText(getApplicationContext(), "Error: Log in failed.", Toast.LENGTH_LONG).show();
        }
    }

    public boolean validLogInInput(String email, String password) {
        boolean validInput = true;
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("Error: ");
        // Email validation
        if (!email.contains("@") || !email.contains(".") || email.length() < 6) {
            validInput = false;
            errorMsg.append("Invalid email. ");
        }
        // password validation
        if (password.length() < 6 || password.length() > 12) {
            validInput = false;
            errorMsg.append("Invalid password.");
        }
        if (!validInput) {
            Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
        }
        return validInput;
    }
}
