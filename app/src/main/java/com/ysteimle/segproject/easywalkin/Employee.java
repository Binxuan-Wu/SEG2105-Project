package com.ysteimle.segproject.easywalkin;

class Employee extends User {
    Employee() {
        super("Employee");
        this.account = new EmployeeAccount();
    }
    Employee(String firstName, String lastName, String email, String address, String passwordHash) {
        super("Employee", firstName, lastName, email, address, passwordHash);
        this.account = new EmployeeAccount();
    }

    @Override
    void setAccountType(String accountType) {
        // We only allow the accountType to be "Employee"
        this.accountType = "Employee";
    }

    // More functionality to be added later
}
