package com.ysteimle.segproject.easywalkin;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


class EmployeeAccount implements UserAccount {
    /*private String employeeId;
    private String clinicId;
    private Clinic clinic;*/
    //private List<Service> selectedServices = new ArrayList<>();

    // For Database
    private static final String clinicOfEmployeePath = "ClinicOfEmployee";
    private static final String clinicListPath = "ClinicList";
    private static final String servicesPath = "Services";
    private static final String noClinicMsg = "None";
    //private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    //private DatabaseReference mReference = mDatabase.getReference();

    // constructors
    public EmployeeAccount () {}
    /*public EmployeeAccount (String employeeId) {
        this.employeeId = employeeId;
    }
    public EmployeeAccount (String employeeId, String clinicId) {
        this.employeeId = employeeId;
        this.clinicId = clinicId;
    }*/

    /*// get and set methods
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId (String id) {
        this.employeeId = id;
    }

    public String getClinicId() {
        return clinicId;
    }

    public void setClinicId(String clinicId) {
        this.clinicId = clinicId;
    }

    public Clinic getClinic() {
        return clinic;
    }

    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
    }
*/
    public static String getClinicOfEmployeePath() { return clinicOfEmployeePath; }

    public static String getClinicListPath() { return clinicListPath; }

    public static String getServicesPath() { return servicesPath; }

    public static String getNoClinicMsg() { return noClinicMsg; }

    /*// method to retrieve clinic id from database and set the clinicId instance variable accordingly
    // note that this will only work if the employeeId has been initialised properly beforehand
    public String retrieveClinicIdFromDB () {
        DatabaseReference clinicReference = mReference.child(clinicOfEmployeePath).child(employeeId);

        // Get clinic id associated to employee
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String clinicIdFromDB = (String) dataSnapshot.getValue();
                if (clinicIdFromDB != null) {
                    setClinicId(clinicIdFromDB);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // something went wrong
            }
        });

        return clinicId;
    }*/

    /*// Method to retrieve and return list of Service objects corresponding to the ids in the
    // clinic.serviceIdList from the database
    public List<Service> getServicesFromDB () {
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
        return selectedServices;
    }*/

    /*public void removeServiceFromClinic(String id) {
        clinic.removeService(id);
    }*/
    /*public void addServiceToClinic(String id) {
        clinic.addService(id);
    }*/

    /*
    public List<String> getServiceIdsOfferedByClinic() {
        return clinic.serviceIdList;
    }*/
}
