package com.ysteimle.segproject.easywalkin;

import androidx.test.espresso.intent.rule.IntentsTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class AdminAccountActivityTest {
    private static final String patientIntent = "Patient";
    private static final String employeeIntent = "Employee";
    private static final String intentKey = "userType";

    @Rule
    public IntentsTestRule<AdminAccountActivity> mActivityRule = new IntentsTestRule<>(AdminAccountActivity.class);

    @Before
    public void setUp() {
        AdminAccountActivity mActivity = mActivityRule.getActivity();
    }

    @Test
    public void managePatientsCorrectIntentSent() {
        onView(withId(R.id.AdminAccManagePatients)).perform(click());

        // check that created intent has correct extra data
        intended(hasExtra(intentKey,patientIntent));
    }

    @Test
    public void manageEmployeesCorrectIntentSent() {
        onView(withId(R.id.AdminAccManageClinics)).perform(click());

        // check that created intent has correct extra data
        intended(hasExtra(intentKey,employeeIntent));
    }
}
