package com.ysteimle.segproject.easywalkin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminAccountActivity extends AppCompatActivity {

    private FirebaseAuth mAuth; // Authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_account);

        // Initialise the Firebase Variables
        mAuth = FirebaseAuth.getInstance();
    }

    public void goToProfile (View view) {
        finish();
    }

    // On Click method for the Log out button
    // Maybe we should not have a log-out button here, but just a return-to-profile
    // button and then we log-out from the profile.
    public void logOut (View view) {
        // sign out from Firebase Authentication
        mAuth.signOut();
        // After signing out from Firebase authentication, finish this activity
        finish();
        // Return to Log in screen (Main Activity)
        startActivity(new Intent(this, MainActivity.class));
    }

    public void manageServices (View view) {
        startActivity(new Intent(getApplicationContext(), ManageServicesActivity.class));
    }

    public void managePatients (View view) {
        Intent patientIntent = new Intent(getApplicationContext(), ManageUsersActivity.class);
        patientIntent.putExtra("userType", "Patient");
        startActivity(patientIntent);
    }

    public void manageEmployees (View view) {
        Intent employeeIntent = new Intent(getApplicationContext(), ManageUsersActivity.class);
        employeeIntent.putExtra("userType", "Employee");
        startActivity(employeeIntent);
    }
}
