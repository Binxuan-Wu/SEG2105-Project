package com.ysteimle.segproject.easywalkin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ClinicSearchActivity extends AppCompatActivity {

    // For Firebase Database and Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private ValueEventListener mClinicListener;

    // for search
    private InputValidator iv = new InputValidator();
    // all available clinics in DB
    private List<Clinic> clinicsInDB = new ArrayList<>();
    // for recycler showing list of clinics matching search term
    private final int selectClinic = 1;
    private Clinic selectedClinic;

    // for clinic id where user is currently on waiting list
    private String currentUserId;
    private String currentWaitingListClinicId;
    private ValueEventListener mClinicIdListener;

    // for search results
    private List<WaitEntry> waitingListOfSelectedClinic = new ArrayList<>();

    // Variables for the views on screen
    // search fields
    private Group searchFieldsGroup;
    private EditText citySearchEdit;
    private EditText provinceSearchEdit;
    private EditText serviceSearchEdit;
    // search results
    private Group searchResultGroup;
    private TextView clinicNameView;
    private TextView clinicWaitingTimeView;
    private TextView clinicAddressView;
    private TextView clinicPhoneView;
    private TextView clinicInsuranceView;
    private TextView clinicPaymentView;
    //private TextView workingHoursDesc;
    private TextView servicesDesc;
    private TextView servicesView;
    private TextView mondayView;
    private TextView tuesdayView;
    private TextView wednesdayView;
    private TextView thursdayView;
    private TextView fridayView;
    private TextView saturdayView;
    private TextView sundayView;
    private Button checkInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clinic_search);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference();
        currentUserId = mUser.getUid();

        /*// get search intent
        Intent searchIntent = getIntent();
        // default value is false
        canCheckIn = searchIntent.getBooleanExtra("canCheckIn",false);*/

        if (mUser == null) {
            // if no user is logged in, we should not be here
            Toast.makeText(getApplicationContext(), "Error: No user logged in", Toast.LENGTH_LONG).show();
            finish();
        } else {

            // associate variables to view on screen
            // search fields
            searchFieldsGroup = findViewById(R.id.ClinicSearchFieldsGroup);
            searchFieldsGroup.setVisibility(View.VISIBLE); // we want to search, so search fields should be visible
            citySearchEdit = findViewById(R.id.ClinicSearchCityEdit);
            provinceSearchEdit = findViewById(R.id.ClinicSearchProvinceEdit);
            serviceSearchEdit = findViewById(R.id.ClinicSearchServiceEdit);

            // search result views
            searchResultGroup = findViewById(R.id.ClinicSearchResultGroup);
            // Variables for views on screen
            clinicWaitingTimeView = findViewById(R.id.SearchResultWaitingTimeView);
            clinicNameView = findViewById(R.id.SearchResultNameView);
            clinicAddressView = findViewById(R.id.SearchResultAddressView);
            clinicPhoneView = findViewById(R.id.SearchResultPhoneView);
            clinicInsuranceView = findViewById(R.id.SearchResultInsuranceView);
            clinicPaymentView = findViewById(R.id.SearchResultPaymentView);
            //workingHoursDesc = findViewById(R.id.SearchResultWorkingHoursTitle);
            servicesDesc = findViewById(R.id.SearchResultServicesOfferedTitle);
            servicesView = findViewById(R.id.SearchResultServicesView);
            mondayView = findViewById(R.id.SearchResultMondayView);
            tuesdayView = findViewById(R.id.SearchResultTuesdayView);
            wednesdayView = findViewById(R.id.SearchResultWednesdayView);
            thursdayView = findViewById(R.id.SearchResultThursdayView);
            fridayView = findViewById(R.id.SearchResultFridayView);
            saturdayView = findViewById(R.id.SearchResultSaturdayView);
            sundayView = findViewById(R.id.SearchResultSundayView);
            checkInBtn = findViewById(R.id.AddToWaitingListBtn);

            searchResultGroup.setVisibility(View.GONE); // we have not searched yet, so search results should not be visible

            // value event listener for the clinic id of the waiting list the patient is on
            ValueEventListener waitingListClinicIdListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String fromDB = (String) dataSnapshot.getValue();
                    if (fromDB != null) {
                        currentWaitingListClinicId = fromDB;
                    } else {
                        currentWaitingListClinicId = DBHelper.noClinicMsg;
                    }
                    Toast.makeText(getApplicationContext(), "Currently on waiting list of clinic with id " + currentWaitingListClinicId, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mReference.child(DBHelper.waitingListByPatient).child(currentUserId).addValueEventListener(waitingListClinicIdListener);
            mClinicIdListener = waitingListClinicIdListener;

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
            mReference.child(DBHelper.clinicListPath).addValueEventListener(clinicListener);
            mClinicListener = clinicListener;
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // remove value event listeners added to the database
        if (mClinicListener != null) {
            mReference.child(DBHelper.clinicListPath).removeEventListener(mClinicListener);
        }

        if (mClinicIdListener != null && currentUserId != null) {
            mReference.child(DBHelper.waitingListByPatient).child(currentUserId).removeEventListener(mClinicIdListener);
        }
    }

    // method to search for all clinics in a certain location
    public List<Clinic> searchClinicsByLocation(String cityName, String provinceCode) {
        List<Clinic> matchingClinics = new ArrayList<>();
        for (Clinic clinic : clinicsInDB) {
            if (clinic.isAtLocation(cityName,provinceCode)) {
                matchingClinics.add(clinic);
            }
        }
        return matchingClinics;
    }

    // Method to search for all clinics offering a service with a given id
    // Note: to search for all clinics offering a certain service, we must first determine if this service
    // exists and, if it exists, what the id of the service is. Might be simpler to just select a
    // service from a list of available services
    public List<Clinic> searchClinicsByServiceId(String serviceIdToSearch) {
        List<Clinic> matchingClinics = new ArrayList<>();
        for (Clinic clinic : clinicsInDB) {
            if (clinic.offersServiceWithID(serviceIdToSearch)) {
                matchingClinics.add(clinic);
            }
        }
        return matchingClinics;
    }

    // method to find clinics that are currently open
    public List<Clinic> findOpenClinics() {
        // determine what day of the week it is right now
        int currentDayCode = LocalDate.now().getDayOfWeek().getValue();
        // determine what time it is right now
        String currentTime = LocalTime.now().toString();
        List<Clinic> matchingClinics = new ArrayList<>();
        for (Clinic clinic : clinicsInDB) {
            if (clinic.isOpenAtDateTime(currentDayCode,currentTime)) {
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
            resultsActionMsgView.setText("Select a clinic in the list below.");
            // Hide password field as it is not needed here
            resultsPwdEdit.setVisibility(View.GONE);
            // Use linear layout manager
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            resultsRecycler.setLayoutManager(layoutManager);
            // Make dialog appear
            final AlertDialog dialog = dialogBuilder.create();
            dialog.show();
            // set up clinic adapter and attach it to the recycler
            ClinicAdapter clinicAdapter = new ClinicAdapter(selectClinic, new ClinicAdapter.OnClinicClickListener() {
                @Override
                public void onClinicClick(Clinic clinic) {
                    // store chosen clinic object and display it on screen
                    selectedClinic = clinic;
                    // display clinic info
                    displaySelectedClinicInfo();
                    // close dialog
                    dialog.dismiss();
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

    // method to display the selected clinic information
    // also hides the search fields
    public void displaySelectedClinicInfo() {
        // hide search fields
        searchFieldsGroup.setVisibility(View.GONE);
        // show search result views
        searchResultGroup.setVisibility(View.VISIBLE);
        // display clinic info
        clinicNameView.setText(String.format("Clinic name: %s",selectedClinic.clinicName));
        clinicAddressView.setText(String.format("Address: %s",selectedClinic.clinicAddress.printFormat()));
        clinicPhoneView.setText(String.format("Phone: %s",selectedClinic.phone));
        clinicInsuranceView.setText(String.format("Accepted insurance types: %s",selectedClinic.insuranceTypes));
        clinicPaymentView.setText(String.format("Accepted payment methods: %s",selectedClinic.paymentMethods));
        // hours
        mondayView.setText(selectedClinic.getOpenTimePrintFormatForDay(1));
        tuesdayView.setText(selectedClinic.getOpenTimePrintFormatForDay(2));
        wednesdayView.setText(selectedClinic.getOpenTimePrintFormatForDay(3));
        thursdayView.setText(selectedClinic.getOpenTimePrintFormatForDay(4));
        fridayView.setText(selectedClinic.getOpenTimePrintFormatForDay(5));
        saturdayView.setText(selectedClinic.getOpenTimePrintFormatForDay(6));
        sundayView.setText(selectedClinic.getOpenTimePrintFormatForDay(7));

        // for now, don't display services offered
        servicesDesc.setVisibility(View.GONE);
        servicesView.setVisibility(View.GONE);

        // waiting time
        boolean canAddAPatientToWaitingList = computeDisplayWaitingTimeForSelectedClinic();

        // set visibility of check-in button according to if clinic is open and has space on
        // waiting list
        // Note: if patient tries to check in to clinic while they are checked-in with some clinic,
        // this won't work
        if (canAddAPatientToWaitingList) {
            checkInBtn.setVisibility(View.VISIBLE);
        } else {
            checkInBtn.setVisibility(View.GONE);
        }
    }

    // method to compute and display expected waiting time for selected clinic
    // returns true if and only if a patient can add themselves to waiting list
    public boolean computeDisplayWaitingTimeForSelectedClinic() {
        // info needed
        String selectedClinicId = selectedClinic.clinicId;
        LocalDate today = LocalDate.now();
        int todayCode = today.getDayOfWeek().getValue();

        // check if clinic is even open at the moment. if it is closed, there is no point in computing anything
        if (!selectedClinic.isOpenAtDateTime(todayCode,LocalTime.now().toString())) {
            // clinic is closed. clear waiting list and return false as patient cannot be added to waiting list
            clinicWaitingTimeView.setText("Clinic closed");
            clearWaitingListOfClinic();
            return false;
        } else {
            // clinic is open right now, so continue
            LocalTime selectedClinicClosingTime = getSelectedClinicClosingTimeForDay(todayCode);

            if (selectedClinicClosingTime != null && LocalTime.now().isBefore(selectedClinicClosingTime)) {
                // retrieve and update the waiting list of the selected clinic
                // and get expected waiting time in minutes
                LocalTime expectedTimeForLastPatient = updateWaitingListOfClinic(selectedClinicId, selectedClinicClosingTime);
                if (expectedTimeForLastPatient != null) {
                    if (expectedTimeForLastPatient.plus(30, ChronoUnit.MINUTES).isAfter(selectedClinicClosingTime)) {
                        // we use 30 minutes here because both the last patient on the list and the
                        // prospective new patient would need 15 minutes, so both must be able to be seen
                        // before closing time
                        // no more patients can be accepted today
                        clinicWaitingTimeView.setText("Not currently accepting patients");
                        return false;
                    } else {
                        int expectedWaitingTime = getExpectedWaitingTime(expectedTimeForLastPatient);
                        // display expected waiting time on screen
                        clinicWaitingTimeView.setText(String.format("%d minutes", expectedWaitingTime));
                        return true;
                    }
                } else {
                    // no patients in waiting list, so need to check if there is enough time left today
                    // (note that we use 20 minutes here since we will give the patient an expected
                    // appointment time 5 minutes from now to allow them time to arrive
                    if (LocalTime.now().plus(20, ChronoUnit.MINUTES).isAfter(selectedClinicClosingTime)) {
                        // no more time today
                        clinicWaitingTimeView.setText("Not currently accepting patients");
                        return false;
                    } else {
                        // patient can be seen today and expected waiting time is 0 minutes
                        clinicWaitingTimeView.setText("0 minutes");
                        return true;
                    }
                }
            } else {
                // the selected clinic is closed right now
                clinicWaitingTimeView.setText("Not currently accepting patients");
                return false;
            }
        }
    }

    // method to clear all entries from waiting list of patient
    // that is, we remove all entries from the database
    public void clearWaitingListOfClinic() {
        getWaitingListFromDB(selectedClinic.clinicId,true);
    }

    // method to compute waiting time. Returns -1 if waiting list is full or
    // clinic is closed
    public int computeWaitingTime(LocalTime lastPatientTime, LocalTime closingTime) {
        if (closingTime != null && LocalTime.now().isBefore(closingTime)) {
            if (lastPatientTime != null) {
                if (lastPatientTime.plus(30,ChronoUnit.MINUTES).isAfter(closingTime)) {
                    // waiting list full
                    return -1;
                } else {
                    // there is space
                    return (int) LocalTime.now().until(lastPatientTime.plus(15,ChronoUnit.MINUTES),ChronoUnit.MINUTES);
                }
            } else {
                // nobody on waiting list, check that there is time left
                // (note that we use 20 minutes here since we will give the patient an expected
                // appointment time 5 minutes from now to allow them time to arrive
                if (LocalTime.now().plus(20,ChronoUnit.MINUTES).isAfter(closingTime)) {
                    // no more time today
                    return -1;
                } else {
                    // patient can go in immediately (although we will give appointment time 5
                    // minutes after current time)
                    return 0;
                }
            }
        } else {
            // clinic is closed right now
            return -1;
        }
    }

    // method to clear search fields
    public void clearSearchFields() {
        citySearchEdit.setText("");
        provinceSearchEdit.setText("");
        serviceSearchEdit.setText("");
    }

    // onClick Method for the currently open clinics search buttons
    public void onSearchOpenClinicsClick(View view) {
        clearSearchFields();
        List<Clinic> searchResults = findOpenClinics();
        // display search results
        showClinicSearchResultsDialog(searchResults);
    }

    // onClick Method for the search by location button
    public void onSearchClinicsByLocationClick(View view) {
        String cityToSearch = citySearchEdit.getText().toString().trim();
        String provinceToSearch = provinceSearchEdit.getText().toString().trim();

        // validate user input
        if (iv.isValidCity(cityToSearch) && iv.isValidProvinceCode(provinceToSearch)) {
            clearSearchFields();
            List<Clinic> searchResults = searchClinicsByLocation(cityToSearch,provinceToSearch);
            showClinicSearchResultsDialog(searchResults);
        } else {
            Toast.makeText(this, "Error: Invalid location.", Toast.LENGTH_LONG).show();
        }
    }

    // onClick method for the new search button
    public void newSearchOnClick(View view){
        // Note: not sure if clearing search result views is necessary
        // hide search result views
        searchResultGroup.setVisibility(View.GONE);
        // show search fields
        searchFieldsGroup.setVisibility(View.VISIBLE);
    }

    // onclick method for the cancel button
    public void cancelBtnOnClick(View view) {
        finish();
    }

    // onClick method for the check-in button
    public void checkInBtnOnClick(View view) {
        // update waiting list and waiting time before adding patient and double-check if patient
        // can be added to waiting list
        boolean canAddPatientToWaitingList = computeDisplayWaitingTimeForSelectedClinic();
        if (canAddPatientToWaitingList) {
            addPatientToWaitingListOfSelectedClinic();
        } else {
            // patient could not be added to waiting list
            Toast.makeText(getApplicationContext(), "Error: cannot check-in remotely for this clinic.", Toast.LENGTH_LONG).show();
        }
    }

    // method to add patient to waiting list of selected clinic
    // should only be called if we are actually allowed to add a patient to the waiting list
    // and after we have updated the waiting list of the selected clinic
    // note: if patient is currently checked-in at a different clinic, this won't work
    public void addPatientToWaitingListOfSelectedClinic() {
        try {
            String relevantClinicId = selectedClinic.clinicId;
            String relevantPatientId = mUser.getUid();
            String currentClinicId = currentWaitingListClinicId;
            // check that patient is not already on a waiting list
            if (currentClinicId != null && !currentClinicId.equals(DBHelper.noClinicMsg)) {
                // patient cannot be on waiting list twice
                Toast.makeText(getApplicationContext(), "Error: You are already on the waiting list for a clinic.", Toast.LENGTH_LONG).show();
            } else {
                // get id for WaitEntry object
                DatabaseReference selectedWaitingListRef = mReference.child(DBHelper.waitingListByClinic)
                        .child(relevantClinicId);
                String entryId = selectedWaitingListRef.push().getKey();
                if (entryId != null) {
                    mReference.child(DBHelper.waitingListByPatient).child(relevantPatientId).setValue(relevantClinicId);
                    WaitEntry entry = new WaitEntry(entryId, relevantPatientId, relevantClinicId, LocalDate.now().toString(), LocalTime.now().toString());
                    // set expected appointment time
                    if (waitingListOfSelectedClinic.isEmpty()) {
                        // if no patients currently in waiting list, we set the expected time to be 5 minutes
                        // from now
                        entry.setExpectedAppDate(LocalDate.now().toString());
                        entry.setExpectedAppTime(LocalTime.now().plus(5, ChronoUnit.MINUTES).toString());
                    } else {
                        LocalTime lastPatientTime = LocalTime.parse(waitingListOfSelectedClinic.get(waitingListOfSelectedClinic.size() - 1).expectedAppTime);
                        String expectedTime = lastPatientTime.plus(15, ChronoUnit.MINUTES).toString();
                        entry.setExpectedAppDate(LocalDate.now().toString());
                        entry.setExpectedAppTime(expectedTime);
                    }
                    // add to database
                    selectedWaitingListRef.child(entryId).setValue(entry);

                    // finish this activity and return to patient account
                    Toast.makeText(getApplicationContext(), "Remote check-in successful.", Toast.LENGTH_LONG).show();

                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Error: Could not check-in remotely to this clinic (could not generate WaitEntry id).", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error: Could not check-in remotely to this clinic.", Toast.LENGTH_LONG).show();
        }

    }

    // method to retrieve waiting list for a given clinic from the database
    public void getWaitingListFromDB(final String clinicId, final boolean clear) {
        waitingListOfSelectedClinic.clear();
        mReference.child(DBHelper.waitingListByClinic).child(clinicId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    WaitEntry waitEntry = snapshot.getValue(WaitEntry.class);
                    if (waitEntry != null) {
                        waitingListOfSelectedClinic.add(waitEntry);
                    }
                }
                if (clear) {
                    // we want to clear (empty) the waiting list
                    for (WaitEntry entry : waitingListOfSelectedClinic) {
                        // set waitingListOfPatient value to "None" in database
                        mReference.child(DBHelper.waitingListByPatient).child(entry.patientId).setValue(DBHelper.noClinicMsg);
                    }
                    // remove entire waiting list of given clinic from database
                    mReference.child(DBHelper.waitingListByClinic).child(clinicId).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // returns closing time of the selected clinic
    public LocalTime getSelectedClinicClosingTimeForDay(int dayCode) {
        String closingTime = selectedClinic.getOpenTimeForDay(dayCode).CT;
        if (OpenTime.validTime(closingTime)) {
            return LocalTime.parse(closingTime);
        } else {
            return null;
        }
    }

    // method to update the waiting list for a specified clinic with given closing time
    // and return the expected appointment time of the last patient on the waiting list or null
    // if there are no patients on the waiting list
    // Note: something to check that the clinic is actually open at the moment is in the method that calls this one
    public LocalTime updateWaitingListOfClinic(String clinicId, LocalTime closingTime) {
        try {
            // retrieve waiting list from database, but do not clear it
            getWaitingListFromDB(clinicId, false);
            // get current date and time
            LocalDate today = LocalDate.now();
            int todayCode = today.getDayOfWeek().getValue();
            LocalTime currentTime = LocalTime.now();

            // last patient must be seen 15 minutes before closing time
            LocalTime maxTime = closingTime.minus(15,ChronoUnit.MINUTES);
            if (maxTime != null) {

                // list of ids of patients that were removed from the waiting list
                List<String> removedFromWaitingList = new ArrayList<>();

                // remove outdated entries from the waiting list
                for (Iterator<WaitEntry> waitEntryIterator = waitingListOfSelectedClinic.iterator(); waitEntryIterator.hasNext();) {
                    WaitEntry entry = waitEntryIterator.next();
                    if (entry.hasValidDatesTimes()) {
                        if (!LocalDate.parse(entry.expectedAppDate).isEqual(today)) {
                            // the patient was on the waiting list for a day other than today, so remove entry
                            removedFromWaitingList.add(entry.patientId);
                            // remove last entry returned by iterator next() method
                            waitEntryIterator.remove();
                        } else {
                            // if expected appointment date is today, must check the time
                            if (LocalTime.parse(entry.expectedAppTime).isBefore(currentTime) || LocalTime.parse(entry.expectedAppTime).isAfter(maxTime)) {
                                // the patient has been seen at some earlier time today or expected
                                // appointment time is after the last time a patient can be seen,
                                // so remove entry
                                removedFromWaitingList.add(entry.patientId);
                                // remove last entry returned by iterator next() method
                                waitEntryIterator.remove();
                            }
                        }
                    }
                }

                if (waitingListOfSelectedClinic.isEmpty()) {
                    // the waiting list is now empty
                    // return null
                    return null;
                } else {
                    // sort the remaining entries in the waiting list in order of date and time requested
                    WaitEntryComparator waitEntryComparator = new WaitEntryComparator();
                    waitingListOfSelectedClinic.sort(waitEntryComparator);

                    // find expected appointment time of first entry in list and set all the other expected
                    // times accordingly, counting a default of 15 minutes per patient
                    LocalTime startTime = LocalTime.parse(waitingListOfSelectedClinic.get(0).expectedAppTime);
                    // time at which last patient is expected to be seen
                    LocalTime lastTime = startTime;
                    for (int i = 1; i < waitingListOfSelectedClinic.size(); i++) {
                        // number of minutes to add to startTime
                        int addedMinutes = i * 15;
                        LocalTime newTime = startTime.plus(addedMinutes, ChronoUnit.MINUTES);
                        waitingListOfSelectedClinic.get(i).setExpectedAppTime(newTime.toString());
                        lastTime = newTime;
                    }

                    // update waiting list in database
                    // update waiting list in database
                    DatabaseReference waitingListOfClinicRef = mReference.child(DBHelper.waitingListByClinic).child(clinicId);
                    // remove old waiting list
                    waitingListOfClinicRef.removeValue();
                    // add updated waiting list
                    for (WaitEntry entry : waitingListOfSelectedClinic) {
                        waitingListOfClinicRef.child(entry.entryId).setValue(entry);
                    }

                    // update status of patients whose WaitEntries were removed
                    for (String removedId : removedFromWaitingList) {
                        mReference.child(DBHelper.waitingListByPatient).child(removedId).setValue(DBHelper.noClinicMsg);
                    }

                    // return expected time for last patient to be seen
                    return lastTime;
                }
            } else {
                // selected clinic is not open today
                Toast.makeText(getApplicationContext(),
                        "An error occurred while attempting to update the waiting list for the clinic with id " + clinicId + " as the clinic is closed today.",
                        Toast.LENGTH_LONG).show();
                return null;
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "An error occurred while attempting to update the waiting list for the clinic with id " + clinicId,
                    Toast.LENGTH_LONG).show();
            return null;
        }
    }

    // method to compute waiting time, given the time at which the last patient currently in the
    // waiting list is expected to be seen
    public int getExpectedWaitingTime(LocalTime lastTime) {
        // last patient is seen at lastTime, so next patient can be seen 15 minutes after that
        LocalTime nextPatientTime = lastTime.plus(15,ChronoUnit.MINUTES);
        return (int) LocalTime.now().until(nextPatientTime,ChronoUnit.MINUTES);
    }
}
