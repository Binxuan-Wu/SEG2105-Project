package com.ysteimle.segproject.easywalkin;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    public String accountType;
    public String firstName;
    public String lastName;
    public String email;
    public String address;
    public String passwordHash;

    // The account associated to this user. Patients will have a PatientAccount and Employees will
    // have an EmployeeAccount
    //Account account;

    public User() {}
    public User(String accountType) {
        this.accountType = accountType;
    }
    public User(String accountType, String firstName, String lastName, String email, String address, String passwordHash) {
        this.accountType = accountType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.address = address;
        this.passwordHash = passwordHash;
    }

    /*
    @Exclude
    String getAccountType() { return accountType; }
    @Exclude
    void setAccountType(String accountType) { this.accountType = accountType; }

    @Exclude
    String getFirstName() { return firstName; }
    @Exclude
    void setFirstName(String firstName) { this.firstName = firstName; }

    @Exclude
    String getLastName() { return lastName; }
    @Exclude
    void setLastName(String lastName) { this.lastName = lastName; }

    @Exclude
    String getEmail() { return email; }
    @Exclude
    void setEmail(String email) { this.email = email; }

    @Exclude
    String getAddress() { return address; }
    @Exclude
    void setAddress(String address) { this.address = address; }

    @Exclude
    String getPasswordHash() { return passwordHash; }
    @Exclude
    void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    //Account getAccount() { return account; }
    //void setAccount(Account account) { this.account = account; }
    */
}