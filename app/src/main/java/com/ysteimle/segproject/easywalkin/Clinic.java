package com.ysteimle.segproject.easywalkin;

import java.util.ArrayList;
import java.util.List;

public class Clinic {
    public String clinicId;
    public String clinicName;
    public Address clinicAddress;
    public String phone;
    public String insuranceTypes;
    public String paymentMethods;
    public String clinicPasswordHash;
    public List<String> employeeIdList = new ArrayList<>();
    public List<String> serviceIdList = new ArrayList<>();

    // Also: working hours specified somehow

    public Clinic () {}

    public Clinic (String id, String name, Address address, String phone, String insuranceTypes,
                   String paymentMethods, String passwordHash) {
        this.clinicId = id;
        this.clinicName = name;
        this.clinicAddress = address;
        this.phone = phone;
        this.insuranceTypes = insuranceTypes;
        this.paymentMethods = paymentMethods;
        this.clinicPasswordHash = passwordHash;
    }

    public void addEmployee (String employeeId) {
        this.employeeIdList.add(employeeId);
    }

    // Method to associate a service to the clinic
    public void addService (String serviceId) {
        this.serviceIdList.add(serviceId);
    }
    public void removeService (String serviceId) { this.serviceIdList.remove(serviceId); }

    public boolean equals(Clinic clinic) {
        return this.clinicId.equals(clinic.clinicId) && this.clinicName.equals(clinic.clinicName)
                && this.clinicAddress.equals(clinic.clinicAddress)
                && this.phone.equals(clinic.phone)
                && this.insuranceTypes.equals(clinic.insuranceTypes)
                && this.paymentMethods.equals(clinic.paymentMethods)
                && this.clinicPasswordHash.equals(clinic.clinicPasswordHash)
                && this.employeeIdList.equals(clinic.employeeIdList)
                && this.serviceIdList.equals(clinic.serviceIdList);
    }

    // Set up must be complete before a patient can request an appointment
    // this means that the working hours must be set up and at least one service must be
    // offered by the clinic
    // there must also be at least one employee working for the clinic
    public boolean isSetUpComplete() {
        // add something about the working hours
        return employeeIdList.size() >= 1 && serviceIdList.size() >= 1;
    }
}
