package com.ysteimle.segproject.easywalkin;

public class User implements AppUser {
    private UserProfile userProfile;

    User() {}
    User(String firstName, String lastName, String email, String address, String passwordHash) {
        this.userProfile = new UserProfile(firstName, lastName, email, address, passwordHash);
    }

    public UserProfile getUserProfile() { return userProfile; }
    public void setUserProfile(String firstName, String lastName, String email, String address, String passwordHash) {
        this.userProfile.setFirstName(firstName);
        this.userProfile.setLastName(lastName);
        this.userProfile.setEmail(email);
        this.userProfile.setAddress(address);
        this.userProfile.setPasswordHash(passwordHash);
    }
}

class UserProfile {
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String passwordHash;

    UserProfile() {}
    UserProfile (String firstName, String lastName, String email, String address, String passwordHash) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.address = address;
        this.passwordHash = passwordHash;
    }

    String getFirstName() { return firstName; }
    void setFirstName(String firstName) { this.firstName = firstName; }

    String getLastName() { return lastName; }
    void setLastName(String lastName) { this.lastName = lastName; }

    String getEmail() { return email; }
    void setEmail(String email) { this.email = email; }

    String getAddress() { return address; }
    void setAddress(String address) { this.address = address; }

    String getPasswordHash() { return passwordHash; }
    void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}