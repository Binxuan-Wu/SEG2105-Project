package com.ysteimle.segproject.easywalkin;

import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class PatientAccountActivityTest {

    @Rule
    public ActivityTestRule<PatientAccountActivity> mActivityTestRule = new ActivityTestRule<>(PatientAccountActivity.class);

    private PatientAccountActivity mActivity = null;

    @Before
    public void setUp() {
        mActivity = mActivityTestRule.getActivity();
    }

    @Test
    public void startTest() {
        // checks that it is possible to start this activity
    }
}
