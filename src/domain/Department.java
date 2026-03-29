package com.hospital.appointment.domain;

public enum Department {
    GENERAL("General Medicine"),
    CARDIOLOGY("Cardiology"),
    NEUROLOGY("Neurology"),
    PEDIATRICS("Pediatrics");

    private final String displayName;

    Department(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
