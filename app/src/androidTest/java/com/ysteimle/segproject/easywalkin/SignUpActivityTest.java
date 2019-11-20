package com.ysteimle.segproject.easywalkin;

import android.content.Context;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import androidx.test.annotation.UiThreadTest;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.ActivityTestRule;

public class SignUpActivityTest {
    @Rule
    public ActivityTestRule<SignUpActivity> sActivityTestRule = new ActivityTestRule<>(SignUpActivity.class);

    private SignUpActivity signUpActivity = null;

    @Before
    public void setUp() {
        signUpActivity = sActivityTestRule.getActivity();
    }

    @Test
    @UiThreadTest
    public void inputValidatorTest() {
        assertTrue(signUpActivity.validSignUpScreenInput("Patient","John",
                "Smith", "johnsmith@email.com", "123 Main Street",
                "Password91", "Password91"));
        assertFalse(signUpActivity.validSignUpScreenInput("Employee","John",
                "Smith", "johnsmith@email.com", "123 Main Street",
                "Password91", "Password11"));
        assertFalse(signUpActivity.validSignUpScreenInput("Admin","John",
                "Smith", "johnsmith@email.com", "123 Main Street",
                "Password91", "Password91"));
        assertFalse(signUpActivity.validSignUpScreenInput("Patient","John%$#!",
                "Smith456", "johnsmith@email.com", "123 Main Street",
                "Password91", "Password91"));
        assertFalse(signUpActivity.validSignUpScreenInput("Employee","John",
                "Smith", "email.com", "123 Main Street",
                "Password91", "Password91"));
    }

    // Will this still work even if I have moved the password hashing method to a helper class?
    @Test
    public void passwordHashTest() {
        assertNotEquals(PasswordHelper.hexHash("p4562werG"),"Failure");
        assertEquals(PasswordHelper.hexHash("12345678"),"ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f");
    }
}
