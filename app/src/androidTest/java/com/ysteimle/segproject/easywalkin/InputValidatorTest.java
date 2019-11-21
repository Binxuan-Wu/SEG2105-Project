package com.ysteimle.segproject.easywalkin;

import org.junit.Test;
import static org.junit.Assert.*;

public class InputValidatorTest {
    private InputValidator iv = new InputValidator();

    @Test
    public void nameValidatorTest() {
        assertTrue(iv.isValidName("peter"));
        assertTrue(iv.isValidName("Peter"));
        assertTrue(iv.isValidName("Maxwell Jones-Smith", 10, 25));
        assertFalse(iv.isValidName("Maxwell Jones-Smith", 5, 10));
        assertFalse(iv.isValidName("We #4!"));
    }

    @Test
    public void postalCodeValidatorTest() {
        assertTrue(iv.isValidPostalCode("J1N 7K5"));
        assertTrue(iv.isValidPostalCode("H4L5N2"));
        assertFalse(iv.isValidPostalCode("h4l5n2"));
        assertFalse(iv.isValidPostalCode("J189L2"));
    }

    @Test
    public void provinceValidatorTest() {
        assertTrue(iv.isValidProvinceCode("ON"));
        assertFalse(iv.isValidProvinceCode("ONT"));
        assertFalse(iv.isValidProvinceCode("on"));
        assertFalse(iv.isValidProvinceCode("0N"));
    }

    @Test
    public void phoneValidatorTest() {
        assertTrue(iv.isValidPhoneNumber("123 321 4992"));
        assertTrue(iv.isValidPhoneNumber("6667778888"));
        assertFalse(iv.isValidPhoneNumber("1234567"));
        assertFalse(iv.isValidPhoneNumber("(123) 567-9987"));
    }

    @Test
    public void unitValidatorTest() {
        assertTrue(iv.isValidUnit(""));
        assertTrue(iv.isValidUnit("E102"));
    }
}
