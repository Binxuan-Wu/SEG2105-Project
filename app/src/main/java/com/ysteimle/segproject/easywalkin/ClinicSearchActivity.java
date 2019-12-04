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
    private ValueEventListener mServiceListener;

    // for search
    private InputValidator iv = new InputValidator();
    // all available services in DB
    private List<Service> servicesInDB = new ArrayList<>();
    private static final int selectServiceVersion = 1;
    // all available clinics in DB
    private List<Clinic> clinicsInDB = new ArrayList<>();
    // for recycler showing list of clinics matching search term
    private final int selectClinic = 1;

    // for clinic id where user is currently on waiting list
    private String currentUserId;
    private String currentWaitingListClinicId;
    private ValueEventListener mClinicIdListener;

    // for search results
    private List<WaitEntry> waitingListOfSelectedClinic = new ArrayList<>();
    // displayed clinic
    private Clinic selectedClinic;
    private boolean canCheckInToSelectedClinic;

    // Variables for the views on screen
    // search fields
    private Group searchFieldsGroup;
    private EditText citySearchEdit;
    private EditText provinceSearchEdit;
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

            // we have not searched yet, so search results should not be visible
            searchResultGroup.setVisibility(View.GONE);
            checkInBtn.setVisibility(View.GONE);

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
                    //Toast.makeText(getApplicationContext(),"Currently on waiting list of clinic with id " + currentWaitingListClinicId, Toast.LENGTH_SHORT).show();
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

            // value event listener to find all available services in the database
            ValueEventListener serviceListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    servicesInDB.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Service service = snapshot.getValue(Service.class);
                        if (service != null) {
                            servicesInDB.add(service);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mReference.child(DBHelper.servicesPath).addValueEventListener(serviceListener);
            mServiceListener = serviceListener;
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

        if (mServiceListener != null) {
            mReference.child(DBHelper.servicesPath).removeEventListener(mServiceListener);
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

        // display services offered
        try {
            StringBuilder servicesOffered = new StringBuilder();
            if (selectedClinic.serviceIdList.isEmpty()) {
                servicesOffered.append("This clinic does not offer any services.");
            } else {
                for (Service service : servicesInDB) {
                    if (selectedClinic.serviceIdList.contains(service.id)) {
                        servicesOffered.append(service.name);
                        servicesOffered.append("\n");
                    }
                }
            }
            servicesView.setText(servicesOffered.toString());
        } catch (Exception e) {
            // In case there is a ConcurrentModificationException because the list changes while
            // we are iterating over it
            servicesView.setText("Error: Could not determine the list of services offered by this clinic.");
        }

        // compute and display waiting time
        // this involves updating the waiting list of the selected clinic
        boolean canAddAPatientToWaitingList = computeDisplayWaitingTimeForSelectedClinic();

        // set visibility of check-in button according to if clinic is open and has space on
        // waiting list
        // Note: if patient tries to check in to clinic while they are already checked-in with some
        // clinic, they won't be able to
        if (canAddAPatientToWaitingList && canCheckInToSelectedClinic) {
            checkInBtn.setVisibility(View.VISIBLE);
        } else {
            checkInBtn.setVisibility(View.GONE);
        }
    }

    // method to compute and display expected waiting time for selected clinic
    // returns true if and only if a patient can add themselves to waiting list
    // i.e. returns true if and only if clinic is currently open and there is enough time left
    // before the closing time to see another patient
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
            canCheckInToSelectedClinic = false;
            return false;
        } else {
            // clinic is open right now, so continue
            LocalTime selectedClinicClosingTime = getSelectedClinicClosingTimeForDay(todayCode);
            // retrieve and update the waiting list of the selected clinic
            updateWaitingListOfClinic(selectedClinicId, selectedClinicClosingTime);
            if (waitingListOfSelectedClinic.isEmpty()) {
                // no patients in waiting list, so need to check if there is enough time left today
                // (note that we use 20 minutes here since we will give the patient an expected
                // appointment time a few minutes from now to allow them time to arrive)
                if (LocalTime.now().plus(20, ChronoUnit.MINUTES).isAfter(selectedClinicClosingTime)) {
                    // no more time today
                    clinicWaitingTimeView.setText("Not currently accepting patients");
                    canCheckInToSelectedClinic = false;
                    return false;
                } else {
                    // patient can be seen today and expected waiting time is 0 minutes
                    clinicWaitingTimeView.setText("0 minutes");
                    canCheckInToSelectedClinic = true;
                    return true;
                }
            } else {
                // there are patients on the waiting list, so compute the waiting time
                LocalTime nextAvailableTime = LocalTime.parse(waitingListOfSelectedClinic.get(waitingListOfSelectedClinic.size() -1).expectedAppEndTime);
                if (nextAvailableTime.plus(15,ChronoUnit.MINUTES).isAfter(selectedClinicClosingTime)) {
                    // no more time left to see patients
                    clinicWaitingTimeView.setText("Not currently accepting patients");
                    canCheckInToSelectedClinic = false;
                    return false;
                } else {
                    int expectedWait = (int) LocalTime.now().until(nextAvailableTime,ChronoUnit.MINUTES);
                    clinicWaitingTimeView.setText(expectedWait + " minutes");
                    canCheckInToSelectedClinic = true;
                    return true;
                }
            }

            // old version. doesn't seem to work properly
            /*// retrieve and update the waiting list of the selected clinic
            LocalTime expectedEndTimeForLastPatient = updateWaitingListOfClinic(selectedClinicId, selectedClinicClosingTime);
            // compute waiting time
            if (expectedEndTimeForLastPatient != null) {
                if (expectedEndTimeForLastPatient.plus(15, ChronoUnit.MINUTES).isAfter(selectedClinicClosingTime)) {
                    // no more patients can be accepted today
                    clinicWaitingTimeView.setText("Not currently accepting patients");
                    return false;
                } else {
                    // last patient is finished at expectedEndTimeForLastPatient, so next patient
                    // can be seen at that time. Thus, we must compute time from now until
                    // expectedEndTimeForLastPatient
                    int expectedWaitingTime = (int) LocalTime.now().until(expectedEndTimeForLastPatient,ChronoUnit.MINUTES);
                    // display expected waiting time on screen
                    clinicWaitingTimeView.setText(String.format("%d minutes", expectedWaitingTime));
                    return true;
                }
            } else {
                // no patients in waiting list, so need to check if there is enough time left today
                // (note that we use 20 minutes here since we will give the patient an expected
                // appointment time a few minutes from now to allow them time to arrive)
                if (LocalTime.now().plus(20, ChronoUnit.MINUTES).isAfter(selectedClinicClosingTime)) {
                    // no more time today
                    clinicWaitingTimeView.setText("Not currently accepting patients");
                    return false;
                } else {
                    // patient can be seen today and expected waiting time is 0 minutes
                    clinicWaitingTimeView.setText("0 minutes");
                    return true;
                }
            }*/
        }
    }

    // Note: old version. There seems to be something wrong with it.
    // method to update the waiting list for a specified clinic with given closing time
    // and return the expected appointment end time of the last patient on the waiting list or null
    // if there are no patients on the waiting list
    // Note: something to check that the clinic is actually open at the moment is in the method that
    // calls this one, so closingTime should not be null (but we double-check anyway)
    /*public LocalTime updateWaitingListOfClinic(String clinicId, LocalTime closingTime) {
        try {
            // retrieve waiting list from database, but do not clear it
            getWaitingListFromDB(clinicId, false);
            // get current date and time
            LocalDate today = LocalDate.now();
            int todayCode = today.getDayOfWeek().getValue();
            LocalTime currentTime = LocalTime.now();

            // last patient's appointment must end before closing time
            // we remove all waiting list entries where the expected appointment END time is in the
            // past
            if (closingTime != null) {

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
                            if (LocalTime.parse(entry.expectedAppEndTime).isBefore(LocalTime.now()) ||
                                    LocalTime.parse(entry.expectedAppEndTime).isAfter(closingTime)) {
                                // the patient has been seen at some earlier time today or expected
                                // appointment time ends after the last time a patient can be seen,
                                // so remove entry
                                removedFromWaitingList.add(entry.patientId);
                                // remove last entry returned by iterator next() method
                                waitEntryIterator.remove();
                            }
                        }
                    } else {
                        // the waiting list entry does not have valid dates and/or times, so remove it
                        removedFromWaitingList.add(entry.patientId);
                        waitEntryIterator.remove();
                    }
                }

                LocalTime returnTime;

                if (waitingListOfSelectedClinic.isEmpty()) {
                    // the waiting list is now empty
                    // return null
                    returnTime = null;

                    // update waiting list in database (there is no more waiting list, so just remove it)
                    DatabaseReference waitingListOfClinicRef = mReference.child(DBHelper.waitingListByClinic).child(clinicId);
                    // remove old waiting list
                    waitingListOfClinicRef.removeValue();
                } else {
                    // sort the remaining entries in the waiting list in order of date and time requested
                    WaitEntryComparator waitEntryComparator = new WaitEntryComparator();
                    waitingListOfSelectedClinic.sort(waitEntryComparator);

                    // find expected appointment end time of first entry in list
                    // all the other expected times will be set accordingly, counting a default of
                    // 15 minutes per patient
                    LocalTime firstEndTime = LocalTime.parse(waitingListOfSelectedClinic.get(0).expectedAppEndTime);

                    // the first patient on the waiting list should either be currently being seen
                    // or should be seen within a minute or two from now. Otherwise, some other patient(s)
                    // that used to be on the waiting list before the patient that is currently
                    // first cancelled their appointment. In this case, we need to update
                    // the time of the first patient so that they can be seen immediately
                    // (it is not possible that some other patient is currently being seen as
                    // otherwise their waiting list entry would still be on the list because their
                    // expected appointment end time would not yet have passed)
                    if (LocalTime.now().plus(18, ChronoUnit.MINUTES).isBefore(firstEndTime)) {
                        // we need to update expected times for first patient in waiting list
                        // by default, they will be seen 2 minutes from now
                        LocalTime newStartTime = LocalTime.now().plus(2,ChronoUnit.MINUTES);
                        LocalTime newEndTime = newStartTime.plus(15,ChronoUnit.MINUTES);
                        waitingListOfSelectedClinic.get(0).setExpectedAppTimes(newStartTime.toString(),newEndTime.toString());

                        // reset firstEndTime
                        firstEndTime = newEndTime;
                    }


                    // time at which last patient is expected to be finished
                    LocalTime lastEndTime = firstEndTime;
                    for (int i = 0; i < waitingListOfSelectedClinic.size() - 1; i++) {
                        // the next appointment in the list starts when the previous one ends
                        LocalTime newEndTime = lastEndTime.plus(15, ChronoUnit.MINUTES);
                        waitingListOfSelectedClinic.get(i + 1).setExpectedAppTimes(lastEndTime.toString(),newEndTime.toString());
                        lastEndTime = newEndTime;
                    }

                    // return expected time for last patient to be seen
                    returnTime = lastEndTime;

                    // update waiting list in database
                    DatabaseReference waitingListOfClinicRef = mReference.child(DBHelper.waitingListByClinic).child(clinicId);
                    // remove old waiting list
                    waitingListOfClinicRef.removeValue();
                    // add updated waiting list
                    for (WaitEntry entry : waitingListOfSelectedClinic) {
                        waitingListOfClinicRef.child(entry.entryId).setValue(entry);
                    }
                }

                // update status of patients whose WaitEntries were removed
                for (String removedId : removedFromWaitingList) {
                    mReference.child(DBHelper.waitingListByPatient).child(removedId).setValue(DBHelper.noClinicMsg);
                }
                return returnTime;
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
    }*/

    // method to update the waiting list for a specified clinic with given closing time
    // and return the expected appointment end time of the last patient on the waiting list or null
    // if there are no patients on the waiting list
    // Note: something to check that the clinic is actually open at the moment is in the method that
    // calls this one, so closingTime should not be null (but we double-check anyway)
    // Note: New version. this works as intended.
    public void updateWaitingListOfClinic(String clinicId, LocalTime closingTime) {
        try {
            // retrieve waiting list from database, but do not clear it
            getWaitingListFromDB(clinicId, false);
            // get current date and time
            LocalDate today = LocalDate.now();
            int todayCode = today.getDayOfWeek().getValue();
            LocalTime currentTime = LocalTime.now();

            // last patient's appointment must end before closing time
            // we remove all waiting list entries where the expected appointment END time is in the
            // past
            if (closingTime != null) {

                // list of entries to be removed from the waiting list
                List<WaitEntry> removedEntries = new ArrayList<>();

                // remove outdated entries from the waiting list
                for (WaitEntry entry : waitingListOfSelectedClinic) {
                    if (entry.hasValidDatesTimes()) {
                        if (!LocalDate.parse(entry.expectedAppDate).isEqual(today)) {
                            // the patient was on the waiting list for a day other than today, so remove entry
                            removedEntries.add(entry);
                        } else {
                            // if expected appointment date is today, must check the time
                            if (LocalTime.parse(entry.expectedAppEndTime).isBefore(LocalTime.now()) ||
                                    LocalTime.parse(entry.expectedAppEndTime).isAfter(closingTime)) {
                                // the patient has been seen at some earlier time today or expected
                                // appointment time ends after the last time a patient can be seen,
                                // so remove entry
                                removedEntries.add(entry);
                            }
                        }
                    } /*else {
                        // the waiting list entry does not have valid dates and/or times, so remove it
                        removedEntries.add(entry);
                    }*/
                }

                // remove the removed entries from the waitingListOfSelectedClinic
                if (!removedEntries.isEmpty()) {
                    waitingListOfSelectedClinic.removeAll(removedEntries);

                    // sort the remaining entries in the waiting list in order of date and time requested
                    WaitEntryComparator waitEntryComparator = new WaitEntryComparator();
                    waitingListOfSelectedClinic.sort(waitEntryComparator);

                    // find expected appointment end time of first entry in list
                    // all the other expected times will be set accordingly, counting a default of
                    // 15 minutes per patient
                    LocalTime firstEndTime = LocalTime.parse(waitingListOfSelectedClinic.get(0).expectedAppEndTime);

                    // the first patient on the waiting list should either be currently being seen
                    // or should be seen within a minute or two from now. Otherwise, some other patient(s)
                    // that used to be on the waiting list before the patient that is currently
                    // first cancelled their appointment. In this case, we need to update
                    // the time of the first patient so that they can be seen immediately
                    // (it is not possible that some other patient is currently being seen as
                    // otherwise their waiting list entry would still be on the list because their
                    // expected appointment end time would not yet have passed)
                    if (LocalTime.now().plus(20, ChronoUnit.MINUTES).isBefore(firstEndTime)) {
                        // we need to update expected times for first patient in waiting list
                        // by default, they will be seen 2 minutes from now
                        LocalTime newStartTime = LocalTime.now().plus(2, ChronoUnit.MINUTES);
                        LocalTime newEndTime = newStartTime.plus(15, ChronoUnit.MINUTES);
                        waitingListOfSelectedClinic.get(0).setExpectedAppTimes(newStartTime.toString(), newEndTime.toString());

                        // reset firstEndTime
                        firstEndTime = newEndTime;
                    }

                    // time at which last patient is expected to be finished
                    LocalTime lastEndTime = firstEndTime;
                    for (int i = 0; i < waitingListOfSelectedClinic.size() - 1; i++) {
                        // the next appointment in the list starts when the previous one ends
                        LocalTime newEndTime = lastEndTime.plus(15, ChronoUnit.MINUTES);
                        waitingListOfSelectedClinic.get(i + 1).setExpectedAppTimes(lastEndTime.toString(), newEndTime.toString());
                        lastEndTime = newEndTime;
                    }

                    // update waiting list in database
                    DatabaseReference waitingListOfClinicRef = mReference.child(DBHelper.waitingListByClinic).child(clinicId);

                    // add updated waiting list
                    for (WaitEntry entry : waitingListOfSelectedClinic) {
                        waitingListOfClinicRef.child(entry.entryId).setValue(entry);
                    }

                    // remove old waiting list entries
                    for (WaitEntry removedEntry : removedEntries) {
                        waitingListOfClinicRef.child(removedEntry.entryId).removeValue();
                        mReference.child(DBHelper.waitingListByPatient).child(removedEntry.patientId).setValue(DBHelper.noClinicMsg);
                    }
                }
            } else {
                // selected clinic is not open today
                Toast.makeText(getApplicationContext(),
                        "An error occurred while attempting to update the waiting list for the clinic with id " + clinicId + " as the clinic is closed today.",
                        Toast.LENGTH_LONG).show();
                canCheckInToSelectedClinic = false;
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "An error occurred while attempting to update the waiting list for the clinic with id " + clinicId,
                    Toast.LENGTH_LONG).show();
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

    // method to clear all entries from waiting list of patient
    // that is, we remove all entries from the database
    public void clearWaitingListOfClinic() {
        getWaitingListFromDB(selectedClinic.clinicId,true);
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

    // onClick Method for the search by service button
    public void onSearchByServiceOfferedClick(View view) {
        showSelectServiceDialog();
    }

    // method to display a dialog that has all the services in the database. The patient can select
    // whichever one they want, so that they can then see all of the clinics that offer this service
    // The method redirects to the method that shows the clinics matching the search if a service is
    // selected
    public void showSelectServiceDialog() {
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

        // Set up recycler view to show all available services, provided there are some in the DB
        if (servicesInDB.isEmpty()) {
            resultCountMsgView.setText("There are no available services to choose from.");
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
            resultCountMsgView.setText(String.format("%d services match your search.", servicesInDB.size()));
            resultsActionMsgView.setText("Select the desired service below to search for clinics offering this service.");
            RecyclerView.LayoutManager searchResultsLayoutManager = new LinearLayoutManager(this);
            resultsRecycler.setLayoutManager(searchResultsLayoutManager);
            // Make dialog appear
            final AlertDialog dialog = dialogBuilder.create();
            dialog.show();
            // set up service adapter
            ServiceAdapter serviceAdapter = new ServiceAdapter(selectServiceVersion, new ServiceAdapter.OnServiceClickListener() {
                @Override
                public void onServiceClick(Service service) {
                    String serviceIdToSearch = service.id;
                    dialog.dismiss();
                    List<Clinic> matchingClinics = searchClinicsByServiceId(serviceIdToSearch);
                    showClinicSearchResultsDialog(matchingClinics);
                }
            });
            // attach adapter to the recycler
            resultsRecycler.setAdapter(serviceAdapter);
            // give adapter list of services to display
            serviceAdapter.submitList(servicesInDB);

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

    // method to clear search fields
    public void clearSearchFields() {
        citySearchEdit.setText("");
        provinceSearchEdit.setText("");
    }

    // onClick method for the new search button
    public void newSearchOnClick(View view){
        // Note: not sure if clearing search result views is necessary
        canCheckInToSelectedClinic = false; // no more clinic selected
        selectedClinic = null;
        // hide search result views
        searchResultGroup.setVisibility(View.GONE);
        checkInBtn.setVisibility(View.GONE);
        // show search fields
        searchFieldsGroup.setVisibility(View.VISIBLE);
    }

    // onclick method for the cancel button
    public void cancelBtnOnClick(View view) {
        finish();
    }

    // onClick method for the check-in button
    public void checkInBtnOnClick(View view) {
        String currentClinicId = currentWaitingListClinicId;
        // check that patient is not already on a waiting list
        if (currentClinicId != null && !currentClinicId.equals(DBHelper.noClinicMsg)) {
            // patient cannot be on waiting list twice
            Toast.makeText(getApplicationContext(), "Error: You are already on the waiting list for a clinic.", Toast.LENGTH_LONG).show();
        } else {
            if (canCheckInToSelectedClinic) {
                addPatientToWaitingListOfSelectedClinic();
            } else {
                Toast.makeText(getApplicationContext(), "Error: Clinic is not currently accepting patients.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // method to add patient to waiting list of selected clinic
    // should only be called if we are actually allowed to add a patient to the waiting list
    // and after we have updated the waiting list of the selected clinic
    // note: if patient is currently checked-in at a different clinic, the patient won't be allowed
    // to add themselves to the waiting list of the selected clinic
    public void addPatientToWaitingListOfSelectedClinic() {
        try {
            String relevantClinicId = selectedClinic.clinicId;
            String relevantPatientId = mUser.getUid();
            String currentClinicId = currentWaitingListClinicId;
            // check that patient is not already on a waiting list
            if (currentClinicId != null && !currentClinicId.equals(DBHelper.noClinicMsg)) {
                // patient cannot be on waiting list twice
                Toast.makeText(getApplicationContext(), "Error: You are already on the waiting list for a clinic.", Toast.LENGTH_LONG).show();
            } else if (!canCheckInToSelectedClinic) {
                Toast.makeText(getApplicationContext(), "Error: This clinic is not accepting patients at the moment.", Toast.LENGTH_LONG).show();
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
                        // if no patients currently in waiting list, we set the expected time to be 2 minutes
                        // from now
                        entry.setExpectedAppDate(LocalDate.now().toString());
                        entry.setExpectedAppStartTime(LocalTime.now().plus(2, ChronoUnit.MINUTES).toString());
                    } else {
                        // there is at least one patient already on waiting list
                        LocalTime lastPatientEndTime = LocalTime.parse(waitingListOfSelectedClinic.get(waitingListOfSelectedClinic.size() - 1).expectedAppEndTime);
                        // patient to be added will be seen when last patient on list finishes
                        String expectedStartTime = lastPatientEndTime.toString();
                        entry.setExpectedAppDate(LocalDate.now().toString());
                        entry.setExpectedAppStartTime(expectedStartTime);
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

    // returns closing time of the selected clinic
    public LocalTime getSelectedClinicClosingTimeForDay(int dayCode) {
        String closingTime = selectedClinic.getOpenTimeForDay(dayCode).CT;
        if (OpenTime.validTime(closingTime)) {
            return LocalTime.parse(closingTime);
        } else {
            return null;
        }
    }
}
