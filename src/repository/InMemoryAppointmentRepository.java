package com.hospital.appointment.repository;

import com.hospital.appointment.domain.Appointment;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAppointmentRepository implements AppointmentRepository {

    private final Map<Long, Appointment> store = new ConcurrentHashMap<>();

    @Override
    public Appointment save(Appointment appointment) {
        store.put(appointment.getId(), appointment);
        return appointment;
    }

    @Override
    public Optional<Appointment> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Collection<Appointment> findAll() {
        return Collections.unmodifiableCollection(store.values());
    }

    @Override
    public void delete(long id) {
        store.remove(id);
    }

    @Override
    public void flush() {
        // no-op for pure in-memory
    }

    protected Map<Long, Appointment> getStore() {
        return store;
    }
}
