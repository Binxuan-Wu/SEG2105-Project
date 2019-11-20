package com.ysteimle.segproject.easywalkin;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.time.LocalTime;

@IgnoreExtraProperties
public class OpenTime {
    // day codes: Monday = 1, Tuesday = 2, Wednesday = 3, Thursday = 4,
    // Friday = 5, Saturday = 6, Sunday = 7
    public int dayCode;
    // opening time
    public LocalTime OT;
    // closing time
    public LocalTime CT;

    public OpenTime () {}
    public OpenTime (int dayCode) {
        this.dayCode = dayCode;
        this.OT = null;
        this.CT = null;
    }
    public OpenTime (int dayCode, LocalTime openingTime, LocalTime closingTime) {
        this.dayCode = dayCode;
        this.OT = openingTime;
        this.CT = closingTime;
    }

    @Exclude
    public boolean isValid() {
        return validDayCode(dayCode) && ((OT == null && CT == null) || (OT != null && CT != null && OT.isBefore(CT)));
    }

    @Exclude
    public String printFormat() {
        StringBuilder sb = new StringBuilder();
        if (isValid()) {
            if (OT == null && CT == null) {
                sb.append("Closed");
            } else {
                sb.append(OT.toString());
                sb.append(" to ");
                sb.append(CT.toString());
            }
        } else {
            sb.append("Invalid Time");
        }
        return sb.toString();
    }

    @Exclude
    public static boolean validDayCode (int i) {
        return (i >= 1 && i <= 7);
    }
}