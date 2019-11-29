package com.ysteimle.segproject.easywalkin;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;

/**
 * To compare WaitEntry objects according to date and time requested
 */

public class WaitEntryComparator implements Comparator<WaitEntry> {

    // note: this method might result in a DateTimeParseException if the entries have invalid values
    // for the dates and/or times requested
    public int compare(WaitEntry entry1, WaitEntry entry2) {
        LocalDate date1 = LocalDate.parse(entry1.dateRequested);
        LocalDate date2 = LocalDate.parse(entry2.dateRequested);
        LocalTime time1 = LocalTime.parse(entry1.timeRequested);
        LocalTime time2 = LocalTime.parse(entry2.timeRequested);
        if (date1.isBefore(date2)) {
            return -1;
        } else if (date1.isAfter(date2)) {
            return 1;
        } else {
            // the two dates are equal
            // we must now compare the times
            if (time1.isBefore(time2)) {
                return -1;
            } else if (time1.isAfter(time2)) {
                return 1;
            } else {
                // the times are equal (rather unlikely to happen for distinct WaitEntry objects)
                return 0;
            }
        }
    }
}
