package com.ysteimle.segproject.easywalkin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    //private ChildEventListener mServiceChildEventListener;
    private ValueEventListener mServiceValueEventListener;

    // For the list of available services that have been created
    private static final int editVersion = 0;
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

        // The create-adapter part is now inside the value event listener. Maybe this will solve
        // the time-lag for the changes to appear on the screen. Yes, it does.
        /*// Specify adapter for the service list
        serviceAdapter = new ServiceAdapter(new ServiceAdapter.OnServiceClickListener() {
            @Override
            public void onServiceClick(Service service) {
                updateDeleteService(service);
            }
        });
        serviceRecycler.setAdapter(serviceAdapter);*/
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Make a value event listener for the services list
        ValueEventListener serviceValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                services.clear();
                for (DataSnapshot serviceSnapshot : dataSnapshot.getChildren()) {
                    Service service = serviceSnapshot.getValue(Service.class);
                    services.add(service);
                }
                // Specify adapter for the service list
                serviceAdapter = new ServiceAdapter(editVersion, new ServiceAdapter.OnServiceClickListener() {
                    @Override
                    public void onServiceClick(Service service) {
                        updateDeleteService(service);
                    }
                });
                serviceRecycler.setAdapter(serviceAdapter);
                serviceAdapter.submitList(services);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // something went wrong
                Toast.makeText(getApplicationContext(), "Error with the services Value Event Listener.", Toast.LENGTH_LONG).show();
            }
        };

        // attach value event listener to the services list in the database
        // since it takes a while for the list to update, I refresh the screen when I change data in
        // the database
        serviceReference.addValueEventListener(serviceValueEventListener);

        // store reference to value event listener so that we can remove it when the app stops
        mServiceValueEventListener = serviceValueEventListener;
    }


    @Override
    public void onStop() {
        super.onStop();

        // Remove services value event listener
        if (mServiceValueEventListener != null) {
            serviceReference.removeEventListener(mServiceValueEventListener);
        }
    }

    // method to edit service (update or delete) -- to be called when clicking on an item in the
    // recyclerview for the services
    public void updateDeleteService (Service service) {
        final String serviceId = service.id;

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

        // Show the Delete Service button
        deleteBtn.setVisibility(View.VISIBLE);

        // Change the message on the create service button. It should be written Update Service there
        // instead
        createServiceBtn.setText(R.string.Update_Service);

        // Display the info of the service
        serviceNameEdit.setText(service.name);
        serviceDescriptionEdit.setText(service.description);
        // Not sure how to select one of the radio group buttons depending on what provider type is
        // I guess I won't make that appear for now

        // Make dialog appear
        dialogBuilder.setTitle(R.string.Edit_Service);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        // onClickListener for update service button
        createServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        // Update service in database
                        updateService(serviceId, serviceName, provider, serviceDescription);
                        dialog.dismiss();
                        // Don't refresh automatically but rather prompt user to do so if necessary
                        //refreshScreen();
                    }
                    // If there was an error in the input, do nothing and wait for user to either
                    // try again or cancel.
                }
            }
        });

        // onClickListener for Delete button
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteService(serviceId);
                dialog.dismiss();
                // Don't refresh automatically but rather prompt user to do so if necessary
                //refreshScreen();
            }
        });


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

    // On click method for New Service button
    public void createNewService (View view) {
        showNewServiceDialog();
    }

    // Method to show the new service dialog

    private void showNewServiceDialog () {

        // Create dialog with appropriate layout
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.new_service_dialog, null);
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

                        // Maybe not do this automatically, but rather prompt user to do it if necessary.
                        //refreshScreen();
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

    // Method to update displayed service list (to call when we create a new service)
    // this probably won't work anymore as I have changed how I set up the serviceAdapter
    public void updateDisplayedServices () {
        serviceReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                services.clear();
                for (DataSnapshot serviceSnapshot : dataSnapshot.getChildren()) {
                    Service service = serviceSnapshot.getValue(Service.class);
                    services.add(service);
                }
                serviceAdapter.submitList(services);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Something went wrong
            }
        });
    }

    // Method to update displayed service list by adding new service
    // this probably won't work anymore as I have changed how I set up the serviceAdapter
    public void updateDisplayedServices (Service newService) {
        if (services.indexOf(newService) == -1) {
            // If service not in displayed list, add it.
            services.add(newService);
            serviceAdapter.submitList(services);
        }
    }

    // Method to update a service in the database
    private void updateService(String id, String name, String provider, String description) {
        // get reference to service in the database
        DatabaseReference databaseReference = serviceReference.child(id);

        // update service
        Service service = new Service(id,name,provider,description);
        databaseReference.setValue(service);

        // success toast
        Toast.makeText(getApplicationContext(), "Service updated.", Toast.LENGTH_LONG).show();
    }

    // Method to delete a service from the database
    private void deleteService(String id) {
        // get reference to service in the database
        DatabaseReference databaseReference = serviceReference.child(id);

        // remove service
        databaseReference.removeValue();

        // success toast
        Toast.makeText(getApplicationContext(), "Service deleted.", Toast.LENGTH_LONG).show();
    }


}
