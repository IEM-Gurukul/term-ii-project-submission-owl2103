package com.hospital.appointment.persistence;

import com.hospital.appointment.domain.Appointment;
import com.hospital.appointment.domain.AppointmentStatus;
import com.hospital.appointment.exception.HospitalException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Simple CSV persistence — no external libraries (demonstrates file handling).
 */
public final class CsvAppointmentStore {

    private static final String HEADER = "id,patientId,doctorId,start,end,status";

    private CsvAppointmentStore() {
    }

    public static List<Appointment> readAll(Path file) throws IOException {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        List<Appointment> out = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            if (line == null) {
                return out;
            }
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                out.add(parseLine(line));
            }
        }
        return out;
    }

    public static void writeAll(Path file, Collection<Appointment> appointments) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            for (Appointment a : appointments) {
                writer.write(toLine(a));
                writer.newLine();
            }
        }
    }

    private static Appointment parseLine(String line) {
        String[] parts = line.split(",", 6);
        if (parts.length != 6) {
            throw new HospitalException("Invalid CSV row: " + line);
        }
        try {
            long id = Long.parseLong(parts[0]);
            long patientId = Long.parseLong(parts[1]);
            long doctorId = Long.parseLong(parts[2]);
            LocalDateTime start = LocalDateTime.parse(parts[3]);
            LocalDateTime end = LocalDateTime.parse(parts[4]);
            AppointmentStatus status = AppointmentStatus.valueOf(parts[5]);
            return new Appointment(id, patientId, doctorId, start, end, status);
        } catch (Exception e) {
            throw new HospitalException("Cannot parse appointment row: " + line, e);
        }
    }

    private static String toLine(Appointment a) {
        return a.getId()
                + ","
                + a.getPatientId()
                + ","
                + a.getDoctorId()
                + ","
                + a.getStart()
                + ","
                + a.getEnd()
                + ","
                + a.getStatus();
    }
}
