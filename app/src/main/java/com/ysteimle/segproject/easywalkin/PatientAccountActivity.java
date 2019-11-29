package com.ysteimle.segproject.easywalkin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;

import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.Dataset;
import android.view.View;
import android.widget.Button;
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
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PatientAccountActivity extends AppCompatActivity {

    // For Firebase Database and Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;

    // for waiting list
    // id of the clinic whose waiting list patient is on; "None" (i.e. DBHelper.noClinicMsg) if on no waiting list
    private String waitingListClinicId;
    private Clinic waitingListClinic;
    private List<WaitEntry> waitingListOfPatient = new ArrayList<>();
    private WaitEntryComparator waitEntryComparator = new WaitEntryComparator();
    private WaitEntry currentWaitEntry;
    private DatabaseReference waitingListOfPatientClinicIdRef; // id of the clinic where patient is on waiting list
    private ValueEventListener mWaitingListClinicIdListener;

    // Patient account
    private String patientUserId;

    // Views on the screen
    // general
    private Button clinicSearchBtn;
    private Button refreshBtn;
    private TextView lastUpdatedView;
    // Views for remote Check-In
    private Group checkInGroup;
    private Button checkInCancelBtn;
    private TextView checkInMsgView;
    private TextView checkInClinicInfoView;
    private TextView checkInDateView;
    private TextView checkInWaitView;

    // Views for scheduled appointments
    private Button newAppointmentBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_account);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference();

        if (mUser == null) {
            // if no user is logged in, we should not be here
            Toast.makeText(getApplicationContext(), "Error: No user logged in", Toast.LENGTH_LONG).show();
            finish();
        } else {
            patientUserId = mUser.getUid();

            // instead, check manually
            /*// Value event listener for which (if any) clinic's waiting list patient is on
            ValueEventListener waitingListClinicIdListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String clinicId = (String) dataSnapshot.getValue();
                    if (clinicId != null) {
                        waitingListClinicId = clinicId;
                    } else {
                        // if patient does not have an entry in the WaitingListByPatient list in the
                        // database, add an entry for this patient with value "None", indicating that
                        // the patient is on no waiting list
                        //mReference.child(DBHelper.waitingListByPatient).child(patientUserId).setValue(DBHelper.noClinicMsg);
                        waitingListClinicId = DBHelper.noClinicMsg;
                    }
                    getPatientAccountInfoFromDB();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // something went wrong
                }
            };
            waitingListOfPatientRef = mReference.child(DBHelper.waitingListByPatient).child(patientUserId);
            waitingListOfPatientRef.addValueEventListener(waitingListClinicIdListener);
            mWaitingListClinicIdListener = waitingListClinicIdListener;*/

            // associate variables to views on screen
            // general
            clinicSearchBtn = findViewById(R.id.PatAccSearchBtn);
            refreshBtn = findViewById(R.id.PatAccRefreshBtn);
            lastUpdatedView = findViewById(R.id.PatAccLastUpdated);

            // Remote Check-In
            checkInGroup = findViewById(R.id.CheckInGroup);
            checkInMsgView = findViewById(R.id.CheckInMsg);
            checkInCancelBtn = findViewById(R.id.CheckInCancelBtn);
            checkInClinicInfoView = findViewById(R.id.CheckInClinicInfo);
            checkInDateView = findViewById(R.id.CheckInDate);
            checkInWaitView = findViewById(R.id.CheckInExpectedWaitTime);

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // display info on screen
        getPatientAccountInfoFromDB();
        //updatePatientAccountInfo();
        //displayPatientAccountInfo();
    }

    /*@Override
    public void onStop() {
        super.onStop();

        // remove value event listeners added to the database
        if (mWaitingListClinicIdListener != null && waitingListOfPatientRef != null) {
            waitingListOfPatientRef.removeEventListener(mWaitingListClinicIdListener);
        }
    }*/

    /* Methods to update information regarding waiting list status and scheduled appointment both in
       database and displayed on screen. Checks current time and date, removes all past appointments
       and check waiting time. Method called on activity start and on activity resume, each time
       the user changes something, and each time the refresh button is clicked. */

    // Method to get info from DB
    // maybe put single value event listener in here rather than value event listener in onCreate();
    public void getPatientAccountInfoFromDB() {
        waitingListOfPatientClinicIdRef = mReference.child(DBHelper.waitingListByPatient).child(patientUserId);
        waitingListOfPatientClinicIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String clinicId = (String) dataSnapshot.getValue();
                if (clinicId != null) {
                    waitingListClinicId = clinicId;
                    updatePatientAccountInfo();
                    //displayPatientAccountInfo();
                } else {
                    // if patient does not have an entry in the WaitingListByPatient list in the
                    // database, add an entry for this patient with value "None", indicating that
                    // the patient is on no waiting list
                    //waitingListOfPatientClinicIdRef.setValue(DBHelper.noClinicMsg);
                    //waitingListClinicId = DBHelper.noClinicMsg;
                    //Toast.makeText(getApplicationContext(),"Error: Could not find clinic id in Database",Toast.LENGTH_LONG).show();
                    //displayNoWaitingListInfo("Error: Could not find clinic id in Database");
                    displayNoWaitingListInfo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),"Error: Could not find waiting list clinic id in Database.",Toast.LENGTH_LONG).show();
            }
        });

        /*if (waitingListClinicId != null && !waitingListClinicId.equals(DBHelper.noClinicMsg)) {
            // patient is on a waiting list
            Toast.makeText(this,"On waiting list of clinic with id " + waitingListClinicId,Toast.LENGTH_LONG).show();
            // get WaitEntry object from database

        } else {
            // patient not on a waiting list
            Toast.makeText(this,"Not on a waiting list. waitingListClinicId = " + waitingListClinicId,Toast.LENGTH_LONG).show();
        }*/
    }

    // method to update info (both stored and in database), based on current date and time and
    // number of other patients in relevant waiting list(s)
    // must have called getPatientAccountInfoFromDB() first
    public void updatePatientAccountInfo() {
        // get waiting list entries for relevant clinic
        if (waitingListClinicId != null && !waitingListClinicId.equals(DBHelper.noClinicMsg)) {
            // patient is on a waiting list.
            // Retrieve the clinic object corresponding to the waiting list the patient is
            // on (we need the opening hours and other info)
            mReference.child(DBHelper.clinicListPath).child(waitingListClinicId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Clinic clinic = dataSnapshot.getValue(Clinic.class);
                    if (clinic != null) {
                        waitingListClinic = clinic;
                    } else {
                        // could not find clinic (maybe it has been deleted?)
                        waitingListClinic = null;
                        Toast.makeText(getApplicationContext(),
                                "Error: Could not find clinic corresponding to waiting list patient is on in the database.",
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(),
                            "Error: Could not find clinic corresponding to waiting list patient is on in the database due to onCancelled() being called.",
                            Toast.LENGTH_LONG).show();
                }
            });

            // Retrieve the waiting list so that we can update it.
            // waiting list of the clinic patient has checked-in to
            DatabaseReference waitingListOfClinicRef = mReference.child(DBHelper.waitingListByClinic).child(waitingListClinicId);
            waitingListOfClinicRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    waitingListOfPatient.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        WaitEntry waitEntry = snapshot.getValue(WaitEntry.class);
                        if (waitEntry != null) {
                            waitingListOfPatient.add(waitEntry);
                        }
                    }
                    if (!waitingListOfPatient.isEmpty()) {
                        // update the waiting list (this will also display the info)
                        //Toast.makeText(getApplicationContext(), "Found waiting list of patient", Toast.LENGTH_LONG).show();
                        updateWaitingListOfPatient(waitingListClinic,waitingListOfPatient);
                    } else {
                        // waiting list of patient is empty, so patient is not on the waiting list
                        displayNoWaitingListInfo("Error: user is indicated as being on a waiting list, but the waiting list is empty.");
                        // TODO maybe also set clinic id corresponding to patient id in the waitingListByPatient list in the DB to "None"?
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // something went wrong
                    displayNoWaitingListInfo("An error occurred while trying to retrieve the waiting list from the database");
                }
            });
        } else {
            displayNoWaitingListInfo();
            //displayNoWaitingListInfo("Method updatePatientAccountInfo() called, but waitingListClinicID() is " + waitingListClinicId);
        }

        // say when we updated the info
        LocalTime currentTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        lastUpdatedView.setText(String.format("(Last updated at %s)",currentTime.toString()));
    }

    // method to update the waiting list that the patient is on
    public void updateWaitingListOfPatient() {
        try {
            LocalDate today = LocalDate.now();
            int todayCode = today.getDayOfWeek().getValue();
            LocalTime currentTime = LocalTime.now();
            boolean currentPatientRemoved = false;
            int indexOfPatient = -1;
            if (waitingListClinic != null) {
                OpenTime todayOpenTime = waitingListClinic.getOpenTimeForDay(todayCode);
                // get opening and closing times for today
                LocalTime openingTime = LocalTime.parse(todayOpenTime.OT);
                LocalTime closingTime = LocalTime.parse(todayOpenTime.CT);
                // check that clinic is open at the moment. If not, remove everyone from waiting list.
                if (!waitingListClinic.isOpenAtDateTime(todayCode, LocalTime.now().toString())) {
                    // clinic is closed, so clear waiting list
                    clearWaitingListOfPatient(waitingListOfPatient,waitingListClinicId);
                    waitingListOfPatient.clear();
                    // patient was removed
                    currentPatientRemoved = true;
                } else {
                    // last patient must be seen 15 minutes before closing time
                    LocalTime maxTime = closingTime.minus(15, ChronoUnit.MINUTES);
                    // remove all entries from waiting list that are not relevant anymore
                    // keep track of patients that were removed from waiting list
                    List<String> removedPatientIds = new ArrayList<>();
                    // Note: if we remove while iterating, we get a ConcurrentModificationException,
                    // so we must use an iterator instead
                    for (Iterator<WaitEntry> waitEntryIterator = waitingListOfPatient.iterator(); waitEntryIterator.hasNext();) {
                        WaitEntry entry = waitEntryIterator.next();
                        if (entry.hasValidDatesTimes()) {
                            if (!LocalDate.parse(entry.expectedAppDate).isEqual(today)) {
                                // the patient was on the waiting list for a day other than today, so remove entry
                                removedPatientIds.add(entry.patientId);
                                // remove last entry returned by iterator next() method
                                waitEntryIterator.remove();
                            } else {
                                // if expected appointment date is today, must check the time
                                if (LocalTime.parse(entry.expectedAppTime).isBefore(LocalTime.now())
                                        || LocalTime.parse(entry.expectedAppTime).isAfter(maxTime)) {
                                    // the patient has been seen at some earlier time today or expected
                                    // appointment time is after the last time a patient can be seen,
                                    // so remove entry
                                    removedPatientIds.add(entry.patientId);
                                    // remove last entry returned by iterator next() method
                                    waitEntryIterator.remove();
                                }
                            }
                        } else {
                            // entry is not valid, so remove it
                            removedPatientIds.add(entry.patientId);
                            // remove last entry returned by iterator next() method
                            waitEntryIterator.remove();
                        }
                    }

                    if (!waitingListOfPatient.isEmpty()) {
                        // sort the waitEntries according to expected appointment time
                        waitingListOfPatient.sort(waitEntryComparator);

                        // find expected appointment time of first entry in list and set all the other expected
                        // times accordingly, counting a default of 15 minutes per patient
                        LocalTime startTime = LocalTime.parse(waitingListOfPatient.get(0).expectedAppTime);
                        // check if current user is first in list
                        if (waitingListOfPatient.get(0).patientId.equals(patientUserId)) {
                            indexOfPatient = 0;
                        }
                        for (int i = 1; i < waitingListOfPatient.size(); i++) {
                            // number of minutes to add to startTime
                            int addedMinutes = i * 15;
                            WaitEntry currentEntry = waitingListOfPatient.get(i);
                            LocalTime newTime = startTime.plus(addedMinutes, ChronoUnit.MINUTES);
                            waitingListOfPatient.get(i).setExpectedAppTime(newTime.toString());

                            // check if current user is at this index in list
                            if (currentEntry.patientId.equals(patientUserId)) {
                                indexOfPatient = i;
                            }
                        }

                        // update waiting list in database
                        DatabaseReference waitingListOfClinicRef = mReference.child(DBHelper.waitingListByClinic).child(waitingListClinicId);
                        // remove old waiting list
                        waitingListOfClinicRef.removeValue();
                        // add updated waiting list
                        for (WaitEntry entry : waitingListOfPatient) {
                            waitingListOfClinicRef.child(entry.entryId).setValue(entry);
                        }

                        // update status of patients whose WaitEntries were removed
                        for (String removedId : removedPatientIds) {
                            // check if current patient was removed
                            if (removedId.equals(patientUserId)) {
                                currentPatientRemoved = true;
                            }
                            mReference.child(DBHelper.waitingListByPatient).child(removedId).setValue(DBHelper.noClinicMsg);
                        }
                    } else {
                        // waiting list is empty, so patient cannot possibly be on it
                        mReference.child(DBHelper.waitingListByPatient).child(patientUserId).setValue(DBHelper.noClinicMsg);
                        currentPatientRemoved = true;
                    }
                }

                if (currentPatientRemoved) {
                    // patient not on waiting list
                    displayNoWaitingListInfo();
                } else {
                    // patient is on the waiting list of the selected clinic, so display the info
                    if (indexOfPatient == -1) {
                        // something went wrong
                        Toast.makeText(getApplicationContext(), "Error: Could not find your entry in the waiting list.", Toast.LENGTH_LONG).show();
                        displayNoWaitingListInfo("Error: Could not find your entry in the waiting list.");
                    } else {
                        WaitEntry userWaitEntry = waitingListOfPatient.get(indexOfPatient);
                        displayWaitingListInfo(userWaitEntry);
                    }
                }

            } else {
                // we did not manage to retrieve the clinic object from the database
                // display no waiting list info
                displayNoWaitingListInfo("Error: Could not retrieve clinic object from database");
            }

        } catch (Exception e) {
            // something went wrong
            Toast.makeText(getApplicationContext(), "Error: Could not update waiting list of patient properly.", Toast.LENGTH_LONG).show();
            displayNoWaitingListInfo("Error: Could not update waiting list of patient properly due to:\n " + e.toString());
        }
    }

    // method to update the waiting list that the patient is on
    // this time with passed parameters
    public void updateWaitingListOfPatient(Clinic waitingListClinic, List<WaitEntry> waitingListOfPatient) {
        try {
            StringBuilder errorMsg = new StringBuilder();
            LocalDate today = LocalDate.now();
            int todayCode = today.getDayOfWeek().getValue();
            LocalTime currentTime = LocalTime.now();
            boolean currentPatientRemoved = false;
            int indexOfPatient = -1;
            if (waitingListClinic != null) {
                OpenTime todayOpenTime = waitingListClinic.getOpenTimeForDay(todayCode);
                // get opening and closing times for today
                LocalTime openingTime = LocalTime.parse(todayOpenTime.OT);
                LocalTime closingTime = LocalTime.parse(todayOpenTime.CT);
                // check that clinic is open at the moment. If not, remove everyone from waiting list.
                if (!waitingListClinic.isOpenAtDateTime(todayCode, LocalTime.now().toString())) {
                    // clinic is closed, so clear waiting list
                    clearWaitingListOfPatient(waitingListOfPatient,waitingListClinic.clinicId);
                    waitingListOfPatient.clear();
                    // patient was removed
                    currentPatientRemoved = true;
                    errorMsg.append("Clinic currently closed.");
                } else {
                    // last patient must be seen 15 minutes before closing time
                    LocalTime maxTime = closingTime.minus(15, ChronoUnit.MINUTES);
                    // remove all entries from waiting list that are not relevant anymore
                    // keep track of patients that were removed from waiting list
                    List<String> removedPatientIds = new ArrayList<>();
                    // Note: if we remove while iterating, we get a ConcurrentModificationException,
                    // so we must use an iterator instead
                    for (Iterator<WaitEntry> waitEntryIterator = waitingListOfPatient.iterator(); waitEntryIterator.hasNext();) {
                        WaitEntry entry = waitEntryIterator.next();
                        if (entry.hasValidDatesTimes()) {
                            if (!LocalDate.parse(entry.expectedAppDate).isEqual(today)) {
                                // the patient was on the waiting list for a day other than today, so remove entry
                                removedPatientIds.add(entry.patientId);
                                // remove last entry returned by iterator next() method
                                waitEntryIterator.remove();
                            } else {
                                // if expected appointment date is today, must check the time
                                if (LocalTime.parse(entry.expectedAppTime).isBefore(LocalTime.now())
                                        || LocalTime.parse(entry.expectedAppTime).isAfter(maxTime)) {
                                    // the patient has been seen at some earlier time today or expected
                                    // appointment time is after the last time a patient can be seen,
                                    // so remove entry
                                    removedPatientIds.add(entry.patientId);
                                    // remove last entry returned by iterator next() method
                                    waitEntryIterator.remove();
                                }
                            }
                        } else {
                            // entry is not valid, so remove it
                            removedPatientIds.add(entry.patientId);
                            errorMsg.append("WaitEntry for current patient does not have valid dates/times. ");
                            // remove last entry returned by iterator next() method
                            waitEntryIterator.remove();
                        }
                    }

                    if (!waitingListOfPatient.isEmpty()) {
                        // sort the waitEntries according to expected appointment time
                        waitingListOfPatient.sort(waitEntryComparator);

                        // find expected appointment time of first entry in list and set all the other expected
                        // times accordingly, counting a default of 15 minutes per patient
                        LocalTime startTime = LocalTime.parse(waitingListOfPatient.get(0).expectedAppTime);
                        // check if current user is first in list
                        if (waitingListOfPatient.get(0).patientId.equals(patientUserId)) {
                            indexOfPatient = 0;
                        }
                        for (int i = 1; i < waitingListOfPatient.size(); i++) {
                            // number of minutes to add to startTime
                            int addedMinutes = i * 15;
                            WaitEntry currentEntry = waitingListOfPatient.get(i);
                            LocalTime newTime = startTime.plus(addedMinutes, ChronoUnit.MINUTES);
                            waitingListOfPatient.get(i).setExpectedAppTime(newTime.toString());

                            // check if current user is at this index in list
                            if (currentEntry.patientId.equals(patientUserId)) {
                                indexOfPatient = i;
                            }
                        }

                        // update waiting list in database
                        DatabaseReference waitingListOfClinicRef = mReference.child(DBHelper.waitingListByClinic).child(waitingListClinicId);
                        // remove old waiting list
                        waitingListOfClinicRef.removeValue();
                        // add updated waiting list
                        for (WaitEntry entry : waitingListOfPatient) {
                            waitingListOfClinicRef.child(entry.entryId).setValue(entry);
                        }

                        // update status of patients whose WaitEntries were removed
                        for (String removedId : removedPatientIds) {
                            // check if current patient was removed
                            if (removedId.equals(patientUserId)) {
                                currentPatientRemoved = true;
                                errorMsg.append("Current user removed from waiting list. ");
                            }
                            mReference.child(DBHelper.waitingListByPatient).child(removedId).setValue(DBHelper.noClinicMsg);
                        }
                    } else {
                        // waiting list is empty, so patient cannot possibly be on it
                        // update waiting list in database
                        DatabaseReference waitingListOfClinicRef = mReference.child(DBHelper.waitingListByClinic).child(waitingListClinicId);
                        // remove old waiting list
                        waitingListOfClinicRef.removeValue();
                        // update reference to clinic in database
                        mReference.child(DBHelper.waitingListByPatient).child(patientUserId).setValue(DBHelper.noClinicMsg);
                        currentPatientRemoved = true;
                        errorMsg.append("Waiting list was emptied. ");
                    }
                }

                if (currentPatientRemoved) {
                    // patient not on waiting list
                    //displayNoWaitingListInfo(errorMsg.toString());
                    displayNoWaitingListInfo();
                } else {
                    // patient is on the waiting list of the selected clinic, so display the info
                    if (indexOfPatient == -1) {
                        // something went wrong
                        Toast.makeText(getApplicationContext(), "Error: Could not find your entry in the waiting list.", Toast.LENGTH_LONG).show();
                        displayNoWaitingListInfo("Error: Could not find your entry in the waiting list.");
                    } else {
                        WaitEntry userWaitEntry = waitingListOfPatient.get(indexOfPatient);
                        displayWaitingListInfo(userWaitEntry);
                    }
                }



            } else {
                // we did not manage to retrieve the clinic object from the database
                // display no waiting list info
                displayNoWaitingListInfo("Error: Could not retrieve clinic object from database");
            }

        } catch (Exception e) {
            // something went wrong
            Toast.makeText(getApplicationContext(), "Error: Could not update waiting list of patient properly.", Toast.LENGTH_LONG).show();
            displayNoWaitingListInfo("Error: Could not update waiting list of patient properly due to:\n " + e.toString());
        }
    }

    // method to clear all entries from waiting list of patient
    // that is, we remove all entries from the database
    public void clearWaitingListOfPatient(List<WaitEntry> waitingList, String clinicId) {
        for (WaitEntry entry : waitingList) {
            // set waitingListOfPatient value to "None" in database
            mReference.child(DBHelper.waitingListByPatient).child(entry.patientId).setValue(DBHelper.noClinicMsg);
        }
        // remove entire waiting list of given clinic from database
        mReference.child(DBHelper.waitingListByClinic).child(clinicId).removeValue();
    }

    // method to display info on screen when patient is not on a waiting list.
    public void displayNoWaitingListInfo() {
        // store the waitEntry object we are displaying (in this case, there is none)
        currentWaitEntry = null;
        checkInGroup.setVisibility(View.GONE);
        checkInMsgView.setText(R.string.Not_on_Waiting_List);
    }

    // method to display info on screen when patient is not on a waiting list.
    // with added parameter that gives error message to display
    public void displayNoWaitingListInfo(String errorMsg) {
        // store the waitEntry object we are displaying (in this case, there is none)
        currentWaitEntry = null;
        checkInGroup.setVisibility(View.GONE);
        checkInMsgView.setText(errorMsg);
    }

    // method to display info on screen when patient is on a waiting list.
    // parameter: waitEntry of current user
    // TODO maybe check that clinicId in userWaitEntry matches clinicId of waitingListClinic and matches waitingListClinicId; if not, we have a problem
    public void displayWaitingListInfo(WaitEntry userWaitEntry) {
        // store the waitEntry object we are displaying
        currentWaitEntry = userWaitEntry;
        checkInGroup.setVisibility(View.VISIBLE);
        checkInMsgView.setText(R.string.On_Waiting_List);
        // compute and display waiting time
        try {
            // compute waiting time
            LocalTime expectedTime = LocalTime.parse(userWaitEntry.expectedAppTime);
            int expectedWait = (int) LocalTime.now().until(expectedTime,ChronoUnit.MINUTES);
            // display waiting time
            checkInWaitView.setText(String.format("%d minutes",expectedWait));
        } catch (DateTimeParseException e) {
            // could not parse expected appointment time properly
            checkInWaitView.setText("Error");
        }
        // display clinic info
        checkInClinicInfoView.setText(waitingListClinic.getClinicInfo());
        // date of check-in
        checkInDateView.setText(userWaitEntry.expectedAppDate);
    }

    // old method
    // must have called getPatientAccountInfoFromDB() and updatePatientAccountInfo() before-hand
    public void displayPatientAccountInfo() {
        if (waitingListClinicId == null || waitingListClinicId.equals(DBHelper.noClinicMsg)) {
            checkInGroup.setVisibility(View.GONE);
            checkInMsgView.setText(R.string.Not_on_Waiting_List);
        } else {
            checkInGroup.setVisibility(View.VISIBLE);
            checkInMsgView.setText(R.string.On_Waiting_List);
        }
    }

    // onClick method for the search button
    public void goToSearchClinicActivity(View view) {
        /*Intent searchIntent = new Intent(this,ClinicSearchActivity.class);
        // add info to intent to say if patient can check-in to a clinic or not
        // (i.e. is on a waiting list or not)
        if (waitingListClinicId.equals(DBHelper.noClinicMsg)) {
            searchIntent.putExtra("canCheckIn",true);
        } else {
            searchIntent.putExtra("canCheckIn",false);
        }
        startActivity(searchIntent);*/
        startActivity(new Intent(this,ClinicSearchActivity.class));
    }

    // onClick method for the refresh button
    public void refreshBtnOnClick(View view) {
        getPatientAccountInfoFromDB();
    }

    // onClick method for the cancel check-in button
    public void checkInCancelBtnOnClick(View view) {
        // start by refreshing to see if patient is still on a waiting list
        getPatientAccountInfoFromDB();

        // check if patient has an associated waitEntry object
        if (currentWaitEntry != null) {
            // remove this entry from database and set clinic id associated to current user
            // in waitingListByPatient list in DB to "None"
            mReference.child(DBHelper.waitingListByClinic).child(currentWaitEntry.clinicId).child(currentWaitEntry.entryId).removeValue();
            mReference.child(DBHelper.waitingListByPatient).child(currentWaitEntry.patientId).setValue(DBHelper.noClinicMsg);

            // user not on a waiting list anymore, so change displayed info
            displayNoWaitingListInfo();

            Toast.makeText(getApplicationContext(), "Successfully removed from waiting list.", Toast.LENGTH_LONG).show();
        }
        // otherwise: patient already remove from waiting list, do nothing
    }
}
