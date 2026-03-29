package com.hospital.appointment.service;

import com.hospital.appointment.domain.Appointment;
import com.hospital.appointment.domain.AppointmentStatus;
import com.hospital.appointment.exception.EntityNotFoundException;
import com.hospital.appointment.exception.SlotUnavailableException;
import com.hospital.appointment.exception.ValidationException;
import com.hospital.appointment.repository.AppointmentRepository;
import com.hospital.appointment.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class DefaultSchedulingService implements SchedulingService {

    private final AppointmentRepository appointments;
    private final HospitalRegistry registry;
    private final IdGenerator idGenerator;
    private final int slotMinutes;

    public DefaultSchedulingService(
            AppointmentRepository appointments,
            HospitalRegistry registry,
            IdGenerator idGenerator,
            int slotMinutes) {
        if (slotMinutes <= 0) {
            throw new ValidationException("slotMinutes must be positive");
        }
        this.appointments = appointments;
        this.registry = registry;
        this.idGenerator = idGenerator;
        this.slotMinutes = slotMinutes;
    }

    @Override
    public Appointment book(long patientId, long doctorId, LocalDateTime start) {
        registry.requirePatient(patientId);
        registry.requireDoctor(doctorId);
        LocalDateTime end = start.plusMinutes(slotMinutes);
        long newId = idGenerator.nextId();
        Appointment candidate = new Appointment(newId, patientId, doctorId, start, end, AppointmentStatus.SCHEDULED);

        for (Appointment existing : appointments.findAll()) {
            if (existing.getStatus() != AppointmentStatus.SCHEDULED) {
                continue;
            }
            if (candidate.overlaps(existing)) {
                throw new SlotUnavailableException(
                        "Doctor already has an overlapping appointment in " + existing.getStart() + " – " + existing.getEnd());
            }
        }
        return appointments.save(candidate);
    }

    @Override
    public Appointment cancel(long appointmentId) {
        Appointment current = appointments.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("No appointment with id " + appointmentId));
        if (current.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ValidationException("Appointment is already cancelled");
        }
        Appointment updated = current.withStatus(AppointmentStatus.CANCELLED);
        return appointments.save(updated);
    }

    @Override
    public Appointment complete(long appointmentId) {
        Appointment current = appointments.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("No appointment with id " + appointmentId));
        if (current.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new ValidationException("Only scheduled appointments can be completed");
        }
        Appointment updated = current.withStatus(AppointmentStatus.COMPLETED);
        return appointments.save(updated);
    }

    @Override
    public List<Appointment> listForDoctor(long doctorId) {
        registry.requireDoctor(doctorId);
        return appointments.findAll().stream()
                .filter(a -> a.getDoctorId() == doctorId)
                .sorted((a, b) -> a.getStart().compareTo(b.getStart()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Appointment> listForPatient(long patientId) {
        registry.requirePatient(patientId);
        return appointments.findAll().stream()
                .filter(a -> a.getPatientId() == patientId)
                .sorted((a, b) -> a.getStart().compareTo(b.getStart()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public int getSlotMinutes() {
        return slotMinutes;
    }
}
