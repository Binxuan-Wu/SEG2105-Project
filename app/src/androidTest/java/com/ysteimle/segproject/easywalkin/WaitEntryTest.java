package com.ysteimle.segproject.easywalkin;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class WaitEntryTest {
    private WaitEntryComparator comparator = new WaitEntryComparator();

    private WaitEntry entry1 = new WaitEntry("id1","patient1","clinic","2019-12-03","06:12");
    private WaitEntry entry2 = new WaitEntry("id2","patient2","clinic","2019-12-02","12:11");
    private WaitEntry entry3 = new WaitEntry("id3","patient3","clinic","2019-12-02","06:12");

    @Test
    public void waitEntryComparatorTest() {
        List<WaitEntry> waitEntryList = new ArrayList<>();
        waitEntryList.add(entry1);
        waitEntryList.add(entry2);
        waitEntryList.add(entry3);
        assertEquals(waitEntryList.get(0).entryId,"id1");
        waitEntryList.sort(comparator);
        assertEquals(waitEntryList.get(0).entryId,"id3");
        assertEquals(waitEntryList.get(1).entryId,"id2");
        assertEquals(waitEntryList.get(2).entryId,"id1");
    }

    @Test
    public void waitEntryExpectedAppTimeTest() {
        String startTime = "10:00";
        String endTime = "10:15";
        entry1.setExpectedAppStartTime(startTime);
        assertEquals(entry1.expectedAppStartTime,startTime);
        assertEquals(entry1.expectedAppEndTime,endTime);
    }

}
