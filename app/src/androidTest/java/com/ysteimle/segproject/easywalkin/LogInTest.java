package com.ysteimle.segproject.easywalkin;

import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

public class LogInTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    private MainActivity mainActivity = null;

    @Before
    public void setUp() {
        mainActivity = mActivityTestRule.getActivity();
    }

    @Test
    public void validLogInInfoTest() {
        onView(withId(R.id.LogInScreenUserNameEdit)).perform(typeText("notAnEmail.com"),closeSoftKeyboard());
        onView(withId(R.id.LogInScreenPwdEdit)).perform(typeText("short"),closeSoftKeyboard());
        onView(withId(R.id.LogInButton)).perform(click());
        onView(withText("Error: " + "Invalid email. " + "Invalid password."))
                .inRoot(withDecorView(not(mainActivity.getWindow().getDecorView())))
                .check(matches(isDisplayed()));
    }

    @Test
    public void noEmailLogInTest() {
        onView(withId(R.id.LogInScreenPwdEdit)).perform(typeText("notshort"),closeSoftKeyboard());
        onView(withId(R.id.LogInButton)).perform(click());
        onView(withText("Please enter your email address and password"))
                .inRoot(withDecorView(not(mainActivity.getWindow().getDecorView())))
                .check(matches(isDisplayed()));
    }
}
