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
        OpenTime validTime = new OpenTime(1, LocalTime.of(6,30),LocalTime.of(16,0));
        assertTrue(validTime.isValid());
        OpenTime validTime2 = new OpenTime(3);
        assertTrue(validTime2.isValid());
        OpenTime inValidTime = new OpenTime(2, LocalTime.of(16,30),LocalTime.of(6,0));
        assertFalse(inValidTime.isValid());
        OpenTime inValidTime2 = new OpenTime(2, null,LocalTime.of(6,0));
        assertFalse(inValidTime2.isValid());
    }
}
