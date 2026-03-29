package com.hospital.appointment.domain;

import com.hospital.appointment.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable appointment value object after construction (fields exposed via getters only).
 */
public final class Appointment {

    private final long id;
    private final long patientId;
    private final long doctorId;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final AppointmentStatus status;

    public Appointment(
            long id,
            long patientId,
            long doctorId,
            LocalDateTime start,
            LocalDateTime end,
            AppointmentStatus status) {
        if (id <= 0 || patientId <= 0 || doctorId <= 0) {
            throw new ValidationException("ids must be positive");
        }
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.start = Objects.requireNonNull(start, "start");
        this.end = Objects.requireNonNull(end, "end");
        this.status = Objects.requireNonNull(status, "status");
        if (!end.isAfter(start)) {
            throw new ValidationException("end must be after start");
        }
    }

    public long getId() {
        return id;
    }

    public long getPatientId() {
        return patientId;
    }

    public long getDoctorId() {
        return doctorId;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public Appointment withStatus(AppointmentStatus newStatus) {
        return new Appointment(id, patientId, doctorId, start, end, newStatus);
    }

    public boolean overlaps(Appointment other) {
        if (other == null) {
            return false;
        }
        if (this.doctorId != other.doctorId) {
            return false;
        }
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Appointment that = (Appointment) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
