package com.hospital.appointment.repository;

import com.hospital.appointment.domain.Appointment;
import com.hospital.appointment.exception.HospitalException;
import com.hospital.appointment.persistence.CsvAppointmentStore;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * In-memory store with CSV snapshots written on a dedicated thread (threads + file handling).
 */
public class FileBackedAppointmentRepository extends InMemoryAppointmentRepository {

    private final Path dataFile;
    private final ExecutorService writer;

    public FileBackedAppointmentRepository(Path dataFile) {
        this.dataFile = dataFile;
        this.writer = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "appointment-csv-writer");
            t.setDaemon(true);
            return t;
        });
        loadInitial();
    }

    private void loadInitial() {
        try {
            List<Appointment> existing = CsvAppointmentStore.readAll(dataFile);
            for (Appointment a : existing) {
                super.save(a);
            }
        } catch (IOException e) {
            throw new HospitalException("Failed to load appointments from file", e);
        }
    }

    @Override
    public synchronized Appointment save(Appointment appointment) {
        Appointment saved = super.save(appointment);
        scheduleWriteAll();
        return saved;
    }

    @Override
    public synchronized void delete(long id) {
        super.delete(id);
        scheduleWriteAll();
    }

    private void scheduleWriteAll() {
        writer.submit(() -> {
            try {
                CsvAppointmentStore.writeAll(dataFile, getStore().values());
            } catch (IOException e) {
                System.err.println("Persist failed: " + e.getMessage());
            }
        });
    }

    /**
     * Blocks until the pending write (if any) completes — call before exit.
     */
    @Override
    public void flush() {
        Future<?> f = writer.submit(() -> {
            try {
                CsvAppointmentStore.writeAll(dataFile, getStore().values());
            } catch (IOException e) {
                throw new HospitalException("Flush failed", e);
            }
        });
        try {
            f.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new HospitalException("Flush interrupted or failed", e);
        }
    }

    public void shutdown() {
        flush();
        writer.shutdown();
        try {
            if (!writer.awaitTermination(5, TimeUnit.SECONDS)) {
                writer.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            writer.shutdownNow();
        }
    }

    public Path getDataFile() {
        return dataFile;
    }

    @Override
    public Optional<Appointment> findById(long id) {
        return super.findById(id);
    }

    @Override
    public Collection<Appointment> findAll() {
        return super.findAll();
    }
}
