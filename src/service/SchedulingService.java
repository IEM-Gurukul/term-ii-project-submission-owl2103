package com.hospital.appointment.service;

import com.hospital.appointment.domain.Appointment;

import java.time.LocalDateTime;
import java.util.List;

public interface SchedulingService {

    Appointment book(long patientId, long doctorId, LocalDateTime start);

    Appointment cancel(long appointmentId);

    Appointment complete(long appointmentId);

    List<Appointment> listForDoctor(long doctorId);

    List<Appointment> listForPatient(long patientId);

    int getSlotMinutes();
}
