package com.ysteimle.segproject.easywalkin;

public abstract class User implements AppUser {
    String accountType;
    String firstName;
    String lastName;
    String email;
    String address;
    String passwordHash;

    // The account associated to this user. Patients will have a PatientAccount and Employees will
    // have an EmployeeAccount
    Account account;

    User() {}
    User(String accountType) {
        this.accountType = accountType;
    }
    User(String accountType, String firstName, String lastName, String email, String address, String passwordHash) {
        this.accountType = accountType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.address = address;
        this.passwordHash = passwordHash;
    }

    String getAccountType() { return accountType; }
    void setAccountType(String accountType) { this.accountType = accountType; }

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

    Account getAccount() { return account; }
    void setAccount(Account account) { this.account = account; }
}