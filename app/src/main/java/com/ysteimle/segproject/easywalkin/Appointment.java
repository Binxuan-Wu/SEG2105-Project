package com.ysteimle.segproject.easywalkin;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * Class for scheduled appointments.
 */

@IgnoreExtraProperties
public class Appointment {
    public String instanceId;
    public String patientId;
    public String clinicId;
    public String clinicInfo;
    public String date;
    public String time;

    public Appointment () {}
    public Appointment (String instanceId, String patientId, String clinicId, String date, String time) {
        this.instanceId = instanceId;
        this.patientId = patientId;
        this.clinicId = clinicId;
        this.date = date;
        this.time = time;
    }

    @Exclude
    public boolean validDate() {
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Exclude
    public boolean validTime() {
        try {
            LocalTime.parse(time);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Exclude
    public String dateTimePrintFormat() {
        if (!validDate() || !validTime()) {
            return "Error: Could not determine date and/or time.";
        } else {
            return date + " at " + time;
        }
    }

    public void setClinicInfo(String clinicInfo) {
        this.clinicInfo = clinicInfo;
    }
}
