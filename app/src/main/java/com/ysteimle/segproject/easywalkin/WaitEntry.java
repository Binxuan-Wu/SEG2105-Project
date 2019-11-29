package com.ysteimle.segproject.easywalkin;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * Class for waiting list entries.
 */

@IgnoreExtraProperties
public class WaitEntry {
    public String entryId;
    public String patientId;
    public String clinicId;
    public String dateRequested;
    public String timeRequested;
    public String expectedAppDate;
    public String expectedAppTime;

    public WaitEntry () {}

    public WaitEntry (String entryId, String patientId, String clinicId, String dateRequested, String timeRequested) {
        this.entryId = entryId;
        this.patientId = patientId;
        this.clinicId = clinicId;
        this.dateRequested = dateRequested;
        this.timeRequested = timeRequested;
    }

    public void setExpectedAppTime(String expectedAppTime) {
        this.expectedAppTime = expectedAppTime;
    }

    public void setExpectedAppDate(String expectedAppDate) {
        this.expectedAppDate = expectedAppDate;
    }

    @Exclude
    public static boolean isValidDate(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Exclude
    public static boolean isValidTime(String time) {
        try {
            LocalTime.parse(time);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Exclude
    public boolean hasValidDatesTimes() {
        return isValidDate(dateRequested) && isValidDate(expectedAppDate)
                && isValidTime(timeRequested) && isValidTime(expectedAppTime);
    }
}
