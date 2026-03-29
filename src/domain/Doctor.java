package com.hospital.appointment.domain;

import java.util.Objects;

public final class Doctor extends User {

    private Department department;
    private String licenseId;

    public Doctor(long id, String fullName, String email, Department department, String licenseId) {
        super(id, fullName, email);
        setDepartment(department);
        setLicenseId(licenseId);
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = Objects.requireNonNull(department, "department");
    }

    public String getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(String licenseId) {
        this.licenseId = Objects.requireNonNull(licenseId, "licenseId").trim();
        if (this.licenseId.isEmpty()) {
            throw new IllegalArgumentException("licenseId cannot be blank");
        }
    }

    @Override
    public String getRoleLabel() {
        return "Doctor (" + department.getDisplayName() + ")";
    }

    @Override
    public String getContactSummary() {
        return "Dept: " + department.getDisplayName() + ", License: " + licenseId;
    }
}
