package com.ysteimle.segproject.easywalkin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    // DatabaseReference instances
    private DatabaseReference mDatabase;
    private DatabaseReference mAccountTypeReference;
    private DatabaseReference mUserReference;

    // Value Event Listener Variables (so that they can be removed when app is stopped)
    private ValueEventListener mAccountTypeListener;
    private ValueEventListener mUserListener;

    private TextView ProfileAccountTextView;
    private TextView ProfileFirstNameTextView;
    private TextView ProfileLastNameTextView;
    private TextView ProfileEmailTextView;
    private TextView ProfileAddressTextView;

    String firstName;
    String lastName;
    String email;
    String address;

    boolean isAdmin;

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

        // If no user is logged in, this Activity should not be displayed, and we should return to
        // the Log in screen (Main Activity)
        if (mUser == null) {
            Toast.makeText(getApplicationContext(), "Error: No user logged in", Toast.LENGTH_LONG).show();
            finish();
        }
        else {
            //Toast.makeText(getApplicationContext(), "There is a logged in user", Toast.LENGTH_LONG).show();
            // If a user is logged in, find out the type of the user.
            // Create Value Event Listener for determining the account type
            ValueEventListener accountTypeListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Find account Type associated to Uid of currently logged in user in the
                    // AccountTypes directory of the database
                    String accountTypeFromDatabase = (String) dataSnapshot.getValue();
                    if (accountTypeFromDatabase != null) {
                        if (accountTypeFromDatabase.equals("Deleted")) {
                            deleteUserFromAuthScheme();
                        } else {
                            ProfileAccountTextView.setText(String.format("Account Type: %s",accountTypeFromDatabase));
                            if (accountTypeFromDatabase.equals("Admin")) {
                                getAdminInfo();
                                isAdmin = true;
                            } else if (accountTypeFromDatabase.equals("Employee") || accountTypeFromDatabase.equals("Patient")) {
                                getUserInfo(accountTypeFromDatabase);
                                isAdmin = false;
                            }
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Error: Could not find user in AccountTypes List in Database.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // There was an error somewhere
                }
            };
            // Determine account type using the account type value event listener
            mAccountTypeReference = mDatabase.child("AccountTypes").child(mUser.getUid());
            mAccountTypeReference.addListenerForSingleValueEvent(accountTypeListener);
            // keep copy of account type listener so that we can remove it when app stops
            mAccountTypeListener = accountTypeListener;
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove value event listeners
        if (mAccountTypeListener != null) {
            mAccountTypeReference.removeEventListener(mAccountTypeListener);
        }
    }

    // Method to retrieve user info from database
    public void getUserInfo (String accountType) {
        String databasePath = accountType + "List";
        final String accountTypeForInfoMsg = accountType;
        //Toast.makeText(getApplicationContext(), "Database Path set to: " + databasePath, Toast.LENGTH_LONG).show();

        // Get reference for User object in appropriate user list in database
        mDatabase.child(databasePath).child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Retrieve user object
                User currentUser = dataSnapshot.getValue(User.class);

                try {
                    // Get the info from the user Object
                    firstName = currentUser.firstName;
                    lastName = currentUser.lastName;
                    email = currentUser.email;
                    address = currentUser.address;
                    // Fill in the information in the fields
                    ProfileFirstNameTextView.setText(String.format("First Name: %s",firstName));
                    ProfileLastNameTextView.setText(String.format("Last Name: %s",lastName));
                    ProfileEmailTextView.setText(String.format("Email: %s",email));
                    ProfileAddressTextView.setText(String.format("Address: %s",address));
                    // Display Welcome message
                    Toast.makeText(getApplicationContext(), "Welcome " + firstName + "! You are logged in as " + accountTypeForInfoMsg + ".", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error: User object retrieved from database is null.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Error: Failed to retrieve user object from database.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Method to retrieve Admin info
    public void getAdminInfo () {
        ProfileFirstNameTextView.setText(String.format("Username: %s", AdminAccount.email));
        Toast.makeText(getApplicationContext(), "Welcome! You are logged in as Admin.", Toast.LENGTH_LONG).show();
    }

    // On Click method for the Log out button
    public void logOut (View view) {
        // sign out from Firebase Authentication
        mAuth.signOut();
        // After signing out from Firebase authentication, finish this activity
        finish();
        // Return to Log in screen (Main Activity) -- this should be automatic (don't need to start activity again)
        //startActivity(new Intent(this, MainActivity.class));
    }

    // On Click method for the Go to Account button
    public void goToAccount (View view) {
        if (isAdmin) {
            // keep profile activity open when going to account activity
            startActivity(new Intent(this, AdminAccountActivity.class));
        } else {
            // Logged-in user is not the admin user
            // To be implemented later
            Toast.makeText(getApplicationContext(), "Not implemented yet.", Toast.LENGTH_LONG).show();
        }
    }

    // Method to delete user from authentication scheme
    // Assumes that the user profile info has already been deleted from the database
    private void deleteUserFromAuthScheme() {
        String userId = mUser.getUid();
        DatabaseReference databaseReference = mDatabase.child("AccountTypes").child(userId);

        // delete information from Account Types List in database
        databaseReference.removeValue();

        // delete user from authentication scheme
        mUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // if successfully deleted user from authentication scheme, close this activity
                    // and display message
                    finish();
                    Toast.makeText(getApplicationContext(), "Your account has been deleted.", Toast.LENGTH_LONG).show();
                } else {
                    // something went wrong
                    Toast.makeText(getApplicationContext(), "An error occurred while deleting your account.", Toast.LENGTH_LONG).show();
                    // go back anyway
                    finish();
                }
            }
        });


    }

}
