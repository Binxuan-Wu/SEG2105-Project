package com.ysteimle.segproject.easywalkin;

/**
 * Class that provides methods used to validate user input.
 */

public class InputValidator {

    // Default constructor
    InputValidator () {}

    // for emails
    private static final int emailMinLen = 6;

    public boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.length() >= emailMinLen;
    }

    // for passwords
    private static final int pwdMinLen = 6;
    private static final int pwdMaxLen = 12;

    public boolean isValidPassword(String password) {
        return password.length() >= pwdMinLen && password.length() <= pwdMaxLen;
    }

    // For names (of a person, a clinic, a service...):
    // unicode regular expressions to validate names
    // Must start with a letter character (can be either upper or lower case), can contain spaces, hyphens, accents
    // Default with max 25 characters
    private static final String nameRegexDef = "\\p{L}+[\\p{L}\\p{Z}\\p{Pd}\\p{Mn}\\p{Mc}]{0,24}";
    //private static final String nameRegexDef2 = "^\\p{L}+[\\p{L}\\p{Z}\\p{Pd}\\p{Mn}\\p{Mc}]{0,24}";
    // no length specified -- need to concatenate a String of the form "{i,j}" for some integers i < j
    // at the end before using it
    private static final String nameRegexNoLen = "\\p{L}+[\\p{L}\\p{Z}\\p{Pd}\\p{Mn}\\p{Mc}]";
    // method to generate nameRegex of a specific minimum and maximum length (which must be distinct)
    private String getNameRegexWithLengthRange (int minLen, int maxLen) {
        if (minLen <= 0 || minLen >= maxLen) {
            return nameRegexDef;
        } else {
            return nameRegexNoLen + "{" + (minLen - 1) + "," + (maxLen - 1) + "}";
        }
    }

    // methods to validate a name (of a person, a clinic, a service...), possibly with specified
    // length range
    public boolean isValidName(String name) {
        return !name.isEmpty() && name.matches(nameRegexDef);
    }

    public boolean isValidName(String name, int minLen, int maxLen) {
        String regex = getNameRegexWithLengthRange(minLen, maxLen);
        return !name.isEmpty() && name.matches(regex);
    }

    // for addresses
    // street address
    private static final int streetAddMinLen = 4;
    private static final int streetAddMaxLen = 50;
    public boolean isValidStreetAddress(String address) {
        return address.length() >= streetAddMinLen && address.length() <= streetAddMaxLen;
    }

    // province
    private static final String provinceRegex = "[A-Z]{2}";
    public boolean isValidProvinceCode(String province) {
        return province.matches(provinceRegex);
    }

    // City
    public boolean isValidCity(String city) {
        return isValidName(city, 3, 20);
    }

    // Postal Code (format : LdL dLd, where L represents an uppercase letter and d is a digit)
    private static final String postalCodeRegex = "[A-Z]\\d[A-Z]\\s?\\d[A-Z]\\d";
    public boolean isValidPostalCode(String postalCode) {
        return postalCode.matches(postalCodeRegex);
    }

    // unit (could be empty)
    private static final String unitRegex = "\\w{0,10}";
    public boolean isValidUnit(String unit) {
        return unit.matches(unitRegex);
    }

    // phone numbers
    // phone number must have format 888 888 8888 (where each 8 can be replaced by any digit)
    private static final String phoneRegex = "\\d{3}\\s?\\d{3}\\s?\\d{4}";
    public boolean isValidPhoneNumber(String phone) {
        return phone.matches(phoneRegex);
    }

    // insurance types and payment methods should be strings between 3 and 100 characters
    public boolean isValidInsuranceOrPayment(String input) {
        return (input.length() >= 3 && input.length() <= 100);
    }


}
