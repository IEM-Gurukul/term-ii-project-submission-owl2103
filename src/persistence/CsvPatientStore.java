package com.hospital.appointment.persistence;

import com.hospital.appointment.domain.Patient;
import com.hospital.appointment.exception.HospitalException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Simple CSV persistence for patients.
 */
public final class CsvPatientStore {

    private static final String HEADER = "id,fullName,email,phone";

    private CsvPatientStore() {
    }

    public static List<Patient> readAll(Path file) throws IOException {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        List<Patient> out = new ArrayList<>();
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

    public static void writeAll(Path file, Collection<Patient> patients) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            for (Patient patient : patients) {
                writer.write(toLine(patient));
                writer.newLine();
            }
        }
    }

    private static Patient parseLine(String line) {
        String[] parts = line.split(",", 4);
        if (parts.length != 4) {
            throw new HospitalException("Invalid patient CSV row: " + line);
        }
        try {
            long id = Long.parseLong(parts[0]);
            String fullName = parts[1];
            String email = parts[2];
            String phone = parts[3];
            return new Patient(id, fullName, email, phone);
        } catch (Exception e) {
            throw new HospitalException("Cannot parse patient CSV row: " + line, e);
        }
    }

    private static String toLine(Patient patient) {
        return patient.getId()
                + ","
                + patient.getFullName()
                + ","
                + patient.getEmail()
                + ","
                + patient.getPhone();
    }
}
