package com.ysteimle.segproject.easywalkin;

import androidx.test.annotation.UiThreadTest;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


public class PatientAccountActivityTest {

    @Rule
    public IntentsTestRule<PatientAccountActivity> mActivityTestRule = new IntentsTestRule<>(PatientAccountActivity.class);

    public PatientAccountActivity mActivity = null;

    @Before
    public void setUp() {
        mActivity = mActivityTestRule.getActivity();
    }

    @Test
    public void startTest() {
        // does this activity even start?
        onView(withText(R.string.Patient_Account)).check(matches(isDisplayed()));
    }

    @Test
    public void refreshTest() {
        onView(withId(R.id.PatAccRefreshBtn)).perform(click());
        onView(withText(String.format("(Last updated at %s)",LocalTime.now().truncatedTo(ChronoUnit.MINUTES).toString()))).check(matches(isDisplayed()));
    }
}
