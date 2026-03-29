package com.hospital.appointment.domain;

import java.util.Objects;

public final class Patient extends User {

    private String phone;

    public Patient(long id, String fullName, String email, String phone) {
        super(id, fullName, email);
        setPhone(phone);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = Objects.requireNonNull(phone, "phone").trim();
        if (this.phone.isEmpty()) {
            throw new IllegalArgumentException("phone cannot be blank");
        }
    }

    @Override
    public String getRoleLabel() {
        return "Patient";
    }

    @Override
    public String getContactSummary() {
        return "Phone: " + phone;
    }
}
