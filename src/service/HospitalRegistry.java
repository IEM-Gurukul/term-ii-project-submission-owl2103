package com.hospital.appointment.service;

import com.hospital.appointment.domain.Doctor;
import com.hospital.appointment.domain.Patient;
import com.hospital.appointment.exception.EntityNotFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory registry of doctors and patients (collections).
 */
public final class HospitalRegistry {

    private final Map<Long, Patient> patients = new HashMap<>();
    private final Map<Long, Doctor> doctors = new HashMap<>();

    public void registerPatient(Patient patient) {
        patients.put(patient.getId(), patient);
    }

    public void registerDoctor(Doctor doctor) {
        doctors.put(doctor.getId(), doctor);
    }

    public Patient requirePatient(long id) {
        Patient p = patients.get(id);
        if (p == null) {
            throw new EntityNotFoundException("No patient with id " + id);
        }
        return p;
    }

    public Doctor requireDoctor(long id) {
        Doctor d = doctors.get(id);
        if (d == null) {
            throw new EntityNotFoundException("No doctor with id " + id);
        }
        return d;
    }

    public Collection<Patient> listPatients() {
        return Collections.unmodifiableCollection(patients.values());
    }

    public Collection<Doctor> listDoctors() {
        return Collections.unmodifiableCollection(doctors.values());
    }
}
