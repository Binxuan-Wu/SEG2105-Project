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

import java.time.LocalTime;
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

    // database constants (note: moved to DBHelper class; should replace with static variables from
    // that class)
    private final String clinicOfEmployeePath = "ClinicOfEmployee";
    private final String clinicListPath = "ClinicList";
    private final String servicesPath = "Services";
    private final String noClinicMsg = "None";

    // Employee account-related objects stored here
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
    private TextView workingHoursDesc;
    private TextView servicesDesc;
    private TextView mondayView;
    private TextView tuesdayView;
    private TextView wednesdayView;
    private TextView thursdayView;
    private TextView fridayView;
    private TextView saturdayView;
    private TextView sundayView;
    private TextView mondayDesc;
    private TextView tuesdayDesc;
    private TextView wednesdayDesc;
    private TextView thursdayDesc;
    private TextView fridayDesc;
    private TextView saturdayDesc;
    private TextView sundayDesc;
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
            workingHoursDesc = findViewById(R.id.EmpAccClinicWorkingHoursTitle);
            servicesDesc = findViewById(R.id.EmpAccServicesOfferedTitle);
            mondayView = findViewById(R.id.EmpAccMondayView);
            tuesdayView = findViewById(R.id.EmpAccTuesdayView);
            wednesdayView = findViewById(R.id.EmpAccWednesdayView);
            thursdayView = findViewById(R.id.EmpAccThursdayView);
            fridayView = findViewById(R.id.EmpAccFridayView);
            saturdayView = findViewById(R.id.EmpAccSaturdayView);
            sundayView = findViewById(R.id.EmpAccSundayView);
            mondayDesc = findViewById(R.id.EmpAccMondayDesc);
            tuesdayDesc = findViewById(R.id.EmpAccTuesdayDesc);
            wednesdayDesc = findViewById(R.id.EmpAccWednesdayDesc);
            thursdayDesc = findViewById(R.id.EmpAccThursdayDesc);
            fridayDesc = findViewById(R.id.EmpAccFridayDesc);
            saturdayDesc = findViewById(R.id.EmpAccSaturdayDesc);
            sundayDesc = findViewById(R.id.EmpAccSundayDesc);
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

    public void getClinicInfo(String clinicIdToSearch) {
        //final String clinicIdForDB = id;

        if (clinicIdToSearch.equals(noClinicMsg)) {
            // no clinic set up
            clinicInfoTitleView.setText(R.string.No_clinic_msg);
            clinicInfoTitleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    startActivity(new Intent(getApplicationContext(), CreateJoinClinicActivity.class));
                }
            });

            // hide everything else
            workingHoursDesc.setVisibility(View.GONE);
            servicesDesc.setVisibility(View.GONE);
            mondayDesc.setVisibility(View.GONE);
            tuesdayDesc.setVisibility(View.GONE);
            wednesdayDesc.setVisibility(View.GONE);
            thursdayDesc.setVisibility(View.GONE);
            fridayDesc.setVisibility(View.GONE);
            saturdayDesc.setVisibility(View.GONE);
            sundayDesc.setVisibility(View.GONE);
            editHoursBtn.setVisibility(View.GONE);
            addServiceBtn.setVisibility(View.GONE);

        } else {
            // get reference to clinic in ClinicList in database and attach a value event listener
            mReference.child(clinicListPath).child(clinicIdToSearch).addListenerForSingleValueEvent(new ValueEventListener() {
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
        clinicNameView.setText(String.format("Name: %s", clinic.clinicName));
        clinicAddressView.setText(String.format("Address: %s", clinic.clinicAddress.printFormat()));
        clinicPhoneView.setText(String.format("Phone number: %s", clinic.phone));
        clinicInsuranceView.setText(String.format("Accepted insurance types: %s", clinic.insuranceTypes));
        clinicPaymentView.setText(String.format("Accepted payment methods: %s", clinic.paymentMethods));
        // working hours
        mondayView.setText(clinic.getOpenTimePrintFormatForDay(1));
        tuesdayView.setText(clinic.getOpenTimePrintFormatForDay(2));
        wednesdayView.setText(clinic.getOpenTimePrintFormatForDay(3));
        thursdayView.setText(clinic.getOpenTimePrintFormatForDay(4));
        fridayView.setText(clinic.getOpenTimePrintFormatForDay(5));
        saturdayView.setText(clinic.getOpenTimePrintFormatForDay(6));
        sundayView.setText(clinic.getOpenTimePrintFormatForDay(7));

        // list of selected services
        if (clinic.serviceIdList.size() >= 1) {
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

    public void setWorkingHoursOfClinic(List<OpenTime> newTimes) {
        clinic.setOpenTimes(newTimes);
        // update clinic in database
        mReference.child(clinicListPath).child(clinicId).setValue(clinic);
        // after updating working hours, need to update the displayed information
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
    // services currently available in the database (that the employee has not yet added to the
    // clinic) and allow the employee to choose one of them to add to the clinic
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
    // We do this via a Value Event Listener instead
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
    // We won't do this, but rather use the method below instead.
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

    // Method to retrieve and return list of Service objects corresponding to the ids in the
    // clinic.serviceIdList from the availableServicesInDB list
    // Instead of removing serviceId from clinic in database if service has been removed by admin,
    // admin should remove serviceId from all clinics
    // I try to update clinic in database in method below, app crashes when we try to open EmployeeAccountActivity
    public void getSelectedServices () {
        selectedServices.clear();
        //boolean serviceRemoved = false;
        if (clinic.serviceIdList.size() >= 1) {
            /*for (Service service : availableServicesInDB) {
                if (clinic.serviceIdList.contains(service.id)) {
                    selectedServices.add(service);
                }
            }*/
            for (String Id : clinic.serviceIdList) {
                // find the Service object with this id in the list of services that are in the DB
                Service service = findServiceById(Id, availableServicesInDB);
                if (service != null) {
                    // if this service indeed exists in the database, add it to the list of
                    // selected services
                    selectedServices.add(service);
                } /*else {
                    // this Service is no longer in the DB (the admin user must have deleted it)
                    // so, remove it from the clinic.serviceIdList
                    clinic.removeService(Id);
                    // we now need to update the clinic in the database
                    serviceRemoved = true;
                }*/
            }
            /*if (serviceRemoved) {
                // at least one service has been removed, so we need to update the clinic in the
                // database
                mReference.child(clinicListPath).child(clinicId).setValue(clinic);
            }*/
        }
    }

    // method to search for a Service having a given id attribute in a list of Service objects
    // this method assumes that the Service objects in the provided list all have distinct id's
    // method returns null if no Service in the list has the given id
    public Service findServiceById (String idToSearch, List<Service> listToSearch) {
        Service result = null;
        for (Service service : listToSearch) {
            if (service.id.equals(idToSearch)) {
                result = service;
            }
        }
        return result;
    }

    public void workingHoursEditBtnOnClick (View view) {
        showUpdateWorkingHoursDialog();
    }

    // method to show dialog through which we can set the opening hours of the clinic
    public void showUpdateWorkingHoursDialog () {
        // Create dialog with appropriate layout
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.working_hours_dialog, null);
        dialogBuilder.setView(dialogView);

        // Declare variables for elements in the dialog
        final EditText monOT = dialogView.findViewById(R.id.whDialogMonOTEdit);
        final EditText monCT = dialogView.findViewById(R.id.whDialogMonCTEdit);
        final EditText tueOT = dialogView.findViewById(R.id.whDialogTueOTEdit);
        final EditText tueCT = dialogView.findViewById(R.id.whDialogTueCTEdit);
        final EditText wedOT = dialogView.findViewById(R.id.whDialogWedOTEdit);
        final EditText wedCT = dialogView.findViewById(R.id.whDialogWedCTEdit);
        final EditText thuOT = dialogView.findViewById(R.id.whDialogThuOTEdit);
        final EditText thuCT = dialogView.findViewById(R.id.whDialogThuCTEdit);
        final EditText friOT = dialogView.findViewById(R.id.whDialogFriOTEdit);
        final EditText friCT = dialogView.findViewById(R.id.whDialogFriCTEdit);
        final EditText satOT = dialogView.findViewById(R.id.whDialogSatOTEdit);
        final EditText satCT = dialogView.findViewById(R.id.whDialogSatCTEdit);
        final EditText sunOT = dialogView.findViewById(R.id.whDialogSunOTEdit);
        final EditText sunCT = dialogView.findViewById(R.id.whDialogSunCTEdit);
        final Button updateBtn = dialogView.findViewById(R.id.whDialogUpdateBtn);
        final Button cancelBtn = dialogView.findViewById(R.id.whDialogCancelBtn);

        // Make dialog appear
        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        // add onClick listeners to the buttons
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean errorFound = false;
                String monOpen = monOT.getText().toString().trim();
                String monClosed = monCT.getText().toString().trim();
                String tueOpen = tueOT.getText().toString().trim();
                String tueClosed = tueCT.getText().toString().trim();
                String wedOpen = wedOT.getText().toString().trim();
                String wedClosed = wedCT.getText().toString().trim();
                String thuOpen = thuOT.getText().toString().trim();
                String thuClosed = thuCT.getText().toString().trim();
                String friOpen = friOT.getText().toString().trim();
                String friClosed = friCT.getText().toString().trim();
                String satOpen = satOT.getText().toString().trim();
                String satClosed = satCT.getText().toString().trim();
                String sunOpen = sunOT.getText().toString().trim();
                String sunClosed = sunCT.getText().toString().trim();

                String[] inputTimes = {monOpen, monClosed, tueOpen, tueClosed, wedOpen, wedClosed,
                        thuOpen, thuClosed, friOpen, friClosed, satOpen, satClosed, sunOpen, sunClosed};

                List<OpenTime> newTimes = getInitialOpenTimes();
                for (int i = 1; i <= 7 && !errorFound; i ++) {
                    int index = indexOfDayCode(i);
                    OpenTime newTime;
                    if (inputTimes[index].isEmpty() && inputTimes[index + 1].isEmpty()) {
                        newTime = new OpenTime(i);
                    } else if (OpenTime.validTime(inputTimes[index]) && OpenTime.validTime(inputTimes[index + 1])) {
                        newTime = new OpenTime(i,inputTimes[index],inputTimes[index + 1]);
                        if (!newTime.isValid()) {
                            errorFound = true;
                        } else {
                            newTimes.set(i-1,newTime);
                        }
                    } else {
                        errorFound = true;
                    }

                }

                if (errorFound) {
                    Toast.makeText(getApplicationContext(), "Error: Invalid working hours.", Toast.LENGTH_LONG).show();
                } else {
                    setWorkingHoursOfClinic(newTimes);
                    Toast.makeText(getApplicationContext(), "Successfully updated working hours.", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }
        });

    }


    public int indexOfDayCode (int i) {
        return (i - 1) * 2;
    }

    public List<OpenTime> getInitialOpenTimes () {
        List<OpenTime> initialTimes = new ArrayList<>();
        for (int i = 1; i<=7; i++) {
            OpenTime openTime = new OpenTime(i);
            initialTimes.add(openTime);
        }
        return initialTimes;
    }

}
