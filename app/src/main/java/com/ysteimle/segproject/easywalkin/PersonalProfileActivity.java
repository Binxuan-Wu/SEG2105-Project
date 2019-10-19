package com.ysteimle.segproject.easywalkin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PersonalProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth; // Authentication instance
    private FirebaseUser mUser; // User that is currently logged in
    private DatabaseReference mDatabase; // DatabaseReference instance

    private TextView ProfileAccountTextView;
    private TextView ProfileFirstNameTextView;
    private TextView ProfileLastNameTextView;
    private TextView ProfileEmailTextView;
    private TextView ProfileAddressTextView;
    //private TextView ProfileAddressLine2View; // Might not be necessary if text wraps in a TextView (i.e. if text goes onto a second line if too long for the screen)

    String accountType;
    String databasePath;
    String firstName;
    String lastName;
    String email;
    String address;

    boolean logInError = false;
    boolean displayProfileInfo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_profile);

        // Initialise the Firebase Variables
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Connect TextView variables to TextView fields
        ProfileAccountTextView = findViewById(R.id.ProfileAccountView);
        ProfileFirstNameTextView = findViewById(R.id.ProfileFirstNameView);
        ProfileLastNameTextView = findViewById(R.id.ProfileLastNameView);
        ProfileEmailTextView = findViewById(R.id.ProfileEmailView);
        ProfileAddressTextView = findViewById(R.id.ProfileAddressView);
        //profileAddressLine2View = findViewById(R.id.ProfileAddressLine2View);

        // If no user is logged in, this Activity should not be displayed, and we should return to
        // the Log in screen (Main Activity)
        if (mUser == null) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
        /*
        else {
            // If a user is logged in, find out the type of the user.
            mDatabase.child("Account Types").child(mUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //Find the type of the user
                            accountType = dataSnapshot.getValue().toString();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            //There was an error
                            accountType = "Error";
                            logInError = true;
                        }
                    });
            // Get information about user from Database
            if (!logInError) {
                if (accountType.equals("Admin")) {
                    // If the admin user logged in, there is no profile info to display and we just make
                    // a welcome message and set the Account Type field to Admin.
                    Toast.makeText(getApplicationContext(), "Welcome! You are logged in as Admin", Toast.LENGTH_LONG).show();
                    ProfileAccountTextView.setText("Account type: Admin");
                } else if (accountType.equals("Patient")) {
                    // A patient logged in
                    databasePath = "Patients";
                    // retrieve child matching currently logged in user's Uid
                    // from the Patients directory of the Database
                    mDatabase.child(databasePath).child(mUser.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    // get patient object from database
                                    Patient patient = dataSnapshot.getValue(Patient.class);

                                    // Get the patient information
                                    firstName = patient.getFirstName();
                                    lastName = patient.getLastName();
                                    email = patient.getEmail();
                                    address = patient.getAddress();

                                    // Say we should display the profile info
                                    displayProfileInfo = true;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    logInError = true;
                                }
                            });
                } else if (accountType.equals("Employee")) {
                    // A patient logged in
                    databasePath = "Employees";
                    // retrieve child matching currently logged in user's Uid
                    // from the Patients directory of the Database
                    mDatabase.child(databasePath).child(mUser.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    // get Employee object from database
                                    Employee employee = dataSnapshot.getValue(Employee.class);

                                    // Get the patient information
                                    firstName = employee.getFirstName();
                                    lastName = employee.getLastName();
                                    email = employee.getEmail();
                                    address = employee.getAddress();

                                    // Say we should display the profile info
                                    displayProfileInfo = true;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    logInError = true;
                                }
                            });
                } else {
                    Toast.makeText(getApplicationContext(),"Error: Could not determine the account type", Toast.LENGTH_LONG).show();
                    logInError = true;
                }
            }
            // Display profile information
            if (displayProfileInfo && !logInError) {
                // Display the information in the TextViews
                ProfileAccountTextView.setText("Account Type: " + accountType);
                ProfileFirstNameTextView.setText("First Name: " + firstName);
                ProfileLastNameTextView.setText("Last Name: " + lastName);
                ProfileEmailTextView.setText("Email: " + email);
                ProfileAddressTextView.setText("Address: " + address);

                // Display Welcome message
                Toast.makeText(getApplicationContext(), "Welcome " + firstName + "! You are logged in as " + accountType, Toast.LENGTH_LONG).show();
            }

        }

         */

    }

    // On Click method for the Log out button
    public void logOut (View view) {
        // sign out from Firebase Authentication
        mAuth.signOut();
        // After signing out from Firebase authentication, finish this activity
        finish();
        // Return to Log in screen (Main Activity)
        startActivity(new Intent(this, MainActivity.class));
    }
}
