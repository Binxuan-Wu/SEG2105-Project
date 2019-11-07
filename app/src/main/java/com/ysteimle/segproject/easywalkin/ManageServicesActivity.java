package com.ysteimle.segproject.easywalkin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ListAdapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageServicesActivity extends AppCompatActivity {

    // For the Firebase Database
    DatabaseReference serviceReference;

    // For the list of available services that have been created
    private RecyclerView serviceRecycler;
    private ServiceAdapter serviceAdapter;
    private RecyclerView.LayoutManager layoutManager;
    List<Service> services;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_services);

        // Firebase Database
        serviceReference = FirebaseDatabase.getInstance().getReference("Services");

        // List of services
        serviceRecycler = (RecyclerView) findViewById(R.id.adminServiceRecycler);
        services = new ArrayList<>();

        // Use linear layout manager
        layoutManager = new LinearLayoutManager(this);
        serviceRecycler.setLayoutManager(layoutManager);

        // Specify adapter for the service list
        // Do I remove this from here and just have it in the value event listener below?
        serviceAdapter = new ServiceAdapter();
        serviceRecycler.setAdapter(serviceAdapter);
        serviceAdapter.submitList(services);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // we want to add a child event listener to the services list
        ChildEventListener serviceEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // A new service has been added to the list in the database
                // Add this service to the displayed list
                Service service = dataSnapshot.getValue(Service.class);
                services.add(service);
                serviceAdapter.submitList(services);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // A service has been changed
                // Must find the service in the services list and replace it with the new service
                boolean foundInList = false;
                int serviceIndex = -1;
                String serviceKey = dataSnapshot.getKey();
                // Find index of Service object in services list
                for (int i=0; i<services.size() && !foundInList;i++) {
                    if (services.get(i).id.equals(serviceKey)) {
                        // the service object is at index i in the services list
                        serviceIndex = i;
                        foundInList = true;
                    }
                }
                if (foundInList) {
                    Service service = dataSnapshot.getValue(Service.class);
                    services.set(serviceIndex,service);
                    // update displayed list
                    serviceAdapter.submitList(services);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error when calling onChildChanged: Service not found in list.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        // attach value event listener to listen for changes in the Services list
        // in the database
        serviceReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // empty previous service list we had
                services.clear();

                // get all the services from the database and add them to the list
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // get service
                    Service service = snapshot.getValue(Service.class);
                    // add service to list
                    services.add(service);
                }

                // Ideally, I would like to arrange things so that my list of services
                // is in lexicographic order of service name. How to do this?

                // adapter has been created and attached to recycler view in the onCreate() method
                // I just need to provide new list to the service adapter
                serviceAdapter.submitList(services);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Do nothing
            }
        });
    }


    // On click method for New Service button
    public void createNewService (View view) {
        showNewServiceDialog();
    }

    // Method to show the new service dialog

    private void showNewServiceDialog () {

        // Create dialog with appropriate layout
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.new_service_dialog,null);
        dialogBuilder.setView(dialogView);

        // Declare variables for elements in the dialog
        final EditText serviceNameEdit = (EditText) dialogView.findViewById(R.id.serviceNameEdit);
        final RadioGroup providerSelectGrp = (RadioGroup) dialogView.findViewById(R.id.providerSelectionGrp);
        final EditText serviceDescriptionEdit = (EditText) dialogView.findViewById(R.id.serviceDescriptionEdit);
        final Button createServiceBtn = (Button) dialogView.findViewById(R.id.serviceCreateBtn);
        final Button deleteBtn = (Button) dialogView.findViewById(R.id.serviceDeleteBtn);
        final Button cancelBtn = (Button) dialogView.findViewById(R.id.dialogCancelBtn);

        // Hide the Delete Service button as we are creating a new service, not updating an
        // existing one.
        deleteBtn.setVisibility(View.GONE);

        // Make dialog appear
        dialogBuilder.setTitle(R.string.Add_new_service);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        // Set on-click listener for the create new service button
        View.OnClickListener createServiceListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Determine if all info has been provided and is correct

                // Get info
                String serviceName = serviceNameEdit.getText().toString().trim();
                String serviceDescription = serviceDescriptionEdit.getText().toString().trim();
                int providerBtnId = providerSelectGrp.getCheckedRadioButtonId();
                String provider;

                // Has a provider type been selected?
                if (providerBtnId == -1) {
                    // no provider selected
                    Toast.makeText(getApplicationContext(),"Error: Please select a provider.", Toast.LENGTH_LONG).show();
                } else {
                    // a provider has been selected. Find out which one it is.
                    RadioButton selectedProvider = (RadioButton) dialogView.findViewById(providerBtnId);
                    provider = selectedProvider.getText().toString().trim();

                    // validate the input
                    if (validInput(serviceName, serviceDescription)) {
                        // Put service into database
                        addServiceToDB(serviceName, provider, serviceDescription);
                        // Display Toast to say we added the service
                        Toast.makeText(getApplicationContext(), "Service created.", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                    // If there was an error in the input, do nothing and wait for user to either
                    // try again or cancel.
                }

            }
        };
        createServiceBtn.setOnClickListener(createServiceListener);

        // Set on-click listener for the cancel button
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // set providerSelection back to "None" before exiting
                //providerSelection = "None";
                // simply close the dialog
                dialog.dismiss();
            }
        });

    }

    // To validate input when creating or editing a service
    public boolean validInput (String serviceName, String serviceDescription) {
        boolean validInput = true;
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("Error: ");
        // unicode regular expression to validate names of services
        // Must start with uppercase letter character, can contain spaces, hyphens, accents
        // Maximum 50 characters
        // Should I allow numbers?
        String serviceNameRegex = "^\\p{L}+[\\p{L}\\p{Z}\\p{Pd}\\p{Mn}\\p{Mc}]{0,49}";
        if (serviceName.isEmpty() || !serviceName.matches(serviceNameRegex)) {
            validInput = false;
            errorMsg.append("Invalid service name. ");
        }
        if (serviceDescription.length() > 100) {
            // Note: Service Description is allowed to be empty since that is optional.
            validInput = false;
            errorMsg.append("Description is too long.");
        }
        if (!validInput) {
            Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
        }

        return validInput;
    }

    // Method to add new service to database
    private void addServiceToDB (String serviceName, String provider, String serviceDesc) {
        // get a unique id for the service using the push().getKey() method
        String id = serviceReference.push().getKey();

        // Create a Service object
        Service service = new Service(id, serviceName, provider, serviceDesc);

        // Save the service in the database
        serviceReference.child(id).setValue(service);
    }


}
