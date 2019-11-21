package com.ysteimle.segproject.easywalkin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CreateJoinClinicActivity extends AppCompatActivity {

    // For Firebase Database and Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private ValueEventListener mClinicListener;

    private String currentEmployeeId;
    private List<Clinic> clinicsInDB = new ArrayList<> ();

    private final String clinicOfEmployeePath = "ClinicOfEmployee";
    private final String clinicListPath = "ClinicList";


    private final int joinClinic = 0;
    private boolean hasJoinedClinic;
    private InputValidator iv = new InputValidator();

    // Text Edit fields and buttons
    private EditText searchClinicEdit;
    private EditText clinicNameEdit;
    private EditText clinicPhoneEdit;
    private EditText clinicStreetAddEdit;
    private EditText clinicUnitEdit;
    private EditText clinicProvinceEdit;
    private EditText clinicCityEdit;
    private EditText clinicPostalCodeEdit;
    private EditText clinicInsuranceEdit;
    private EditText clinicPaymentEdit;
    private EditText clinicPasswordEdit;
    private EditText clinicConfirmPasswordEdit;
    private Button searchClinicBtn;
    private Button createClinicBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_join_clinic);

        // Firebase Database and Authentication
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference();
        if (mUser == null) {
            // no user logged in, we should not be here
            finish();
        } else {
            // determine who is currently logged in
            currentEmployeeId = mUser.getUid();
            // we only arrived here because the employee has not yet joined a clinic
            // or maybe I should set this variable via a value event listener...
            hasJoinedClinic = false;

            // set up the variables for the text edit fields
            searchClinicEdit = (EditText) findViewById(R.id.SearchClinicToJoinEdit);
            clinicNameEdit = (EditText) findViewById(R.id.ClinicNameEdit);
            clinicPhoneEdit = (EditText) findViewById(R.id.ClinicPhoneEdit);
            clinicStreetAddEdit = (EditText) findViewById(R.id.ClinicStreetAddEdit);
            clinicUnitEdit = (EditText) findViewById(R.id.ClinicUnitEdit);
            clinicProvinceEdit = (EditText) findViewById(R.id.ClinicProvinceEdit);
            clinicCityEdit = (EditText) findViewById(R.id.ClinicCityEdit);
            clinicPostalCodeEdit = (EditText) findViewById(R.id.ClinicPostalCodeEdit);
            clinicInsuranceEdit = (EditText) findViewById(R.id.ClinicInsuranceEdit);
            clinicPaymentEdit = (EditText) findViewById(R.id.ClinicPaymentEdit);
            searchClinicBtn = (Button) findViewById(R.id.JoinExistingClinicBtn);
            createClinicBtn = (Button) findViewById(R.id.CreateClinicBtn);
            clinicPasswordEdit = (EditText) findViewById(R.id.ClinicPasswordEdit);
            clinicConfirmPasswordEdit = (EditText) findViewById(R.id.ClinicConfirmPasswordEdit);

            // value event listener to find existing clinics in database
            ValueEventListener clinicListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    clinicsInDB.clear();
                    for (DataSnapshot clinicSnapshot : dataSnapshot.getChildren()) {
                        Clinic clinic = clinicSnapshot.getValue(Clinic.class);
                        if (clinic != null) {
                            clinicsInDB.add(clinic);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mReference.child(clinicListPath).addValueEventListener(clinicListener);
            mClinicListener = clinicListener;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mClinicListener != null) {
            mReference.child(clinicListPath).removeEventListener(mClinicListener);
        }
    }

    public void searchClinicOnClick(View view) {
        String nameToSearch = searchClinicEdit.getText().toString().trim();
        List<Clinic> clinicsFromDB = clinicsInDB;
        if (!clinicsFromDB.isEmpty()) {
            List<Clinic> matchingClinics = findClinicsByName(nameToSearch);
            showClinicSearchResultsDialog(matchingClinics);
        } else {
            Toast.makeText(getApplicationContext(), "Error: Could not find any clinics in database.", Toast.LENGTH_LONG).show();
        }
        if (hasJoinedClinic) {
            // If employee successfully joined a clinic via the dialog, exit this activity
            // and go back to employee account activity
            Toast.makeText(getApplicationContext(), "You have successfully joined a clinic.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /*// Method to retrieve all existing clinics from database
    public void getExistingClinicsFromDB() {
        DatabaseReference clinicReference = mReference.child(clinicListPath);
        clinicReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                clinicsInDB.clear();
                for (DataSnapshot clinicSnapshot : dataSnapshot.getChildren()) {
                    Clinic clinic = clinicSnapshot.getValue(Clinic.class);
                    clinicsInDB.add(clinic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // something went wrong
            }
        });

    }*/

    // Methods to find clinic with certain information in the clinicsInDB list
    public List<Clinic> findClinicsByName (String name) {
        List<Clinic> matchingClinics = new ArrayList<>();
        List<Clinic> clinicsFromDB = clinicsInDB;
        for (Clinic clinic : clinicsFromDB) {
            if (clinic.clinicName.equals(name)) {
                matchingClinics.add(clinic);
            }
        }
        return matchingClinics;
    }

    // Dialog for clinic search results
    public void showClinicSearchResultsDialog(List<Clinic> matchingClinics) {
        // Create dialog with appropriate layout
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.search_results_dialog, null);
        dialogBuilder.setView(dialogView);

        // Declare variables for elements in the dialog
        final TextView resultCountMsgView = dialogView.findViewById(R.id.searchResultsCountMsg);
        final TextView resultsActionMsgView = dialogView.findViewById(R.id.searchResultsActionMsg);
        final RecyclerView resultsRecycler = dialogView.findViewById(R.id.searchResultsRecycler);
        final EditText resultsPwdEdit = dialogView.findViewById(R.id.searchResultsPwdEdit);
        final Button resultsCancelBtn = dialogView.findViewById(R.id.searchResultsCancelBtn);

        // Text to display
        int numberOfClinicsFound = matchingClinics.size();
        // Note: not grammatically correct when there is exactly 1 clinic found...
        resultCountMsgView.setText(String.format("%d Clinics match your search.", numberOfClinicsFound));
        if (numberOfClinicsFound <= 0) {
            resultsActionMsgView.setText("Please try again.");
            resultsRecycler.setVisibility(View.GONE);
            resultsPwdEdit.setVisibility(View.GONE);

            // Make dialog appear
            final AlertDialog dialog = dialogBuilder.create();
            dialog.show();

            // Cancel button onClick listener
            resultsCancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // simply close the dialog
                    dialog.dismiss();
                }
            });
        } else {
            resultsActionMsgView.setText("To join a clinic, enter the password below, then select the corresponding clinic in the list.");
            // Use linear layout manager
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            resultsRecycler.setLayoutManager(layoutManager);
            // Make dialog appear
            final AlertDialog dialog = dialogBuilder.create();
            dialog.show();
            // set up clinic adapter and attach it to the recycler
            ClinicAdapter clinicAdapter = new ClinicAdapter(joinClinic, new ClinicAdapter.OnClinicClickListener() {
                @Override
                public void onClinicClick(Clinic clinic) {
                    String password = resultsPwdEdit.getText().toString().trim();
                    if (PasswordHelper.validatePassword(password,clinic.clinicPasswordHash)) {
                        // The correct password for the clinic was entered. The employee can now join this clinic.
                        String chosenClinicId = clinic.clinicId;
                        // add employee to clinic in database
                        addEmployeeToClinicInDB(chosenClinicId,currentEmployeeId);
                        // status message
                        Toast.makeText(getApplicationContext(), "Successfully joined clinic.", Toast.LENGTH_LONG).show();
                        // close dialog
                        dialog.dismiss();
                        // Put this here to automatically return to employee account upon successfully joining a clinic
                        finish();
                    } else {
                        // Invalid password. Employee cannot join clinic.
                        Toast.makeText(getApplicationContext(), "Error: Invalid Password.", Toast.LENGTH_LONG).show();
                    }
                }
            });
            resultsRecycler.setAdapter(clinicAdapter);
            clinicAdapter.submitList(matchingClinics);

            // Cancel button onClick listener
            resultsCancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // simply close the dialog
                    dialog.dismiss();
                }
            });
        }

    }

    public void addEmployeeToClinicInDB(String selectedClinicID, final String selectedEmployeeId) {
        // This adds employee id to list stored in clinic in database
        final DatabaseReference clinicReference = mReference.child(clinicListPath).child(selectedClinicID);
        clinicReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Clinic clinicFromDB = dataSnapshot.getValue(Clinic.class);
                // add employee to clinic if not null
                if (clinicFromDB != null) {
                    clinicFromDB.addEmployee(selectedEmployeeId);
                    // save updated clinic to database
                    clinicReference.setValue(clinicFromDB);
                    // employee has successfully joined a clinic
                    hasJoinedClinic = true;
                } else {
                    // clinic could not be found in database
                    Toast.makeText(getApplicationContext(), "Error: Could not find clinic in database.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        // also set the clinic associated to employee in database to appropriate value
        final DatabaseReference clinicOfEmployeeRef = mReference.child(clinicOfEmployeePath).child(currentEmployeeId);
        clinicOfEmployeeRef.setValue(selectedClinicID);
    }

    public void createClinicOnClick(View view) {
        // Get information entered by user and validate it
        String Name = clinicNameEdit.getText().toString().trim();
        String Phone = clinicPhoneEdit.getText().toString().trim();
        String StreetAdd = clinicStreetAddEdit.getText().toString().trim();
        String Unit = clinicUnitEdit.getText().toString().trim();
        String Province = clinicProvinceEdit.getText().toString().trim();
        String City = clinicCityEdit.getText().toString().trim();
        String PostalCode = clinicPostalCodeEdit.getText().toString().trim();
        String Insurances = clinicInsuranceEdit.getText().toString().trim();
        String Payments = clinicPaymentEdit.getText().toString().trim();
        String Password = clinicPasswordEdit.getText().toString().trim();
        String ConfirmPassword = clinicConfirmPasswordEdit.getText().toString().trim();

        if (validClinicCreationInfo(Name, Phone, StreetAdd, Unit, Province, City, PostalCode,
                Insurances, Payments, Password, ConfirmPassword)) {
            Address address = new Address(Unit, StreetAdd, City, Province, PostalCode);
            DatabaseReference clinicListRef = mReference.child(clinicListPath);
            String id = clinicListRef.push().getKey();
            String passwordHash = PasswordHelper.hexHash(Password);
            Clinic clinic = new Clinic(id, Name, address, Phone, Insurances, Payments, passwordHash);
            // add current employee to employee list of clinic
            clinic.addEmployee(currentEmployeeId);
            // add clinic to database
            clinicListRef.child(id).setValue(clinic);
            // also set the clinic associated to employee in database to appropriate value
            DatabaseReference clinicOfEmployeeRef = mReference.child(clinicOfEmployeePath).child(currentEmployeeId);
            clinicOfEmployeeRef.setValue(id);
            // employee has now joined a clinic
            hasJoinedClinic = true;
        }

        // If employee has successfully created and joined clinic, close this activity
        if (hasJoinedClinic) {
            // status message
            Toast.makeText(getApplicationContext(), "Successfully created clinic.", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    public boolean validClinicCreationInfo(String name, String phone, String streetAdd,
                                           String unit, String province, String city,
                                           String postalCode, String insuranceTypes,
                                           String paymentMethods, String password,
                                           String confirmPassword) {
        boolean verdict = true;
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("Error:");
        // clinic name must be between 3 and 50 characters
        if (!iv.isValidName(name, 3, 50)) {
            verdict = false;
            errorMsg.append(" Invalid Clinic name.");
        }
        if (!iv.isValidPhoneNumber(phone)) {
            verdict = false;
            errorMsg.append(" Invalid phone number.");
        }
        if (!iv.isValidStreetAddress(streetAdd)) {
            verdict = false;
            errorMsg.append(" Invalid street address.");
        }
        if (!iv.isValidUnit(unit)) {
            verdict = false;
            errorMsg.append(" Invalid unit.");
        }
        if (!iv.isValidProvinceCode(province)) {
            verdict = false;
            errorMsg.append(" Invalid province code.");
        }
        if (!iv.isValidCity(city)) {
            verdict = false;
            errorMsg.append(" Invalid city name.");
        }
        if (!iv.isValidPostalCode(postalCode)) {
            verdict = false;
            errorMsg.append(" Invalid postal code.");
        }
        if (!iv.isValidInsuranceOrPayment(insuranceTypes)) {
            verdict = false;
            errorMsg.append(" Invalid insurance types.");
        }
        if (!iv.isValidInsuranceOrPayment(paymentMethods)) {
            verdict = false;
            errorMsg.append(" Invalid payment methods.");
        }
        if (!iv.isValidPassword(password)) {
            verdict = false;
            errorMsg.append(" Invalid password.");
        } else if (!password.equals(confirmPassword)) {
            verdict = false;
            errorMsg.append(" Both passwords must match.");
        }
        if (!verdict) {
            Toast.makeText(getApplicationContext(), errorMsg.toString(), Toast.LENGTH_LONG).show();
        }
        return verdict;
    }
}
