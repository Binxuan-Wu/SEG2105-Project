package com.ysteimle.segproject.easywalkin;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHelper {
    private static final String defaultAlg = "SHA-256";

    // Method that takes a plaintext String as an input and returns a String containing the hexadecimal
    // representation of the hashed plaintext using SHA-256
    public static String hexHash (String plaintext) {
        try {
            MessageDigest md = MessageDigest.getInstance(defaultAlg);
            byte[] hashBytes = md.digest(plaintext.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "Failure";
        }
    }

    // Method to verify if password corresponds to a given hashed password
    public static boolean validatePassword (String plaintext, String expectedHash) {
        String hash = hexHash(plaintext);
        return hash.equals(expectedHash);
    }
}
