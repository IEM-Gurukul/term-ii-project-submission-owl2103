package com.hospital.appointment.exception;

/**
 * Base for domain-specific failures (unchecked so service API stays clean for a small app).
 */
public class HospitalException extends RuntimeException {

    public HospitalException(String message) {
        super(message);
    }

    public HospitalException(String message, Throwable cause) {
        super(message, cause);
    }
}
