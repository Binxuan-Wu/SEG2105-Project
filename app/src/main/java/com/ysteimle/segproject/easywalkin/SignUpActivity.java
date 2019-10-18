package com.ysteimle.segproject.easywalkin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignUpActivity extends AppCompatActivity implements OnItemSelectedListener {

    // Variables for the information fields
    private Spinner accountSpinner;
    private EditText firstNameEdit;
    private EditText lastNameEdit;
    private EditText emailEdit;
    private EditText addressEdit;
    private EditText passwordEdit;
    private EditText confirmPasswordEdit;

    // Variables for the Firebase Authentication and Database
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Configuring the spinner for the account type selection
        accountSpinner = (Spinner) findViewById(R.id.SignUpScreenAccountTypeSpinner);
        accountSpinner.setOnItemSelectedListener(this);

        // Firebase Authentication and Database Instances
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        // Connect variables defined above to correct layout elements on the SignUp Screen
        setUpVariables();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //accountType = parent.getItemAtPosition(position).toString().trim();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // Make accountType default to Patient
        //accountType = "Patient";
    }

    private void setUpVariables() {
        firstNameEdit = (EditText) findViewById(R.id.SignUpScreenFirstNameEdit);
        lastNameEdit = (EditText) findViewById(R.id.SignUpScreenLastNameEdit);
        emailEdit = (EditText) findViewById(R.id.SignUpScreenEmailEdit);
        addressEdit = (EditText) findViewById(R.id.SignUpScreenAddressEdit);
        passwordEdit = (EditText) findViewById(R.id.SignUpScreenPasswordEdit);
        confirmPasswordEdit = (EditText) findViewById(R.id.SignUpScreenConfirmPasswordEdit);
    }

    public void onClickPreviousUserMsg (View view) {
        finish(); // Finish activity and return to main screen (which is the Log in Activity)
    }

    public void onClickSignUp (View view) {
        // Get user input from all EditText fields
        String firstName = firstNameEdit.getText().toString();
        String lastName = lastNameEdit.getText().toString();
        String email = emailEdit.getText().toString();
        String address = addressEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        String confirmpassword = confirmPasswordEdit.getText().toString();

        // Get account type
        final String accountType = accountSpinner.getSelectedItem().toString().trim();

        // Verify that all fields contain appropriate input
        if (validSignUpScreenInput(accountType, firstName, lastName, email, address, password,
                confirmpassword)) {
            // Hash password
            String passwordHash = hexHash(password);
            // Confirm hashing worked
            if (passwordHash.equals("Failure")) {
                Toast.makeText(this, "Password Hashing Failed", Toast.LENGTH_LONG).show();
                return; // terminate since we did not manage to hash the password
            }

            // Create appropriate User object
            if (accountType.equals("Employee")) {
                final Employee mEmployee = new Employee(firstName, lastName, email, address, passwordHash);
                // Register user in Firebase Authentication Scheme
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // If sign up successful, add key value pair (Uid, "Employee") to AccountType list in database
                                    mDatabase.getReference("Account Types").child(mAuth.getCurrentUser().getUid()).setValue("Employee");
                                    // Also add employee object to "Employees" directory in database
                                    mDatabase.getReference("Employees")
                                            .child(mAuth.getCurrentUser().getUid()).setValue(mEmployee) // Key: currentUser.Uid, Value: Employee Object
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        // if adding to database successful, display success message,
                                                        // finish activity, and go to personal profile activity
                                                        finish();
                                                        startActivity(new Intent(getApplicationContext(), PersonalProfileActivity.class));
                                                    }
                                                    else {
                                                        // if adding to database failed, show error message
                                                        Toast.makeText(getApplicationContext(), "Firebase Database Error", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                }
                                else {
                                    // If sign up failed, show error message
                                    Toast.makeText(getApplicationContext(), "Firebase Authentication Error", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
            else if (accountType.equals("Patient")) {
                // By default, user is a patient.
                final Patient mPatient = new Patient(firstName, lastName, email, address, passwordHash);
                // Register user in Firebase Authentication Scheme
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // If sign up successful, add key value pair (Uid, "Patient") to AccountType list in database
                                    mDatabase.getReference("Account Types").child(mAuth.getCurrentUser().getUid()).setValue("Patient");
                                    // Also add Patient object to "Patients" directory in database
                                    mDatabase.getReference("Patients")
                                            .child(mAuth.getCurrentUser().getUid()).setValue(mPatient) // Key: currentUser.Uid, Value: Employee Object
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        // if adding to database successful, display success message,
                                                        // finish activity, and go to personal profile activity
                                                        finish();
                                                        startActivity(new Intent(getApplicationContext(), PersonalProfileActivity.class));
                                                    } else {
                                                        // if adding to database failed, show error message
                                                        Toast.makeText(getApplicationContext(), "Firebase Database Error", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                } else {
                                    // If sign up failed, show error message
                                    Toast.makeText(getApplicationContext(), "Firebase Authentication Error", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        }
    }


    public boolean validSignUpScreenInput(String accountType, String firstName, String lastName,
                                          String email, String address, String password,
                                          String confirmpassword) {
        // To be implemented
        return true;
    }

    // Method that takes a plaintext String as an input and heturns a String containing the hexadecimal
    // representation of the hashed plaintext using SHA-256
    public String hexHash (String plaintext) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(plaintext.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "Failure";
        }
    }

    // Testing method that verifies what spinner selection returns
    public void spinnerTest (View view) {
        // Get account type
        String accountType = accountSpinner.getSelectedItem().toString().trim();
        Toast.makeText(this, accountType, Toast.LENGTH_LONG).show();
    }
}
