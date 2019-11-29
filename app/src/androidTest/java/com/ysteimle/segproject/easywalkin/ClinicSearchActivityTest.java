package com.ysteimle.segproject.easywalkin;

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
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;

import static org.junit.Assert.*;


public class ClinicSearchActivityTest {
    @Rule
    public ActivityTestRule<ClinicSearchActivity> mActivityTestRule = new ActivityTestRule<>(ClinicSearchActivity.class);

    private ClinicSearchActivity mActivity = null;

    @Before
    public void setUp() {
        mActivity = mActivityTestRule.getActivity();
    }

    @Test
    public void searchFieldsTest() {
        onView(withId(R.id.ClinicSearchCityEdit)).perform(typeText("Ottawa"),closeSoftKeyboard());
        onView(withId(R.id.ClinicSearchProvinceEdit)).perform(typeText("ON"),closeSoftKeyboard());
        mActivity.clearSearchFields();
        onView(withId(R.id.ClinicSearchCityEdit)).perform(typeText("Toronto"),closeSoftKeyboard());
        onView(withText("Toronto")).check(matches(isDisplayed()));
    }

    @Test
    public void waitingTimeTest() {
        LocalTime current = LocalTime.now();
        LocalTime inFifteenMins = current.plus(15,ChronoUnit.MINUTES);
        LocalTime inHalfHour = current.plus(30,ChronoUnit.MINUTES);
        int waitTime1 = mActivity.getExpectedWaitingTime(inFifteenMins);
        assertTrue(waitTime1 >= 29 && waitTime1 <= 30);
        int waitTime2 = mActivity.getExpectedWaitingTime(inHalfHour);
        assertTrue(waitTime2 >= 44 && waitTime2 <= 45);
    }

    // Note: this test will only work if there is currently a unique open clinic
    /*@Test
    public void displayClinicTest() {
        onView(withId(R.id.ClinicSearchCurrentlyOpenBtn)).perform(click());
        onView(withText("Select")).perform(click());
        onView(withText("Clinic information")).check(matches(isDisplayed()));
    }*/
}
