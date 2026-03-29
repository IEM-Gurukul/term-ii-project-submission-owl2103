package com.hospital.appointment.repository;

import com.hospital.appointment.domain.Appointment;

import java.util.Collection;
import java.util.Optional;

/**
 * Persistence abstraction: in-memory, file-backed, or database implementations can be swapped.
 */
public interface AppointmentRepository {

    Appointment save(Appointment appointment);

    Optional<Appointment> findById(long id);

    Collection<Appointment> findAll();

    void delete(long id);

    /** Flush pending writes (e.g. after async persistence). */
    void flush();
}
