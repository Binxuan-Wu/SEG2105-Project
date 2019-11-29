package com.ysteimle.segproject.easywalkin;

import android.graphics.Path;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
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
    // Maybe I need to use a list for the database?
    public List<OpenTime> openTimes = new ArrayList<>();

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
        initialiseOpenTimes();
    }

    @Exclude
    public void addEmployee (String employeeId) {
        this.employeeIdList.add(employeeId);
    }

    // Method to associate a service to the clinic
    @Exclude
    public void addService (String serviceId) {
        this.serviceIdList.add(serviceId);
    }
    @Exclude
    public void removeService (String serviceId) { this.serviceIdList.remove(serviceId); }

    @Exclude
    public boolean equals(Clinic clinic) {
        return this.clinicId.equals(clinic.clinicId) && this.clinicName.equals(clinic.clinicName)
                && this.clinicAddress.equals(clinic.clinicAddress)
                && this.phone.equals(clinic.phone)
                && this.insuranceTypes.equals(clinic.insuranceTypes)
                && this.paymentMethods.equals(clinic.paymentMethods)
                && this.clinicPasswordHash.equals(clinic.clinicPasswordHash)
                && this.employeeIdList.equals(clinic.employeeIdList)
                && this.serviceIdList.equals(clinic.serviceIdList);
        // probably need to add something about equality of working hours as well...
    }

    // Set up must be complete before a patient can request an appointment
    // this means that the working hours must be set up and at least one service must be
    // offered by the clinic
    // there must also be at least one employee working for the clinic
    @Exclude
    public boolean isSetUpComplete() {
        return employeeIdList.size() >= 1 && serviceIdList.size() >= 1;
        // add something about opening hours...
    }

    // method to get clinic information (name, phone, address) in a single string
    @Exclude
    public String getClinicInfo() {
        return clinicName + "\n" + phone + "\n" + clinicAddress.printFormat();
    }

    // to deal with the working hours:
    // day codes: Monday = 1, Tuesday = 2, Wednesday = 3, Thursday = 4,
    // Friday = 5, Saturday = 6, Sunday = 7

    @Exclude
    public OpenTime getOpenTimeForDay (int day) {
        if (!OpenTime.validDayCode(day) || openTimes.size() < day) {
            // day code is invalid or list is empty
            return null;
        } else {
            return openTimes.get(day - 1);
        }
    }

    @Exclude
    public String getOpenTimePrintFormatForDay (int day) {
        OpenTime openTime = getOpenTimeForDay(day);
        if (openTime != null) {
            return openTime.printFormat();
        } else {
            return "Undetermined";
        }
    }

    // initialises empty open times (i.e. the clinic is always closed)
    @Exclude
    public void initialiseOpenTimes () {
        for (int i = 1; i<=7; i++) {
            OpenTime openTime = new OpenTime(i);
            openTimes.add(openTime);
        }
    }

    // sets an open time for a specific day, provided it is a valid open time. Returns true iff
    // the open time was successfully added
    // note: need to have initialised openTimes before we can set an open time for a specific day
    @Exclude
    public boolean setOpenTimeForDay (int dayCode, String openingTime, String closingTime) {
        int index = dayCode - 1;
        OpenTime openTime = new OpenTime(dayCode,openingTime,closingTime);
        if (openTime.isValid()) {
            if (openTimes.isEmpty()) {
                initialiseOpenTimes();
            }
            openTimes.set(index, openTime);
            return true;
        } else {
            return false;
        }
    }

    @Exclude
    public void setOpenTimes (List<OpenTime> times) {
        this.openTimes = times;
    }

    // methods to help with searching for clinics according to various criteria
    @Exclude
    public boolean isAtLocation(String cityName, String provinceCode) {
        return clinicAddress.isAtLocation(cityName,provinceCode);
    }

    @Exclude
    public boolean offersServiceWithID(String serviceId) {
        return serviceIdList.contains(serviceId);
    }


    @Exclude
    public boolean isOpenAtDateTime(int dayCode, String time) {
        if (OpenTime.validDayCode(dayCode) && OpenTime.validTime(time)){
            OpenTime openTime = getOpenTimeForDay(dayCode);
            return openTime.isOpenAtTime(time);
        } else {
            return false;
        }
    }

}
