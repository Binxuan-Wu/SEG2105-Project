package com.ysteimle.segproject.easywalkin;

import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.time.LocalTime;

import static org.junit.Assert.*;

public class EmployeeAccountActivityTest {
    @Rule
    public ActivityTestRule<EmployeeAccountActivity> mActivityTestRule = new ActivityTestRule<>(EmployeeAccountActivity.class);

    private EmployeeAccountActivity mActivity = null;

    @Before
    public void setUp() {
        mActivity = mActivityTestRule.getActivity();
    }

    @Test
    public void openTimeTest() {
        assertTrue(OpenTime.validDayCode(1));
        assertFalse(OpenTime.validDayCode(0));
        assertFalse(OpenTime.validDayCode(8));
        OpenTime validTime = new OpenTime(1, "06:30","16:00");
        assertTrue(validTime.isValid());
        OpenTime validTime2 = new OpenTime(3);
        assertTrue(validTime2.isValid());
        OpenTime inValidTime = new OpenTime(2, "16:30","08:00");
        assertFalse(inValidTime.isValid());
        OpenTime inValidTime2 = new OpenTime(2, null,"");
        assertFalse(inValidTime2.isValid());
        OpenTime validTime3 = new OpenTime(2, "","");
        assertTrue(validTime3.isValid());
    }
}
