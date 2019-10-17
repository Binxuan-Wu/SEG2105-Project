package com.ysteimle.segproject.easywalkin;

class Patient extends User {
    Patient() {
        super("Patient");
        this.account = new PatientAccount();
    }
    Patient(String firstName, String lastName, String email, String address, String passwordHash) {
        super("Patient", firstName, lastName, email, address, passwordHash);
        this.account = new PatientAccount();
    }

    @Override
    void setAccountType(String accountType) {
        // We only allow the accountType to be "Patient"
        this.accountType = "Patient";
    }

    // More functionality to be added later
}
