package com.hospital.appointment.persistence;

import com.hospital.appointment.domain.Department;
import com.hospital.appointment.domain.Doctor;
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
 * Simple CSV persistence for doctors.
 */
public final class CsvDoctorStore {

    private static final String HEADER = "id,fullName,email,department,licenseId";

    private CsvDoctorStore() {
    }

    public static List<Doctor> readAll(Path file) throws IOException {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        List<Doctor> out = new ArrayList<>();
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

    public static void writeAll(Path file, Collection<Doctor> doctors) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            for (Doctor doctor : doctors) {
                writer.write(toLine(doctor));
                writer.newLine();
            }
        }
    }

    private static Doctor parseLine(String line) {
        String[] parts = line.split(",", 5);
        if (parts.length != 5) {
            throw new HospitalException("Invalid doctor CSV row: " + line);
        }
        try {
            long id = Long.parseLong(parts[0]);
            String fullName = parts[1];
            String email = parts[2];
            Department department = Department.valueOf(parts[3]);
            String licenseId = parts[4];
            return new Doctor(id, fullName, email, department, licenseId);
        } catch (Exception e) {
            throw new HospitalException("Cannot parse doctor CSV row: " + line, e);
        }
    }

    private static String toLine(Doctor doctor) {
        return doctor.getId()
                + ","
                + doctor.getFullName()
                + ","
                + doctor.getEmail()
                + ","
                + doctor.getDepartment().name()
                + ","
                + doctor.getLicenseId();
    }
}
