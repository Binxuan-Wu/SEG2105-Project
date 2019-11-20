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

public class EmployeeAccountActivity extends AppCompatActivity {

    // For Firebase Database and Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private DatabaseReference clinicRef;
    private ValueEventListener mclinicListener;
    private DatabaseReference mClinicIdReference;
    private ValueEventListener mClinicIdListener;
    private ValueEventListener mAvailableServicesListener;

    private final String clinicOfEmployeePath = "ClinicOfEmployee";
    private final String clinicListPath = "ClinicList";
    private final String servicesPath = "Services";
    private final String noClinicMsg = "None";

    //private EmployeeAccount employeeAccount;
    private String employeeUserId;
    private String clinicId;
    private Clinic clinic;

    // For list of selected services
    private static final int editVersion = 0;
    private static final int selectVersion = 1;
    private RecyclerView selectedServicesRecycler;
    private ServiceAdapter selectedServicesAdapter;
    private RecyclerView.LayoutManager selectedServicesLayoutManager;
    private List<Service> selectedServices = new ArrayList<>();
    private List<Service> availableServicesInDB = new ArrayList<>();
    private List<Service> remainingServices = new ArrayList<>();

    // Variables to access various view fields on screen
    private TextView clinicNameView;
    private TextView clinicAddressView;
    private TextView clinicPhoneView;
    private TextView clinicInsuranceView;
    private TextView clinicPaymentView;
    private TextView clinicInfoTitleView;
    private Button editHoursBtn;
    private Button addServiceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_account);


        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference();

        if (mUser == null) {
            // if no user is logged in, we should not be here
            Toast.makeText(getApplicationContext(), "Error: No user logged in", Toast.LENGTH_LONG).show();
            finish();
        } else {
            // Get employee Id and clinic id from database
            employeeUserId = mUser.getUid();

            // retrieve all available services from database
            ValueEventListener availableServicesListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    availableServicesInDB.clear();
                    for (DataSnapshot serviceSnapshot : dataSnapshot.getChildren()) {
                        Service service = serviceSnapshot.getValue(Service.class);
                        if (service != null) {
                            availableServicesInDB.add(service);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mReference.child(servicesPath).addValueEventListener(availableServicesListener);
            // store reference to remove at app stop
            mAvailableServicesListener = availableServicesListener;

            // make clinic id listener
            ValueEventListener clinicIdListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String clinicIdFromDB = (String) dataSnapshot.getValue();
                    if (clinicIdFromDB != null) {
                        clinicId = clinicIdFromDB;
                        //Toast.makeText(getApplicationContext(), "Clinic id from database is " + clinicIdFromDB + " and clinicId instance vble is " + clinicId, Toast.LENGTH_LONG).show();
                        getClinicInfo(clinicIdFromDB);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), "Error: Could not find clinic id in database", Toast.LENGTH_LONG).show();
                }
            };
            mClinicIdReference = mReference.child(clinicOfEmployeePath).child(employeeUserId);
            mClinicIdReference.addValueEventListener(clinicIdListener);
            // keep a copy to remove it when app stops
            mClinicIdListener = clinicIdListener;

            // Variables for views on screen
            clinicNameView = findViewById(R.id.EmpAccClinicNameView);
            clinicAddressView = findViewById(R.id.EmpAccClinicAddressView);
            clinicPhoneView = findViewById(R.id.EmpAccClinicPhoneView);
            clinicInsuranceView = findViewById(R.id.EmpAccClinicInsuranceView);
            clinicPaymentView = findViewById(R.id.EmpAccClinicPaymentView);
            clinicInfoTitleView = findViewById(R.id.ClinicInfoTitle);
            editHoursBtn = findViewById(R.id.EmpAccEditHoursBtn);
            addServiceBtn = findViewById(R.id.EmpAccAddServiceBtn);

            // for list of selected services
            // Use linear layout manager
            selectedServicesLayoutManager = new LinearLayoutManager(this);
            selectedServicesRecycler = findViewById(R.id.EmpAccServicesOfferedRecycler);
            selectedServicesRecycler.setLayoutManager(selectedServicesLayoutManager);

        }

    }

    /*@Override
    public void onStart() {
        super.onStart();

        *//*if (clinicId.equals(noClinicMsg)) {
            // if employee has no associated clinic, go immediately to CreateJoinClinicActivity
            startActivity(new Intent(getApplicationContext(), CreateJoinClinicActivity.class));
        } else {
            // if there is an associated clinic, display the information

            clinicRef = mReference.child(clinicListPath).child(clinicId);

            ValueEventListener clinicListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Clinic updatedClinic = dataSnapshot.getValue(Clinic.class);
                    clinic = updatedClinic;
                    updateDisplayedClinicInfo(clinic);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            // attach clinic listener
            clinicRef.addValueEventListener(clinicListener);
            // keep reference to value event listener so that we can remove it at app stop
            mclinicListener = clinicListener;

            // display information of clinic on screen -- not needed since it is called by the value event listener (I think)
            //updateDisplayedClinicInfo();
        }*//*

    }*/

    @Override
    public void onStop() {
        super.onStop();

        /*if (mclinicListener != null) {
            clinicRef.removeEventListener(mclinicListener);
        }*/

        if (mClinicIdListener != null) {
            mClinicIdReference.removeEventListener(mClinicIdListener);
        }

        if (mAvailableServicesListener != null) {
            mReference.child(servicesPath).removeEventListener(mAvailableServicesListener);
        }
    }

    public void getClinicInfo(String id) {
        //final String clinicIdForDB = id;

        if (id.equals(noClinicMsg)) {
            // no clinic set up
            clinicInfoTitleView.setText(R.string.No_clinic_msg);
            clinicInfoTitleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), CreateJoinClinicActivity.class));
                }
            });

            // hide everything else
            editHoursBtn.setVisibility(View.GONE);
            addServiceBtn.setVisibility(View.GONE);

        } else {
            // get reference to clinic in ClinicList in database and attach a value event listener
            mReference.child(clinicListPath).child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Clinic clinicFromDB = dataSnapshot.getValue(Clinic.class);
                    if (clinicFromDB != null) {
                        updateDisplayedClinicInfo(clinicFromDB);
                        /*clinicNameView.setText(String.format("Name: %s", clinicFromDB.clinicName));
                        clinicAddressView.setText(String.format("Address: %s", clinicFromDB.clinicAddress.printFormat()));
                        clinicPhoneView.setText(String.format("Phone number: %s", clinicFromDB.phone));
                        clinicInsuranceView.setText(String.format("Accepted insurance types: %s", clinicFromDB.insuranceTypes));
                        clinicPaymentView.setText(String.format("Accepted payment methods: %s", clinicFromDB.paymentMethods));*/
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

    }

    // Method to display clinic information on the screen
    // and update the locally stored clinic object
    public void updateDisplayedClinicInfo(Clinic clinicToDisplay) {
        // update stored clinic
        clinic = clinicToDisplay;
        // clinic information
        clinicNameView.setText(String.format("Name: %s", clinicToDisplay.clinicName));
        clinicAddressView.setText(String.format("Address: %s", clinicToDisplay.clinicAddress.printFormat()));
        clinicPhoneView.setText(String.format("Phone number: %s", clinicToDisplay.phone));
        clinicInsuranceView.setText(String.format("Accepted insurance types: %s", clinicToDisplay.insuranceTypes));
        clinicPaymentView.setText(String.format("Accepted payment methods: %s", clinicToDisplay.paymentMethods));
        if (clinicToDisplay.serviceIdList.size() >= 1) {
            // list of selected services
            selectedServicesAdapter = new ServiceAdapter(editVersion, new ServiceAdapter.OnServiceClickListener() {
                @Override
                public void onServiceClick(Service service) {
                    showDeleteServiceDialog(service);
                }
            });
            selectedServicesRecycler.setAdapter(selectedServicesAdapter);
            //getSelectedServicesFromDB();
            getSelectedServices();
            selectedServicesAdapter.submitList(selectedServices);
        }
    }

    // Method to show dialog to delete service
    public void showDeleteServiceDialog(Service service) {
        // update stored clinic from database before making any changes
        getClinicInfo(clinicId);

        final String serviceId = service.id;
        // make sure that service selected is indeed offered by the clinic (someone else may have
        // removed it), or there is no point in removing it
        if (clinic.serviceIdList.contains(serviceId)) {

            // Create dialog with appropriate layout
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.delete_dialog, null);
            dialogBuilder.setView(dialogView);

            // Declare variables for buttons in dialog
            final Button deleteBtn = (Button) dialogView.findViewById(R.id.delDialogDeleteBtn);
            final Button cancelBtn = (Button) dialogView.findViewById(R.id.delDialogCancelBtn);

            // Make dialog appear
            final AlertDialog dialog = dialogBuilder.create();
            dialog.show();

            // onClickListener for delete button
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteServiceFromClinic(serviceId);
                    dialog.dismiss();
                }
            });

            // onClickListener for cancel button
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
    }

    // Methods to add and delete selected services from clinic stored locally and stored in database,
    // and display changes on screen
    // Note: maybe I should get clinic info from database again to make sure no
    // other employee has changed things in the meantime?
    public void deleteServiceFromClinic(String id) {
        clinic.removeService(id);
        // update clinic in database
        mReference.child(clinicListPath).child(clinicId).setValue(clinic);
        // after deletion of service, need to update the displayed information
        updateDisplayedClinicInfo(clinic);
    }

    public void addServiceToClinic(String id) {
        clinic.addService(id);
        // update clinic in database
        mReference.child(clinicListPath).child(clinicId).setValue(clinic);
        // after adding service, need to update the displayed information
        updateDisplayedClinicInfo(clinic);
    }

    public void addServiceBtnClick (View view) {
        // update stored clinic from database before making any changes
        getClinicInfo(clinicId);
        showAddServiceDialog();
    }

    // Method to get list of services that have not yet been added to the clinic
    public void getNonSelectedServices() {
        remainingServices.clear();
        for (Service service : availableServicesInDB) {
            if (!clinic.serviceIdList.contains(service.id)) {
                remainingServices.add(service);
            }
        }
    }

    // Method to show a dialog that will display the list of all
    // services currently available in the database and allow the
    // employee to choose one of them to add to the clinic
    public void showAddServiceDialog() {

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

        // Hide irrelevant fields
        resultsPwdEdit.setVisibility(View.GONE);

        // retrieve all available services from database that have not yet been selected for the clinic
        getNonSelectedServices();

        // Set up recycler view to show all available services, provided there are some in the DB
        // that have not yet been chosen
        if (remainingServices.isEmpty()) {
            resultCountMsgView.setText("There are no more available services to choose from.");
            resultsActionMsgView.setVisibility(View.GONE);
            resultsRecycler.setVisibility(View.GONE);
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
            resultCountMsgView.setText(String.format("%d services match your search.", remainingServices.size()));
            resultsActionMsgView.setText("Select the service to be added below.");
            RecyclerView.LayoutManager searchResultsLayoutManager = new LinearLayoutManager(this);
            resultsRecycler.setLayoutManager(searchResultsLayoutManager);
            // Make dialog appear
            final AlertDialog dialog = dialogBuilder.create();
            dialog.show();
            // set up service adapter
            ServiceAdapter serviceAdapter = new ServiceAdapter(selectVersion, new ServiceAdapter.OnServiceClickListener() {
                @Override
                public void onServiceClick(Service service) {
                    addServiceToClinic(service.id);
                    dialog.dismiss();
                }
            });
            // attach adapter to the recycler
            resultsRecycler.setAdapter(serviceAdapter);
            // give adapter list of services to display
            serviceAdapter.submitList(remainingServices);

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

    // method to retrieve clinic id from database and set the clinicId instance variable accordingly
    // note that this will only work if the employeeUserId has been initialised properly beforehand
    public void retrieveClinicIdFromDB (String employeeId) {
        DatabaseReference clinicReference = mReference.child(clinicOfEmployeePath).child(employeeId);

        // Get clinic id associated to employee
        clinicReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String clinicIdFromDB = (String) dataSnapshot.getValue();
                if (clinicIdFromDB != null) {
                    clinicId = clinicIdFromDB;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // something went wrong
            }
        });
    }

    // Method to retrieve and return list of Service objects corresponding to the ids in the
    // clinic.serviceIdList from the database
    public void getSelectedServicesFromDB () {
        if (clinic.serviceIdList.size() >= 1) {
            DatabaseReference servicesReference = mReference.child(servicesPath);
            selectedServices.clear();
            for (String serviceId : clinic.serviceIdList) {
                servicesReference.child(serviceId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Service service = dataSnapshot.getValue(Service.class);
                        selectedServices.add(service);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }
    // or maybe just get them from the availableServicesInDB list?
    public void getSelectedServices () {
        selectedServices.clear();
        if (clinic.serviceIdList.size() >= 1) {
            for (Service service : availableServicesInDB) {
                if (clinic.serviceIdList.contains(service.id)) {
                    selectedServices.add(service);
                }
            }
        }
    }

    public void goToCreateJoinClinic(View view) {
        startActivity(new Intent(getApplicationContext(), CreateJoinClinicActivity.class));
    }

}
