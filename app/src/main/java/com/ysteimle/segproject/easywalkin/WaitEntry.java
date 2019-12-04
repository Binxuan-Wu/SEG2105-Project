package com.ysteimle.segproject.easywalkin;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

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
    public String expectedAppStartTime;
    public String expectedAppEndTime;

    public WaitEntry () {}

    public WaitEntry (String entryId, String patientId, String clinicId, String dateRequested, String timeRequested) {
        this.entryId = entryId;
        this.patientId = patientId;
        this.clinicId = clinicId;
        this.dateRequested = dateRequested;
        this.timeRequested = timeRequested;
    }

    // this method automatically sets the appointment end time to be 15 minutes after the expected
    // appointment time
    public void setExpectedAppStartTime(String expectedAppStartTime) {
        this.expectedAppStartTime = expectedAppStartTime;
        if (isValidTime(this.expectedAppStartTime)) {
            this.expectedAppEndTime = LocalTime.parse(this.expectedAppStartTime).plus(15, ChronoUnit.MINUTES).toString();
        } else {
            // expected appointment time is not valid (cannot parse time)
            // set expected appointment end time to be the empty string by default
            // (so that it isn't null). this entry will be invalid and so won't be considered
            // anyway, so it doesn't actually matter.
            this.expectedAppEndTime = "";
        }
    }

    // this method takes both the expected appointment start time and expected appointment end
    // time as parameters. To use this, the end time had better be the correct number of minutes
    // after the start time.
    public void setExpectedAppTimes(String expectedAppStartTime, String expectedAppEndTime) {
        this.expectedAppStartTime = expectedAppStartTime;
        this.expectedAppEndTime = expectedAppEndTime;
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
                && isValidTime(timeRequested) && isValidTime(expectedAppStartTime)
                && isValidTime(expectedAppEndTime);
    }

    // method to determine if this wait entry is expected to be taking place at the moment
    @Exclude
    public boolean isNow() {
        if (hasValidDatesTimes()) {
            return LocalDate.parse(expectedAppDate).isEqual(LocalDate.now())
                    && LocalTime.parse(expectedAppStartTime).isBefore(LocalTime.now())
                    && LocalTime.parse(expectedAppEndTime).isAfter(LocalTime.now());
        } else {
            return false;
        }
    }
}
