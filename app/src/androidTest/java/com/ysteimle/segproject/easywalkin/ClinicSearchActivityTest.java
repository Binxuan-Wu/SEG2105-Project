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
    public void startTest() {
        onView(withText(R.string.Clinic_search_currently_open)).check(matches(isDisplayed()));
    }

    @Test
    public void searchCurrentOpenTest() {
        onView(withId(R.id.ClinicSearchCurrentlyOpenBtn)).perform(click());
        onView(withId(R.id.searchResultsActionMsg)).check(matches(isDisplayed()));
    }

    @Test
    public void searchByService() {
        onView(withId(R.id.ClinicSearchByServiceBtn)).perform(click());
        onView(withText("Select the desired service below to search for clinics offering this service.")).check(matches(isDisplayed()));
    }

    @Test
    public void searchByLocationBtn() {
        onView(withId(R.id.ClinicSearchCityEdit)).perform(typeText("Ottawa"),closeSoftKeyboard());
        onView(withId(R.id.ClinicSearchProvinceEdit)).perform(typeText("ON"),closeSoftKeyboard());
        onView(withId(R.id.ClinicSearchByLocationBtn)).perform(click());
        onView(withText("Desmarais Clinic")).check(matches(isDisplayed()));
    }

    // Note: this test will only work if there is currently a unique open clinic
    /*@Test
    public void displayClinicTest() {
        onView(withId(R.id.ClinicSearchCurrentlyOpenBtn)).perform(click());
        onView(withText("Select")).perform(click());
        onView(withText("Clinic information")).check(matches(isDisplayed()));
    }*/
}
