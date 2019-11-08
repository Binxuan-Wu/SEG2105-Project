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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    String userType;
    // true if we are managing patients, false otherwise
    boolean managePatients;
    // true if we have received either Patients or Employees from the intent, false otherwise
    boolean validUserType;

    private TextView titleView;
    private TextView listDescView;

    // For the Firebase Database
    DatabaseReference userReference;
    DatabaseReference accountReference;
    private ValueEventListener mUserValueEventListener;
    private String userDatabasePath;

    // For the list of Users
    private RecyclerView userRecycler;
    private UserAdapter userAdapter;
    private RecyclerView.LayoutManager layoutManager;
    List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        // get intent we passed to this activity to figure out if we are managing patients or employees
        Intent userIntent = getIntent();
        userType = userIntent.getStringExtra("userType");
        if (userType == null) {
            validUserType = false;
        } else if (userType.equals("Patient")) {
            managePatients = true;
            validUserType = true;
        } else if (userType.equals("Employee")) {
            managePatients = false;
            validUserType = true;
        } else {
            validUserType = false;
        }

        if (!validUserType) {
            // We did not get a valid user type from the Intent that sent us to this activity.
            // So, close this activity. We should not be here
            Toast.makeText(getApplicationContext(), "Error: Could not determine user type.", Toast.LENGTH_LONG).show();
            finish();
        }

        // user Type is valid if we are still here, so we set everything up
        // Set-up screen according to user Type received from intent
        titleView = (TextView) findViewById(R.id.ManageUsersTitle);
        listDescView = (TextView) findViewById(R.id.manageUsersListDesc);
        if (managePatients) {
            titleView.setText(R.string.Manage_Patients);
            listDescView.setText(R.string.Patient_List);
        } else {
            titleView.setText(R.string.Manage_Employees);
            listDescView.setText(R.string.Employee_List);
        }

        // Firebase Database
        if (managePatients) {
            userDatabasePath = "PatientList";
        } else {
            userDatabasePath = "EmployeeList";
        }
        userReference = FirebaseDatabase.getInstance().getReference(userDatabasePath);
        accountReference = FirebaseDatabase.getInstance().getReference("AccountTypes");

        // List of users
        userRecycler = (RecyclerView) findViewById(R.id.adminUserRecycler);
        userList = new ArrayList<>();

        // Use linear layout manager
        layoutManager = new LinearLayoutManager(this);
        userRecycler.setLayoutManager(layoutManager);

        // Specify adapter for user list
        userAdapter = new UserAdapter(userDatabasePath, new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user, String databasePath) {
                showDeleteUserDialog(user, databasePath);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Value Event listener for the user list in the database
        ValueEventListener userValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    userList.add(user);
                }

                userAdapter.submitList(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // something went wrong
                Toast.makeText(getApplicationContext(), "Error with the user Value Event Listener.", Toast.LENGTH_LONG).show();
            }
        };

        // attach value event listener
        userReference.addValueEventListener(userValueEventListener);

        // store reference to remove listener again when we stop
        mUserValueEventListener = userValueEventListener;
    }

    @Override
    public void onStop(){
        super.onStop();

        if (mUserValueEventListener != null) {
            userReference.removeEventListener(mUserValueEventListener);
        }
    }

    // Method to show a dialog to delete user
    public void showDeleteUserDialog(User user, final String databasePath) {
        // user id
        final String userId = user.id;

        // Create dialog with appropriate layout
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.delete_dialog,null);
        dialogBuilder.setView(dialogView);

        // Declare variables for buttons in dialog
        final Button deleteBtn = (Button) dialogView.findViewById(R.id.delDialogDeleteBtn);
        final Button cancelBtn = (Button) dialogView.findViewById(R.id.delDialogCancelBtn);

        // Make dialog appear
        dialogBuilder.setTitle(R.string.Edit_Service);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        // onClickListener for delete button
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUserFromDB(userId,databasePath);
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

    // Method to delete a user from the database and from the authentication scheme
    private void deleteUserFromDB (String userId, String databasePath) {
        // get reference to user entry in account type list in database
        DatabaseReference accountRef = accountReference.child(userId);
        // get reference to user entry in appropriate user list in database
        DatabaseReference userRef = userReference.child(userId);

        // Remove both entries from database
        //accountRef.removeValue();
        userRef.removeValue();

        // Remove user from authentication scheme
        // I can't get Firebase Admin SDK to work properly, so I will just make the user automatically
        // delete their own account when they try to log in again next time.

        // Set account type entry to Deleted so that account will be deleted
        accountRef.setValue("Deleted");

        // Display message
        Toast.makeText(getApplicationContext(), "User deleted. You may need to refresh the screen to see the changes immediately.", Toast.LENGTH_LONG).show();
    }

    // Refresh method
    public void refreshScreen () {
        // essentially close and re-open activity
        finish();
        startActivity(new Intent(getApplicationContext(), ManageUsersActivity.class));
    }

    // Refresh button onClick method
    public void refreshBtnOnClick (View view) {
        refreshScreen();
    }
}
